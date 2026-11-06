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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.collab.ChatMessage
import com.example.collab.DiscoveredStudio
import com.example.collab.Peer
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
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val roomState by viewModel.roomState.collectAsStateWithLifecycle()
  val discoveredStudios by viewModel.collabManager.discoveredStudios.collectAsStateWithLifecycle()
  val isScanning by viewModel.collabManager.isScanning.collectAsStateWithLifecycle()

  var showAddPeerDialog by remember { mutableStateOf(false) }
  var showQrModal by remember { mutableStateOf(false) }
  var chatInput by remember { mutableStateOf("") }

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
    item {
      // Room Overview Header Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("collab_room_overview_card"),
        colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(LanternNeonGreen, LanternCyan)))
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "WebRTC P2P Collaboration",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = LanternBrightGreen
                )
              )
              Text(
                text = "Session: ${room?.roomName ?: "Default Mesh"}",
                style = MaterialTheme.typography.bodySmall.copy(color = LanternTextSecondary)
              )
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(LanternNeonGreen.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Icon(Icons.Default.Lock, contentDescription = null, tint = LanternNeonGreen, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("E2E P2P", fontSize = 10.sp, color = LanternNeonGreen, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Room ID badge
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.background)
              .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("ROOM IDENTIFIER", fontSize = 9.sp, color = LanternTextMuted, fontWeight = FontWeight.Bold)
              Text(
                text = room?.roomId ?: "rm-mesh-local",
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = LanternCyan,
                fontWeight = FontWeight.SemiBold
              )
            }

            IconButton(
              onClick = { room?.roomId?.let { copyToClipboard("Room ID", it) } },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(Icons.Default.ContentCopy, contentDescription = "Copy ID", tint = LanternTextSecondary, modifier = Modifier.size(16.dp))
            }
          }
        }
      }
    }

    item {
      // Invite Links Section
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LanternObsidianBorder))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Share, contentDescription = null, tint = LanternNeonGreen, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Team & Browser Invite Links", fontWeight = FontWeight.Bold, color = LanternTextPrimary)
            }

            IconButton(
              onClick = { showQrModal = true },
              modifier = Modifier.size(28.dp).testTag("show_qr_code_button")
            ) {
              Icon(Icons.Default.QrCode2, contentDescription = "Show QR", tint = LanternCyan, modifier = Modifier.size(20.dp))
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          InviteLinkItem(
            title = "LANtern App Invite Protocol",
            subtitle = "Custom deep-link for instant peer join inside LANtern Code",
            link = room?.inviteLink ?: "lantern://join?room=rm-local",
            icon = Icons.Default.Router,
            iconTint = LanternNeonGreen,
            onCopy = { copyToClipboard("App Invite Link", room?.inviteLink ?: "") }
          )

          Spacer(modifier = Modifier.height(8.dp))

          InviteLinkItem(
            title = "Browser LAN Link (Local Wi-Fi)",
            subtitle = "Direct live IDE viewer for laptops and PCs on your Wi-Fi router",
            link = room?.browserLanUrl ?: "http://192.168.1.108:8080/workspace",
            icon = Icons.Default.Wifi,
            iconTint = LanternCyan,
            onCopy = { copyToClipboard("Browser LAN Link", room?.browserLanUrl ?: "") }
          )

          Spacer(modifier = Modifier.height(8.dp))

          InviteLinkItem(
            title = "Browser WAN Relay Link (Remote)",
            subtitle = "Encrypted cloud bridge for remote team members over the Internet",
            link = room?.browserWanUrl ?: "https://lantern.mesh.direct/session",
            icon = Icons.Default.Language,
            iconTint = Color(0xFFA78BFA),
            onCopy = { copyToClipboard("Browser WAN Link", room?.browserWanUrl ?: "") }
          )
        }
      }
    }

    item {
      // Local Network Discovery (mDNS / NSD)
      Card(
        modifier = Modifier.fillMaxWidth().testTag("local_discovery_card"),
        colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LanternBrightGreen.copy(alpha = 0.4f)))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Radar, contentDescription = null, tint = LanternBrightGreen, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text("Wi-Fi Studio Discovery (mDNS/NSD)", fontWeight = FontWeight.Bold, color = LanternTextPrimary, fontSize = 13.sp)
                Text("Auto-scan local subnet for nearby nodes", fontSize = 11.sp, color = LanternTextSecondary)
              }
            }

            Button(
              onClick = { viewModel.collabManager.scanLocalNetwork() },
              colors = ButtonDefaults.buttonColors(containerColor = LanternBrightGreen, contentColor = Color(0xFF003D1A)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.height(28.dp).testTag("scan_lan_button")
            ) {
              if (isScanning) {
                CircularProgressIndicator(modifier = Modifier.size(12.dp), color = Color(0xFF003D1A))
              } else {
                Text("Scan Wi-Fi", fontSize = 10.sp, fontWeight = FontWeight.Bold)
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          discoveredStudios.forEach { studio ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(studio.name, fontWeight = FontWeight.Bold, color = LanternTextPrimary, fontSize = 12.sp)
                Text(
                  text = "${studio.hostIp}:${studio.port} • ${studio.activeMembers} peer(s) • ${studio.latencyMs}ms ping",
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace,
                  color = LanternCyan
                )
              }

              Button(
                onClick = { viewModel.showMessage("Connected to nearby mesh studio: ${studio.name}") },
                colors = ButtonDefaults.buttonColors(containerColor = LanternNeonGreen, contentColor = Color(0xFF003D1A)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.height(24.dp)
              ) {
                Text("Join", fontSize = 10.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }

    item {
      // Team Live Chat & AI Co-Pilot Card
      val messages = room?.messages ?: emptyList()
      Card(
        modifier = Modifier.fillMaxWidth().testTag("team_chat_card"),
        colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Chat, contentDescription = null, tint = LanternCyan, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Team Mesh Chat (${messages.size})", fontWeight = FontWeight.Bold, color = LanternTextPrimary, fontSize = 13.sp)
            }

            Text("@ai responds to questions", fontSize = 10.sp, color = LanternCyan)
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Quick Prompt suggestions
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val chips = listOf("@ai check tests", "@ai summarize git", "@ai mesh health")
            items(chips) { chip ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(LanternCyan.copy(alpha = 0.15f))
                  .clickable {
                    viewModel.collabManager.sendChatMessage(chip)
                  }
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(chip, fontSize = 10.sp, color = LanternCyan)
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Message history list
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.background)
              .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              items(messages) { msg ->
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (msg.isAi) LanternCyan.copy(alpha = 0.12f) else LanternObsidianCard)
                    .padding(8.dp)
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
                      text = if (msg.isAi) "AI Assistant" else "Team",
                      fontSize = 9.sp,
                      color = LanternTextMuted
                    )
                  }
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = msg.text,
                    fontSize = 11.sp,
                    color = LanternTextPrimary
                  )
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
              placeholder = { Text("Message team or ask @ai...", fontSize = 11.sp) },
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
                  viewModel.collabManager.sendChatMessage(chatInput.trim())
                  chatInput = ""
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = LanternCyan, contentColor = Color(0xFF002F48)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.height(48.dp).testTag("send_chat_message_button")
            ) {
              Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
            }
          }
        }
      }
    }

    item {
      // AI Assistant in Team Card
      val hasAi = room?.peers?.any { it.isAi } == true
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LanternCyan.copy(alpha = 0.4f)))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(LanternCyan.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = LanternCyan, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "LANtern AI Co-Pilot",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = LanternTextPrimary
                )
              )
              Text(
                text = if (hasAi) "Connected to Room as Peer Pair Programmer" else "Offline / Not joined",
                style = MaterialTheme.typography.bodySmall.copy(color = LanternTextSecondary)
              )
            }
          }

          Switch(
            checked = hasAi,
            onCheckedChange = { viewModel.collabManager.toggleAiAssistant() },
            colors = SwitchDefaults.colors(
              checkedThumbColor = LanternCyan,
              checkedTrackColor = LanternCyan.copy(alpha = 0.3f)
            ),
            modifier = Modifier.testTag("toggle_ai_team_switch")
          )
        }
      }
    }

    item {
      // Connected Peers Header & Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Active Team Peers (${room?.peers?.size ?: 0})",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = LanternTextPrimary
          )
        )

        Button(
          onClick = { showAddPeerDialog = true },
          colors = ButtonDefaults.buttonColors(containerColor = LanternNeonGreen, contentColor = Color(0xFF003D1A)),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("add_teammate_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Invite Member", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    val peers = room?.peers ?: emptyList()
    items(peers) { peer ->
      PeerCard(
        peer = peer,
        onRemove = { viewModel.collabManager.removePeer(peer.id) }
      )
    }

    item {
      Spacer(modifier = Modifier.height(32.dp))
    }
  }

  if (showAddPeerDialog) {
    AddPeerDialog(
      onDismiss = { showAddPeerDialog = false },
      onAdd = { name, role ->
        viewModel.collabManager.addPeer(name, role, isAi = false)
        showAddPeerDialog = false
      }
    )
  }

  if (showQrModal) {
    QrCodePreviewDialog(
      link = room?.inviteLink ?: "lantern://join?room=mesh",
      onDismiss = { showQrModal = false }
    )
  }
}

