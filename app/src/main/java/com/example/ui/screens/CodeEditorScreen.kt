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
import androidx.compose.foundation.layout.height
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.ProjectFileEntity
import com.example.ui.WorkspaceViewModel
import com.example.ui.components.CodeSyntaxVisualTransformation
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

  var isFileTreeOpen by remember { mutableStateOf(false) }
  var isAiDrawerOpen by remember { mutableStateOf(false) }
  var isPreviewTabActive by remember { mutableStateOf(false) }
  var aiPromptInput by remember { mutableStateOf("") }
  var showNewFileDialog by remember { mutableStateOf(false) }
  var showCommitDialog by remember { mutableStateOf(false) }
  var showDiffDialog by remember { mutableStateOf(false) }
  var showPreviewDialog by remember { mutableStateOf(false) }
  var showSnippetsDialog by remember { mutableStateOf(false) }

  val isPreviewable = activeFile?.let {
    it.name.endsWith(".html", ignoreCase = true) ||
      it.name.endsWith(".htm", ignoreCase = true) ||
      it.name.endsWith(".md", ignoreCase = true) ||
      it.name.endsWith(".markdown", ignoreCase = true) ||
      it.name.endsWith(".css", ignoreCase = true) ||
      it.name.endsWith(".js", ignoreCase = true)
  } ?: false

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CodeEditorBg)
  ) {
    // Editor Header Bar
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

    // Tabs bar for open files
    if (openTabs.isNotEmpty()) {
      val rawIndex = openTabs.indexOfFirst { it.id == activeFile?.id }
      val safeIndex = if (rawIndex >= 0) rawIndex.coerceIn(0, openTabs.size - 1) else 0
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

    // File Tree Drawer Overlay
    AnimatedVisibility(visible = isFileTreeOpen) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp)
          .testTag("file_tree_drawer"),
        colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LanternObsidianBorder))
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Folder, contentDescription = null, tint = LanternCyan, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Project Explorer (${files.size})", fontWeight = FontWeight.Bold, color = LanternTextPrimary, fontSize = 13.sp)
            }

            IconButton(
              onClick = { showNewFileDialog = true },
              modifier = Modifier.size(24.dp).testTag("new_file_button")
            ) {
              Icon(Icons.Default.Add, contentDescription = "New File", tint = LanternNeonGreen, modifier = Modifier.size(20.dp))
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          LazyColumn(modifier = Modifier.height(160.dp)) {
            items(files) { file ->
              val isCurrent = file.id == activeFile?.id
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (isCurrent) LanternNeonGreen.copy(alpha = 0.15f) else Color.Transparent)
                  .clickable {
                    viewModel.openFile(file)
                    isFileTreeOpen = false
                  }
                  .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                    contentDescription = null,
                    tint = if (isCurrent) LanternNeonGreen else LanternTextMuted,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = file.path,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (isCurrent) LanternNeonGreen else LanternTextPrimary
                  )
                }
                Text(
                  text = file.language,
                  fontSize = 10.sp,
                  color = LanternTextMuted
                )
              }
            }
          }
        }
      }
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
          val lines = editorContent.lines()
          val lineCount = maxOf(1, lines.size)

          Row(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(verticalScrollState)
          ) {
            // Gutter with line numbers (WRAPS content matching lines, NO fillMaxHeight)
            Column(
              modifier = Modifier
                .background(CodeGutterBg)
                .padding(horizontal = 10.dp, vertical = 8.dp),
              horizontalAlignment = Alignment.End
            ) {
              for (index in 1..lineCount) {
                Text(
                  text = "$index",
                  fontSize = 12.sp,
                  fontFamily = FontFamily.Monospace,
                  color = LanternTextMuted,
                  lineHeight = 20.sp
                )
              }
            }

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
                visualTransformation = CodeSyntaxVisualTransformation(activeFile?.language ?: "text"),
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

    // Quick Code Syntax Helper Toolbar
    val syntaxShortcuts = listOf("{ }", "( )", "[ ]", "->", "::", "val", "fun", "class", ";", "=", "==", "!= ", "\"\"", "/* */")
    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .background(LanternObsidianCard)
        .padding(horizontal = 8.dp, vertical = 6.dp),
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
      onDismiss = { showCommitDialog = false },
      onCommit = { msg ->
        viewModel.commitGitChanges(msg)
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
  onDismiss: () -> Unit,
  onCommit: (message: String) -> Unit
) {
  var commitMsg by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Commit Changes to Git", color = LanternTextPrimary, fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Stage all modified files and record commit in SQLite Git history.",
          style = MaterialTheme.typography.bodySmall.copy(color = LanternTextSecondary)
        )
        OutlinedTextField(
          value = commitMsg,
          onValueChange = { commitMsg = it },
          label = { Text("Commit Message") },
          placeholder = { Text("feat: implement peer synchronization") },
          modifier = Modifier.fillMaxWidth().testTag("commit_message_field")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (commitMsg.isNotBlank()) onCommit(commitMsg.trim())
        },
        colors = ButtonDefaults.buttonColors(containerColor = LanternCyan, contentColor = Color(0xFF002F48)),
        modifier = Modifier.testTag("confirm_commit_button")
      ) {
        Text("Commit", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel", color = LanternTextSecondary) }
    },
    containerColor = LanternObsidianCard
  )
}
