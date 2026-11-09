package com.example

import com.example.ai.AuditSeverity
import com.example.ai.ProjectAuditor
import com.example.collab.WebRtcCollaborationManager
import com.example.data.LanternDatabase
import com.example.data.WorkspaceRepository
import com.example.data.entity.ProjectFileEntity
import com.example.git.DiffLineType
import com.example.git.GitHubRemoteRepo
import com.example.git.GitManager
import com.example.testdeploy.DeploymentPlatform
import com.example.testdeploy.TestAndDeploymentRunner
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import androidx.test.core.app.ApplicationProvider
import android.content.Context
import com.example.collab.CodeReference
import com.example.data.LanternDiskStorage
import com.example.server.WebCodeStudioHtml
import com.example.ui.components.TreeNode
import com.example.ui.components.buildProjectTree
import kotlinx.coroutines.flow.first

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleUnitTest {

  @Test
  fun testWebRtcRoomCreation() {
    val manager = WebRtcCollaborationManager()
    val room = manager.createRoom("Mesh-Test-Room", "Host Dev")
    assertNotNull(room.roomId)
    assertTrue(room.inviteLink.startsWith("lantern://join?room="))
    assertTrue(room.peers.isNotEmpty())
    assertTrue(room.peers.any { it.isAi })
  }

  @Test
  fun testProjectAuditorDetectsHardcodedSecret() {
    val files = listOf(
      ProjectFileEntity(
        projectId = 1,
        path = "src/Api.kt",
        name = "Api.kt",
        content = "val token = \"sk-live-98234729384\"\nval safeVar = 42",
        language = "kotlin"
      )
    )
    val result = ProjectAuditor.auditFiles(1, files)
    assertTrue(result.issues.any { it.severity == AuditSeverity.CRITICAL })
    assertTrue(result.healthScore < 100)
  }

  @Test
  fun testGitDiffComputation() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = LanternDatabase.getDatabase(context)
    val repo = WorkspaceRepository(db)
    val gitManager = GitManager(repo)

    val oldCode = "line 1\nline 2"
    val newCode = "line 1\nline 2 modified\nline 3"
    val diff = gitManager.computeDiff(oldCode, newCode)
    assertTrue(diff.isNotEmpty())
    assertTrue(diff.any { it.type == DiffLineType.ADDED })
  }

  @Test
  fun testGitHubManagerTokenAuthentication() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = LanternDatabase.getDatabase(context)
    val repo = WorkspaceRepository(db)
    val gitManager = GitManager(repo)

    val result = gitManager.connectWithGitHubToken("ghp_test_developer_token_12345")
    assertTrue(result.isSuccess)
    val profile = result.getOrThrow()
    assertTrue(profile.isAuthenticated)
    assertNotNull(profile.login)
  }

  @Test
  fun testGitHubRepoImportToWorkspace() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = LanternDatabase.getDatabase(context)
    val repo = WorkspaceRepository(db)
    val gitManager = GitManager(repo)

    val dummyRepo = GitHubRemoteRepo(
      id = 999,
      name = "mobile-crypto-mesh",
      fullName = "dev/mobile-crypto-mesh",
      description = "Crypto mesh relay",
      isPrivate = true,
      htmlUrl = "https://github.com/dev/mobile-crypto-mesh",
      cloneUrl = "https://github.com/dev/mobile-crypto-mesh.git"
    )

    val projId = gitManager.importRepoToWorkspace(dummyRepo)
    assertTrue(projId > 0)
  }

  @Test
  fun testDeploymentPackageGeneration() {
    val pkg = TestAndDeploymentRunner.generateDeployment(DeploymentPlatform.ANDROID_APK, "MeshStudio")
    assertEquals(DeploymentPlatform.ANDROID_APK, pkg.platform)
    assertTrue(pkg.artifactName.endsWith(".apk"))
    assertTrue(pkg.configurationScript.contains("applicationId"))
  }

  @Test
  fun testAutomatedTestRunner() = runBlocking {
    val files = listOf(
      ProjectFileEntity(
        projectId = 1,
        path = "src/Mesh.kt",
        name = "Mesh.kt",
        content = "class MeshPeer {}",
        language = "kotlin"
      )
    )
    val summary = TestAndDeploymentRunner.runTests(files)
    assertTrue(summary.totalTests > 0)
    assertTrue(summary.passedCount > 0)
  }

  @Test
  fun testMarkdownToStyledHtml() {
    val md = """# Project Title
## Subheading
- [x] Finished task
- [ ] Todo item
`val x = 10`
```javascript
console.log("hello");
```
| Key | Value |
| --- | --- |
| theme | neon |
"""
    val html = com.example.ui.screens.convertMarkdownToStyledHtml("README.md", md)
    assertTrue(html.contains("<h1>Project Title</h1>"))
    assertTrue(html.contains("<h2>Subheading</h2>"))
    assertTrue(html.contains("class=\"task-item checked\""))
    assertTrue(html.contains("<pre><code class=\"language-javascript\">"))
    assertTrue(html.contains("<table>"))
  }

  @Test
  fun testMultiLanguageProjectTemplateCreation() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = LanternDatabase.getDatabase(context)
    val repo = WorkspaceRepository(db)

    val webProjId = repo.createProject("MyWebApp", "HTML5 web app", "", com.example.data.ProjectTemplate.WEB_HTML_JS)
    assertTrue(webProjId > 0)

    val pyProjId = repo.createProject("DataScript", "Python script", "", com.example.data.ProjectTemplate.PYTHON)
    assertTrue(pyProjId > 0)
  }

  @Test
  fun testCodeSyntaxTransformationSafety() {
    val transformer = com.example.ui.components.CodeSyntaxVisualTransformation("python")
    val res1 = transformer.filter(androidx.compose.ui.text.AnnotatedString(""))
    assertEquals("", res1.text.text)

    val res2 = transformer.filter(androidx.compose.ui.text.AnnotatedString("def run(x):\n    # comment\n    return 'val' + 42\n"))
    assertEquals("def run(x):\n    # comment\n    return 'val' + 42\n", res2.text.text)

    val htmlTransformer = com.example.ui.components.CodeSyntaxVisualTransformation("html")
    val res3 = htmlTransformer.filter(androidx.compose.ui.text.AnnotatedString("<!DOCTYPE html><html><body class='card'>Hello</body></html>"))
    assertEquals("<!DOCTYPE html><html><body class='card'>Hello</body></html>", res3.text.text)
  }

  @Test
  fun testTreeViewFileExplorerHierarchyBuilding() {
    val files = listOf(
      ProjectFileEntity(id = 1, projectId = 10, path = "src/main/App.kt", name = "App.kt", content = "", language = "kotlin"),
      ProjectFileEntity(id = 2, projectId = 10, path = "src/utils/Helper.kt", name = "Helper.kt", content = "", language = "kotlin"),
      ProjectFileEntity(id = 3, projectId = 10, path = "README.md", name = "README.md", content = "", language = "markdown"),
      ProjectFileEntity(id = 4, projectId = 10, path = "package.json", name = "package.json", content = "", language = "json")
    )

    val tree = buildProjectTree(files, filterQuery = "")
    // Should have 1 folder ("src") and 2 root files ("package.json", "README.md")
    assertEquals(3, tree.size)
    val srcFolder = tree.filterIsInstance<TreeNode.FolderNode>().firstOrNull { it.name == "src" }
    assertNotNull(srcFolder)
    assertEquals(2, srcFolder!!.children.size)

    val mainFolder = srcFolder.children.filterIsInstance<TreeNode.FolderNode>().firstOrNull { it.name == "main" }
    assertNotNull(mainFolder)
    assertEquals(1, mainFolder!!.children.size)
    assertEquals("App.kt", mainFolder.children[0].name)

    // Test filtered search query
    val filtered = buildProjectTree(files, filterQuery = "Helper")
    val filteredFolder = filtered.filterIsInstance<TreeNode.FolderNode>().firstOrNull { it.name == "src" }
    assertNotNull(filteredFolder)
    val utilsFolder = filteredFolder!!.children.filterIsInstance<TreeNode.FolderNode>().firstOrNull { it.name == "utils" }
    assertNotNull(utilsFolder)
    assertEquals("Helper.kt", utilsFolder!!.children[0].name)
  }

  @Test
  fun testProjectFileDeletionAndCreation() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = LanternDatabase.getDatabase(context)
    val repo = WorkspaceRepository(db)

    val projId = repo.createProject("TestFileOpsProject", "File ops", "", com.example.data.ProjectTemplate.BLANK)
    val fileId = repo.addFile(projId, "src/models/User.kt", "User.kt", "data class User(val id: String)", "kotlin")
    assertTrue(fileId > 0)

    val initialFiles = repo.getFilesForProject(projId).first()
    assertTrue(initialFiles.any { it.name == "User.kt" })

    // Delete file
    repo.deleteFile(fileId)
    val updatedFiles = repo.getFilesForProject(projId).first()
    assertTrue(updatedFiles.none { it.id == fileId })
  }

  @Test
  fun testGitDashboardOperationsAndCommits() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = LanternDatabase.getDatabase(context)
    val repo = WorkspaceRepository(db)

    val projId = repo.createProject("GitTestProject", "Git dashboard testing", "", com.example.data.ProjectTemplate.KOTLIN_ANDROID)
    val initialCommits = repo.getCommitsForProject(projId).first()
    assertTrue(initialCommits.isNotEmpty())
    assertTrue(initialCommits.first().message.startsWith("Initial commit"))

    // Switch branch / update project
    val project = db.projectDao().getProjectById(projId)
    assertNotNull(project)
    db.projectDao().updateProject(project!!.copy(currentBranch = "feature/dashboard"))

    val updatedProject = db.projectDao().getProjectById(projId)
    assertEquals("feature/dashboard", updatedProject?.currentBranch)

    // Create a new git commit
    repo.addCommit(projId, "feat: Add tree-view explorer and git dashboard", "Lantern Dev", "feature/dashboard")

    val finalCommits = repo.getCommitsForProject(projId).first()
    assertTrue(finalCommits.size >= 2)
    assertEquals("feat: Add tree-view explorer and git dashboard", finalCommits.first().message)
    assertEquals("feature/dashboard", finalCommits.first().branch)
  }

  @Test
  fun testLanternDiskStorageAndDiffBlame() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = LanternDatabase.getDatabase(context)
    val repo = WorkspaceRepository(db)

    val projId = repo.createProject("CryptoMeshNode", "WebRTC crypto relay", "", com.example.data.ProjectTemplate.BLANK)
    val fileId = repo.addFile(projId, "index.js", "index.js", "const express = require('express');\nconst app = express();\n", "javascript")

    val project = db.projectDao().getProjectById(projId)
    val file = db.projectFileDao().getFileById(fileId)
    assertNotNull(project)
    assertNotNull(file)

    // Save project files organized in home/LANtern/projects/{project-name}
    val allFiles = repo.getFilesForProject(projId).first()
    val projectDir = LanternDiskStorage.saveProjectToDisk(context, project!!, allFiles)
    assertTrue(projectDir.exists())
    assertTrue(java.io.File(projectDir, "index.js").exists())

    // Record diff and blame in databases and code in home/LANtern/.diffs/{diff-id}/
    val updatedCode = "const express = require('express');\nconst app = express();\nconst router = express.Router();\nmodule.exports = router;\n"
    val diff = LanternDiskStorage.recordDiffAndBlame(
      context = context,
      db = db,
      project = project,
      file = file!!.copy(content = updatedCode),
      oldContent = file.content,
      newContent = updatedCode,
      commitHash = "c78ab91",
      author = "Dev Alice"
    )

    assertNotNull(diff)
    assertTrue(diff.linesAdded > 0)
    assertTrue(diff.rawPatch.contains("router"))
    assertTrue(java.io.File(diff.diskPath).exists())
    assertTrue(java.io.File(diff.diskPath, "diff.patch").exists())

    // Check DB blame records
    val blameRecords = repo.getBlameForFile(fileId).first()
    assertTrue(blameRecords.isNotEmpty())
    assertEquals(4, blameRecords.size)
    assertEquals("Dev Alice", blameRecords.first().author)
  }

  @Test
  fun testWebRtcPairingCodeAndCodeReferencingChat() {
    val manager = WebRtcCollaborationManager()
    val room = manager.createRoom("Global-Dev-Mesh", "Host Dev")
    assertNotNull(room.pairingCode)
    assertTrue(room.pairingCode.isNotEmpty())

    // Connect with pairing code
    val joinedRoom = manager.connectWithPairingCode(room.pairingCode, "Collaborator Bob")
    assertEquals(room.pairingCode, joinedRoom.pairingCode)

    // Send chat with code reference: team member a references index.js line 46
    val codeRef = CodeReference(
      filePath = "index.js",
      fileName = "index.js",
      lineNumber = 46,
      lineSnippet = "const router = express.Router();"
    )
    manager.sendChatMessage(
      text = "there's need to add this and that at line 46",
      senderName = "Dev Alice",
      codeReference = codeRef
    )

    val currentRoom = manager.roomState.value
    assertNotNull(currentRoom)
    val lastMsg = currentRoom!!.messages.find { it.codeReference != null }
    assertNotNull(lastMsg)
    assertEquals("index.js", lastMsg!!.codeReference?.fileName)
    assertEquals(46, lastMsg.codeReference?.lineNumber)
    assertEquals("const router = express.Router();", lastMsg.codeReference?.lineSnippet)
  }

  @Test
  fun testWebCodeStudioHtmlRendering() {
    val html = WebCodeStudioHtml.getHtml(
      projectName = "LANternNode",
      branch = "main",
      pairingCode = "LANT-7731",
      hostIp = "192.168.1.108",
      port = 8080
    )
    assertTrue(html.contains("LANtern Code Studio"))
    assertTrue(html.contains("LANT-7731"))
    assertTrue(html.contains("openFile"))
    assertTrue(html.contains("code-editor"))
    assertTrue(html.contains("attachCurrentLineReference"))
  }
}

