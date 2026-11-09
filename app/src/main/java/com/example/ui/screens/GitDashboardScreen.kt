package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.GitCommitEntity
import com.example.data.entity.ProjectFileEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GitDashboardScreen(
  viewModel: WorkspaceViewModel,
  onNavigateToEditor: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val selectedProject by viewModel.selectedProject.collectAsStateWithLifecycle()
  val projectFiles by viewModel.projectFiles.collectAsStateWithLifecycle()
  val gitStatus by viewModel.gitStatus.collectAsStateWithLifecycle()
  val commits by viewModel.commits.collectAsStateWithLifecycle()
  val userProfile by viewModel.gitHubProfile.collectAsStateWithLifecycle()

  val modifiedFiles = remember(projectFiles) {
    projectFiles.filter { it.isModified }
  }

  var commitMessageInput by remember { mutableStateOf("") }
  var showBranchDropdown by remember { mutableStateOf(false) }
  var showNewBranchDialog by remember { mutableStateOf(false) }
  var showDiffViewerForFile by remember { mutableStateOf<ProjectFileEntity?>(null) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(CodeEditorBg)
      .padding(16.dp)
      .testTag("git_dashboard_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Dashboard Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Difference,
              contentDescription = null,
              tint = LanternNeonGreen,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Git Dashboard",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = LanternBrightGreen
              )
            )
          }
          Text(
            text = "Active Workspace: ${selectedProject?.name ?: "No project selected"}",
            fontSize = 12.sp,
            color = LanternTextMuted,
            fontFamily = FontFamily.Monospace
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(
            onClick = onNavigateToEditor,
            colors = ButtonDefaults.buttonColors(containerColor = LanternObsidianCard),
            border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LanternObsidianBorder)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("git_to_editor_button")
          ) {
            Text("Open Editor", fontSize = 11.sp, color = LanternCyan)
          }
        }
      }
    }

    // 2. Branch Status & Operations Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("git_branch_status_card"),
        colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LanternObsidianBorder))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "CURRENT BRANCH",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = LanternTextMuted
              )
              Spacer(modifier = Modifier.height(4.dp))
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(LanternCyan.copy(alpha = 0.2f))
                    .border(1.dp, LanternCyan, RoundedCornerShape(6.dp))
                    .clickable { showBranchDropdown = true }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("git_branch_selector_badge")
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                      contentDescription = null,
                      tint = LanternCyan,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = gitStatus.currentBranch,
                      fontSize = 13.sp,
                      fontFamily = FontFamily.Monospace,
                      fontWeight = FontWeight.Bold,
                      color = LanternCyan
                    )
                  }

                  DropdownMenu(
                    expanded = showBranchDropdown,
                    onDismissRequest = { showBranchDropdown = false },
                    modifier = Modifier.background(LanternObsidianCard).border(1.dp, LanternObsidianBorder)
                  ) {
                    Text(
                      text = "Switch Branch",
                      fontSize = 11.sp,
                      color = LanternTextMuted,
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                    gitStatus.branches.forEach { branch ->
                      DropdownMenuItem(
                        text = {
                          Row(verticalAlignment = Alignment.CenterVertically) {
                            if (branch == gitStatus.currentBranch) {
                              Icon(Icons.Default.Check, contentDescription = null, tint = LanternNeonGreen, modifier = Modifier.size(14.dp))
                              Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                              text = branch,
                              color = if (branch == gitStatus.currentBranch) LanternNeonGreen else LanternTextPrimary,
                              fontFamily = FontFamily.Monospace,
                              fontSize = 12.sp
                            )
                          }
                        },
                        onClick = {
                          showBranchDropdown = false
                          viewModel.switchBranch(branch)
                        }
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.width(10.dp))

                OutlinedButton(
                  onClick = { showNewBranchDialog = true },
                  shape = RoundedCornerShape(6.dp),
                  modifier = Modifier.height(34.dp).testTag("git_new_branch_button")
                ) {
                  Icon(Icons.Default.Add, contentDescription = null, tint = LanternBrightGreen, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("New Branch", fontSize = 11.sp, color = LanternBrightGreen)
                }
              }
            }

            // Ahead / Behind Badges
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (gitStatus.aheadCount > 0) LanternNeonGreen.copy(alpha = 0.2f) else LanternObsidianBorder.copy(alpha = 0.4f))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = if (gitStatus.aheadCount > 0) LanternNeonGreen else LanternTextMuted, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "${gitStatus.aheadCount} ahead",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (gitStatus.aheadCount > 0) LanternNeonGreen else LanternTextMuted
                  )
                }
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (gitStatus.behindCount > 0) Color(0xFFF59E0B).copy(alpha = 0.2f) else LanternObsidianBorder.copy(alpha = 0.4f))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = if (gitStatus.behindCount > 0) Color(0xFFF59E0B) else LanternTextMuted, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "${gitStatus.behindCount} behind",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (gitStatus.behindCount > 0) Color(0xFFF59E0B) else LanternTextMuted
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Last Sync status line
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(LanternObsidianBorder.copy(alpha = 0.3f))
              .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Sync, contentDescription = null, tint = LanternCyan, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = gitStatus.lastSyncMessage,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = LanternTextSecondary
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Common Operations: Push, Pull, and Commit Triggers
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Push Button
            Button(
              onClick = { viewModel.pushGit() },
              colors = ButtonDefaults.buttonColors(containerColor = LanternNeonGreen, contentColor = Color(0xFF003D1A)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).testTag("git_push_button")
            ) {
              Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (gitStatus.aheadCount > 0) "Push (${gitStatus.aheadCount})" else "Push",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            }

            // Pull Button
            Button(
              onClick = { viewModel.pullGit() },
              colors = ButtonDefaults.buttonColors(containerColor = LanternCyan.copy(alpha = 0.2f), contentColor = LanternCyan),
              border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LanternCyan)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).testTag("git_pull_button")
            ) {
              Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (gitStatus.behindCount > 0) "Pull (${gitStatus.behindCount})" else "Pull",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            }
          }
        }
      }
    }

    // 3. Working Tree & Commit Action Section
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("git_working_tree_card"),
        colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LanternObsidianBorder))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "CHANGES & STAGING",
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              color = LanternBrightGreen
            )

            Text(
              text = "${modifiedFiles.size} modified file(s)",
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = if (modifiedFiles.isNotEmpty()) LanternNeonGreen else LanternTextMuted
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          if (modifiedFiles.isEmpty()) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(LanternObsidianBorder.copy(alpha = 0.25f))
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LanternNeonGreen, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Working tree clean. All files committed & synchronized.",
                fontSize = 12.sp,
                color = LanternTextSecondary
              )
            }
          } else {
            // Modified files list
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              modifiedFiles.forEach { file ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(LanternObsidianBorder.copy(alpha = 0.3f))
                    .clickable { showDiffViewerForFile = file }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                      contentDescription = null,
                      tint = LanternCyan,
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = file.path,
                      fontSize = 12.sp,
                      fontFamily = FontFamily.Monospace,
                      color = LanternTextPrimary
                    )
                  }

                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(Color(0xFFF59E0B).copy(alpha = 0.25f))
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = "MODIFIED",
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFFF59E0B),
                      fontFamily = FontFamily.Monospace
                    )
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Commit Box
          Text(
            text = "Commit Message",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = LanternTextMuted
          )
          Spacer(modifier = Modifier.height(4.dp))

          OutlinedTextField(
            value = commitMessageInput,
            onValueChange = { commitMessageInput = it },
            placeholder = { Text("Describe changes made...", fontSize = 12.sp, color = LanternTextMuted) },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("git_commit_message_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = LanternNeonGreen,
              unfocusedBorderColor = LanternObsidianBorder,
              focusedContainerColor = LanternObsidianBorder.copy(alpha = 0.3f),
              unfocusedContainerColor = LanternObsidianBorder.copy(alpha = 0.2f)
            ),
            textStyle = androidx.compose.ui.text.TextStyle(
              fontFamily = FontFamily.Monospace,
              fontSize = 12.sp,
              color = LanternTextPrimary
            )
          )

          Spacer(modifier = Modifier.height(6.dp))

          // Quick Preset Messages
          val presets = listOf("feat: update logic", "fix: resolve edge case", "style: polish UI layout", "refactor: clean structure")
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(presets) { preset ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(LanternObsidianBorder.copy(alpha = 0.5f))
                  .clickable { commitMessageInput = preset }
                  .padding(horizontal = 8.dp, vertical = 3.dp)
              ) {
                Text(preset, fontSize = 10.sp, color = LanternCyan, fontFamily = FontFamily.Monospace)
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = {
              if (commitMessageInput.isNotBlank()) {
                viewModel.commitGitChanges(commitMessageInput.trim())
                commitMessageInput = ""
              }
            },
            enabled = commitMessageInput.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = LanternNeonGreen, contentColor = Color(0xFF003D1A)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().testTag("git_commit_button")
          ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Commit to '${gitStatus.currentBranch}'",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }
        }
      }
    }

    // 4. Commit History Timeline Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("git_commit_history_card"),
        colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LanternObsidianBorder))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.History, contentDescription = null, tint = LanternBrightGreen, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "COMMIT HISTORY",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = LanternBrightGreen
              )
            }

            Text(
              text = "${commits.size} commit(s)",
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = LanternTextMuted
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          if (commits.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "No commits found. Stage changes and commit above.",
                fontSize = 12.sp,
                color = LanternTextMuted,
                fontFamily = FontFamily.Monospace
              )
            }
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              commits.forEachIndexed { index, commit ->
                CommitTimelineItem(commit = commit, isLatest = index == 0)
              }
            }
          }
        }
      }
    }
  }

  // Create New Branch Dialog
  if (showNewBranchDialog) {
    var newBranchName by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { showNewBranchDialog = false },
      containerColor = LanternObsidianCard,
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = null, tint = LanternNeonGreen, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Create New Branch", color = LanternBrightGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Branch name will branch off '${gitStatus.currentBranch}'.", fontSize = 12.sp, color = LanternTextSecondary)
          OutlinedTextField(
            value = newBranchName,
            onValueChange = { newBranchName = it },
            placeholder = { Text("e.g. feature/responsive-ui") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("new_branch_name_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = LanternNeonGreen,
              unfocusedBorderColor = LanternObsidianBorder,
              focusedTextColor = LanternTextPrimary,
              unfocusedTextColor = LanternTextPrimary
            ),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newBranchName.isNotBlank()) {
              viewModel.createBranch(newBranchName.trim())
              showNewBranchDialog = false
            }
          },
          enabled = newBranchName.isNotBlank(),
          colors = ButtonDefaults.buttonColors(containerColor = LanternNeonGreen, contentColor = Color(0xFF003D1A)),
          modifier = Modifier.testTag("confirm_create_branch_button")
        ) {
          Text("Create & Checkout", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showNewBranchDialog = false }) {
          Text("Cancel", color = LanternTextMuted)
        }
      }
    )
  }

  // Diff Viewer Modal
  if (showDiffViewerForFile != null) {
    val file = showDiffViewerForFile!!
    val diff = viewModel.gitManager.computeDiff(file.content, viewModel.editorContent.value)
    GitDiffViewerDialog(
      fileName = file.name,
      diffLines = diff,
      onAcceptLocal = {
        viewModel.saveCurrentFile()
        showDiffViewerForFile = null
      },
      onAcceptRemote = {
        showDiffViewerForFile = null
      },
      onDismiss = { showDiffViewerForFile = null }
    )
  }
}