@Composable
fun InviteLinkItem(
  title: String,
  subtitle: String,
  link: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  iconTint: Color,
  onCopy: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(MaterialTheme.colorScheme.background)
      .padding(10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(title, fontWeight = FontWeight.SemiBold, color = LanternTextPrimary, fontSize = 12.sp)
        Text(
          text = link,
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp,
          color = iconTint,
          maxLines = 1
        )
      }
    }

    IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
      Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = LanternTextSecondary, modifier = Modifier.size(16.dp))
    }
  }
}

@Composable
fun PeerCard(
  peer: Peer,
  onRemove: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
    shape = RoundedCornerShape(12.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(peer.colorHex).copy(alpha = 0.2f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (peer.isAi) Icons.Default.AutoAwesome else Icons.Default.Person,
            contentDescription = null,
            tint = Color(peer.colorHex),
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = peer.name,
              fontWeight = FontWeight.Bold,
              color = LanternTextPrimary,
              fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(peer.colorHex).copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = peer.role,
                fontSize = 9.sp,
                color = Color(peer.colorHex),
                fontWeight = FontWeight.Bold
              )
            }
          }

          Text(
            text = "Editing: ${peer.activeFile} | Ping: ${peer.pingMs}ms",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = LanternTextMuted
          )
        }
      }

      if (peer.role != "Host") {
        IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
          Icon(Icons.Default.Delete, contentDescription = "Remove peer", tint = LanternTextMuted, modifier = Modifier.size(16.dp))
        }
      }
    }
  }
}

@Composable
fun AddPeerDialog(
  onDismiss: () -> Unit,
  onAdd: (name: String, role: String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var role by remember { mutableStateOf("Core Developer") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add Peer to Workspace", color = LanternTextPrimary, fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Teammate Name / Handle") },
          placeholder = { Text("e.g. Alex (Frontend)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("teammate_name_input")
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf("Core Developer", "Reviewer", "Observer").forEach { r ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (role == r) LanternNeonGreen else LanternObsidianBorder)
                .clickable { role = r }
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = r,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (role == r) Color(0xFF003D1A) else LanternTextPrimary
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { if (name.isNotBlank()) onAdd(name.trim(), role) },
        colors = ButtonDefaults.buttonColors(containerColor = LanternNeonGreen, contentColor = Color(0xFF003D1A)),
        modifier = Modifier.testTag("confirm_add_teammate_button")
      ) {
        Text("Add to Mesh", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel", color = LanternTextSecondary) }
    },
    containerColor = LanternObsidianCard
  )
}

@Composable
fun QrCodePreviewDialog(
  link: String,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Scan to Join Room", color = LanternTextPrimary, fontWeight = FontWeight.Bold) },
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
