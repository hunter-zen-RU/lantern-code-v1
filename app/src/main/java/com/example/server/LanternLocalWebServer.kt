package com.example.server

import android.content.Context
import android.util.Log
import com.example.data.LanternDatabase
import com.example.data.LanternDiskStorage
import com.example.data.WorkspaceRepository
import com.example.data.entity.ProjectEntity
import com.example.data.entity.ProjectFileEntity
import com.example.data.entity.TeamChatMessageEntity
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.net.Inet4Address
import java.net.NetworkInterface

class LanternLocalWebServer(
  port: Int = 8080,
  private val context: Context,
  private val repository: WorkspaceRepository,
  private val db: LanternDatabase,
  private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : NanoHTTPD(port) {

  private var activeProject: ProjectEntity? = null
  private var pairingCode: String = "LANT-8821"

  fun setActiveProject(project: ProjectEntity, pairing: String) {
    this.activeProject = project
    this.pairingCode = pairing
  }

  override fun serve(session: IHTTPSession): Response {
    val uri = session.uri
    val method = session.method

    return try {
      when {
        uri == "/" || uri == "/index.html" -> serveStudioHtml()
        uri == "/api/project" && method == Method.GET -> handleGetProject()
        uri == "/api/files" && method == Method.GET -> handleGetFiles()
        uri == "/api/file" && method == Method.GET -> handleGetFile(session.parameters)
        uri == "/api/save" && method == Method.POST -> handleSaveFile(session)
        uri == "/api/create" && method == Method.POST -> handleCreateFile(session)
        uri == "/api/chat" && method == Method.GET -> handleGetChat()
        uri == "/api/chat" && method == Method.POST -> handlePostChat(session)
        uri == "/api/status" -> handleGetStatus()
        else -> newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found")
      }.apply {
        addHeader("Access-Control-Allow-Origin", "*")
        addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        addHeader("Access-Control-Allow-Headers", "Content-Type")
      }
    } catch (e: Exception) {
      Log.e("LanternWebServer", "Error serving request", e)
      newFixedLengthResponse(
        Response.Status.INTERNAL_ERROR,
        MIME_PLAINTEXT,
        "Server Error: ${e.message}"
      )
    }
  }

  private fun serveStudioHtml(): Response {
    val project = activeProject ?: runBlocking {
      repository.allProjects.first().firstOrNull() ?: ProjectEntity(
        id = 1,
        name = "LANtern Workspace",
        description = "Collaborative Studio"
      )
    }

    val ip = getLocalIpAddress()
    val html = WebCodeStudioHtml.getHtml(
      projectName = project.name,
      branch = project.currentBranch,
      pairingCode = pairingCode,
      hostIp = ip,
      port = listeningPort
    )
    return newFixedLengthResponse(Response.Status.OK, "text/html", html)
  }

  private fun handleGetProject(): Response {
    val project = activeProject ?: runBlocking { repository.allProjects.first().firstOrNull() }
    val json = JSONObject().apply {
      put("id", project?.id ?: 0)
      put("name", project?.name ?: "LANtern Workspace")
      put("branch", project?.currentBranch ?: "main")
      put("pairingCode", pairingCode)
    }
    return newFixedLengthResponse(Response.Status.OK, "application/json", json.toString())
  }

  private fun handleGetFiles(): Response {
    val project = activeProject ?: runBlocking { repository.allProjects.first().firstOrNull() }
    if (project == null) {
      return newFixedLengthResponse(Response.Status.OK, "application/json", "[]")
    }

    val files = runBlocking { repository.getFilesForProject(project.id).first() }
    val array = JSONArray()
    for (f in files) {
      array.put(JSONObject().apply {
        put("id", f.id)
        put("name", f.name)
        put("path", f.path)
        put("language", f.language)
        put("size", f.content.length)
      })
    }
    return newFixedLengthResponse(Response.Status.OK, "application/json", array.toString())
  }

  private fun handleGetFile(params: Map<String, List<String>>): Response {
    val path = params["path"]?.firstOrNull()
    val fileId = params["id"]?.firstOrNull()?.toLongOrNull()
    val project = activeProject ?: runBlocking { repository.allProjects.first().firstOrNull() }

    val file = runBlocking {
      if (fileId != null) {
        db.projectFileDao().getFileById(fileId)
      } else if (project != null && path != null) {
        repository.getFilesForProject(project.id).first().firstOrNull { it.path == path }
      } else null
    }

    return if (file != null) {
      val json = JSONObject().apply {
        put("id", file.id)
        put("name", file.name)
        put("path", file.path)
        put("content", file.content)
        put("language", file.language)
      }
      newFixedLengthResponse(Response.Status.OK, "application/json", json.toString())
    } else {
      newFixedLengthResponse(Response.Status.NOT_FOUND, "application/json", "{\"error\":\"File not found\"}")
    }
  }

  private fun handleSaveFile(session: IHTTPSession): Response {
    val body = parseRequestBody(session)
    val json = JSONObject(body)
    val path = json.optString("path")
    val content = json.optString("content")
    val fileId = json.optLong("fileId", 0L)
    val project = activeProject ?: runBlocking { repository.allProjects.first().firstOrNull() }

    if (project == null) {
      return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", "{\"error\":\"No active project\"}")
    }

    runBlocking {
      val existing = if (fileId > 0) {
        db.projectFileDao().getFileById(fileId)
      } else {
        repository.getFilesForProject(project.id).first().firstOrNull { it.path == path }
      }

      if (existing != null) {
        val oldContent = existing.content
        val updated = existing.copy(content = content, isModified = true)
        repository.updateFile(updated)

        // Save to /home/LANtern/projects/
        val allFiles = repository.getFilesForProject(project.id).first()
        LanternDiskStorage.saveProjectToDisk(context, project, allFiles)

        // Record diff and blame in database and /home/LANtern/.diffs/
        LanternDiskStorage.recordDiffAndBlame(
          context = context,
          db = db,
          project = project,
          file = updated,
          oldContent = oldContent,
          newContent = content,
          commitHash = "web-" + System.currentTimeMillis().toString().takeLast(6),
          author = "Web Client Dev"
        )
      }
    }

    return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"success\":true}")
  }

  private fun handleCreateFile(session: IHTTPSession): Response {
    val body = parseRequestBody(session)
    val json = JSONObject(body)
    val path = json.optString("path", "NewFile.txt")
    val name = json.optString("name", path.substringAfterLast("/"))
    val content = json.optString("content", "")
    val project = activeProject ?: runBlocking { repository.allProjects.first().firstOrNull() }

    if (project == null) {
      return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", "{\"error\":\"No active project\"}")
    }

    val ext = path.substringAfterLast(".", "txt")
    val newId = runBlocking {
      repository.addFile(project.id, path, name, content, ext)
    }

    return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"success\":true,\"id\":$newId}")
  }

  private fun handleGetChat(): Response {
    val roomId = activeProject?.let { "room-${it.id}" } ?: "global-room"
    val messages = runBlocking {
      db.teamChatMessageDao().getMessagesForRoom(roomId).first()
    }

    val array = JSONArray()
    for (m in messages) {
      array.put(JSONObject().apply {
        put("id", m.messageId)
        put("senderName", m.senderName)
        put("text", m.text)
        put("hasCodeReference", m.hasCodeReference)
        put("referencedFilePath", m.referencedFilePath)
        put("referencedLineNumber", m.referencedLineNumber)
        put("referencedCodeSnippet", m.referencedCodeSnippet)
        put("timestamp", m.timestamp)
      })
    }
    return newFixedLengthResponse(Response.Status.OK, "application/json", array.toString())
  }

  private fun handlePostChat(session: IHTTPSession): Response {
    val body = parseRequestBody(session)
    val json = JSONObject(body)
    val roomId = activeProject?.let { "room-${it.id}" } ?: "global-room"

    val msg = TeamChatMessageEntity(
      messageId = "msg-" + System.currentTimeMillis(),
      roomId = roomId,
      senderId = "web-client",
      senderName = json.optString("senderName", "Web Dev"),
      text = json.optString("text", ""),
      hasCodeReference = json.optBoolean("hasCodeReference", false),
      referencedFilePath = json.optString("referencedFilePath", ""),
      referencedLineNumber = json.optInt("referencedLineNumber", 0),
      referencedCodeSnippet = json.optString("referencedCodeSnippet", ""),
      timestamp = System.currentTimeMillis()
    )

    runBlocking {
      db.teamChatMessageDao().insertMessage(msg)
    }

    return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"success\":true}")
  }

  private fun handleGetStatus(): Response {
    val json = JSONObject().apply {
      put("status", "running")
      put("port", listeningPort)
      put("ip", getLocalIpAddress())
      put("pairingCode", pairingCode)
      put("activeProject", activeProject?.name ?: "None")
    }
    return newFixedLengthResponse(Response.Status.OK, "application/json", json.toString())
  }

  private fun parseRequestBody(session: IHTTPSession): String {
    val map = HashMap<String, String>()
    session.parseBody(map)
    return map["postData"] ?: ""
  }

  companion object {
    fun getLocalIpAddress(): String {
      try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
          val intf = interfaces.nextElement()
          val addrs = intf.inetAddresses
          while (addrs.hasMoreElements()) {
            val addr = addrs.nextElement()
            if (!addr.isLoopbackAddress && addr is Inet4Address) {
              return addr.hostAddress ?: "127.0.0.1"
            }
          }
        }
      } catch (_: Exception) {}
      return "127.0.0.1"
    }
  }
}
