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
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.git.DiffLine
import com.example.git.DiffLineType
import com.example.ui.theme.CodeEditorBg
import com.example.ui.theme.LanternBrightGreen
import com.example.ui.theme.LanternCyan
import com.example.ui.theme.LanternNeonGreen
import com.example.ui.theme.LanternObsidianCard
import com.example.ui.theme.LanternTextMuted
import com.example.ui.theme.LanternTextPrimary
import com.example.ui.theme.LanternTextSecondary

@Composable
fun GitDiffViewerDialog(
  fileName: String,
  diffLines: List<DiffLine>,
  onAcceptLocal: () -> Unit,
  onAcceptRemote: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Difference, contentDescription = null, tint = LanternCyan, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text("Git Diff Inspector", fontWeight = FontWeight.Bold, color = LanternTextPrimary, fontSize = 14.sp)
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
        // Merge Actions Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CallMerge, contentDescription = null, tint = LanternCyan, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Resolver:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LanternTextSecondary)
          }

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(
              onClick = onAcceptLocal,
              colors = ButtonDefaults.buttonColors(containerColor = LanternNeonGreen, contentColor = Color(0xFF003D1A)),
              shape = RoundedCornerShape(6.dp),
              modifier = Modifier.height(26.dp)
            ) {
              Text("Keep Mine", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
              onClick = onAcceptRemote,
              shape = RoundedCornerShape(6.dp),
              modifier = Modifier.height(26.dp)
            ) {
              Text("Accept Peer", fontSize = 10.sp, color = LanternCyan)
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Diff lines viewer
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
          colors = CardDefaults.cardColors(containerColor = CodeEditorBg),
          shape = RoundedCornerShape(8.dp)
        ) {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            items(diffLines) { line ->
              val bg = when (line.type) {
                DiffLineType.ADDED -> Color(0xFF00E676).copy(alpha = 0.18f)
                DiffLineType.REMOVED -> Color(0xFFEF4444).copy(alpha = 0.18f)
                DiffLineType.UNCHANGED -> Color.Transparent
              }

              val textColor = when (line.type) {
                DiffLineType.ADDED -> Color(0xFF69FFA1)
                DiffLineType.REMOVED -> Color(0xFFFFA5A5)
                DiffLineType.UNCHANGED -> LanternTextSecondary
              }

              val prefix = when (line.type) {
                DiffLineType.ADDED -> "+ "
                DiffLineType.REMOVED -> "- "
                DiffLineType.UNCHANGED -> "  "
              }

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(2.dp))
                  .background(bg)
                  .padding(horizontal = 4.dp, vertical = 2.dp)
              ) {
                Text(
                  text = prefix + line.text,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                  color = textColor,
                  lineHeight = 15.sp
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
        Text("Done", fontWeight = FontWeight.Bold)
      }
    },
    containerColor = LanternObsidianCard
  )
}
