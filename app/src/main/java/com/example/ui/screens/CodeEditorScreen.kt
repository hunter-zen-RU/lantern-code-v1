@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.AuditSeverity
import com.example.ai.ProjectAuditor
import com.example.data.entity.ProjectFileEntity
import com.example.ui.WorkspaceViewModel
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import com.example.ui.components.AiCompletionOverlay
import com.example.ui.components.BreadcrumbBar
import com.example.ui.components.CodeSyntaxVisualTransformation
import com.example.ui.components.CommandAction
import com.example.ui.components.CommandPaletteModal
import com.example.ui.components.EditorMinimap
import com.example.ui.components.GeminiAutocompleter
import com.example.ui.components.TreeViewFileExplorer
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

@Composable
fun CodeEditorScreen(
  viewModel: WorkspaceViewModel,
  onNavigateToAudit: () -> Unit,
  onNavigateToCollab: () -> Unit,
  onNavigateToGit: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val selectedProject by viewModel.selectedProject.collectAsStateWithLifecycle()
  val files by viewModel.projectFiles.collectAsStateWithLifecycle()
  val activeFile by viewModel.activeFile.collectAsStateWithLifecycle()
  val editorContent by viewModel.editorContent.collectAsStateWithLifecycle()
  val openTabs by viewModel.openTabs.collectAsStateWithLifecycle()
  val roomState by viewModel.roomState.collectAsStateWithLifecycle()
  val aiResult by viewModel.aiResult.collectAsStateWithLifecycle()
  val isAiWorking by viewModel.isAiWorking.collectAsStateWithLifecycle()
  val targetScrollLine by viewModel.targetScrollLine.collectAsStateWithLifecycle()

  var isFileTreeOpen by remember { mutableStateOf(false) }
  var isAiDrawerOpen by remember { mutableStateOf(false) }
  var isPreviewTabActive by remember { mutableStateOf(false) }
  var aiPromptInput by remember { mutableStateOf("") }
  var showNewFileDialog by remember { mutableStateOf(false) }
  var showCommitDialog by remember { mutableStateOf(false) }
  var showDiffDialog by remember { mutableStateOf(false) }
  var showPreviewDialog by remember { mutableStateOf(false) }
  var showSnippetsDialog by remember { mutableStateOf(false) }
  var showFuzzySearchDialog by remember { mutableStateOf(false) }
  var selectedSymbolSet by remember { mutableStateOf("General") }
  var customSymbols by remember { mutableStateOf(listOf<String>()) }
  var showAddSymbolDialog by remember { mutableStateOf(false) }
  var showCommandPalette by remember { mutableStateOf(false) }
  var isMinimapVisible by remember { mutableStateOf(true) }
  var showGoToLineDialog by remember { mutableStateOf(false) }
  var activeLineNumber by remember { mutableIntStateOf(1) }

  val coroutineScope = rememberCoroutineScope()
  var aiSuggestion by remember { mutableStateOf<String?>(null) }
  var aiSource by remember { mutableStateOf("") }
  var isAiLoading by remember { mutableStateOf(false) }

  val isPreviewable = activeFile?.let {
    it.name.endsWith(".html", ignoreCase = true) ||
      it.name.endsWith(".htm", ignoreCase = true) ||
      it.name.endsWith(".md", ignoreCase = true) ||
      it.name.endsWith(".markdown", ignoreCase = true) ||
      it.name.endsWith(".css", ignoreCase = true) ||
      it.name.endsWith(".js", ignoreCase = true)
  } ?: false

  val isImeVisible = WindowInsets.isImeVisible

  Column(
    modifier = modifier
      .fillMaxSize()
      .onKeyEvent { event ->
        if (event.isCtrlPressed && event.isShiftPressed && event.key == Key.P) {
          showCommandPalette = true
          true
        } else false
      }
      .background(CodeEditorBg)
  ) {
    if (isImeVisible) {
      // EDIT MODE ENGINE TOP BAR (Keyboard active: No status bar, no header clutter, showing open files tabs)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(LanternObsidianCard)
          .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = { isFileTreeOpen = !isFileTreeOpen },
          modifier = Modifier.size(32.dp).testTag("toggle_file_tree_button_edit_mode")
        ) {
          Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Toggle Files",
            tint = LanternNeonGreen,
            modifier = Modifier.size(18.dp)
          )
        }

        if (openTabs.isNotEmpty()) {
          val rawIndex = openTabs.indexOfFirst { it.id == activeFile?.id }
          val safeIndex = if (rawIndex >= 0) rawIndex.coerceIn(0, openTabs.size - 1) else 0
          androidx.compose.runtime.key(openTabs.size, activeFile?.id) {
            ScrollableTabRow(
              selectedTabIndex = safeIndex,
              containerColor = Color.Transparent,
              contentColor = LanternNeonGreen,
              edgePadding = 4.dp,
              modifier = Modifier.weight(1f)
            ) {
              openTabs.forEach { tabFile ->
                val isSelected = tabFile.id == activeFile?.id
                Tab(
                  selected = isSelected,
                  onClick = { viewModel.openFile(tabFile) },
                  text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      if (tabFile.isModified) {
                        Box(
                          modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(LanternNeonGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                      }
                      Text(
                        text = tabFile.name,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (isSelected) LanternNeonGreen else LanternTextSecondary
                      )
                      Spacer(modifier = Modifier.width(4.dp))
                      Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close tab",
                        modifier = Modifier
                          .size(12.dp)
                          .clickable { viewModel.closeTab(tabFile) },
                        tint = LanternTextMuted
                      )
                    }
                  }
                )
              }
            }
          }
        } else {
          Text(
            text = activeFile?.name ?: "Edit Mode",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = LanternBrightGreen,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
          )
        }

        IconButton(
          onClick = { viewModel.saveCurrentFile() },
          modifier = Modifier.size(32.dp).testTag("save_file_button_edit_mode")
        ) {
          Icon(
            imageVector = Icons.Default.Save,
            contentDescription = "Save File",
            tint = if (activeFile?.isModified == true) LanternNeonGreen else LanternTextMuted,
            modifier = Modifier.size(18.dp)
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(LanternNeonGreen.copy(alpha = 0.2f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = "EDIT",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = LanternNeonGreen
          )
        }
      }
    } else {
      // NORMAL MODE (Full Editor Header Bar)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(LanternObsidianCard)
          .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = { isFileTreeOpen = !isFileTreeOpen },
            modifier = Modifier.testTag("toggle_file_tree_button")
          ) {
            Icon(
              imageVector = Icons.Default.Menu,
              contentDescription = "Toggle Files",
              tint = LanternNeonGreen
            )
          }

          Text(
            text = selectedProject?.name ?: "LANtern Studio",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = LanternBrightGreen
            ),
            maxLines = 1
          )

          Spacer(modifier = Modifier.width(8.dp))

          // Branch pill
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(LanternCyan.copy(alpha = 0.15f))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = selectedProject?.currentBranch ?: "main",
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              color = LanternCyan
            )
          }

          // Inline Code / Preview toggle switch if previewable
          if (isPreviewable) {
            Spacer(modifier = Modifier.width(8.dp))
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(LanternObsidianBorder)
                .padding(2.dp)
            ) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(if (!isPreviewTabActive) LanternNeonGreen else Color.Transparent)
                  .clickable { isPreviewTabActive = false }
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "Code",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (!isPreviewTabActive) Color(0xFF003D1A) else LanternTextSecondary
                )
              }
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(if (isPreviewTabActive) LanternCyan else Color.Transparent)
                  .clickable { isPreviewTabActive = true }
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "Preview",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isPreviewTabActive) Color(0xFF002F48) else LanternTextSecondary
                )
              }
            }
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          // Snippets Button
          IconButton(
            onClick = { showSnippetsDialog = true },
            modifier = Modifier.size(32.dp).testTag("snippets_palette_button")
          ) {
            Icon(
              imageVector = Icons.Default.DataObject,
              contentDescription = "Snippets Palette",
              tint = LanternCyan,
              modifier = Modifier.size(18.dp)
            )
          }

          // Diff Inspector Button
          IconButton(
            onClick = { showDiffDialog = true },
            modifier = Modifier.size(32.dp).testTag("view_git_diff_button")
          ) {
            Icon(
              imageVector = Icons.Default.Difference,
              contentDescription = "Git Diff",
              tint = LanternBrightGreen,
              modifier = Modifier.size(18.dp)
            )
          }

          // Fuzzy Search Quick Open Button
          IconButton(
            onClick = { showFuzzySearchDialog = true },
            modifier = Modifier.size(32.dp).testTag("fuzzy_search_button")
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Quick Open (Fuzzy Search)",
              tint = LanternNeonGreen,
              modifier = Modifier.size(18.dp)
            )
          }

          // Command Palette Button (Ctrl+Shift+P)
          IconButton(
            onClick = { showCommandPalette = true },
            modifier = Modifier.size(32.dp).testTag("command_palette_button")
          ) {
            Icon(
              imageVector = Icons.Default.Terminal,
              contentDescription = "Command Palette (Ctrl+Shift+P)",
              tint = LanternNeonGreen,
              modifier = Modifier.size(18.dp)
            )
          }

          // Live Web Preview Button
          IconButton(
            onClick = { showPreviewDialog = true },
            modifier = Modifier.size(32.dp).testTag("live_preview_button")
          ) {
            Icon(
              imageVector = Icons.Default.Language,
              contentDescription = "Live Preview",
              tint = Color(0xFFA78BFA),
              modifier = Modifier.size(18.dp)
            )
          }

          // Save Button
          IconButton(
            onClick = { viewModel.saveCurrentFile() },
            modifier = Modifier.size(32.dp).testTag("save_file_button")
          ) {
            Icon(
              imageVector = Icons.Default.Save,
              contentDescription = "Save File",
              tint = if (activeFile?.isModified == true) LanternNeonGreen else LanternTextMuted,
              modifier = Modifier.size(18.dp)
            )
          }

          // Git Dashboard Button
          IconButton(
            onClick = onNavigateToGit,
            modifier = Modifier.size(32.dp).testTag("git_dashboard_icon_button")
          ) {
            Icon(
              imageVector = Icons.Default.Difference,
              contentDescription = "Git Dashboard",
              tint = LanternCyan,
              modifier = Modifier.size(18.dp)
            )
          }

          // Git Commit Button
          TextButton(
            onClick = { showCommitDialog = true },
            modifier = Modifier.testTag("git_commit_dialog_button")
          ) {
            Text("Commit", color = LanternCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          // AI Vibecode toggle button
          IconButton(
            onClick = { isAiDrawerOpen = !isAiDrawerOpen },
            modifier = Modifier.size(32.dp).testTag("toggle_ai_drawer_button")
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = "Vibecoding Assistant",
              tint = if (isAiDrawerOpen) LanternNeonGreen else LanternBrightGreen,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      // Tabs bar for open files in Normal Mode
      if (openTabs.isNotEmpty()) {
        val rawIndex = openTabs.indexOfFirst { it.id == activeFile?.id }
        val safeIndex = if (rawIndex >= 0) rawIndex.coerceIn(0, openTabs.size - 1) else 0
        androidx.compose.runtime.key(openTabs.size, activeFile?.id) {
          ScrollableTabRow(
            selectedTabIndex = safeIndex,
            containerColor = LanternObsidianCard.copy(alpha = 0.8f),
            contentColor = LanternNeonGreen,
            edgePadding = 8.dp,
            modifier = Modifier.fillMaxWidth()
          ) {
            openTabs.forEach { tabFile ->
              val isSelected = tabFile.id == activeFile?.id
              Tab(
                selected = isSelected,
                onClick = { viewModel.openFile(tabFile) },
                text = {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    if (tabFile.isModified) {
                      Box(
                        modifier = Modifier
                          .size(6.dp)
                          .clip(CircleShape)
                          .background(LanternNeonGreen)
                      )
                      Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                      text = tabFile.name,
                      fontSize = 12.sp,
                      fontFamily = FontFamily.Monospace,
                      color = if (isSelected) LanternNeonGreen else LanternTextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                      imageVector = Icons.Default.Close,
                      contentDescription = "Close tab",
                      modifier = Modifier
                        .size(14.dp)
                        .clickable { viewModel.closeTab(tabFile) },
                      tint = LanternTextMuted
                    )
                  }
                }
              )
            }
          }
        }
      }

      // Breadcrumb Navigation Bar showing directory structure
      if (activeFile != null) {
        BreadcrumbBar(
          activeFile = activeFile,
          allProjectFiles = files,
          onSelectFile = { selected -> viewModel.openFile(selected) }
        )
      }
    }

    // Tree-View File Explorer Drawer Overlay
    AnimatedVisibility(visible = isFileTreeOpen) {
      TreeViewFileExplorer(
        files = files,
        activeFile = activeFile,
        onOpenFile = { file ->
          viewModel.openFile(file)
          isFileTreeOpen = false
        },
        onCreateFile = { path, name, lang ->
          selectedProject?.id?.let { pid ->
            viewModel.createNewFile(pid, path, name, lang)
          }
        },
        onDeleteFile = { file ->
          viewModel.deleteFile(file)
        },
        onClose = { isFileTreeOpen = false }
      )
    }

    // Live Peer Cursors Indicator Bar
    val peers = roomState?.peers ?: emptyList()
    if (peers.isNotEmpty()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(LanternObsidianCard.copy(alpha = 0.5f))
          .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text("Active Mesh Peers:", fontSize = 10.sp, color = LanternTextMuted)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          items(peers) { peer ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(peer.colorHex).copy(alpha = 0.2f))
                .border(0.5.dp, Color(peer.colorHex), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(Color(peer.colorHex))
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${peer.name} [L${peer.cursorLine}]",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(peer.colorHex)
              )
            }
          }
        }
      }
    }

    // Main Code Editor Area (or Live Preview if active)
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      if (activeFile != null) {
        if (isPreviewTabActive && isPreviewable) {
          // Inline Document / Web Preview
          DocumentWebPreview(
            fileName = activeFile!!.name,
            content = editorContent,
            projectFiles = files,
            modifier = Modifier.fillMaxSize().padding(8.dp)
          )
        } else {
          val verticalScrollState = rememberScrollState()
          val horizontalScrollState = rememberScrollState()

          // Auto-trigger Gemini AI completion as typing occurs (debounced)
          LaunchedEffect(editorContent) {
            val currentFile = activeFile
            if (currentFile != null && editorContent.isNotBlank()) {
              kotlinx.coroutines.delay(900)
              val prefix = editorContent.take((activeLineNumber * 40).coerceAtMost(editorContent.length))
              val suffix = if (prefix.length < editorContent.length) editorContent.substring(prefix.length) else ""

              isAiLoading = true
              val result = GeminiAutocompleter.fetchCompletion(
                prefix = prefix,
                suffix = suffix,
                fileName = currentFile.name,
                language = currentFile.language
              )
              isAiLoading = false
              aiSuggestion = if (result.isSuccess) result.snippet else null
              aiSource = result.source
            }
          }

          LaunchedEffect(activeFile?.id) {
            verticalScrollState.scrollTo(0)
            horizontalScrollState.scrollTo(0)
            activeLineNumber = 1
            aiSuggestion = null
          }

          LaunchedEffect(targetScrollLine) {
            targetScrollLine?.let { targetLine ->
              activeLineNumber = targetLine
              val offset = ((targetLine - 3).coerceAtLeast(0) * 60)
              verticalScrollState.animateScrollTo(offset)
              viewModel.clearTargetScrollLine()
            }
          }
          val lines = editorContent.lines()
          val lineCount = maxOf(1, lines.size)

          val activeFileIssues = remember(activeFile?.content, activeFile?.name) {
            if (activeFile != null) {
              ProjectAuditor.auditFiles(selectedProject?.id ?: 0, listOf(activeFile!!)).issues
            } else emptyList()
          }
          val issueLineMap = remember(activeFileIssues) {
            activeFileIssues.associate { it.lineNumber to it.severity }
          }
          val peerCursorLines = roomState?.peers?.map { it.cursorLine } ?: emptyList()

          Column(modifier = Modifier.fillMaxSize()) {
            Row(
              modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight()
                  .verticalScroll(verticalScrollState)
              ) {
                // Custom Editor Gutter Component with Line Numbers, Audit Badges, and Peer Cursors
                EditorGutter(
                  lineCount = lineCount,
                  activeLine = activeLineNumber,
                  issueLines = issueLineMap,
                  peerCursorLines = peerCursorLines,
                  onLineClick = { clickedLine ->
                    activeLineNumber = clickedLine
                  }
                )

                // Editable Code Text with syntax highlighting transformation
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScrollState)
                    .padding(8.dp)
                ) {
                  BasicTextField(
                    value = editorContent,
                    onValueChange = { viewModel.updateEditorContent(it) },
                    visualTransformation = CodeSyntaxVisualTransformation(
                      language = activeFile?.language ?: "",
                      fileName = activeFile?.name ?: ""
                    ),
                    modifier = Modifier
                      .widthIn(min = 600.dp)
                      .testTag("code_editor_text_field"),
                    textStyle = TextStyle(
                      fontFamily = FontFamily.Monospace,
                      fontSize = 13.sp,
                      color = LanternTextPrimary,
                      lineHeight = 20.sp
                    ),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(LanternNeonGreen)
                  )
                }
              }

              // Vertical File Scroll Mini-Map on the right side of the editor
              if (isMinimapVisible) {
                EditorMinimap(
                  lines = lines,
                  activeLine = activeLineNumber,
                  scrollState = verticalScrollState,
                  issueLines = issueLineMap,
                  onLineClick = { clickedLine ->
                    activeLineNumber = clickedLine
                  }
                )
              }
            }

            // AI Autocompletion Floating Suggestion Overlay
            AiCompletionOverlay(
              suggestion = aiSuggestion,
              source = aiSource,
              isLoading = isAiLoading,
              onAccept = { snippet ->
                val linesListCurrent = editorContent.lines().toMutableList()
                if (activeLineNumber in 1..linesListCurrent.size) {
                  linesListCurrent[activeLineNumber - 1] = linesListCurrent[activeLineNumber - 1] + snippet
                  viewModel.updateEditorContent(linesListCurrent.joinToString("\n"))
                } else {
                  val newContent = editorContent + (if (editorContent.endsWith("\n") || editorContent.isEmpty()) "" else "\n") + snippet
                  viewModel.updateEditorContent(newContent)
                }
                aiSuggestion = null
              },
              onDismiss = { aiSuggestion = null }
            )

            // Editor Status Bar at bottom of code viewport
            val linesList = editorContent.lines()
            val activeLineText = if (activeLineNumber in 1..linesList.size) linesList[activeLineNumber - 1] else ""
            val activeColumnNumber = activeLineText.length + 1

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(LanternObsidianCard)
                .border(width = 1.dp, color = LanternObsidianBorder)
                .padding(horizontal = 10.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "${activeFile?.name ?: ""}  •  ${(activeFile?.language ?: "text").uppercase()}",
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace,
                  color = LanternCyan,
                  fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                  text = "$lineCount lines  •  ${editorContent.length} chars",
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace,
                  color = LanternTextMuted
                )
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                // Manual AI Autocomplete Trigger Button
                IconButton(
                  onClick = {
                    coroutineScope.launch {
                      val currentFile = activeFile
                      if (currentFile != null) {
                        isAiLoading = true
                        val prefix = editorContent.take((activeLineNumber * 40).coerceAtMost(editorContent.length))
                        val suffix = if (prefix.length < editorContent.length) editorContent.substring(prefix.length) else ""
                        val result = GeminiAutocompleter.fetchCompletion(
                          prefix = prefix,
                          suffix = suffix,
                          fileName = currentFile.name,
                          language = currentFile.language
                        )
                        isAiLoading = false
                        aiSuggestion = if (result.isSuccess) result.snippet else null
                        aiSource = result.source
                      }
                    }
                  },
                  modifier = Modifier.size(24.dp).testTag("trigger_ai_completion_btn")
                ) {
                  Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Trigger AI Autocomplete",
                    tint = LanternNeonGreen,
                    modifier = Modifier.size(14.dp)
                  )
                }

                Spacer(modifier = Modifier.width(6.dp))

                if (activeFileIssues.isNotEmpty()) {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                      .clickable { onNavigateToAudit() }
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = "${activeFileIssues.size} Issues",
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFFEF4444)
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                }

                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(LanternObsidianBorder)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "Ln $activeLineNumber, Col $activeColumnNumber",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = LanternNeonGreen,
                    fontWeight = FontWeight.Bold
                  )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                  onClick = { showGoToLineDialog = true },
                  modifier = Modifier.size(24.dp)
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Go to line",
                    tint = LanternCyan,
                    modifier = Modifier.size(14.dp)
                  )
                }
              }
            }
          }
        }
      } else {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
            contentDescription = null,
            tint = LanternTextMuted,
            modifier = Modifier.size(48.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text("No file currently open", color = LanternTextSecondary)
          TextButton(onClick = { isFileTreeOpen = true }) {
            Text("Open Project Explorer", color = LanternNeonGreen)
          }
        }
      }
    }

    // Quick Code Syntax Helper Toolbar (Always on top of keyboard via imePadding & configurable symbol sets)
    val baseShortcuts = when (selectedSymbolSet) {
      "General" -> listOf("{ }", "( )", "[ ]", ";", "=", "\"", "'")
      "Operators" -> listOf("->", "::", "==", "!=", "&&", "||", "+=", "-=")
      "Web" -> listOf("<tag>", "</>", "class=", "href=", "id=", "<div>")
      "Python" -> listOf("def ", "lambda", "async", "await", "import ", "print()")
      else -> listOf("{ }", "( )", "[ ]")
    }
    val syntaxShortcuts = baseShortcuts + customSymbols

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .imePadding()
        .background(LanternObsidianCard)
        .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          val sets = listOf("General", "Operators", "Web", "Python")
          items(sets) { setName ->
            val isSelected = selectedSymbolSet == setName
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(if (isSelected) LanternNeonGreen.copy(alpha = 0.2f) else Color.Transparent)
                .clickable { selectedSymbolSet = setName }
                .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text(
                text = setName,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) LanternNeonGreen else LanternTextMuted
              )
            }
          }
        }

        IconButton(
          onClick = { showAddSymbolDialog = true },
          modifier = Modifier.size(24.dp).testTag("add_custom_symbol_button")
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Symbol",
            tint = LanternNeonGreen,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        items(syntaxShortcuts) { symbol ->
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(LanternObsidianBorder)
              .clickable {
                val current = editorContent
                viewModel.updateEditorContent(current + symbol)
              }
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Text(
              text = symbol,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = LanternBrightGreen,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // AI Vibecoding Bottom Drawer
    AnimatedVisibility(visible = isAiDrawerOpen) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp)
          .testTag("ai_vibecoding_card"),
        colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LanternNeonGreen.copy(alpha = 0.5f)))
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = LanternNeonGreen, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Vibecoding Agent (Local / Custom API)",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = LanternBrightGreen
                )
              )
            }

            IconButton(onClick = { isAiDrawerOpen = false }, modifier = Modifier.size(20.dp)) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = LanternTextMuted, modifier = Modifier.size(16.dp))
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Preset Vibecoding Prompts
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val presets = listOf("Vibecode Mesh Handler", "Add Unit Test", "Explain Code", "Refactor Async")
            items(presets) { prompt ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(LanternCyan.copy(alpha = 0.15f))
                  .clickable {
                    aiPromptInput = prompt
                    viewModel.vibecodeWithAi(prompt)
                  }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(prompt, fontSize = 11.sp, color = LanternCyan)
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = aiPromptInput,
              onValueChange = { aiPromptInput = it },
              placeholder = { Text("What do you want to vibecode or refactor?", fontSize = 12.sp) },
              modifier = Modifier
                .weight(1f)
                .testTag("ai_prompt_input_field"),
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LanternNeonGreen,
                focusedTextColor = LanternTextPrimary
              )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
              onClick = {
                if (aiPromptInput.isNotBlank()) {
                  viewModel.vibecodeWithAi(aiPromptInput)
                }
              },
              enabled = !isAiWorking,
              colors = ButtonDefaults.buttonColors(containerColor = LanternNeonGreen, contentColor = Color(0xFF003D1A)),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("submit_ai_vibecode_button")
            ) {
              if (isAiWorking) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF003D1A))
              } else {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
              }
            }
          }

          if (aiResult != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = CodeEditorBg),
              shape = RoundedCornerShape(8.dp)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "Source: ${aiResult?.source} (${aiResult?.latencyMs}ms)",
                    fontSize = 11.sp,
                    color = LanternCyan,
                    fontFamily = FontFamily.Monospace
                  )

                  Button(
                    onClick = {
                      aiResult?.outputText?.let { viewModel.applyAiSuggestion(it) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LanternNeonGreen, contentColor = Color(0xFF003D1A)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(28.dp).testTag("apply_ai_code_button")
                  ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Apply to Code", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                  text = aiResult?.outputText ?: "",
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace,
                  color = LanternTextPrimary,
                  maxLines = 6
                )
              }
            }
          }
        }
      }
    }
  }

  if (showNewFileDialog) {
    NewFileDialog(
      onDismiss = { showNewFileDialog = false },
      onCreate = { path, name, lang ->
        selectedProject?.id?.let { pid ->
          viewModel.createNewFile(pid, path, name, lang)
        }
        showNewFileDialog = false
      }
    )
  }

  if (showCommitDialog) {
    CommitGitDialog(
      files = files,
      onDismiss = { showCommitDialog = false },
      onCommit = { msg, staged ->
        viewModel.commitGitChanges("$msg [staged: ${staged.joinToString(",")}]")
        showCommitDialog = false
      }
    )
  }

  if (showDiffDialog && activeFile != null) {
    val origContent = activeFile!!.content
    val diff = viewModel.gitManager.computeDiff(origContent, editorContent)
    GitDiffViewerDialog(
      fileName = activeFile!!.name,
      diffLines = diff,
      onAcceptLocal = {
        viewModel.saveCurrentFile()
        showDiffDialog = false
      },
      onAcceptRemote = {
        viewModel.updateEditorContent(origContent)
        showDiffDialog = false
      },
      onDismiss = { showDiffDialog = false }
    )
  }

  if (showPreviewDialog && activeFile != null) {
    LiveWebPreviewDialog(
      fileName = activeFile!!.name,
      content = editorContent,
      projectFiles = files,
      onDismiss = { showPreviewDialog = false }
    )
  }

  if (showSnippetsDialog) {
    SnippetPaletteDialog(
      onInsert = { snippet ->
        viewModel.updateEditorContent(editorContent + "\n\n" + snippet)
        showSnippetsDialog = false
      },
      onDismiss = { showSnippetsDialog = false }
    )
  }

  if (showFuzzySearchDialog) {
    FuzzySearchFilesDialog(
      files = files,
      onFileSelected = { file ->
        viewModel.openFile(file)
        showFuzzySearchDialog = false
      },
      onDismiss = { showFuzzySearchDialog = false }
    )
  }

  if (showAddSymbolDialog) {
    AddCustomSymbolDialog(
      onDismiss = { showAddSymbolDialog = false },
      onAdd = { newSymbol ->
        customSymbols = customSymbols + newSymbol
        showAddSymbolDialog = false
      }
    )
  }

  if (showGoToLineDialog) {
    val lines = editorContent.lines()
    GoToLineDialog(
      lineCount = maxOf(1, lines.size),
      onDismiss = { showGoToLineDialog = false },
      onGoToLine = { targetLine ->
        activeLineNumber = targetLine
        showGoToLineDialog = false
      }
    )
  }

  if (showCommandPalette) {
    CommandPaletteModal(
      onDismiss = { showCommandPalette = false },
      onExecute = { action ->
        when (action) {
          CommandAction.FORMAT_DOC -> viewModel.formatCurrentDocument()
          CommandAction.SAVE_FILE -> viewModel.saveCurrentFile()
          CommandAction.SAVE_ALL -> viewModel.saveAllFiles()
          CommandAction.CLOSE_FILE -> activeFile?.let { viewModel.closeTab(it) }
          CommandAction.CLOSE_ALL -> viewModel.closeAllTabs()
          CommandAction.FIND_FILES -> showFuzzySearchDialog = true
          CommandAction.GIT_COMMIT -> showCommitDialog = true
          CommandAction.GIT_DASHBOARD -> onNavigateToGit()
          CommandAction.GO_TO_LINE -> showGoToLineDialog = true
          CommandAction.NEW_FILE -> showNewFileDialog = true
          CommandAction.AUDIT -> onNavigateToAudit()
          CommandAction.TOGGLE_AI -> {
            coroutineScope.launch {
              val currentFile = activeFile
              if (currentFile != null) {
                isAiLoading = true
                val prefix = editorContent.take((activeLineNumber * 40).coerceAtMost(editorContent.length))
                val suffix = if (prefix.length < editorContent.length) editorContent.substring(prefix.length) else ""
                val result = GeminiAutocompleter.fetchCompletion(
                  prefix = prefix,
                  suffix = suffix,
                  fileName = currentFile.name,
                  language = currentFile.language
                )
                isAiLoading = false
                aiSuggestion = if (result.isSuccess) result.snippet else null
                aiSource = result.source
              }
            }
          }
          CommandAction.TOGGLE_MINIMAP -> {
            isMinimapVisible = !isMinimapVisible
          }
        }
      }
    )
  }
}

