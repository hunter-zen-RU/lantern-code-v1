package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.collab.ChatMessage
import com.example.collab.CodeReference
import com.example.collab.DiscoveredStudio
import com.example.collab.Peer
import com.example.data.entity.GitDiffEntity
import com.example.data.entity.ProjectFileEntity
import com.example.ui.WorkspaceViewModel
import com.example.ui.theme.LanternBrightGreen
import com.example.ui.theme.LanternCyan
import com.example.ui.theme.LanternNeonGreen
import com.example.ui.theme.LanternObsidianBorder
import com.example.ui.theme.LanternObsidianCard
import com.example.ui.theme.LanternTextMuted
import com.example.ui.theme.LanternTextPrimary
import com.example.ui.theme.LanternTextSecondary

@Composable
fun CollaborationScreen(
  viewModel: WorkspaceViewModel,
  onNavigateToEditor: (filePath: String, lineNumber: Int) -> Unit = { _, _ -> },
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val roomState by viewModel.roomState.collectAsStateWithLifecycle()
  val discoveredStudios by viewModel.collabManager.discoveredStudios.collectAsStateWithLifecycle()
  val isScanning by viewModel.collabManager.isScanning.collectAsStateWithLifecycle()

  val isServerRunning by viewModel.isServerRunning.collectAsStateWithLifecycle()
  val serverUrl by viewModel.serverUrl.collectAsStateWithLifecycle()
  val pairingCode by viewModel.pairingCode.collectAsStateWithLifecycle()
  val projectFiles by viewModel.projectFiles.collectAsStateWithLifecycle()
  val activeFile by viewModel.activeFile.collectAsStateWithLifecycle()
  val selectedProject by viewModel.selectedProject.collectAsStateWithLifecycle()
  val diffRecords by viewModel.diffRecords.collectAsStateWithLifecycle()

  var showAddPeerDialog by remember { mutableStateOf(false) }
  var showQrModal by remember { mutableStateOf(false) }
  var showJoinPairingDialog by remember { mutableStateOf(false) }
  var showAttachRefDialog by remember { mutableStateOf(false) }
  var showDiffsModal by remember { mutableStateOf(false) }
  var selectedDiffForPreview by remember { mutableStateOf<GitDiffEntity?>(null) }

  var chatInput by remember { mutableStateOf("") }
  var attachedCodeRef by remember { mutableStateOf<CodeReference?>(null) }

  val room = roomState

  fun copyToClipboard(label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    viewModel.showMessage("Copied $label to clipboard")
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. GLOBAL TEAM PAIRING CODE CARD
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("global_pairing_card"),
        colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.horizontalGradient(listOf(LanternNeonGreen, LanternCyan))
        )
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
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
                  .background(LanternNeonGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Link, contentDescription = null, tint = LanternNeonGreen, modifier = Modifier.size(20.dp))
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Global Team Connection",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = LanternBrightGreen
                  )
                )
                Text(
                  text = "WebRTC & Firebase Signaling Channel",
                  style = MaterialTheme.typography.bodySmall.copy(color = LanternTextSecondary)
                )
              }
            }

            Button(
              onClick = { showJoinPairingDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = LanternCyan, contentColor = Color(0xFF00293D)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.height(30.dp).testTag("join_with_pairing_code_button")
            ) {
              Text("Join Pair", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Pairing Code display
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.background)
              .border(1.dp, LanternNeonGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
              .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("TEAM PAIRING CODE", fontSize = 9.sp, color = LanternTextMuted, fontWeight = FontWeight.Bold)
              Text(
                text = pairingCode,
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LanternNeonGreen,
                letterSpacing = 1.sp
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                onClick = { copyToClipboard("Pairing Code", pairingCode) },
                colors = ButtonDefaults.buttonColors(containerColor = LanternObsidianCard),
                shape = RoundedCornerShape(8.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(LanternNeonGreen, LanternCyan))),
                modifier = Modifier.height(32.dp).testTag("copy_pairing_code_button")
              ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = LanternNeonGreen, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy Code", fontSize = 11.sp, color = LanternTextPrimary)
              }

              IconButton(
                onClick = { showQrModal = true },
                modifier = Modifier.size(32.dp).testTag("show_qr_pairing_button")
              ) {
                Icon(Icons.Default.QrCode2, contentDescription = "QR Code", tint = LanternCyan, modifier = Modifier.size(20.dp))
              }
            }
          }
        }
      }
    }

    // 2. NANOHTTPD LOCAL SERVER & WEB CODE STUDIO CARD
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("nanohttpd_server_card"),
        colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.horizontalGradient(
            if (isServerRunning) listOf(LanternNeonGreen, LanternCyan) else listOf(LanternObsidianBorder, LanternObsidianBorder)
          )
        )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                  .background(if (isServerRunning) LanternNeonGreen.copy(alpha = 0.2f) else Color.DarkGray),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.Language,
                  contentDescription = null,
                  tint = if (isServerRunning) LanternNeonGreen else LanternTextMuted,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "NanoHTTPD Web Code Studio",
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = LanternTextPrimary
                    )
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(if (isServerRunning) LanternNeonGreen.copy(alpha = 0.2f) else Color.DarkGray)
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = if (isServerRunning) "LIVE" else "OFFLINE",
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (isServerRunning) LanternNeonGreen else LanternTextMuted
                    )
                  }
                }
                Text(
                  text = "Live coding from any local LAN web browser",
                  style = MaterialTheme.typography.bodySmall.copy(color = LanternTextSecondary)
                )
              }
            }

            Button(
              onClick = { viewModel.toggleLocalWebServer() },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isServerRunning) Color(0xFFEF4444) else LanternNeonGreen,
                contentColor = if (isServerRunning) Color.White else Color(0xFF003D1A)
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.height(30.dp).testTag("toggle_nanohttpd_button")
            ) {
              Icon(
                if (isServerRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isServerRunning) "Stop" else "Start Server",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Open on your desktop PC, Mac, or tablet on the same Wi-Fi to code live into this mobile project with synchronized tabs and line references:",
            fontSize = 11.sp,
            color = LanternTextMuted
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.background)
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("LOCAL LAN WEB URL", fontSize = 9.sp, color = LanternTextMuted, fontWeight = FontWeight.Bold)
              Text(
                text = serverUrl,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = if (isServerRunning) LanternCyan else LanternTextMuted,
                fontWeight = FontWeight.SemiBold
              )
            }

            IconButton(
              onClick = { copyToClipboard("LAN Studio URL", serverUrl) },
              modifier = Modifier.size(28.dp).testTag("copy_lan_url_button")
            ) {
              Icon(Icons.Default.ContentCopy, contentDescription = "Copy URL", tint = LanternCyan, modifier = Modifier.size(16.dp))
            }
          }
        }
      }
    }

    // 3. DISK STORAGE & DIFF/BLAME TRACKING CARD
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("disk_storage_card"),
        colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(LanternCyan, LanternNeonGreen)))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Storage, contentDescription = null, tint = LanternCyan, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text("Project Files & Diff Storage", fontWeight = FontWeight.Bold, color = LanternTextPrimary)
                Text("home/LANtern/projects/ & home/LANtern/.diffs/", fontSize = 11.sp, color = LanternTextSecondary)
              }
            }

            Button(
              onClick = { viewModel.exportProjectToDisk() },
              colors = ButtonDefaults.buttonColors(containerColor = LanternNeonGreen, contentColor = Color(0xFF003D1A)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.height(28.dp).testTag("export_project_button")
            ) {
              Text("Sync Disk", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Diff status
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.background)
              .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("SAVED DATABASE DIFFS", fontSize = 9.sp, color = LanternTextMuted, fontWeight = FontWeight.Bold)
              Text(
                text = "${diffRecords.size} diff records logged in SQLite",
                fontSize = 12.sp,
                color = LanternBrightGreen,
                fontWeight = FontWeight.SemiBold
              )
            }

            if (diffRecords.isNotEmpty()) {
              Button(
                onClick = { showDiffsModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = LanternObsidianCard),
                shape = RoundedCornerShape(6.dp),
                border = ButtonDefaults.outlinedButtonBorder,
                modifier = Modifier.height(26.dp)
              ) {
                Text("Inspect Diffs", fontSize = 10.sp, color = LanternCyan)
              }
            }
          }
        }
      }
    }

    // 4. TEAM CHAT SYSTEM WITH CODE REFERENCING
    item {
      val messages = room?.messages ?: emptyList()
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("team_chat_system_card"),
        colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(LanternNeonGreen, LanternCyan)))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = LanternCyan, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Team Chat & Code References (${messages.size})", fontWeight = FontWeight.Bold, color = LanternTextPrimary, fontSize = 13.sp)
            }

            Text("@ai available", fontSize = 10.sp, color = LanternCyan)
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Message history list
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .height(240.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.background)
              .padding(8.dp)
          ) {
            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              items(messages) { msg ->
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (msg.isAi) LanternCyan.copy(alpha = 0.12f) else LanternObsidianCard)
                    .padding(10.dp)
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(
                      text = msg.senderName,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(msg.colorHex)
                    )
                    Text(
                      text = if (msg.isAi) "AI Assistant" else "Team Member",
                      fontSize = 9.sp,
                      color = LanternTextMuted
                    )
                  }

                  Spacer(modifier = Modifier.height(4.dp))

                  Text(
                    text = msg.text,
                    fontSize = 12.sp,
                    color = LanternTextPrimary
                  )

                  // INTERACTIVE CODE REFERENCE CARD
                  if (msg.codeReference != null) {
                    val ref = msg.codeReference
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF090D14))
                        .border(1.dp, LanternNeonGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .clickable {
                          viewModel.navigateToCodeReference(ref.filePath, ref.lineNumber)
                          onNavigateToEditor(ref.filePath, ref.lineNumber)
                        }
                        .padding(8.dp)
                        .testTag("code_reference_card_${ref.fileName}_${ref.lineNumber}")
                    ) {
                      Column {
                        Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.SpaceBetween,
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = LanternNeonGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                              text = "${ref.fileName} : Line ${ref.lineNumber}",
                              fontSize = 11.sp,
                              fontFamily = FontFamily.Monospace,
                              fontWeight = FontWeight.Bold,
                              color = LanternNeonGreen
                            )
                          }

                          Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Jump to Code", fontSize = 10.sp, color = LanternCyan, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = LanternCyan, modifier = Modifier.size(12.dp))
                          }
                        }

                        if (ref.lineSnippet.isNotBlank()) {
                          Spacer(modifier = Modifier.height(4.dp))
                          Text(
                            text = ref.lineSnippet,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFFE2E8F0),
                            modifier = Modifier
                              .fillMaxWidth()
                              .clip(RoundedCornerShape(4.dp))
                              .background(Color.Black)
                              .padding(4.dp),
                            maxLines = 2
                          )
                        }
                      }
                    }
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Attached Code Reference Chip Preview (if user selected one to send)
          if (attachedCodeRef != null) {
            val ref = attachedCodeRef!!
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(LanternNeonGreen.copy(alpha = 0.15f))
                .border(1.dp, LanternNeonGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PinDrop, contentDescription = null, tint = LanternNeonGreen, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Ref: ${ref.fileName}:${ref.lineNumber}",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = LanternNeonGreen,
                  fontFamily = FontFamily.Monospace
                )
                if (ref.lineSnippet.isNotBlank()) {
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "\"${ref.lineSnippet.take(30)}\"",
                    fontSize = 10.sp,
                    color = LanternTextMuted,
                    maxLines = 1
                  )
                }
              }

              IconButton(
                onClick = { attachedCodeRef = null },
                modifier = Modifier.size(20.dp)
              ) {
                Icon(Icons.Default.Close, contentDescription = "Remove Reference", tint = LanternTextMuted, modifier = Modifier.size(12.dp))
              }
            }
            Spacer(modifier = Modifier.height(6.dp))
          }

          // Action row: "+ Reference Code Line" button & Quick prompts
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Button(
              onClick = { showAttachRefDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = LanternCyan.copy(alpha = 0.15f)),
              shape = RoundedCornerShape(6.dp),
              border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(LanternCyan, LanternNeonGreen))),
              modifier = Modifier.height(28.dp).testTag("attach_code_reference_button")
            ) {
              Icon(Icons.Default.Add, contentDescription = null, tint = LanternCyan, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (attachedCodeRef == null) "+ Reference Code Line" else "Change Ref",
                fontSize = 10.sp,
                color = LanternCyan,
                fontWeight = FontWeight.Bold
              )
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              val chips = listOf("@ai check tests", "@ai summarize git")
              items(chips) { chip ->
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(LanternCyan.copy(alpha = 0.1f))
                    .clickable { viewModel.collabManager.sendChatMessage(chip) }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(chip, fontSize = 9.sp, color = LanternCyan)
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Chat Input Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = chatInput,
              onValueChange = { chatInput = it },
              placeholder = { Text("Message team (e.g. check line 46)...", fontSize = 11.sp) },
              modifier = Modifier.weight(1f).height(48.dp).testTag("team_chat_input_field"),
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LanternCyan,
                focusedTextColor = LanternTextPrimary
              )
            )

            Spacer(modifier = Modifier.width(6.dp))

            Button(
              onClick = {
                if (chatInput.isNotBlank()) {
                  val ref = attachedCodeRef
                  viewModel.sendChatMessageWithReference(
                    text = chatInput.trim(),
                    senderName = "Mobile Dev",
                    file = if (ref != null) projectFiles.firstOrNull { it.path == ref.filePath } else null,
                    lineNumber = ref?.lineNumber ?: 0,
                    snippet = ref?.lineSnippet ?: ""
                  )
                  chatInput = ""
                  attachedCodeRef = null
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = LanternCyan, contentColor = Color(0xFF002F48)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.height(48.dp).testTag("send_team_chat_button")
            ) {
              Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
            }
          }
        }
      }
    }
  }

  // DIALOGS

  // 1. Join With Pairing Code Dialog
  if (showJoinPairingDialog) {
    var inputCode by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { showJoinPairingDialog = false },
      title = { Text("Connect to Global Team", color = LanternTextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Enter the 6-8 character team pairing code (e.g., LANT-8821):", fontSize = 12.sp, color = LanternTextSecondary)
          OutlinedTextField(
            value = inputCode,
            onValueChange = { inputCode = it.uppercase() },
            placeholder = { Text("LANT-XXXX") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("pairing_code_input_field"),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LanternNeonGreen)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (inputCode.isNotBlank()) {
              viewModel.connectWithPairingCode(inputCode)
              showJoinPairingDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = LanternNeonGreen, contentColor = Color(0xFF003D1A)),
          modifier = Modifier.testTag("confirm_join_pair_button")
        ) {
          Text("Connect", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showJoinPairingDialog = false }) { Text("Cancel") }
      },
      containerColor = LanternObsidianCard
    )
  }

  // 2. Attach Code Reference Dialog
  if (showAttachRefDialog) {
    var selectedFile by remember { mutableStateOf(activeFile ?: projectFiles.firstOrNull()) }
    var selectedLine by remember { mutableIntStateOf(1) }
    val fileLines = selectedFile?.content?.lines() ?: emptyList()

    AlertDialog(
      onDismissRequest = { showAttachRefDialog = false },
      title = { Text("Reference Code in Chat", color = LanternTextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Pick file and line to reference in team message:", fontSize = 12.sp, color = LanternTextSecondary)

          // File selector pills
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(projectFiles) { f ->
              val isSel = f.id == selectedFile?.id
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (isSel) LanternCyan.copy(alpha = 0.2f) else LanternObsidianBorder)
                  .border(1.dp, if (isSel) LanternCyan else Color.Transparent, RoundedCornerShape(6.dp))
                  .clickable {
                    selectedFile = f
                    selectedLine = 1
                  }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(f.name, fontSize = 11.sp, color = if (isSel) LanternCyan else LanternTextSecondary)
              }
            }
          }

          // Line number input
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Line Number:", fontSize = 12.sp, color = LanternTextMuted)
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
              value = selectedLine.toString(),
              onValueChange = { str ->
                val num = str.filter { it.isDigit() }.toIntOrNull() ?: 1
                selectedLine = num.coerceIn(1, maxOf(1, fileLines.size))
              },
              singleLine = true,
              modifier = Modifier.width(80.dp).height(48.dp).testTag("ref_line_number_input")
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("of ${fileLines.size}", fontSize = 11.sp, color = LanternTextMuted)
          }

          // Snippet preview of that exact line
          val snippet = fileLines.getOrNull(selectedLine - 1)?.trim() ?: "(Empty line)"
          Text("Line content preview:", fontSize = 11.sp, color = LanternTextMuted)
          Text(
            text = snippet,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = LanternBrightGreen,
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(Color.Black)
              .padding(8.dp)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (selectedFile != null) {
              val snippet = fileLines.getOrNull(selectedLine - 1)?.trim() ?: ""
              attachedCodeRef = CodeReference(
                filePath = selectedFile!!.path,
                fileName = selectedFile!!.name,
                lineNumber = selectedLine,
                lineSnippet = snippet
              )
              showAttachRefDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = LanternNeonGreen, contentColor = Color(0xFF003D1A)),
          modifier = Modifier.testTag("confirm_attach_ref_button")
        ) {
          Text("Attach to Message", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showAttachRefDialog = false }) { Text("Cancel") }
      },
      containerColor = LanternObsidianCard
    )
  }

  // 3. Diffs Modal
  if (showDiffsModal) {
    AlertDialog(
      onDismissRequest = { showDiffsModal = false },
      title = { Text("Database Recorded Diffs", color = LanternTextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth().height(260.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(diffRecords) { d ->
              Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { selectedDiffForPreview = d }
              ) {
                Column(modifier = Modifier.padding(8.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(d.filePath, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = LanternCyan)
                    Text(d.summary, fontSize = 11.sp, color = LanternNeonGreen)
                  }
                  Text("Disk: ${d.diskPath}", fontSize = 9.sp, color = LanternTextMuted, fontFamily = FontFamily.Monospace)
                }
              }
            }
          }
        }
      },
      confirmButton = {
        Button(onClick = { showDiffsModal = false }) { Text("Close") }
      },
      containerColor = LanternObsidianCard
    )
  }

  // 4. QR Code Dialog
  if (showQrModal) {
    QrCodePreviewDialog(
      link = serverUrl,
      onDismiss = { showQrModal = false }
    )
  }
}

@Composable
fun QrCodePreviewDialog(
  link: String,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Scan to Join / Open Web IDE", color = LanternTextPrimary, fontWeight = FontWeight.Bold) },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Box(
          modifier = Modifier
            .size(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.QrCode2,
            contentDescription = "QR Code graphic",
            tint = Color.Black,
            modifier = Modifier.fillMaxSize()
          )
        }

        Text(
          text = link,
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp,
          color = LanternCyan,
          maxLines = 2
        )

        Text(
          text = "Teammates on the same Wi-Fi router can scan this QR code or navigate to the LAN browser URL to edit concurrently.",
          fontSize = 11.sp,
          color = LanternTextSecondary
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = LanternNeonGreen, contentColor = Color(0xFF003D1A))
      ) {
        Text("Done", fontWeight = FontWeight.Bold)
      }
    },
    containerColor = LanternObsidianCard
  )
}
