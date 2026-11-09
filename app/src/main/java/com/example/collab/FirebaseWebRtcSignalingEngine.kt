package com.example.collab

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class CodeReference(
  val filePath: String,
  val fileName: String,
  val lineNumber: Int,
  val lineSnippet: String = ""
)

data class TeamChatMessageModel(
  val id: String = "",
  val senderId: String = "",
  val senderName: String = "",
  val text: String = "",
  val codeReference: CodeReference? = null,
  val timestamp: Long = System.currentTimeMillis()
)

data class RoomPeerModel(
  val peerUid: String = "",
  val peerName: String = "",
  val role: String = "Editor",
  val cursorLine: Int = 1,
  val cursorCol: Int = 1,
  val status: String = "online",
  val lastPing: Long = System.currentTimeMillis()
)

data class IceCandidateModel(
  val senderUid: String = "",
  val candidate: String = "",
  val sdpMid: String = "",
  val sdpMLineIndex: Int = 0
)

class FirebaseWebRtcSignalingEngine(private val context: Context) {
  private val tag = "SignalingEngine"

  private val db: FirebaseFirestore by lazy {
    val dbId = try {
      context.getString(R.string.firestore_database_id)
    } catch (_: Exception) {
      "ai-studio-android-lanternc-5aa4f5e1-abd8-4d62-9242-60da1ceccf9d"
    }
    FirebaseFirestore.getInstance(dbId)
  }

  fun generatePairingCode(): String {
    val prefix = listOf("LANT", "CODE", "MESH", "PEER", "DEV").random()
    val digits = (1000..9999).random()
    return "$prefix-$digits"
  }

  suspend fun createRoom(
    roomName: String,
    hostName: String,
    fileName: String = "main.kt",
    fileContent: String = ""
  ): Pair<String, String> {
    val roomId = "rm-" + UUID.randomUUID().toString().take(8)
    val pairingCode = generatePairingCode()
    val uid = Firebase.auth.currentUser?.uid ?: "local-host-${UUID.randomUUID().toString().take(4)}"

    val roomData = hashMapOf(
      "roomId" to roomId,
      "name" to roomName,
      "hostUid" to uid,
      "hostName" to hostName,
      "status" to "active",
      "pairingCode" to pairingCode,
      "activeFileName" to fileName,
      "activeContent" to fileContent,
      "createdAt" to com.google.firebase.Timestamp.now(),
      "updatedAt" to com.google.firebase.Timestamp.now()
    )

    try {
      db.collection("rooms").document(roomId).set(roomData).await()
    } catch (e: Exception) {
      Log.w(tag, "Failed to write room to Firestore (offline fallback used): ${e.message}")
    }

    return Pair(roomId, pairingCode)
  }

  suspend fun sendOfferSdp(roomId: String, offerSdp: String) {
    try {
      db.collection("rooms").document(roomId).update(
        mapOf(
          "offerSdp" to offerSdp,
          "offerType" to "offer",
          "updatedAt" to com.google.firebase.Timestamp.now()
        )
      ).await()
    } catch (e: Exception) {
      Log.w(tag, "sendOfferSdp error: ${e.message}")
    }
  }

  suspend fun sendAnswerSdp(roomId: String, answerSdp: String) {
    try {
      db.collection("rooms").document(roomId).update(
        mapOf(
          "answerSdp" to answerSdp,
          "answerType" to "answer",
          "updatedAt" to com.google.firebase.Timestamp.now()
        )
      ).await()
    } catch (e: Exception) {
      Log.w(tag, "sendAnswerSdp error: ${e.message}")
    }
  }

  suspend fun sendIceCandidate(
    roomId: String,
    senderUid: String,
    candidate: String,
    sdpMid: String = "0",
    sdpMLineIndex: Int = 0
  ) {
    try {
      val data = hashMapOf(
        "senderUid" to senderUid,
        "candidate" to candidate,
        "sdpMid" to sdpMid,
        "sdpMLineIndex" to sdpMLineIndex,
        "createdAt" to com.google.firebase.Timestamp.now()
      )
      db.collection("rooms").document(roomId).collection("candidates").add(data).await()
    } catch (e: Exception) {
      Log.w(tag, "sendIceCandidate error: ${e.message}")
    }
  }