@Composable
fun SnippetPaletteDialog(
  onInsert: (snippet: String) -> Unit,
  onDismiss: () -> Unit
) {
  val snippets = listOf(
    "Room DAO Query" to """@Query("SELECT * FROM project_files WHERE projectId = :id")
fun getProjectFiles(id: Long): Flow<List<ProjectFileEntity>>""",

    "WebRTC Packet Stream" to """fun onPeerPacketReceived(packetId: String, payload: ByteArray) {
    val decoded = String(payload)
    println("Peer message: ${'$'}decoded")
}""",

    "Coroutine Flow Channel" to """import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay

fun observeMeshSync() = flow {
    emit("CONNECTED_TO_ROUTER")
    delay(200)
    emit("PEER_HANDSHAKE_ACK")
}""",

    "SQLite DDL Table" to """CREATE TABLE IF NOT EXISTS peer_telemetry (
    peer_id TEXT PRIMARY KEY,
    latency_ms INTEGER DEFAULT 15,
    last_ping INTEGER DEFAULT (strftime('%s', 'now'))
);""",

    "Unit Test Assertion" to """@Test
fun testMeshProtocolIntegrity() {
    val status = "READY"
    assertEquals("READY", status)
}"""
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.DataObject, contentDescription = null, tint = LanternCyan, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Code Snippets Palette", fontWeight = FontWeight.Bold, color = LanternTextPrimary, fontSize = 14.sp)
      }
    },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth().height(260.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(snippets) { (title, code) ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onInsert(code) },
            colors = CardDefaults.cardColors(containerColor = CodeEditorBg),
            shape = RoundedCornerShape(8.dp)
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text(title, fontWeight = FontWeight.Bold, color = LanternBrightGreen, fontSize = 12.sp)
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = code.lines().take(3).joinToString("\n"),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = LanternTextSecondary
              )
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) { Text("Close", color = LanternTextSecondary) }
    },
    containerColor = LanternObsidianCard
  )
}

