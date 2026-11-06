package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.MainAppScreen
import com.example.ui.WorkspaceViewModel
import com.example.ui.theme.LANternCodeTheme

class MainActivity : ComponentActivity() {
  private val viewModel: WorkspaceViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    handleIncomingInvite(intent)

    setContent {
      LANternCodeTheme {
        MainAppScreen(viewModel = viewModel)
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    handleIncomingInvite(intent)
  }

  private fun handleIncomingInvite(intent: Intent?) {
    val data = intent?.data
    if (data != null && data.scheme == "lantern") {
      val roomId = data.getQueryParameter("room")
      val host = data.getQueryParameter("host") ?: "Peer"
      if (!roomId.isNullOrBlank()) {
        viewModel.showMessage("Joined WebRTC room: $roomId (Host: $host)")
      }
    }
  }
}
