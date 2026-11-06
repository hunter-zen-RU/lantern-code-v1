package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.entity.ProjectEntity
import com.example.ui.WorkspaceViewModel
import com.example.ui.theme.LanternBrightGreen
import com.example.ui.theme.LanternCyan
import com.example.ui.theme.LanternNeonGreen
import com.example.ui.theme.LanternObsidianBorder
import com.example.ui.theme.LanternObsidianCard
import com.example.ui.theme.LanternTextMuted
import com.example.ui.theme.LanternTextPrimary
import com.example.ui.theme.LanternTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkspaceHubScreen(
  viewModel: WorkspaceViewModel,
  onNavigateToEditor: () -> Unit,
  onNavigateToDb: () -> Unit,
  onNavigateToCollab: () -> Unit,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  val projects by viewModel.allProjects.collectAsStateWithLifecycle()
  val selectedProject by viewModel.selectedProject.collectAsStateWithLifecycle()

  var showNewProjectDialog by remember { mutableStateOf(false) }
  var showGitHubManagerDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(8.dp))
      // Hero Header Card with Emblem
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("hero_banner_card"),
        colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(LanternNeonGreen, LanternCyan)))
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(56.dp)
                  .clip(RoundedCornerShape(14.dp))
                  .background(Color.Black)
                  .border(1.dp, LanternNeonGreen.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
              ) {
                Image(
                  painter = painterResource(id = R.drawable.ic_lantern_emblem),
                  contentDescription = "LANtern Code Emblem",
                  modifier = Modifier.size(50.dp)
                )
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column {
                Text(
                  text = "LANtern Code",
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = LanternBrightGreen,
                    letterSpacing = 0.5.sp
                  )
                )
                Text(
                  text = "Decentralized Android IDE & Mesh Studio",
                  style = MaterialTheme.typography.bodySmall.copy(color = LanternTextSecondary)
                )
              }
            }

            IconButton(
              onClick = onOpenSettings,
              modifier = Modifier.testTag("settings_button")
            ) {
              Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Studio Settings",
                tint = LanternTextSecondary
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Real-time telemetry badges
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            StatusPill(label = "WebRTC P2P Active", color = LanternNeonGreen)
            StatusPill(label = "Room SQLite Engine", color = LanternCyan)
            StatusPill(label = "Local Agent Ready", color = LanternBrightGreen)
            StatusPill(label = "LAN 192.168.1.108", color = Color(0xFFA78BFA))
          }
        }
      }
    }

    item {
      // Quick Workspace Action Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = { showNewProjectDialog = true },
          colors = ButtonDefaults.buttonColors(containerColor = LanternNeonGreen, contentColor = Color(0xFF003D1A)),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("new_project_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("New Project", fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
          onClick = { showGitHubManagerDialog = true },
          colors = ButtonDefaults.outlinedButtonColors(contentColor = LanternCyan),
          border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(LanternCyan, LanternCyan))),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("manage_github_button")
        ) {
          Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("GitHub Repos", fontWeight = FontWeight.Bold)
        }
      }
    }

    item {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Local Workspaces (${projects.size})",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = LanternTextPrimary
          )
        )

        TextButton(onClick = onNavigateToDb) {
          Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(16.dp), tint = LanternCyan)
          Spacer(modifier = Modifier.width(4.dp))
          Text("SQLite Studio", color = LanternCyan, fontSize = 13.sp)
        }
      }
    }

    if (projects.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.Folder,
              contentDescription = null,
              modifier = Modifier.size(48.dp),
              tint = LanternTextMuted
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No Workspaces Found",
              style = MaterialTheme.typography.titleMedium.copy(color = LanternTextPrimary)
            )
            Text(
              text = "Create a project or clone from GitHub to begin coding.",
              style = MaterialTheme.typography.bodySmall.copy(color = LanternTextSecondary)
            )
          }
        }
      }
    } else {
      items(projects) { project ->
        ProjectCard(
          project = project,
          isSelected = selectedProject?.id == project.id,
          onClick = {
            viewModel.selectProject(project)
            onNavigateToEditor()
          },
          onCollab = {
            viewModel.selectProject(project)
            onNavigateToCollab()
          }
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(32.dp))
    }
  }

  if (showNewProjectDialog) {
    NewProjectDialog(
      onDismiss = { showNewProjectDialog = false },
      onCreate = { name, desc, git ->
        viewModel.createProject(name, desc, git)
        showNewProjectDialog = false
        onNavigateToEditor()
      }
    )
  }

  if (showGitHubManagerDialog) {
    GitHubManagerDialog(
      viewModel = viewModel,
      onDismiss = { showGitHubManagerDialog = false },
      onRepoSelected = {
        showGitHubManagerDialog = false
        onNavigateToEditor()
      }
    )
  }
}