@Composable
fun NewFileDialog(
  onDismiss: () -> Unit,
  onCreate: (path: String, name: String, lang: String) -> Unit
) {
  var fileName by remember { mutableStateOf("") }
  var folderPath by remember { mutableStateOf("src") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Create Source File", color = LanternTextPrimary, fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = folderPath,
          onValueChange = { folderPath = it },
          label = { Text("Folder Path") },
          placeholder = { Text("e.g. src or db") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = fileName,
          onValueChange = { fileName = it },
          label = { Text("File Name with Extension") },
          placeholder = { Text("e.g. Service.kt or query.sql") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("new_file_name_field")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (fileName.isNotBlank()) {
            val ext = fileName.substringAfterLast(".", "kt")
            val fullPath = if (folderPath.isNotBlank()) "$folderPath/$fileName" else fileName
            onCreate(fullPath, fileName, ext)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = LanternNeonGreen, contentColor = Color(0xFF003D1A)),
        modifier = Modifier.testTag("confirm_create_file_button")
      ) {
        Text("Create", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel", color = LanternTextSecondary) }
    },
    containerColor = LanternObsidianCard
  )
}

@Composable
fun CommitGitDialog(
  files: List<ProjectFileEntity>,
  onDismiss: () -> Unit,
  onCommit: (message: String, stagedFiles: List<String>) -> Unit
) {
  var commitMsg by remember { mutableStateOf("") }
  var stagedPaths by remember { mutableStateOf(files.map { it.path }.toSet()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = LanternObsidianCard,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Difference, contentDescription = null, tint = LanternNeonGreen, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Git Stage & Commit", color = LanternBrightGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .height(340.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = "Select files to stage and enter commit message:",
          style = MaterialTheme.typography.bodySmall.copy(color = LanternTextSecondary)
        )

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          colors = CardDefaults.cardColors(containerColor = CodeEditorBg),
          shape = RoundedCornerShape(8.dp),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LanternObsidianBorder))
        ) {
          LazyColumn(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            items(files) { file ->
              val isStaged = stagedPaths.contains(file.path)
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (isStaged) LanternNeonGreen.copy(alpha = 0.1f) else Color.Transparent)
                  .clickable {
                    stagedPaths = if (isStaged) {
                      stagedPaths - file.path
                    } else {
                      stagedPaths + file.path
                    }
                  }
                  .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                  Checkbox(
                    checked = isStaged,
                    onCheckedChange = { checked ->
                      stagedPaths = if (checked) stagedPaths + file.path else stagedPaths - file.path
                    },
                    colors = CheckboxDefaults.colors(
                      checkedColor = LanternNeonGreen,
                      uncheckedColor = LanternTextMuted,
                      checkmarkColor = Color(0xFF003D1A)
                    ),
                    modifier = Modifier.size(24.dp).testTag("checkbox_${file.name}")
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = file.path,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (isStaged) LanternNeonGreen else LanternTextPrimary,
                    maxLines = 1
                  )
                }
                Text(
                  text = if (file.isModified) "M" else " ",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (file.isModified) LanternCyan else LanternTextMuted
                )
              }
            }
          }
        }

        OutlinedTextField(
          value = commitMsg,
          onValueChange = { commitMsg = it },
          label = { Text("Commit Message") },
          placeholder = { Text("feat: implement git staging UI") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("commit_message_field"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LanternNeonGreen,
            unfocusedBorderColor = LanternObsidianBorder,
            focusedTextColor = LanternTextPrimary,
            unfocusedTextColor = LanternTextPrimary,
            cursorColor = LanternNeonGreen
          )
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (commitMsg.isNotBlank() && stagedPaths.isNotEmpty()) {
            onCommit(commitMsg.trim(), stagedPaths.toList())
          }
        },
        enabled = commitMsg.isNotBlank() && stagedPaths.isNotEmpty(),
        colors = ButtonDefaults.buttonColors(
          containerColor = LanternNeonGreen,
          contentColor = Color(0xFF003D1A),
          disabledContainerColor = LanternObsidianBorder,
          disabledContentColor = LanternTextMuted
        ),
        modifier = Modifier.testTag("confirm_commit_button")
      ) {
        Text("Commit (${stagedPaths.size} staged)", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel", color = LanternTextSecondary) }
    }
  )
}

