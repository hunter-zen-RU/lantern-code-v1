package com.example.data

import android.content.Context
import android.database.Cursor
import com.example.data.entity.AuditReportEntity
import com.example.data.entity.GitCommitEntity
import com.example.data.entity.PeerSessionEntity
import com.example.data.entity.ProjectEntity
import com.example.data.entity.ProjectFileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

data class SqlQueryResult(
  val columns: List<String>,
  val rows: List<List<String>>,
  val executionTimeMs: Long,
  val rowCount: Int,
  val error: String? = null
)

class WorkspaceRepository(private val database: LanternDatabase) {
  val allProjects: Flow<List<ProjectEntity>> = database.projectDao().getAllProjects()

  fun getFilesForProject(projectId: Long): Flow<List<ProjectFileEntity>> =
    database.projectFileDao().getFilesForProject(projectId)

  fun getCommitsForProject(projectId: Long): Flow<List<GitCommitEntity>> =
    database.gitCommitDao().getCommitsForProject(projectId)

  fun getReportsForProject(projectId: Long): Flow<List<AuditReportEntity>> =
    database.auditReportDao().getReportsForProject(projectId)

  fun getSessionsForRoom(roomId: String): Flow<List<PeerSessionEntity>> =
    database.peerSessionDao().getSessionsForRoom(roomId)

  suspend fun createProject(name: String, description: String, gitRemoteUrl: String = ""): Long =
    withContext(Dispatchers.IO) {
      val project = ProjectEntity(
        name = name,
        description = description,
        gitRemoteUrl = gitRemoteUrl,
        currentBranch = "main"
      )
      val projectId = database.projectDao().insertProject(project)

      // Add initial README and starter file
      val readme = ProjectFileEntity(
        projectId = projectId,
        path = "README.md",
        name = "README.md",
        content = """# $name
$description

## 💡 About LANtern Code Workspace
This project is stored directly in device-local SQLite via Room.
- Real-time WebRTC room collaboration
- AI vibecoding assistance
- Instant audit and test execution
""",
        language = "markdown"
      )

      val mainCode = ProjectFileEntity(
        projectId = projectId,
        path = "src/Main.kt",
        name = "Main.kt",
        content = """package $name.core

import kotlinx.coroutines.runBlocking

/**
 * LANtern Code Node Engine
 * Decentralized peer-to-peer workspace execution
 */
fun main() = runBlocking {
    println("💡 LANtern Code session initiated for $name")
    val status = verifyNetworkMesh()
    println("Mesh status: " + status)
}

fun verifyNetworkMesh(): String {
    val nodeActive = true
    return if (nodeActive) "PEER_ONLINE_READY" else "OFFLINE"
}
""",
        language = "kotlin"
      )

      database.projectFileDao().insertFiles(listOf(readme, mainCode))

      // Initial Git commit
      database.gitCommitDao().insertCommit(
        GitCommitEntity(
          projectId = projectId,
          commitHash = "init-${System.currentTimeMillis().toString().takeLast(6)}",
          message = "Initial commit: scaffold $name project",
          author = "LANtern Dev <dev@local>"
        )
      )

      projectId
    }

  suspend fun updateFile(file: ProjectFileEntity) = withContext(Dispatchers.IO) {
    database.projectFileDao().updateFile(file.copy(updatedAt = System.currentTimeMillis()))
  }

  suspend fun addFile(projectId: Long, path: String, name: String, content: String, language: String): Long =
    withContext(Dispatchers.IO) {
      val file = ProjectFileEntity(
        projectId = projectId,
        path = path,
        name = name,
        content = content,
        language = language,
        isModified = true
      )
      database.projectFileDao().insertFile(file)
    }

  suspend fun deleteFile(fileId: Long) = withContext(Dispatchers.IO) {
    database.projectFileDao().deleteFileById(fileId)
  }

  suspend fun deleteProject(projectId: Long) = withContext(Dispatchers.IO) {
    database.projectFileDao().deleteFilesForProject(projectId)
    database.projectDao().deleteProjectById(projectId)
  }

  suspend fun addCommit(projectId: Long, message: String, author: String, branch: String): Long =
    withContext(Dispatchers.IO) {
      val hash = "git-${System.currentTimeMillis().toString().takeLast(7)}"
      database.gitCommitDao().insertCommit(
        GitCommitEntity(
          projectId = projectId,
          commitHash = hash,
          message = message,
          author = author,
          branch = branch
        )
      )
    }

