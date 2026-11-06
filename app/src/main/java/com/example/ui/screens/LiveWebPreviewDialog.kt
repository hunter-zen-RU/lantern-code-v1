package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CodeEditorBg
import com.example.ui.theme.LanternBrightGreen
import com.example.ui.theme.LanternCyan
import com.example.ui.theme.LanternNeonGreen
import com.example.ui.theme.LanternObsidianCard
import com.example.ui.theme.LanternTextMuted
import com.example.ui.theme.LanternTextPrimary
import com.example.ui.theme.LanternTextSecondary

@Composable
fun LiveWebPreviewDialog(
  fileName: String,
  content: String,
  onDismiss: () -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Rendered Preview", "Raw Document")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Language, contentDescription = null, tint = LanternCyan, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text("Live Preview Studio", fontWeight = FontWeight.Bold, color = LanternTextPrimary, fontSize = 14.sp)
            Text(fileName, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = LanternBrightGreen)
          }
        }

        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = LanternTextMuted, modifier = Modifier.size(16.dp))
        }
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = MaterialTheme.colorScheme.background,
          contentColor = LanternNeonGreen
        ) {
          tabs.forEachIndexed { idx, title ->
            Tab(
              selected = selectedTab == idx,
              onClick = { selectedTab = idx },
              text = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
          colors = CardDefaults.cardColors(containerColor = CodeEditorBg),
          shape = RoundedCornerShape(8.dp)
        ) {
          if (selectedTab == 0) {
            // Rendered Markdown / Web representation
            val lines = content.lines()
            LazyColumn(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              items(lines) { line ->
                when {
                  line.startsWith("# ") -> {
                    Text(
                      text = line.removePrefix("# "),
                      fontWeight = FontWeight.ExtraBold,
                      fontSize = 18.sp,
                      color = LanternBrightGreen
                    )
                  }
                  line.startsWith("## ") -> {
                    Text(
                      text = line.removePrefix("## "),
                      fontWeight = FontWeight.Bold,
                      fontSize = 15.sp,
                      color = LanternCyan
                    )
                  }
                  line.startsWith("### ") -> {
                    Text(
                      text = line.removePrefix("### "),
                      fontWeight = FontWeight.Bold,
                      fontSize = 13.sp,
                      color = LanternTextPrimary
                    )
                  }
                  line.startsWith("- ") || line.startsWith("* ") -> {
                    Row {
                      Text("• ", color = LanternNeonGreen, fontWeight = FontWeight.Bold)
                      Text(line.substring(2), color = LanternTextSecondary, fontSize = 12.sp)
                    }
                  }
                  line.startsWith("```") -> {
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                      Text(line, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = LanternTextMuted)
                    }
                  }
                  line.startsWith("package ") || line.startsWith("import ") || line.startsWith("fun ") || line.startsWith("class ") -> {
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(6.dp)
                    ) {
                      Text(line, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = LanternCyan)
                    }
                  }
                  else -> {
                    if (line.isNotBlank()) {
                      Text(line, fontSize = 12.sp, color = LanternTextPrimary, lineHeight = 16.sp)
                    } else {
                      Spacer(modifier = Modifier.height(4.dp))
                    }
                  }
                }
              }
            }
          } else {
            // Raw code / source
            LazyColumn(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
            ) {
              item {
                Text(
                  text = content,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                  color = LanternTextSecondary
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = LanternNeonGreen, contentColor = Color(0xFF003D1A))
      ) {
        Text("Close", fontWeight = FontWeight.Bold)
      }
    },
    containerColor = LanternObsidianCard
  )
}