@Composable
fun FuzzySearchFilesDialog(
  files: List<com.example.data.entity.ProjectFileEntity>,
  onFileSelected: (com.example.data.entity.ProjectFileEntity) -> Unit,
  onDismiss: () -> Unit
) {
  var query by remember { mutableStateOf("") }

  val matchingFiles = remember(query, files) {
    if (query.isBlank()) {
      files
    } else {
      files.map { file ->
        val score = fuzzyMatchScore(query, file.path)
        file to score
      }.filter { it.second >= 0 }
       .sortedByDescending { it.second }
       .map { it.first }
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = LanternObsidianCard,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Search, contentDescription = null, tint = LanternNeonGreen, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Quick Open (Fuzzy Search)", color = LanternBrightGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .height(320.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedTextField(
          value = query,
          onValueChange = { query = it },
          placeholder = { Text("Type file name or path (e.g., MainActivity, index.html)...", fontSize = 12.sp, color = LanternTextMuted) },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("fuzzy_search_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LanternNeonGreen,
            unfocusedBorderColor = LanternObsidianBorder,
            focusedTextColor = LanternTextPrimary,
            unfocusedTextColor = LanternTextPrimary,
            cursorColor = LanternNeonGreen
          ),
          textStyle = TextStyle(fontSize = 13.sp, fontFamily = FontFamily.Monospace)
        )

        Text(
          text = "${matchingFiles.size} matching files",
          fontSize = 11.sp,
          color = LanternTextMuted
        )

        if (matchingFiles.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            contentAlignment = Alignment.Center
          ) {
            Text("No files found matching '$query'", fontSize = 12.sp, color = LanternTextMuted)
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            items(matchingFiles) { file ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(6.dp))
                  .background(LanternObsidianBorder.copy(alpha = 0.3f))
                  .clickable { onFileSelected(file) }
                  .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                    contentDescription = null,
                    tint = LanternCyan,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = file.path,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = LanternTextPrimary,
                    maxLines = 1
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(LanternNeonGreen.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = file.language,
                    fontSize = 10.sp,
                    color = LanternNeonGreen
                  )
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Close", color = LanternTextMuted)
      }
    }
  )
}

