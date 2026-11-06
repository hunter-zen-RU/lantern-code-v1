package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AiConfig
import com.example.ui.WorkspaceViewModel
import com.example.ui.theme.LanternBrightGreen
import com.example.ui.theme.LanternCyan
import com.example.ui.theme.LanternNeonGreen
import com.example.ui.theme.LanternObsidianCard
import com.example.ui.theme.LanternTextPrimary
import com.example.ui.theme.LanternTextSecondary

@Composable
fun SettingsDialog(
  viewModel: WorkspaceViewModel,
  onDismiss: () -> Unit
) {
  val currentAiConfig = viewModel.aiEngine.getConfig()
  var endpointUrl by remember { mutableStateOf(currentAiConfig.endpointUrl) }
  var apiKey by remember { mutableStateOf(currentAiConfig.apiKey) }
  var modelName by remember { mutableStateOf(currentAiConfig.modelName) }
  var preferLocal by remember { mutableStateOf(currentAiConfig.preferLocalAgent) }

  val currentToken = viewModel.gitManager.getSavedToken()
  var ghToken by remember { mutableStateOf(currentToken) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "LANtern Code Studio Settings",
        fontWeight = FontWeight.Bold,
        color = LanternBrightGreen
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Custom AI Endpoint & Vibecoding",
          fontWeight = FontWeight.Bold,
          color = LanternCyan,
          fontSize = 13.sp
        )

        OutlinedTextField(
          value = endpointUrl,
          onValueChange = { endpointUrl = it },
          label = { Text("API Endpoint URL (Ollama/vLLM/OpenAI)") },
          placeholder = { Text("http://10.0.2.2:11434/v1/chat/completions") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("ai_endpoint_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LanternNeonGreen,
            focusedLabelColor = LanternNeonGreen
          )
        )

        OutlinedTextField(
          value = modelName,
          onValueChange = { modelName = it },
          label = { Text("Model Name") },
          placeholder = { Text("qwen2.5-coder:7b or deepseek-coder") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = apiKey,
          onValueChange = { apiKey = it },
          label = { Text("API Key (Optional for local Ollama)") },
          placeholder = { Text("Bearer sk-...") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Offline Mode Only", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = LanternTextPrimary)
            Text("Always prefer on-device agent runner", fontSize = 10.sp, color = LanternTextSecondary)
          }
          Switch(
            checked = preferLocal,
            onCheckedChange = { preferLocal = it },
            colors = SwitchDefaults.colors(checkedThumbColor = LanternNeonGreen)
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "GitHub Personal Access Token (PAT)",
          fontWeight = FontWeight.Bold,
          color = LanternCyan,
          fontSize = 13.sp
        )

        OutlinedTextField(
          value = ghToken,
          onValueChange = { ghToken = it },
          label = { Text("GitHub Token (PAT with 'repo' scope)") },
          placeholder = { Text("ghp_...") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          viewModel.aiEngine.updateConfig(
            AiConfig(
              endpointUrl = endpointUrl.trim(),
              apiKey = apiKey.trim(),
              modelName = modelName.trim(),
              preferLocalAgent = preferLocal
            )
          )
          if (ghToken.isNotBlank() && ghToken != currentToken) {
            viewModel.connectGitHubKey(ghToken.trim())
          }
          viewModel.showMessage("Settings saved successfully")
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(containerColor = LanternNeonGreen, contentColor = Color(0xFF003D1A)),
        modifier = Modifier.testTag("save_settings_button")
      ) {
        Text("Save", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel", color = LanternTextSecondary) }
    },
    containerColor = LanternObsidianCard
  )
}
