package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.git.GitHubRemoteRepo
import com.example.ui.WorkspaceViewModel
import com.example.ui.theme.CodeEditorBg
import com.example.ui.theme.LanternBrightGreen
import com.example.ui.theme.LanternCyan
import com.example.ui.theme.LanternNeonGreen
import com.example.ui.theme.LanternObsidianBorder
import com.example.ui.theme.LanternObsidianCard
import com.example.ui.theme.LanternTextMuted
import com.example.ui.theme.LanternTextPrimary
import com.example.ui.theme.LanternTextSecondary

@Composable
fun GitHubManagerDialog(
  viewModel: WorkspaceViewModel,
  onDismiss: () -> Unit,
  onRepoSelected: () -> Unit
) {
  val profile by viewModel.gitHubProfile.collectAsStateWithLifecycle()
  val repos by viewModel.gitHubRepos.collectAsStateWithLifecycle()
  val isLoading by viewModel.isGitLoading.collectAsStateWithLifecycle()

  var tokenInput by remember { mutableStateOf("") }
  var searchQuery by remember { mutableStateOf("") }
  var showCreateRepoDialog by remember { mutableStateOf(false) }

  val filteredRepos = repos.filter {
    searchQuery.isBlank() ||
      it.name.contains(searchQuery, ignoreCase = true) ||
      it.description.contains(searchQuery, ignoreCase = true) ||
      it.language.contains(searchQuery, ignoreCase = true)
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Key, contentDescription = null, tint = LanternBrightGreen, modifier = Modifier.size(22.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "GitHub Repository Manager",
            fontWeight = FontWeight.Bold,
            color = LanternTextPrimary,
            fontSize = 15.sp
          )
        }

        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = LanternTextMuted, modifier = Modifier.size(16.dp))
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        if (!profile.isAuthenticated) {
          // Unauthenticated State: Request GitHub API Key / PAT
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CodeEditorBg),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LanternCyan))
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "Connect with GitHub Personal Access Token",
                fontWeight = FontWeight.Bold,
                color = LanternCyan,
                fontSize = 13.sp
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Provide your GitHub API token to manage public/private repositories, create new remotes, and push code directly from Android.",
                fontSize = 11.sp,
                color = LanternTextSecondary
              )
              Spacer(modifier = Modifier.height(10.dp))

              OutlinedTextField(
                value = tokenInput,
                onValueChange = { tokenInput = it },
                label = { Text("Personal Access Token (PAT)") },
                placeholder = { Text("ghp_... or github_pat_...") },
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("github_token_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = LanternNeonGreen,
                  focusedTextColor = LanternTextPrimary
                )
              )

              Spacer(modifier = Modifier.height(6.dp))

              Text(
                text = "Requires 'repo' scope at github.com/settings/tokens",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = LanternTextMuted
              )

              Spacer(modifier = Modifier.height(10.dp))

              Button(
                onClick = {
                  if (tokenInput.isNotBlank()) {
                    viewModel.connectGitHubKey(tokenInput.trim())
                  }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = LanternNeonGreen, contentColor = Color(0xFF003D1A)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("connect_github_token_button")
              ) {
                if (isLoading) {
                  CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF003D1A))
                } else {
                  Text("Authenticate & Load Repositories", fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        } else {
          // Authenticated State: User Profile Card
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CodeEditorBg),
            shape = RoundedCornerShape(12.dp)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(LanternCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = profile.login.take(2).uppercase(),
                      fontWeight = FontWeight.Bold,
                      color = LanternCyan,
                      fontSize = 14.sp
                    )
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = "@${profile.login}",
                      fontWeight = FontWeight.Bold,
                      color = LanternBrightGreen,
                      fontSize = 13.sp
                    )
                    Text(
                      text = "${profile.publicRepos} Public • ${profile.totalPrivateRepos} Private",
                      fontSize = 10.sp,
                      color = LanternTextMuted
                    )
                  }
                }

                TextButton(onClick = { viewModel.disconnectGitHub() }) {
                  Text("Disconnect", color = Color(0xFFEF4444), fontSize = 11.sp)
                }
              }

              if (profile.scopes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  profile.scopes.take(3).forEach { scope ->
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(LanternCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                      Text(scope, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = LanternCyan)
                    }
                  }
                }
              }
            }
          }

          // Action Toolbar: Create Repo on GitHub + Refresh
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Button(
              onClick = { showCreateRepoDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = LanternNeonGreen, contentColor = Color(0xFF003D1A)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.height(32.dp).testTag("create_remote_repo_button")
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("New Repo on GitHub", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            IconButton(
              onClick = { viewModel.refreshGitHubRepos() },
              modifier = Modifier.size(32.dp).testTag("refresh_github_repos_button")
            ) {
              Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = LanternCyan, modifier = Modifier.size(18.dp))
            }
          }

          // Search Repositories
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter your repositories...", fontSize = 11.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LanternTextMuted, modifier = Modifier.size(16.dp)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().height(46.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = LanternCyan,
              focusedContainerColor = CodeEditorBg,
              unfocusedContainerColor = CodeEditorBg
            )
          )

          // Repositories List
          Text("Your Repositories (${filteredRepos.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LanternTextPrimary)

          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .height(240.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            items(filteredRepos) { repo ->
              Card(
                modifier = Modifier.fillMaxWidth().testTag("repo_item_${repo.id}"),
                colors = CardDefaults.cardColors(containerColor = CodeEditorBg),
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LanternObsidianBorder))
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.weight(1f)
                    ) {
                      Icon(
                        imageVector = if (repo.isPrivate) Icons.Default.Lock else Icons.Default.Public,
                        contentDescription = null,
                        tint = if (repo.isPrivate) Color(0xFFF59E0B) else LanternCyan,
                        modifier = Modifier.size(14.dp)
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = repo.name,
                        fontWeight = FontWeight.Bold,
                        color = LanternTextPrimary,
                        fontSize = 12.sp,
                        maxLines = 1
                      )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(12.dp))
                      Spacer(modifier = Modifier.width(2.dp))
                      Text("${repo.starsCount}", fontSize = 10.sp, color = LanternTextMuted)
                    }
                  }

                  if (repo.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = repo.description,
                      fontSize = 10.sp,
                      color = LanternTextSecondary,
                      maxLines = 2
                    )
                  }

                  Spacer(modifier = Modifier.height(6.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "${repo.language} • ${repo.defaultBranch}",
                      fontSize = 10.sp,
                      fontFamily = FontFamily.Monospace,
                      color = LanternTextMuted
                    )

                    Button(
                      onClick = {
                        viewModel.importGitHubRepo(repo) {
                          onRepoSelected()
                        }
                      },
                      colors = ButtonDefaults.buttonColors(containerColor = LanternNeonGreen, contentColor = Color(0xFF003D1A)),
                      shape = RoundedCornerShape(6.dp),
                      modifier = Modifier.height(24.dp).testTag("import_repo_button_${repo.id}")
                    ) {
                      Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(12.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("Import / Sync", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Done", color = LanternTextSecondary)
      }
    },
    containerColor = LanternObsidianCard
  )

  if (showCreateRepoDialog) {
    CreateGitHubRemoteRepoDialog(
      onDismiss = { showCreateRepoDialog = false },
      onCreate = { name, desc, isPriv ->
        viewModel.createGitHubRemoteRepo(name, desc, isPriv) {
          showCreateRepoDialog = false
          onRepoSelected()
        }
      }
    )
  }
}