  suspend fun addAuditReport(report: AuditReportEntity) = withContext(Dispatchers.IO) {
    database.auditReportDao().insertReport(report)
  }

  suspend fun addPeerSession(session: PeerSessionEntity) = withContext(Dispatchers.IO) {
    database.peerSessionDao().insertSession(session)
  }

  suspend fun clearSessionsForRoom(roomId: String) = withContext(Dispatchers.IO) {
    database.peerSessionDao().deleteSessionsForRoom(roomId)
  }

  // Interactive SQLite Raw Query Executor for the database management studio
  suspend fun executeRawSqlQuery(sql: String): SqlQueryResult = withContext(Dispatchers.IO) {
    val startTime = System.currentTimeMillis()
    try {
      val cleanSql = sql.trim()
      val db = database.openHelper.writableDatabase

      if (cleanSql.startsWith("SELECT", ignoreCase = true) ||
        cleanSql.startsWith("PRAGMA", ignoreCase = true) ||
        cleanSql.startsWith("EXPLAIN", ignoreCase = true)
      ) {
        val cursor: Cursor = db.query(cleanSql)
        val columns = cursor.columnNames.toList()
        val rows = mutableListOf<List<String>>()

        cursor.use {
          while (it.moveToNext()) {
            val row = mutableListOf<String>()
            for (i in 0 until it.columnCount) {
              row.add(it.getString(i) ?: "NULL")
            }
            rows.add(row)
          }
        }
        val elapsed = System.currentTimeMillis() - startTime
        SqlQueryResult(
          columns = columns,
          rows = rows,
          executionTimeMs = elapsed,
          rowCount = rows.size
        )
      } else {
        db.execSQL(cleanSql)
        val elapsed = System.currentTimeMillis() - startTime
        SqlQueryResult(
          columns = listOf("Status"),
          rows = listOf(listOf("Query executed successfully: affected rows updated")),
          executionTimeMs = elapsed,
          rowCount = 1
        )
      }
    } catch (e: Exception) {
      val elapsed = System.currentTimeMillis() - startTime
      SqlQueryResult(
        columns = listOf("Error"),
        rows = emptyList(),
        executionTimeMs = elapsed,
        rowCount = 0,
        error = e.localizedMessage ?: "Unknown SQL execution error"
      )
    }
  }

  suspend fun seedSampleProjectIfEmpty() = withContext(Dispatchers.IO) {
    val db = database.openHelper.readableDatabase
    val cursor = db.query("SELECT COUNT(*) FROM projects")
    var count = 0
    cursor.use {
      if (it.moveToNext()) {
        count = it.getInt(0)
      }
    }

    if (count == 0) {
      val projId = createProject(
        name = "NeonLantern-Core",
        description = "High-performance collaborative edge IDE & P2P WebRTC engine",
        gitRemoteUrl = "https://github.com/lantern-mesh/neonlantern-core.git"
      )

      // Add additional starter files
      addFile(
        projectId = projId,
        path = "src/MeshPeer.kt",
        name = "MeshPeer.kt",
        content = """package com.lantern.mesh

/**
 * WebRTC DataChannel connection state handler
 */
data class PeerInfo(
    val peerId: String,
    val role: String = "Editor",
    val latencyMs: Long = 18,
    val isEncrypted: Boolean = true
)

class MeshPeer(val info: PeerInfo) {
    fun sendDelta(delta: String): Boolean {
        // Broadcasts delta changes across peer data channels
        return true
    }
}
""",
        language = "kotlin"
      )

      addFile(
        projectId = projId,
        path = "db/schema.sql",
        name = "schema.sql",
        content = """-- LANtern Code SQLite local table definition
CREATE TABLE IF NOT EXISTS local_cache (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL,
    synced_at INTEGER DEFAULT (strftime('%s', 'now'))
);

INSERT INTO local_cache (key, value) VALUES ('session_token', 'rtc-sec-9842');
INSERT INTO local_cache (key, value) VALUES ('peer_name', 'NeonCoder-Mobile');
""",
        language = "sql"
      )

      addFile(
        projectId = projId,
        path = "tests/MeshUnitTest.kt",
        name = "MeshUnitTest.kt",
        content = """package com.lantern.tests

fun testPeerHandshake(): Boolean {
    val peerReady = true
    val encryptionKeySet = true
    return peerReady && encryptionKeySet
}

fun testDeltaSynchronization(): Boolean {
    val initialText = "LANtern Code"
    val delta = " 💡"
    return (initialText + delta).length == 14
}
""",
        language = "kotlin"
      )
    }
  }
}
