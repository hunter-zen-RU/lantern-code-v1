package com.example.ui.components

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.LanternCyan
import com.example.ui.theme.LanternNeonGreen
import com.example.ui.theme.LanternObsidianBorder
import com.example.ui.theme.LanternObsidianCard
import com.example.ui.theme.LanternTextMuted
import com.example.ui.theme.LanternTextPrimary
import com.example.ui.theme.LanternTextSecondary

enum class CommandAction {
  FORMAT_DOC,
  SAVE_FILE,
  SAVE_ALL,
  CLOSE_FILE,
  CLOSE_ALL,
  FIND_FILES,
  GIT_COMMIT,
  GIT_DASHBOARD,
  GO_TO_LINE,
  NEW_FILE,
  AUDIT,
  TOGGLE_AI,
  TOGGLE_MINIMAP
}

data class CommandPaletteItem(
  val title: String,
  val subtitle: String,
  val category: String,
  val shortcut: String,
  val icon: ImageVector,
  val action: CommandAction
)

@Composable
fun CommandPaletteModal(
  onDismiss: () -> Unit,
  onExecute: (CommandAction) -> Unit
) {
  val commands = remember {
    listOf(
      CommandPaletteItem(
        title = "Format Document",
        subtitle = "Reformat whitespace & indentation in active file",
        category = "Editor",
        shortcut = "Alt+Shift+F",
        icon = Icons.AutoMirrored.Filled.FormatAlignLeft,
        action = CommandAction.FORMAT_DOC
      ),
      CommandPaletteItem(
        title = "Save File",
        subtitle = "Save current file changes to workspace",
        category = "File",
        shortcut = "Ctrl+S",
        icon = Icons.Default.Save,
        action = CommandAction.SAVE_FILE
      ),
      CommandPaletteItem(
        title = "Save All Files",
        subtitle = "Save all open modified files in project",
        category = "File",
        shortcut = "Ctrl+Shift+S",
        icon = Icons.Default.Save,
        action = CommandAction.SAVE_ALL
      ),
      CommandPaletteItem(
        title = "Close Active File",
        subtitle = "Close current active tab in studio",
        category = "View",
        shortcut = "Ctrl+W",
        icon = Icons.Default.Close,
        action = CommandAction.CLOSE_FILE
      ),
      CommandPaletteItem(
        title = "Close All Files",
        subtitle = "Close all open editor tabs",
        category = "View",
        shortcut = "Ctrl+Shift+W",
        icon = Icons.Default.Close,
        action = CommandAction.CLOSE_ALL
      ),
      CommandPaletteItem(
        title = "Find in Files",
        subtitle = "Quick fuzzy search across all workspace files",
        category = "Navigation",
        shortcut = "Ctrl+Shift+F",
        icon = Icons.Default.Search,
        action = CommandAction.FIND_FILES
      ),
      CommandPaletteItem(
        title = "Git Commit & Stage",
        subtitle = "Stage changes and commit with message",
        category = "Git",
        shortcut = "Ctrl+Alt+C",
        icon = Icons.Default.Difference,
        action = CommandAction.GIT_COMMIT
      ),
      CommandPaletteItem(
        title = "Open Git Dashboard",
        subtitle = "Branch status, commit history, push & pull operations",
        category = "Git",
        shortcut = "Ctrl+Shift+G",
        icon = Icons.Default.Difference,
        action = CommandAction.GIT_DASHBOARD
      ),
      CommandPaletteItem(
        title = "Go to Line",
        subtitle = "Jump directly to line number",
        category = "Navigation",
        shortcut = "Ctrl+G",
        icon = Icons.Default.Code,
        action = CommandAction.GO_TO_LINE
      ),
      CommandPaletteItem(
        title = "New File",
        subtitle = "Create a new file in active project",
        category = "File",
        shortcut = "Ctrl+N",
        icon = Icons.Default.Folder,
        action = CommandAction.NEW_FILE
      ),
      CommandPaletteItem(
        title = "Run Health & Security Audit",
        subtitle = "Analyze codebase for security and code issues",
        category = "Analysis",
        shortcut = "Ctrl+Shift+A",
        icon = Icons.Default.Terminal,
        action = CommandAction.AUDIT
      ),
      CommandPaletteItem(
        title = "Toggle Gemini AI Autocomplete",
        subtitle = "Enable or disable auto AI snippet suggestions",
        category = "AI",
        shortcut = "Ctrl+Shift+I",
        icon = Icons.Default.AutoAwesome,
        action = CommandAction.TOGGLE_AI
      ),
      CommandPaletteItem(
        title = "Toggle File Mini-Map",
        subtitle = "Show or hide right-side code minimap",
        category = "View",
        shortcut = "Ctrl+M",
        icon = Icons.Default.Map,
        action = CommandAction.TOGGLE_MINIMAP
      )
    )
  }

  var searchQuery by remember { mutableStateOf("") }
  var selectedIndex by remember { mutableIntStateOf(0) }
  val focusRequester = remember { FocusRequester() }
  val listState = rememberLazyListState()

  val filteredCommands = remember(searchQuery) {
    if (searchQuery.isBlank()) commands
    else {
      val query = searchQuery.trim().lowercase()
      commands.filter {
        it.title.lowercase().contains(query) ||
            it.subtitle.lowercase().contains(query) ||
            it.category.lowercase().contains(query)
      }
    }
  }

  LaunchedEffect(filteredCommands) {
    selectedIndex = 0
  }

  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .widthIn(max = 520.dp)
        .fillMaxWidth(0.92f)
        .padding(16.dp)
        .testTag("command_palette_dialog"),
      colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
      shape = RoundedCornerShape(16.dp),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LanternCyan))
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Terminal,
              contentDescription = null,
              tint = LanternNeonGreen,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Command Palette",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = LanternTextPrimary
            )
          }

          Text(
            text = "Ctrl+Shift+P",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = LanternCyan,
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(LanternObsidianBorder)
              .padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Input TextField with keyboard navigation
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Type a command or search action...", fontSize = 12.sp, color = LanternTextMuted) },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LanternCyan, modifier = Modifier.size(18.dp)) },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(Icons.Default.Close, contentDescription = "Clear", tint = LanternTextMuted, modifier = Modifier.size(16.dp))
              }
            }
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onKeyEvent { keyEvent ->
              when (keyEvent.key) {
                Key.DirectionDown -> {
                  if (filteredCommands.isNotEmpty()) {
                    selectedIndex = (selectedIndex + 1) % filteredCommands.size
                  }
                  true
                }
                Key.DirectionUp -> {
                  if (filteredCommands.isNotEmpty()) {
                    selectedIndex = (selectedIndex - 1 + filteredCommands.size) % filteredCommands.size
                  }
                  true
                }
                Key.Enter -> {
                  if (filteredCommands.isNotEmpty() && selectedIndex in filteredCommands.indices) {
                    onExecute(filteredCommands[selectedIndex].action)
                    onDismiss()
                  }
                  true
                }
                Key.Escape -> {
                  onDismiss()
                  true
                }
                else -> false
              }
            }
            .testTag("command_palette_search_input"),
          textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            color = LanternTextPrimary
          ),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LanternNeonGreen,
            unfocusedBorderColor = LanternObsidianBorder,
            focusedContainerColor = LanternObsidianBorder.copy(alpha = 0.5f),
            unfocusedContainerColor = LanternObsidianBorder.copy(alpha = 0.3f)
          ),
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
          keyboardActions = KeyboardActions(onGo = {
            if (filteredCommands.isNotEmpty() && selectedIndex in filteredCommands.indices) {
              onExecute(filteredCommands[selectedIndex].action)
              onDismiss()
            }
          })
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filtered Commands List
        LazyColumn(
          state = listState,
          modifier = Modifier.height(260.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          itemsIndexed(filteredCommands) { index, item ->
            val isSelected = index == selectedIndex

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(
                  if (isSelected) LanternNeonGreen.copy(alpha = 0.18f)
                  else LanternObsidianBorder.copy(alpha = 0.2f)
                )
                .border(
                  width = 1.dp,
                  color = if (isSelected) LanternNeonGreen else Color.Transparent,
                  shape = RoundedCornerShape(8.dp)
                )
                .clickable {
                  onExecute(item.action)
                  onDismiss()
                }
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .testTag("command_item_${item.action.name.lowercase()}"),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Icon(
                  imageVector = item.icon,
                  contentDescription = null,
                  tint = if (isSelected) LanternNeonGreen else LanternCyan,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = item.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) LanternNeonGreen else LanternTextPrimary
                  )
                  Text(
                    text = item.subtitle,
                    fontSize = 10.sp,
                    color = LanternTextMuted
                  )
                }
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(LanternObsidianBorder)
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = item.shortcut,
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace,
                  color = LanternCyan,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }
    }
  }
}
