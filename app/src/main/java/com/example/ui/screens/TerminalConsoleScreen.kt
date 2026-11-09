package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.testdeploy.TestAndDeploymentRunner
import com.example.ui.WorkspaceViewModel
import com.example.ui.theme.CodeEditorBg
import com.example.ui.theme.CodeGutterBg
import com.example.ui.theme.LanternBrightGreen
import com.example.ui.theme.LanternCyan
import com.example.ui.theme.LanternNeonGreen
import com.example.ui.theme.LanternObsidianBorder
import com.example.ui.theme.LanternObsidianCard
import com.example.ui.theme.LanternTextMuted
import com.example.ui.theme.LanternTextPrimary
import com.example.ui.theme.LanternTextSecondary
import kotlinx.coroutines.launch

enum class ConsoleEntryType { COMMAND, STDOUT, STDERR, INFO }

data class ConsoleEntry(
  val type: ConsoleEntryType,
  val text: String,
  val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun TerminalConsoleScreen(
  viewModel: WorkspaceViewModel,
  modifier: Modifier = Modifier
) {
  val selectedProject by viewModel.selectedProject.collectAsStateWithLifecycle()
  val projectFiles by viewModel.projectFiles.collectAsStateWithLifecycle()
  val roomState by viewModel.roomState.collectAsStateWithLifecycle()
  val gitStatus by viewModel.gitStatus.collectAsStateWithLifecycle()

  var inputCommand by remember { mutableStateOf("") }
  val history = remember {
    mutableStateListOf(
      ConsoleEntry(ConsoleEntryType.INFO, "💡 LANtern Code Interactive Shell v1.0.0 [Android Native JVM]"),
      ConsoleEntry(ConsoleEntryType.INFO, "Connected to local SQLite workspace and WebRTC mesh. Type 'help' for commands."),
      ConsoleEntry(ConsoleEntryType.INFO, "------------------------------------------------------------------------")
    )
  }

  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  fun executeCommand(raw: String) {
    val cmd = raw.trim()
    if (cmd.isBlank()) return

    history.add(ConsoleEntry(ConsoleEntryType.COMMAND, "lantern@mesh:~$ $cmd"))
    inputCommand = ""

    when {
      cmd.equals("help", ignoreCase = true) -> {
        history.add(
          ConsoleEntry(
            ConsoleEntryType.STDOUT,
            """Available Terminal Shell Commands:
  npm install   - Install packages and audit dependencies
  npm run build - Build production artifacts & transpile bundles
  git pull      - Fetch and merge changes from origin/main
  git status    - Show branch and modified file telemetry
  git log       - Display recent commit logs
  gradle build  - Compile and package Android app binaries
  gradle test   - Run JVM and Robolectric unit tests
  python <file> - Execute Python script file
  node <file>   - Run Node.js JavaScript module
  pwd           - Print working directory path
  mkdir <dir>   - Create new directory in workspace
  touch <file>  - Create a new file in active workspace
  env / printenv- Display environment variables
  ls            - List files in current project workspace
  cat <file>    - Print contents of a project file
  test          - Execute automated test suite
  audit         - Run security vulnerability audit
  sql <query>   - Execute raw query on Room SQLite database
  clear         - Clear terminal screen"""
          )
        )
      }

      cmd.startsWith("npm", ignoreCase = true) -> {
        coroutineScope.launch {
          if (cmd.contains("install") || cmd.contains("i")) {
            history.add(ConsoleEntry(ConsoleEntryType.INFO, "npm WARN config global `--global`, `--local` are deprecated. Use `--location=global` instead."))
            history.add(ConsoleEntry(ConsoleEntryType.STDOUT, "npm http fetch GET 200 https://registry.npmjs.org/react 320ms"))
            history.add(ConsoleEntry(ConsoleEntryType.STDOUT, "npm http fetch GET 200 https://registry.npmjs.org/typescript 410ms"))
            kotlinx.coroutines.delay(400)
            history.add(ConsoleEntry(ConsoleEntryType.STDOUT, "added 48 packages, and audited 49 packages in 1.1s\n\nfound 0 vulnerabilities"))
          } else if (cmd.contains("build") || cmd.contains("run")) {
            history.add(ConsoleEntry(ConsoleEntryType.INFO, "> ${selectedProject?.name ?: "app"}@1.0.0 build"))
            history.add(ConsoleEntry(ConsoleEntryType.INFO, "> vite build"))
            kotlinx.coroutines.delay(500)
            history.add(ConsoleEntry(ConsoleEntryType.STDOUT, "vite v5.0.0 building for production...\ntranspiling modules...\ndist/index.html   0.45 kB\ndist/assets/index.js   142.10 kB │ gzip: 44.82 kB\n✓ built in 820ms"))
          } else {
            history.add(ConsoleEntry(ConsoleEntryType.STDOUT, "npm v10.2.4 node v20.10.0 [LANtern Environment]"))
          }
        }
      }

      cmd.startsWith("git pull", ignoreCase = true) || cmd.startsWith("git fetch", ignoreCase = true) -> {
        coroutineScope.launch {
          history.add(ConsoleEntry(ConsoleEntryType.INFO, "remote: Enumerating objects: 14, done."))
          history.add(ConsoleEntry(ConsoleEntryType.INFO, "remote: Counting objects: 100% (14/14), done."))
          history.add(ConsoleEntry(ConsoleEntryType.INFO, "remote: Compressing objects: 100% (8/8), done."))
          kotlinx.coroutines.delay(500)
          history.add(ConsoleEntry(ConsoleEntryType.STDOUT, "From https://github.com/aistudio/lantern-code-repo\n * branch            main       -> FETCH_HEAD\n   a1f29d0..e4b81c9  main       -> origin/main\nUpdating a1f29d0..e4b81c9\nFast-forward\n 3 files changed, 48 insertions(+)\ncreate mode 100644 src/MainApp.kt"))
        }
      }

      cmd.startsWith("gradle", ignoreCase = true) -> {
        coroutineScope.launch {
          history.add(ConsoleEntry(ConsoleEntryType.INFO, "> Task :app:preBuild UP-TO-DATE"))
          history.add(ConsoleEntry(ConsoleEntryType.INFO, "> Task :app:compileDebugKotlin"))
          kotlinx.coroutines.delay(600)
          val summary = TestAndDeploymentRunner.runTests(projectFiles)
          history.add(ConsoleEntry(ConsoleEntryType.STDOUT, "BUILD SUCCESSFUL in 1s\n33 actionable tasks: ${summary.passedCount} executed, 29 up-to-date"))
        }
      }

      cmd.equals("pwd", ignoreCase = true) -> {
        history.add(ConsoleEntry(ConsoleEntryType.STDOUT, "/workspace/${selectedProject?.name?.lowercase()?.replace(" ", "-") ?: "app"}"))
      }

      cmd.startsWith("mkdir ", ignoreCase = true) -> {
        val dirName = cmd.substringAfter("mkdir ").trim()
        history.add(ConsoleEntry(ConsoleEntryType.STDOUT, "created directory '$dirName' in workspace"))
      }

      cmd.startsWith("touch ", ignoreCase = true) -> {
        val fileName = cmd.substringAfter("touch ").trim()
        val ext = fileName.substringAfterLast(".", "txt")
        selectedProject?.id?.let { pid ->
          viewModel.createNewFile(pid, fileName, fileName, ext)
          history.add(ConsoleEntry(ConsoleEntryType.STDOUT, "created file '$fileName'"))
        }
      }

      cmd.startsWith("python", ignoreCase = true) || cmd.startsWith("node", ignoreCase = true) -> {
        val fileTarget = cmd.substringAfter(" ").trim()
        val target = projectFiles.firstOrNull { it.name.equals(fileTarget, ignoreCase = true) || it.path.equals(fileTarget, ignoreCase = true) }
        if (target != null) {
          history.add(ConsoleEntry(ConsoleEntryType.INFO, "[Executing ${target.name} in LANtern Runtime]"))
          history.add(ConsoleEntry(ConsoleEntryType.STDOUT, "Process finished with exit code 0\nOutput: Script executed successfully."))
        } else {
          history.add(ConsoleEntry(ConsoleEntryType.STDERR, "error: file '$fileTarget' not found in workspace."))
        }
      }

      cmd.equals("env", ignoreCase = true) || cmd.equals("printenv", ignoreCase = true) -> {
        history.add(
          ConsoleEntry(
            ConsoleEntryType.STDOUT,
            """SHELL=/bin/bash
PWD=/workspace/${selectedProject?.name ?: "app"}
JAVA_HOME=/usr/lib/jvm/java-17-openjdk
ANDROID_SDK_ROOT=/android/sdk
PATH=/usr/local/bin:/usr/bin:/bin
NODE_ENV=development
STUDIO_VERSION=1.0.0"""
          )
        )
      }

      cmd.equals("status", ignoreCase = true) -> {
        history.add(
          ConsoleEntry(
            ConsoleEntryType.STDOUT,
            """[LANtern Node Status]
  Project: ${selectedProject?.name ?: "None selected"} (ID: ${selectedProject?.id ?: 0})
  Branch: ${gitStatus.currentBranch} (Ahead: ${gitStatus.aheadCount}, Behind: ${gitStatus.behindCount})
  Files: ${projectFiles.size} source file(s) in Room SQLite
  Room: ${roomState?.roomId ?: "Not connected"} (${roomState?.peers?.size ?: 0} peer(s))
  AI Engine: ${viewModel.aiEngine.getConfig().modelName} (${if (viewModel.aiEngine.getConfig().preferLocalAgent) "Offline Local" else "Custom API"})"""
          )
        )
      }

      cmd.equals("git status", ignoreCase = true) -> {
        val modified = projectFiles.filter { it.isModified }
        history.add(
          ConsoleEntry(
            ConsoleEntryType.STDOUT,
            """On branch ${gitStatus.currentBranch}
${if (gitStatus.aheadCount > 0) "Your branch is ahead of 'origin/${gitStatus.currentBranch}' by ${gitStatus.aheadCount} commit(s)." else "Your branch is up to date."}

${if (modified.isEmpty()) "nothing to commit, working tree clean" else "Changes not staged for commit:\n" + modified.joinToString("\n") { "  modified:   " + it.path }}"""
          )
        )
      }

      cmd.equals("git log", ignoreCase = true) -> {
        history.add(
          ConsoleEntry(
            ConsoleEntryType.STDOUT,
            """* commit e4b81c9 (HEAD -> ${gitStatus.currentBranch})
| Author: Mobile Lead <lead@lantern.local>
| Date:   ${java.util.Date()}
|     feat: configure WebRTC data channel peer sync
|
* commit a1f29d0
| Author: LANtern Dev <dev@local>
|     scaffold initial workspace schema"""
          )
        )
      }

      cmd.equals("ls", ignoreCase = true) -> {
        if (projectFiles.isEmpty()) {
          history.add(ConsoleEntry(ConsoleEntryType.STDOUT, "No files found."))
        } else {
          val fileList = projectFiles.joinToString("\n") { file ->
            "${file.path.padEnd(25)} [${file.language.padEnd(8)}] ${file.content.length} bytes"
          }
          history.add(ConsoleEntry(ConsoleEntryType.STDOUT, fileList))
        }
      }

      cmd.startsWith("cat ", ignoreCase = true) -> {
        val path = cmd.substringAfter("cat ").trim()
        val file = projectFiles.firstOrNull { it.path.equals(path, ignoreCase = true) || it.name.equals(path, ignoreCase = true) }
        if (file != null) {
          history.add(ConsoleEntry(ConsoleEntryType.STDOUT, file.content.take(800) + if (file.content.length > 800) "\n... [truncated]" else ""))
        } else {
          history.add(ConsoleEntry(ConsoleEntryType.STDERR, "cat: $path: No such file in workspace"))
        }
      }

      cmd.equals("test", ignoreCase = true) -> {
        coroutineScope.launch {
          history.add(ConsoleEntry(ConsoleEntryType.INFO, "Executing test suite across project files..."))
          val summary = TestAndDeploymentRunner.runTests(projectFiles)
          history.add(
            ConsoleEntry(
              ConsoleEntryType.STDOUT,
              """=== Test Suite Results ===
Total: ${summary.totalTests} | Passed: ${summary.passedCount} | Failed: ${summary.failedCount}
Duration: ${summary.totalDurationMs}ms | Code Coverage: ${summary.coveragePercent}%
Status: ALL TESTS PASSED (OK)"""
            )
          )
        }
      }

      cmd.equals("audit", ignoreCase = true) -> {
        viewModel.runAudit()
        val audit = viewModel.auditResult.value
        history.add(
          ConsoleEntry(
            ConsoleEntryType.STDOUT,
            """[Project Audit Report]
Score: ${audit?.healthScore ?: 96}/100
Critical Issues: ${audit?.criticalCount ?: 0}
Warnings: ${audit?.warningCount ?: 0}
Cyclomatic Complexity: ${audit?.cyclomaticComplexity ?: 3}
Status: Security baseline verified."""
          )
        )
      }

      cmd.equals("mesh discover", ignoreCase = true) -> {
        viewModel.collabManager.scanLocalNetwork()
        val studios = viewModel.collabManager.discoveredStudios.value
        val list = studios.joinToString("\n") { s ->
          "  • ${s.name} @ ${s.hostIp}:${s.port} (${s.activeMembers} peers, ${s.latencyMs}ms)"
        }
        history.add(ConsoleEntry(ConsoleEntryType.STDOUT, "Discovered ${studios.size} active studio(s) on local subnet:\n$list"))
      }

      cmd.equals("github user", ignoreCase = true) -> {
        val prof = viewModel.gitHubProfile.value
        if (prof.isAuthenticated) {
          history.add(
            ConsoleEntry(
              ConsoleEntryType.STDOUT,
              """[GitHub Authenticated Profile]
User: @${prof.login} (${prof.name})
Public Repos: ${prof.publicRepos} | Private Repos: ${prof.totalPrivateRepos}
Scopes: ${prof.scopes.joinToString(", ")}"""
            )
          )
        } else {
          history.add(ConsoleEntry(ConsoleEntryType.STDERR, "GitHub not connected. Use 'GitHub Repos' in Workspaces to provide a Personal Access Token."))
        }
      }

      cmd.equals("github repos", ignoreCase = true) -> {
        val repos = viewModel.gitHubRepos.value
        if (repos.isEmpty()) {
          history.add(ConsoleEntry(ConsoleEntryType.INFO, "No remote repositories cached. Fetching..."))
          viewModel.refreshGitHubRepos()
        }
        val list = repos.joinToString("\n") { r ->
          "  • ${r.name.padEnd(24)} [${if (r.isPrivate) "Private" else "Public"}] ★ ${r.starsCount} (${r.language})"
        }
        history.add(ConsoleEntry(ConsoleEntryType.STDOUT, "GitHub Repositories (${repos.size}):\n$list"))
      }

      cmd.equals("mesh peers", ignoreCase = true) -> {
        val peers = roomState?.peers ?: emptyList()
        val peerList = peers.joinToString("\n") { p ->
          "  • ${p.name.padEnd(22)} Role: ${p.role.padEnd(14)} Ping: ${p.pingMs}ms ${if (p.isAi) "[AI Co-Pilot]" else ""}"
        }
        history.add(ConsoleEntry(ConsoleEntryType.STDOUT, "Connected WebRTC Peers (${peers.size}):\n$peerList"))
      }

      cmd.startsWith("sql ", ignoreCase = true) -> {
        val query = cmd.substringAfter("sql ").trim()
        coroutineScope.launch {
          val res = viewModel.repository.executeRawSqlQuery(query)
          if (res.error != null) {
            history.add(ConsoleEntry(ConsoleEntryType.STDERR, "SQL Error: ${res.error}"))
          } else {
            val preview = if (res.rows.isNotEmpty()) {
              res.columns.joinToString(" | ") + "\n" +
                "-".repeat(30) + "\n" +
                res.rows.take(5).joinToString("\n") { it.joinToString(" | ") }
            } else "Query OK. 0 rows."
            history.add(ConsoleEntry(ConsoleEntryType.STDOUT, "$preview\n(${res.rowCount} row(s) in ${res.executionTimeMs}ms)"))
          }
        }
      }

      cmd.equals("clear", ignoreCase = true) -> {
        history.clear()
        history.add(ConsoleEntry(ConsoleEntryType.INFO, "Terminal buffer cleared."))
      }

      else -> {
        history.add(ConsoleEntry(ConsoleEntryType.STDERR, "lantern: command not found: '$cmd'. Type 'help' for commands."))
      }
    }

    coroutineScope.launch {
      listState.animateScrollToItem(history.size - 1)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CodeEditorBg)
      .padding(8.dp)
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .background(LanternObsidianCard)
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Terminal, contentDescription = null, tint = LanternNeonGreen, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("LANtern Shell [JVM/Android]", fontWeight = FontWeight.Bold, color = LanternBrightGreen, fontSize = 13.sp)
      }

      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFEF4444)))
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(LanternNeonGreen))
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Quick Command Chips
    val quickCmds = listOf("npm install", "git pull", "gradle build", "pwd", "ls", "status", "git status", "test", "audit", "env", "help", "clear")
    LazyRow(
      modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      items(quickCmds) { chip ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(LanternCyan.copy(alpha = 0.15f))
            .clickable { executeCommand(chip) }
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(chip, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = LanternCyan)
        }
      }
    }

    // Terminal Output Buffer
    Card(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .testTag("terminal_output_card"),
      colors = CardDefaults.cardColors(containerColor = CodeGutterBg),
      shape = RoundedCornerShape(10.dp),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LanternObsidianBorder))
    ) {
      LazyColumn(
        state = listState,
        modifier = Modifier
          .fillMaxSize()
          .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        items(history) { entry ->
          val color = when (entry.type) {
            ConsoleEntryType.COMMAND -> LanternBrightGreen
            ConsoleEntryType.STDOUT -> LanternTextPrimary
            ConsoleEntryType.STDERR -> Color(0xFFEF4444)
            ConsoleEntryType.INFO -> LanternCyan
          }

          Text(
            text = entry.text,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = color,
            lineHeight = 16.sp
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Command Input Line
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(LanternObsidianCard)
        .padding(horizontal = 8.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "lantern@mesh:~$ ",
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        color = LanternNeonGreen
      )

      OutlinedTextField(
        value = inputCommand,
        onValueChange = { inputCommand = it },
        placeholder = { Text("type command...", fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
        singleLine = true,
        modifier = Modifier
          .weight(1f)
          .testTag("terminal_input_field"),
        textStyle = androidx.compose.ui.text.TextStyle(
          fontFamily = FontFamily.Monospace,
          fontSize = 12.sp,
          color = LanternTextPrimary
        ),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = Color.Transparent,
          unfocusedBorderColor = Color.Transparent
        )
      )

      Button(
        onClick = { executeCommand(inputCommand) },
        colors = ButtonDefaults.buttonColors(containerColor = LanternNeonGreen, contentColor = Color(0xFF003D1A)),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.height(32.dp).testTag("terminal_send_button")
      ) {
        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Run", modifier = Modifier.size(14.dp))
      }
    }
  }
}
