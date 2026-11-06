package com.example.collab

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class Peer(
  val id: String,
  val name: String,
  val role: String, // "Host", "Core Developer", "Reviewer", "AI Assistant"
  val isAi: Boolean = false,
  val cursorLine: Int = 1,
  val cursorCol: Int = 1,
  val activeFile: String = "src/Main.kt",
  val colorHex: Long = 0xFF00E676,
  val isConnected: Boolean = true,
  val pingMs: Long = 14
)

data class ChatMessage(
  val id: String,
  val senderName: String,
  val text: String,
  val timestamp: Long = System.currentTimeMillis(),
  val isAi: Boolean = false,
  val colorHex: Long = 0xFF00E676
)

data class DiscoveredStudio(
  val studioId: String,
  val name: String,
  val hostIp: String,
  val port: Int,
  val activeMembers: Int,
  val latencyMs: Long
)

data class WebRtcRoom(
  val roomId: String,
  val roomName: String,
  val inviteLink: String,
  val browserLanUrl: String,
  val browserWanUrl: String,
  val isHosting: Boolean,
  val isEncrypted: Boolean = true,
  val peers: List<Peer> = emptyList(),
  val messages: List<ChatMessage> = emptyList()
)

class WebRtcCollaborationManager {
  private val _roomState = MutableStateFlow<WebRtcRoom?>(null)
  val roomState: StateFlow<WebRtcRoom?> = _roomState.asStateFlow()

  private val _discoveredStudios = MutableStateFlow<List<DiscoveredStudio>>(emptyList())
  val discoveredStudios: StateFlow<List<DiscoveredStudio>> = _discoveredStudios.asStateFlow()

  private val _isScanning = MutableStateFlow(false)
  val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

  init {
    // Populate starter discovery studios
    _discoveredStudios.value = listOf(
      DiscoveredStudio("nsd-alpha", "Hackathon-Pod-North", "192.168.1.114", 8080, 3, 12),
      DiscoveredStudio("nsd-beta", "PairStation-Alpha", "192.168.1.152", 9090, 2, 8)
    )
  }

  fun createRoom(roomName: String, hostName: String = "Mobile Host"): WebRtcRoom {
    val roomId = "rm-" + UUID.randomUUID().toString().take(8)
    val inviteLink = "lantern://join?room=$roomId&host=${hostName.replace(" ", "%20")}"
    val lanUrl = "http://192.168.1.108:8080/workspace/$roomId"
    val wanUrl = "https://lantern.mesh.direct/session/$roomId"

    val hostPeer = Peer(
      id = "host-0",
      name = hostName,
      role = "Host",
      isAi = false,
      cursorLine = 12,
      cursorCol = 5,
      activeFile = "src/Main.kt",
      colorHex = 0xFF00E676
    )

    val aiPeer = Peer(
      id = "ai-agent-0",
      name = "LANtern AI Co-Pilot",
      role = "AI Assistant",
      isAi = true,
      cursorLine = 15,
      cursorCol = 1,
      activeFile = "src/Main.kt",
      colorHex = 0xFF38BDF8
    )

    val welcomeMsg = ChatMessage(
      id = "msg-init",
      senderName = "LANtern AI Co-Pilot",
      text = "Mesh session initialized. WebRTC data channels active. Ready to pair program!",
      isAi = true,
      colorHex = 0xFF38BDF8
    )

    val room = WebRtcRoom(
      roomId = roomId,
      roomName = roomName,
      inviteLink = inviteLink,
      browserLanUrl = lanUrl,
      browserWanUrl = wanUrl,
      isHosting = true,
      peers = listOf(hostPeer, aiPeer),
      messages = listOf(welcomeMsg)
    )

    _roomState.value = room
    return room
  }

  fun sendChatMessage(text: String, senderName: String = "Mobile Host") {
    val current = _roomState.value ?: return
    val userMsg = ChatMessage(
      id = "msg-${System.currentTimeMillis()}",
      senderName = senderName,
      text = text,
      isAi = false,
      colorHex = 0xFF00E676
    )

    val updatedMessages = current.messages + userMsg
    _roomState.value = current.copy(messages = updatedMessages)

    // AI Co-Pilot automatic response if addressed or asking assistance
    val lower = text.lowercase()
    if (lower.contains("@ai") || lower.contains("help") || lower.contains("how") || lower.contains("check") || lower.contains("test")) {
      val aiReply = ChatMessage(
        id = "msg-ai-${System.currentTimeMillis()}",
        senderName = "LANtern AI Co-Pilot",
        text = when {
          lower.contains("test") -> "I've reviewed the test suite in tests/MeshUnitTest.kt. All delta assertions pass under 45ms latency."
          lower.contains("git") || lower.contains("commit") -> "Working tree is clean. Ready to push to origin/main or create a PR branch."
          lower.contains("sql") || lower.contains("db") -> "Local SQLite cache schema is validated in Room WAL mode."
          else -> "Acknowledged! Synchronizing AST state across peer data channels."
        },
        isAi = true,
        colorHex = 0xFF38BDF8
      )
      _roomState.value = _roomState.value?.copy(messages = updatedMessages + aiReply)
    }
  }

  fun scanLocalNetwork() {
    _isScanning.value = true
    _discoveredStudios.value = listOf(
      DiscoveredStudio("nsd-alpha", "Hackathon-Pod-North", "192.168.1.114", 8080, 3, 11),
      DiscoveredStudio("nsd-beta", "PairStation-Alpha", "192.168.1.152", 9090, 2, 7),
      DiscoveredStudio("nsd-gamma", "IoT-Edge-Relay", "192.168.1.189", 8000, 1, 16)
    )
    _isScanning.value = false
  }

  fun addPeer(name: String, role: String, isAi: Boolean = false) {
    val current = _roomState.value ?: return
    val newPeer = Peer(
      id = "peer-${System.currentTimeMillis() % 1000}",
      name = name,
      role = role,
      isAi = isAi,
      cursorLine = (1..25).random(),
      cursorCol = (1..20).random(),
      activeFile = "src/Main.kt",
      colorHex = if (isAi) 0xFF38BDF8 else listOf(0xFFF59E0B, 0xFFE879F9, 0xFF34D399, 0xFFA78BFA).random()
    )
    val joinMsg = ChatMessage(
      id = "msg-join-${System.currentTimeMillis()}",
      senderName = "System",
      text = "$name joined the workspace ($role)",
      isAi = false,
      colorHex = 0xFF94A3B8
    )
    _roomState.value = current.copy(
      peers = current.peers + newPeer,
      messages = current.messages + joinMsg
    )
  }

  fun removePeer(peerId: String) {
    val current = _roomState.value ?: return
    _roomState.value = current.copy(peers = current.peers.filterNot { it.id == peerId })
  }

  fun toggleAiAssistant() {
    val current = _roomState.value ?: return
    val hasAi = current.peers.any { it.isAi }
    if (hasAi) {
      _roomState.value = current.copy(peers = current.peers.filterNot { it.isAi })
    } else {
      addPeer("LANtern AI Co-Pilot", "AI Assistant", isAi = true)
    }
  }

  fun updateMyCursor(line: Int, col: Int, file: String) {
    val current = _roomState.value ?: return
    val updated = current.peers.map { peer ->
      if (peer.role == "Host") {
        peer.copy(cursorLine = line, cursorCol = col, activeFile = file)
      } else peer
    }
    _roomState.value = current.copy(peers = updated)
  }

  fun leaveRoom() {
    _roomState.value = null
  }
}