@Composable
fun CreateGitHubRemoteRepoDialog(
  onDismiss: () -> Unit,
  onCreate: (name: String, desc: String, isPrivate: Boolean) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var desc by remember { mutableStateOf("") }
  var isPrivate by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text("Create Remote Repository on GitHub", color = LanternTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Repository Name") },
          placeholder = { Text("e.g. mobile-mesh-service") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("new_remote_repo_name_field")
        )

        OutlinedTextField(
          value = desc,
          onValueChange = { desc = it },
          label = { Text("Description") },
          placeholder = { Text("Repository description on GitHub") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(if (isPrivate) "Private Repository 🔒" else "Public Repository 🌐", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = LanternTextPrimary)
            Text(if (isPrivate) "Only you and collaborators can view" else "Anyone on GitHub can see this repo", fontSize = 10.sp, color = LanternTextSecondary)
          }

          Switch(
            checked = isPrivate,
            onCheckedChange = { isPrivate = it },
            colors = SwitchDefaults.colors(checkedThumbColor = LanternNeonGreen)
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) onCreate(name.trim(), desc.trim(), isPrivate)
        },
        colors = ButtonDefaults.buttonColors(containerColor = LanternNeonGreen, contentColor = Color(0xFF003D1A)),
        modifier = Modifier.testTag("confirm_create_remote_repo_button")
      ) {
        Text("Create on GitHub", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel", color = LanternTextSecondary) }
    },
    containerColor = LanternObsidianCard
  )
}
