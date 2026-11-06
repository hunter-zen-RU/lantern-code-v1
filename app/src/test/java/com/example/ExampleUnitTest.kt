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
}
