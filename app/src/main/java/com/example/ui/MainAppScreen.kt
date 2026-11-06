package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AuditAndTestScreen
import com.example.ui.screens.CodeEditorScreen
import com.example.ui.screens.CollaborationScreen
import com.example.ui.screens.DatabaseStudioScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.screens.TerminalConsoleScreen
import com.example.ui.screens.WorkspaceHubScreen
import com.example.ui.theme.LanternBrightGreen
import com.example.ui.theme.LanternNeonGreen
import com.example.ui.theme.LanternObsidianCard
import com.example.ui.theme.LanternTextMuted

enum class NavDestination(val label: String, val icon: ImageVector) {
  WORKSPACES("Workspaces", Icons.Default.Folder),
  STUDIO("Studio", Icons.Default.Code),
  TERMINAL("Terminal", Icons.Default.Terminal),
  COLLAB("Mesh P2P", Icons.Default.Lan),
  DATABASE("SQLite", Icons.Default.Storage),
  AUDIT_TEST("Audit", Icons.Default.CheckCircle)
}

@Composable
fun MainAppScreen(
  viewModel: WorkspaceViewModel,
  modifier: Modifier = Modifier
) {
  var currentTab by remember { mutableStateOf(NavDestination.WORKSPACES) }
  var showSettingsDialog by remember { mutableStateOf(false) }

  val snackbarHostState = remember { SnackbarHostState() }
  val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

  // Handle Back press: navigate back to WORKSPACES if in subscreen
  BackHandler(enabled = currentTab != NavDestination.WORKSPACES) {
    currentTab = NavDestination.WORKSPACES
  }

  LaunchedEffect(userMessage) {
    userMessage?.let {
      snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
      viewModel.clearUserMessage()
    }
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .statusBarsPadding(),
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    bottomBar = {
      NavigationBar(
        modifier = Modifier
          .navigationBarsPadding()
          .testTag("main_bottom_nav_bar"),
        containerColor = LanternObsidianCard,
        contentColor = LanternNeonGreen
      ) {
        NavDestination.values().forEach { dest ->
          val isSelected = currentTab == dest
          NavigationBarItem(
            selected = isSelected,
            onClick = { currentTab = dest },
            icon = {
              Icon(
                imageVector = dest.icon,
                contentDescription = dest.label,
                tint = if (isSelected) LanternNeonGreen else LanternTextMuted
              )
            },
            label = {
              Text(
                text = dest.label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) LanternBrightGreen else LanternTextMuted
              )
            },
            colors = NavigationBarItemDefaults.colors(
              indicatorColor = LanternNeonGreen.copy(alpha = 0.2f)
            ),
            modifier = Modifier.testTag("nav_item_${dest.name.lowercase()}")
          )
        }
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(MaterialTheme.colorScheme.background)
    ) {
      when (currentTab) {
        NavDestination.WORKSPACES -> {
          WorkspaceHubScreen(
            viewModel = viewModel,
            onNavigateToEditor = { currentTab = NavDestination.STUDIO },
            onNavigateToDb = { currentTab = NavDestination.DATABASE },
            onNavigateToCollab = { currentTab = NavDestination.COLLAB },
            onOpenSettings = { showSettingsDialog = true }
          )
        }
        NavDestination.STUDIO -> {
          CodeEditorScreen(
            viewModel = viewModel,
            onNavigateToAudit = { currentTab = NavDestination.AUDIT_TEST },
            onNavigateToCollab = { currentTab = NavDestination.COLLAB }
          )
        }
        NavDestination.TERMINAL -> {
          TerminalConsoleScreen(viewModel = viewModel)
        }
        NavDestination.COLLAB -> {
          CollaborationScreen(viewModel = viewModel)
        }
        NavDestination.DATABASE -> {
          DatabaseStudioScreen(viewModel = viewModel)
        }
        NavDestination.AUDIT_TEST -> {
          AuditAndTestScreen(viewModel = viewModel)
        }
      }
    }
  }

  if (showSettingsDialog) {
    SettingsDialog(
      viewModel = viewModel,
      onDismiss = { showSettingsDialog = false }
    )
  }
}