@Composable
fun StatusPill(label: String, color: Color) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(color.copy(alpha = 0.15f))
      .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
      .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
    Box(
      modifier = Modifier
        .size(6.dp)
        .clip(CircleShape)
        .background(color)
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      color = color
    )
  }
}

@Composable
fun ProjectCard(
  project: ProjectEntity,
  isSelected: Boolean,
  onClick: () -> Unit,
  onCollab: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("project_card_${project.id}"),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) LanternObsidianCard.copy(alpha = 0.9f) else LanternObsidianCard
    ),
    shape = RoundedCornerShape(16.dp),
    border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(LanternNeonGreen, LanternCyan))) else null
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Code,
            contentDescription = null,
            tint = LanternNeonGreen,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = project.name,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = LanternTextPrimary
              )
            )
            Text(
              text = "Branch: ${project.currentBranch}",
              style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                color = LanternCyan
              )
            )
          }
        }

        IconButton(
          onClick = onCollab,
          modifier = Modifier.testTag("collab_room_button_${project.id}")
        ) {
          Icon(
            imageVector = Icons.Default.Lan,
            contentDescription = "Peer Collaboration Room",
            tint = LanternBrightGreen
          )
        }
      }

      if (project.description.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = project.description,
          style = MaterialTheme.typography.bodySmall.copy(color = LanternTextSecondary),
          maxLines = 2
        )
      }

      if (project.gitRemoteUrl.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = project.gitRemoteUrl,
          style = MaterialTheme.typography.bodySmall.copy(
            fontFamily = FontFamily.Monospace,
            color = LanternTextMuted,
            fontSize = 11.sp
          ),
          maxLines = 1
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "SQLite Room Stored",
          fontSize = 11.sp,
          color = LanternTextMuted
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "Open Studio",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = LanternNeonGreen
          )
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = LanternNeonGreen,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}

@Composable
fun NewProjectDialog(
  onDismiss: () -> Unit,
  onCreate: (name: String, desc: String, git: String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var desc by remember { mutableStateOf("") }
  var git by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text("Create SQLite Workspace", color = LanternTextPrimary, fontWeight = FontWeight.Bold)
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Project Name") },
          placeholder = { Text("e.g. MyMeshApp") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("new_project_name_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LanternNeonGreen,
            focusedLabelColor = LanternNeonGreen
          )
        )

        OutlinedTextField(
          value = desc,
          onValueChange = { desc = it },
          label = { Text("Description") },
          placeholder = { Text("Purpose of this project") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = git,
          onValueChange = { git = it },
          label = { Text("Git Remote URL (Optional)") },
          placeholder = { Text("https://github.com/user/repo.git") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) onCreate(name.trim(), desc.trim(), git.trim())
        },
        colors = ButtonDefaults.buttonColors(containerColor = LanternNeonGreen, contentColor = Color(0xFF003D1A)),
        modifier = Modifier.testTag("confirm_create_project_button")
      ) {
        Text("Create", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = LanternTextSecondary)
      }
    },
    containerColor = LanternObsidianCard
  )
}