  suspend fun updatePeerPresence(
    roomId: String,
    peerUid: String,
    peerName: String,
    role: String,
    cursorLine: Int,
    cursorCol: Int
  ) {
    try {
      val data = hashMapOf(
        "peerUid" to peerUid,
        "peerName" to peerName,
        "role" to role,
        "cursorLine" to cursorLine,
        "cursorCol" to cursorCol,
        "status" to "online",
        "lastPing" to com.google.firebase.Timestamp.now()
      )
      db.collection("rooms").document(roomId).collection("peers").document(peerUid).set(data).await()
    } catch (e: Exception) {
      Log.w(tag, "updatePeerPresence error: ${e.message}")
    }
  }

  fun observePeers(roomId: String): Flow<List<RoomPeerModel>> = callbackFlow {
    var reg: ListenerRegistration? = null
    try {
      reg = db.collection("rooms").document(roomId).collection("peers")
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.w(tag, "observePeers listener error: ${error.message}")
            return@addSnapshotListener
          }
          val peers = snapshot?.documents?.mapNotNull { doc ->
            RoomPeerModel(
              peerUid = doc.getString("peerUid") ?: doc.id,
              peerName = doc.getString("peerName") ?: "Peer",
              role = doc.getString("role") ?: "Editor",
              cursorLine = doc.getLong("cursorLine")?.toInt() ?: 1,
              cursorCol = doc.getLong("cursorCol")?.toInt() ?: 1,
              status = doc.getString("status") ?: "online"
            )
          } ?: emptyList()
          trySend(peers)
        }
    } catch (e: Exception) {
      Log.w(tag, "Failed to attach peers listener: ${e.message}")
    }

    awaitClose { reg?.remove() }
  }

  suspend fun sendChatMessage(
    roomId: String,
    senderId: String,
    senderName: String,
    text: String,
    codeReference: CodeReference? = null
  ) {
    val data = hashMapOf<String, Any>(
      "senderId" to senderId,
      "senderName" to senderName,
      "text" to text,
      "timestamp" to System.currentTimeMillis()
    )
    if (codeReference != null) {
      data["hasCodeReference"] = true
      data["referencedFilePath"] = codeReference.filePath
      data["referencedFileName"] = codeReference.fileName
      data["referencedLineNumber"] = codeReference.lineNumber
      data["referencedCodeSnippet"] = codeReference.lineSnippet
    }

    try {
      db.collection("rooms").document(roomId).collection("messages").add(data).await()
    } catch (e: Exception) {
      Log.w(tag, "sendChatMessage error: ${e.message}")
    }
  }

  fun observeChatMessages(roomId: String): Flow<List<TeamChatMessageModel>> = callbackFlow {
    var reg: ListenerRegistration? = null
    try {
      reg = db.collection("rooms").document(roomId).collection("messages")
        .orderBy("timestamp")
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.w(tag, "observeChatMessages error: ${error.message}")
            return@addSnapshotListener
          }
          val msgs = snapshot?.documents?.mapNotNull { doc ->
            val hasRef = doc.getBoolean("hasCodeReference") ?: false
            val codeRef = if (hasRef) {
              CodeReference(
                filePath = doc.getString("referencedFilePath") ?: "",
                fileName = doc.getString("referencedFileName") ?: "",
                lineNumber = doc.getLong("referencedLineNumber")?.toInt() ?: 1,
                lineSnippet = doc.getString("referencedCodeSnippet") ?: ""
              )
            } else null

            TeamChatMessageModel(
              id = doc.id,
              senderId = doc.getString("senderId") ?: "",
              senderName = doc.getString("senderName") ?: "Peer",
              text = doc.getString("text") ?: "",
              codeReference = codeRef,
              timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
            )
          } ?: emptyList()
          trySend(msgs)
        }
    } catch (e: Exception) {
      Log.w(tag, "Failed to attach messages listener: ${e.message}")
    }

    awaitClose { reg?.remove() }
  }
}