@Composable
private fun CommitTimelineItem(commit: GitCommitEntity, isLatest: Boolean) {
  val dateFormatted = remember(commit.timestamp) {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    sdf.format(Date(commit.timestamp))
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(LanternObsidianBorder.copy(alpha = 0.2f))
      .border(
        width = 1.dp,
        color = if (isLatest) LanternNeonGreen.copy(alpha = 0.5f) else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
      )
      .padding(12.dp)
      .testTag("git_commit_item_${commit.commitHash}"),
    verticalAlignment = Alignment.Top
  ) {
    // Timeline node indicator
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(top = 2.dp)
    ) {
      Box(
        modifier = Modifier
          .size(10.dp)
          .clip(CircleShape)
          .background(if (isLatest) LanternNeonGreen else LanternCyan)
      )
    }

    Spacer(modifier = Modifier.width(12.dp))

    Column(modifier = Modifier.weight(1f)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = commit.message,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = LanternTextPrimary
        )

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(LanternObsidianBorder)
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = commit.commitHash.take(10),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = LanternCyan,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = commit.author,
          fontSize = 11.sp,
          color = LanternTextSecondary,
          fontFamily = FontFamily.Monospace
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(LanternCyan.copy(alpha = 0.15f))
              .padding(horizontal = 4.dp, vertical = 1.dp)
          ) {
            Text(
              text = commit.branch,
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace,
              color = LanternCyan
            )
          }

          Spacer(modifier = Modifier.width(6.dp))

          Text(
            text = dateFormatted,
            fontSize = 10.sp,
            color = LanternTextMuted,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }
  }
}
