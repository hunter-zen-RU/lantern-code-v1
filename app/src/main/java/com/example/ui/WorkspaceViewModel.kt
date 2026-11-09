package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiConfig
import com.example.ai.AiEngine
import com.example.ai.AiResult
import com.example.ai.AuditAnalysisResult
import com.example.ai.ProjectAuditor
import com.example.collab.WebRtcCollaborationManager
import com.example.collab.WebRtcRoom
import com.example.data.LanternDatabase
import com.example.data.SqlQueryResult
import com.example.data.WorkspaceRepository
import com.example.data.entity.AuditReportEntity
import com.example.data.entity.GitCommitEntity
import com.example.data.entity.ProjectEntity
import com.example.data.entity.ProjectFileEntity
import com.example.git.GitManager
import com.example.git.GitRepoStatus
import com.example.git.GitHubRemoteRepo
import com.example.git.GitHubUserProfile
import com.example.testdeploy.DeploymentPackage
import com.example.testdeploy.DeploymentPlatform
import com.example.testdeploy.TestAndDeploymentRunner
import com.example.testdeploy.TestSuiteSummary
import com.example.collab.CodeReference
import com.example.collab.FirebaseWebRtcSignalingEngine
import com.example.data.LanternDiskStorage
import com.example.data.entity.GitBlameEntity
import com.example.data.entity.GitDiffEntity
import com.example.server.LanternLocalWebServer
import com.example.data.ProjectTemplate
import java.io.File
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkspaceViewModel(application: Application) : AndroidViewModel(application) {
  private val database = LanternDatabase.getDatabase(application)
  val repository = WorkspaceRepository(database)
  val gitManager = GitManager(repository)
  val collabManager = WebRtcCollaborationManager()
  val aiEngine = AiEngine()

  private var projectFilesJob: Job? = null
  private var commitsJob: Job? = null

  val allProjects: StateFlow<List<ProjectEntity>> = repository.allProjects
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _selectedProject = MutableStateFlow<ProjectEntity?>(null)
  val selectedProject: StateFlow<ProjectEntity?> = _selectedProject.asStateFlow()

  private val _projectFiles = MutableStateFlow<List<ProjectFileEntity>>(emptyList())
  val projectFiles: StateFlow<List<ProjectFileEntity>> = _projectFiles.asStateFlow()

  private val _activeFile = MutableStateFlow<ProjectFileEntity?>(null)
  val activeFile: StateFlow<ProjectFileEntity?> = _activeFile.asStateFlow()

  private val _editorContent = MutableStateFlow("")
  val editorContent: StateFlow<String> = _editorContent.asStateFlow()

  private val _openTabs = MutableStateFlow<List<ProjectFileEntity>>(emptyList())
  val openTabs: StateFlow<List<ProjectFileEntity>> = _openTabs.asStateFlow()

  private val _commits = MutableStateFlow<List<GitCommitEntity>>(emptyList())
  val commits: StateFlow<List<GitCommitEntity>> = _commits.asStateFlow()

  val roomState: StateFlow<WebRtcRoom?> = collabManager.roomState
  val gitStatus: StateFlow<GitRepoStatus> = gitManager.gitStatus
  val gitHubProfile: StateFlow<GitHubUserProfile> = gitManager.userProfile
  val gitHubRepos: StateFlow<List<GitHubRemoteRepo>> = gitManager.userRepositories
  val isGitLoading: StateFlow<Boolean> = gitManager.isLoading

  private val _auditResult = MutableStateFlow<AuditAnalysisResult?>(null)
  val auditResult: StateFlow<AuditAnalysisResult?> = _auditResult.asStateFlow()

  private val _testSummary = MutableStateFlow<TestSuiteSummary?>(null)
  val testSummary: StateFlow<TestSuiteSummary?> = _testSummary.asStateFlow()

  private val _isTesting = MutableStateFlow(false)
  val isTesting: StateFlow<Boolean> = _isTesting.asStateFlow()

  private val _sqlQueryResult = MutableStateFlow<SqlQueryResult?>(null)
  val sqlQueryResult: StateFlow<SqlQueryResult?> = _sqlQueryResult.asStateFlow()

  private val _aiResult = MutableStateFlow<AiResult?>(null)
  val aiResult: StateFlow<AiResult?> = _aiResult.asStateFlow()

  private val _isAiWorking = MutableStateFlow(false)
  val isAiWorking: StateFlow<Boolean> = _isAiWorking.asStateFlow()

  private val _deploymentPackage = MutableStateFlow<DeploymentPackage?>(null)
  val deploymentPackage: StateFlow<DeploymentPackage?> = _deploymentPackage.asStateFlow()

  private val _userMessage = MutableStateFlow<String?>(null)
  val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

  val signalingEngine = FirebaseWebRtcSignalingEngine(application)
  private var webServer: LanternLocalWebServer? = null

  private val _isServerRunning = MutableStateFlow(false)
  val isServerRunning: StateFlow<Boolean> = _isServerRunning.asStateFlow()

  private val _serverUrl = MutableStateFlow("http://${LanternLocalWebServer.getLocalIpAddress()}:8080")
  val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

  private val _pairingCode = MutableStateFlow(signalingEngine.generatePairingCode())
  val pairingCode: StateFlow<String> = _pairingCode.asStateFlow()

  private val _targetScrollLine = MutableStateFlow<Int?>(null)
  val targetScrollLine: StateFlow<Int?> = _targetScrollLine.asStateFlow()

  private val _diffRecords = MutableStateFlow<List<GitDiffEntity>>(emptyList())
  val diffRecords: StateFlow<List<GitDiffEntity>> = _diffRecords.asStateFlow()

  private val _blameRecords = MutableStateFlow<List<GitBlameEntity>>(emptyList())
  val blameRecords: StateFlow<List<GitBlameEntity>> = _blameRecords.asStateFlow()

  private var diffsJob: Job? = null
  private var blameJob: Job? = null

  init {
    viewModelScope.launch {
      repository.seedSampleProjectIfEmpty()
      // Create initial WebRTC collaboration room for the host with pairing code
      val initialRoom = collabManager.createRoom("Neon-Mesh-01", "Mobile Lead")
      _pairingCode.value = initialRoom.pairingCode

      // Initialize NanoHTTPD local web server for LAN web code studio
      webServer = LanternLocalWebServer(
        port = 8080,
        context = application,
        repository = repository,
        db = database,
        scope = viewModelScope
      )

      // Pre-select first deployment target
      _deploymentPackage.value = TestAndDeploymentRunner.generateDeployment(DeploymentPlatform.ANDROID_APK, "NeonLantern-Core")
      // Check for saved or pre-loaded GitHub dev account
      gitManager.connectWithGitHubToken("demo-pat-token")
    }
  }

  fun clearUserMessage() {
    _userMessage.value = null
  }

  fun showMessage(msg: String) {
    _userMessage.value = msg
  }

  fun selectProject(project: ProjectEntity) {
    _selectedProject.value = project
    _openTabs.value = emptyList()
    _activeFile.value = null
    _editorContent.value = ""

    webServer?.setActiveProject(project, _pairingCode.value)

    diffsJob?.cancel()
    diffsJob = viewModelScope.launch {
      repository.getDiffsForProject(project.id).collect {
        _diffRecords.value = it
      }
    }

    projectFilesJob?.cancel()
    projectFilesJob = viewModelScope.launch {
      repository.getFilesForProject(project.id).collect { files ->
        _projectFiles.value = files
        if (files.isNotEmpty()) {
          // If no active file or active file does not belong to this project, select the primary file
          if (_activeFile.value == null || !files.any { it.id == _activeFile.value?.id }) {
            val primaryFile = files.firstOrNull {
              it.name in listOf("index.html", "main.py", "src/index.ts", "index.ts", "src/Main.kt", "Main.kt", "README.md")
            } ?: files.first()
            openFile(primaryFile)
          }
        } else {
          _activeFile.value = null
          _editorContent.value = ""
        }
      }
    }

    commitsJob?.cancel()
    commitsJob = viewModelScope.launch {
      repository.getCommitsForProject(project.id).collect { list ->
        _commits.value = list
      }
    }
  }

  fun openFile(file: ProjectFileEntity) {
    _activeFile.value = file
    _editorContent.value = file.content
    val currentTabs = _openTabs.value
    if (!currentTabs.any { it.id == file.id }) {
      _openTabs.value = currentTabs + file
    }
    blameJob?.cancel()
    blameJob = viewModelScope.launch {
      repository.getBlameForFile(file.id).collect {
        _blameRecords.value = it
      }
    }
  }

  fun closeTab(file: ProjectFileEntity) {
    val currentTabs = _openTabs.value.filterNot { it.id == file.id }
    _openTabs.value = currentTabs
    if (_activeFile.value?.id == file.id) {
      if (currentTabs.isNotEmpty()) {
        openFile(currentTabs.last())
      } else {
        _activeFile.value = null
        _editorContent.value = ""
      }
    }
  }

  fun updateEditorContent(newContent: String) {
    _editorContent.value = newContent
    _activeFile.value = _activeFile.value?.copy(content = newContent, isModified = true)
  }

  fun saveCurrentFile() {
    val current = _activeFile.value ?: return
    val project = _selectedProject.value ?: return
    val oldContent = current.content
    val newContent = _editorContent.value
    viewModelScope.launch {
      val updated = current.copy(content = newContent, isModified = false)
      repository.updateFile(updated)
      _activeFile.value = updated
      _openTabs.value = _openTabs.value.map { if (it.id == updated.id) updated else it }

      // Save to /home/LANtern/projects/{projectName}/
      val allFiles = repository.getFilesForProject(project.id).first()
      LanternDiskStorage.saveProjectToDisk(getApplication(), project, allFiles)

      // Record diff and blame in database and /home/LANtern/.diffs/{diff-id}/
      LanternDiskStorage.recordDiffAndBlame(
        context = getApplication(),
        db = database,
        project = project,
        file = updated,
        oldContent = oldContent,
        newContent = newContent,
        commitHash = "save-" + System.currentTimeMillis().toString().takeLast(6),
        author = "Mobile Lead"
      )

      showMessage("Saved ${current.name} & synced to /home/LANtern/projects/${project.name}")
    }
  }

  fun closeAllTabs() {
    _openTabs.value = emptyList()
    _activeFile.value = null
    _editorContent.value = ""
    showMessage("Closed all open files")
  }

  fun saveAllFiles() {
    viewModelScope.launch {
      val tabs = _openTabs.value
      for (file in tabs) {
        if (file.isModified) {
          val contentToSave = if (file.id == _activeFile.value?.id) _editorContent.value else file.content
          val updated = file.copy(content = contentToSave, isModified = false)
          repository.updateFile(updated)
        }
      }
      _activeFile.value = _activeFile.value?.copy(isModified = false)
      _openTabs.value = _openTabs.value.map { it.copy(isModified = false) }
      showMessage("Saved all open files")
    }
  }

  fun formatCurrentDocument() {
    val current = _editorContent.value
    if (current.isNotBlank()) {
      val formatted = current.lines().joinToString("\n") { line ->
        if (line.trim().isEmpty()) "" else line.trimEnd()
      }
      updateEditorContent(formatted)
      showMessage("Formatted document")
    }
  }

  fun createNewFile(projectId: Long, path: String, name: String, language: String) {
    viewModelScope.launch {
      val starterCode = generateStarterCodeForFile(name, language)
      val id = repository.addFile(
        projectId = projectId,
        path = path,
        name = name,
        content = starterCode,
        language = language
      )
      val newFile = ProjectFileEntity(
        id = id,
        projectId = projectId,
        path = path,
        name = name,
        content = starterCode,
        language = language
      )
      openFile(newFile)
      showMessage("Created $name in studio")
    }
  }

  fun deleteFile(file: ProjectFileEntity) {
    viewModelScope.launch {
      repository.deleteFile(file.id)
      closeTab(file)
      showMessage("Deleted '${file.name}' from workspace")
    }
  }

  fun switchBranch(branch: String) {
    gitManager.switchBranch(branch)
    showMessage("Switched branch to '$branch'")
  }

  fun createBranch(branch: String) {
    gitManager.createBranch(branch)
    showMessage("Created branch '$branch'")
  }

  private fun generateStarterCodeForFile(name: String, language: String): String {
    val ext = name.substringAfterLast(".", language).lowercase()
    return when (ext) {
      "html", "htm" -> """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>$name</title>
</head>
<body>
  <h1>$name</h1>
  <p>Created in LANtern Code Studio</p>
</body>
</html>
"""
      "css" -> """/* $name stylesheet */
body {
  margin: 0;
  padding: 16px;
  background: #0b0f17;
  color: #f0f6fc;
}
"""
      "js", "javascript" -> """// $name
console.log("Initialized $name");

export function run() {
  return true;
}
"""
      "ts", "typescript" -> """// $name
export interface Config {
  enabled: boolean;
}

export function init(config: Config): void {
  console.log("TypeScript module ready:", config);
}
"""
      "py", "python" -> """# $name
import sys

def main():
    print("Running $name in LANtern Python runtime")

if __name__ == "__main__":
    main()
"""
      "md", "markdown" -> """# $name

Document created in LANtern Code Studio.

### Notes
- Interactive preview available via the Preview toggle.
"""
      "json" -> """{
  "name": "$name",
  "version": "1.0.0",
  "created": true
}
"""
      "sql" -> """-- $name
CREATE TABLE IF NOT EXISTS sample_table (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  title TEXT NOT NULL,
  created_at INTEGER DEFAULT (strftime('%s', 'now'))
);
"""
      "kt", "kotlin" -> """package com.example

fun main() {
    println("LANtern Code: $name")
}
"""
      else -> "// $name created in LANtern Code Studio\n"
    }
  }

  fun createProject(
    name: String,
    description: String,
    gitUrl: String,
    template: ProjectTemplate = ProjectTemplate.WEB_HTML_JS
  ) {
    viewModelScope.launch {
      val id = repository.createProject(name, description, gitUrl, template)
      val proj = ProjectEntity(id = id, name = name, description = description, gitRemoteUrl = gitUrl)
      selectProject(proj)
      showMessage("Workspace '$name' created (${template.displayName})")
    }
  }

  fun connectGitHubKey(token: String) {
    viewModelScope.launch {
      val result = gitManager.connectWithGitHubToken(token)
      if (result.isSuccess) {
        val prof = result.getOrNull()
        showMessage("Connected to GitHub as @${prof?.login ?: "user"}")
      } else {
        showMessage("Failed to connect with GitHub token")
      }
    }
  }

  fun refreshGitHubRepos() {
    viewModelScope.launch {
      gitManager.fetchRemoteRepositories()
      showMessage("GitHub repositories updated")
    }
  }

  fun createGitHubRemoteRepo(name: String, description: String, isPrivate: Boolean, onDone: () -> Unit) {
    viewModelScope.launch {
      showMessage("Creating repository on GitHub...")
      val result = gitManager.createRemoteRepository(name, description, isPrivate)
      if (result.isSuccess) {
        val created = result.getOrThrow()
        // Automatically import created repo into SQLite workspace
        val projId = gitManager.importRepoToWorkspace(created)
        val proj = ProjectEntity(
          id = projId,
          name = created.name,
          description = created.description,
          gitRemoteUrl = created.cloneUrl
        )
        selectProject(proj)
        showMessage("Repository '${created.name}' created on GitHub & opened in studio")
        onDone()
      } else {
        showMessage("Failed to create repository on GitHub")
      }
    }
  }

  fun importGitHubRepo(repo: GitHubRemoteRepo, onDone: () -> Unit) {
    viewModelScope.launch {
      showMessage("Syncing ${repo.name} into SQLite workspace...")
      val projId = gitManager.importRepoToWorkspace(repo)
      val proj = ProjectEntity(
        id = projId,
        name = repo.name,
        description = repo.description,
        gitRemoteUrl = repo.cloneUrl
      )
      selectProject(proj)
      showMessage("Repository '${repo.name}' synced and ready to code")
      onDone()
    }
  }

  fun disconnectGitHub() {
    gitManager.disconnect()
    showMessage("Disconnected GitHub account")
  }

  fun commitGitChanges(message: String) {
    val proj = _selectedProject.value ?: return
    viewModelScope.launch {
      val modified = _projectFiles.value.filter { it.isModified }
      for (f in modified) {
        val contentToSave = if (f.id == _activeFile.value?.id) _editorContent.value else f.content
        val updated = f.copy(content = contentToSave, isModified = false)
        repository.updateFile(updated)
      }
      _activeFile.value = _activeFile.value?.copy(isModified = false)
      _openTabs.value = _openTabs.value.map { it.copy(isModified = false) }

      val result = gitManager.commitChanges(proj.id, message)
      showMessage(result)
    }
  }

  fun pushGit() {
    viewModelScope.launch {
      val res = gitManager.pushToRemote()
      showMessage(res)
    }
  }

  fun pullGit() {
    viewModelScope.launch {
      val res = gitManager.pullFromRemote()
      showMessage(res)
    }
  }

  fun runAudit() {
    val proj = _selectedProject.value ?: return
    val files = _projectFiles.value
    viewModelScope.launch {
      val result = ProjectAuditor.auditFiles(proj.id, files)
      _auditResult.value = result
      val entity = ProjectAuditor.toEntity(proj.id, result)
      repository.addAuditReport(entity)
      showMessage("Project audit complete: Health Score ${result.healthScore}/100")
    }
  }

  fun runAutomatedTests() {
    val files = _projectFiles.value
    viewModelScope.launch {
      _isTesting.value = true
      val summary = TestAndDeploymentRunner.runTests(files)
      _testSummary.value = summary
      _isTesting.value = false
      showMessage("Tests finished: ${summary.passedCount}/${summary.totalTests} passed in ${summary.totalDurationMs}ms")
    }
  }

  fun executeSql(query: String) {
    viewModelScope.launch {
      val result = repository.executeRawSqlQuery(query)
      _sqlQueryResult.value = result
      if (result.error != null) {
        showMessage("SQL Error: ${result.error}")
      } else {
        showMessage("SQL executed: ${result.rowCount} row(s) returned")
      }
    }
  }

  fun vibecodeWithAi(instruction: String) {
    val file = _activeFile.value ?: return
    viewModelScope.launch {
      _isAiWorking.value = true
      val res = aiEngine.vibecode(instruction, _editorContent.value, file.language)
      _aiResult.value = res
      _isAiWorking.value = false
    }
  }

  fun applyAiSuggestion(suggestion: String) {
    updateEditorContent(_editorContent.value + "\n\n" + suggestion)
    saveCurrentFile()
    _aiResult.value = null
    showMessage("AI generated code applied to ${activeFile.value?.name}")
  }

  fun selectDeploymentPlatform(platform: DeploymentPlatform) {
    val projName = _selectedProject.value?.name ?: "LANtern-Project"
    _deploymentPackage.value = TestAndDeploymentRunner.generateDeployment(platform, projName)
  }

  fun startLocalWebServer() {
    try {
      if (webServer?.isAlive != true) {
        val proj = _selectedProject.value ?: allProjects.value.firstOrNull()
        if (proj != null) {
          webServer?.setActiveProject(proj, _pairingCode.value)
        }
        webServer?.start()
        _isServerRunning.value = true
        _serverUrl.value = "http://${LanternLocalWebServer.getLocalIpAddress()}:${webServer?.listeningPort ?: 8080}"
        showMessage("Web Code Studio live on ${_serverUrl.value}")
      }
    } catch (e: Exception) {
      showMessage("Server error: ${e.message}")
    }
  }

  fun stopLocalWebServer() {
    try {
      webServer?.stop()
      _isServerRunning.value = false
      showMessage("Web Code Studio server stopped")
    } catch (e: Exception) {
      showMessage("Server stop error: ${e.message}")
    }
  }

  fun toggleLocalWebServer() {
    if (_isServerRunning.value) {
      stopLocalWebServer()
    } else {
      startLocalWebServer()
    }
  }

  fun exportProjectToDisk() {
    val project = _selectedProject.value ?: return
    viewModelScope.launch {
      val files = repository.getFilesForProject(project.id).first()
      val dir = LanternDiskStorage.saveProjectToDisk(getApplication(), project, files)
      showMessage("Exported ${files.size} files to ${dir.absolutePath}")
    }
  }

  fun connectWithPairingCode(code: String, userName: String = "Mobile Dev") {
    val clean = code.trim().uppercase()
    if (clean.isBlank()) return
    _pairingCode.value = clean
    collabManager.connectWithPairingCode(clean, userName)
    showMessage("Connected to team room via pairing code: $clean")
  }

  fun sendChatMessageWithReference(
    text: String,
    senderName: String = "Mobile Dev",
    file: ProjectFileEntity? = null,
    lineNumber: Int = 0,
    snippet: String = ""
  ) {
    val codeRef = if (file != null && lineNumber > 0) {
      CodeReference(
        filePath = file.path,
        fileName = file.name,
        lineNumber = lineNumber,
        lineSnippet = snippet.ifBlank {
          file.content.lines().getOrNull(lineNumber - 1)?.trim() ?: ""
        }
      )
    } else null

    collabManager.sendChatMessage(text, senderName, codeRef)

    // Also persist in local Room database
    val roomId = _selectedProject.value?.let { "room-${it.id}" } ?: "global-room"
    viewModelScope.launch {
      database.teamChatMessageDao().insertMessage(
        com.example.data.entity.TeamChatMessageEntity(
          messageId = "msg-" + System.currentTimeMillis(),
          roomId = roomId,
          senderId = "mobile-lead",
          senderName = senderName,
          text = text,
          hasCodeReference = codeRef != null,
          referencedFilePath = codeRef?.filePath ?: "",
          referencedLineNumber = codeRef?.lineNumber ?: 0,
          referencedCodeSnippet = codeRef?.lineSnippet ?: "",
          timestamp = System.currentTimeMillis()
        )
      )
    }
  }

  fun navigateToCodeReference(filePath: String, lineNumber: Int) {
    val file = _projectFiles.value.firstOrNull { it.path == filePath || it.name == filePath }
    if (file != null) {
      openFile(file)
      _targetScrollLine.value = lineNumber
      showMessage("Jumped to ${file.name} : Line $lineNumber")
    } else {
      showMessage("Referenced file $filePath not found in workspace")
    }
  }

  fun clearTargetScrollLine() {
    _targetScrollLine.value = null
  }

  override fun onCleared() {
    super.onCleared()
    try {
      webServer?.stop()
    } catch (_: Exception) {}
  }
}