fun fuzzyMatchScore(query: String, target: String): Int {
  if (query.isEmpty()) return 1
  val q = query.lowercase()
  val t = target.lowercase()
  if (t.contains(q)) return 100 + (t.length - q.length)

  var qIdx = 0
  var score = 0
  for (i in t.indices) {
    if (qIdx < q.length && t[i] == q[qIdx]) {
      score += (10 - i.coerceAtMost(9))
      qIdx++
    }
  }
  return if (qIdx == q.length) score else -1
}

@Composable
fun AddCustomSymbolDialog(
  onDismiss: () -> Unit,
  onAdd: (String) -> Unit
) {
  var symbolText by remember { mutableStateOf("") }
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = LanternObsidianCard,
    title = { Text("Add Custom Symbol / Snippet", color = LanternTextPrimary, fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = symbolText,
          onValueChange = { symbolText = it },
          label = { Text("Symbol or Snippet") },
          placeholder = { Text("e.g. console.log() or val x = ") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("custom_symbol_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LanternNeonGreen,
            unfocusedBorderColor = LanternObsidianBorder,
            focusedTextColor = LanternTextPrimary,
            unfocusedTextColor = LanternTextPrimary,
            cursorColor = LanternNeonGreen
          )
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (symbolText.isNotBlank()) {
            onAdd(symbolText.trim())
          }
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = LanternNeonGreen,
          contentColor = Color(0xFF003D1A)
        ),
        modifier = Modifier.testTag("confirm_add_symbol_button")
      ) {
        Text("Add", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel", color = LanternTextSecondary) }
    }
  )
}

@Composable
fun EditorGutter(
  lineCount: Int,
  activeLine: Int = -1,
  issueLines: Map<Int, AuditSeverity> = emptyMap(),
  peerCursorLines: List<Int> = emptyList(),
  onLineClick: (Int) -> Unit = {}
) {
  val minDigits = when {
    lineCount >= 1000 -> 4
    lineCount >= 100 -> 3
    else -> 2
  }

  Column(
    modifier = Modifier
      .background(CodeGutterBg)
      .border(
        width = 1.dp,
        color = LanternObsidianBorder
      )
      .padding(horizontal = 8.dp, vertical = 8.dp),
    horizontalAlignment = Alignment.End
  ) {
    for (line in 1..lineCount) {
      val isCurrentLine = line == activeLine
      val issueSeverity = issueLines[line]
      val hasPeer = peerCursorLines.contains(line)

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
          .height(20.dp)
          .clickable { onLineClick(line) }
      ) {
        if (issueSeverity != null) {
          val dotColor = when (issueSeverity) {
            AuditSeverity.CRITICAL -> Color(0xFFEF4444)
            AuditSeverity.WARNING -> Color(0xFFF59E0B)
            AuditSeverity.INFO -> LanternCyan
          }
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(dotColor)
          )
          Spacer(modifier = Modifier.width(4.dp))
        } else if (hasPeer) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(LanternBrightGreen)
          )
          Spacer(modifier = Modifier.width(4.dp))
        }

        Text(
          text = line.toString().padStart(minDigits, ' '),
          fontSize = 12.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = if (isCurrentLine) FontWeight.Bold else FontWeight.Normal,
          color = if (isCurrentLine) LanternNeonGreen else LanternTextMuted,
          lineHeight = 20.sp
        )
      }
    }
  }
}

@Composable
fun GoToLineDialog(
  lineCount: Int,
  onDismiss: () -> Unit,
  onGoToLine: (Int) -> Unit
) {
  var lineInput by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = LanternObsidianCard,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Search, contentDescription = null, tint = LanternNeonGreen, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Go to Line (1..$lineCount)", color = LanternBrightGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = lineInput,
          onValueChange = { lineInput = it.filter { char -> char.isDigit() } },
          label = { Text("Target Line Number") },
          placeholder = { Text("e.g. 12") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("go_to_line_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LanternNeonGreen,
            unfocusedBorderColor = LanternObsidianBorder,
            focusedTextColor = LanternTextPrimary,
            unfocusedTextColor = LanternTextPrimary,
            cursorColor = LanternNeonGreen
          ),
          textStyle = TextStyle(fontSize = 13.sp, fontFamily = FontFamily.Monospace)
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val num = lineInput.toIntOrNull()
          if (num != null && num in 1..lineCount) {
            onGoToLine(num)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = LanternNeonGreen, contentColor = Color(0xFF003D1A)),
        modifier = Modifier.testTag("confirm_go_to_line_button")
      ) {
        Text("Jump", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel", color = LanternTextMuted) }
    }
  )
}


