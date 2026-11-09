package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LanternCyan
import com.example.ui.theme.LanternNeonGreen
import com.example.ui.theme.LanternObsidianBorder
import com.example.ui.theme.LanternObsidianCard
import com.example.ui.theme.LanternTextMuted
import com.example.ui.theme.LanternTextPrimary

@Composable
fun AiCompletionOverlay(
  suggestion: String?,
  source: String,
  isLoading: Boolean,
  onAccept: (String) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  AnimatedVisibility(
    visible = isLoading || !suggestion.isNullOrBlank(),
    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    modifier = modifier
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 6.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(LanternObsidianCard)
        .border(1.dp, LanternCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
        .padding(10.dp)
        .testTag("ai_completion_card")
    ) {
      if (isLoading) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(vertical = 4.dp)
        ) {
          CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            color = LanternCyan,
            strokeWidth = 2.dp
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Gemini AI is analyzing code context & generating completion...",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = LanternCyan
          )
        }
      } else if (!suggestion.isNullOrEmpty()) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = LanternNeonGreen,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "AI Completion ($source)",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = LanternNeonGreen
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Button(
                onClick = { onAccept(suggestion) },
                colors = ButtonDefaults.buttonColors(containerColor = LanternNeonGreen),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                  .height(26.dp)
                  .testTag("accept_ai_completion_btn")
              ) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Accept",
                  tint = LanternObsidianCard,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Accept (Tab)",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = LanternObsidianCard
                )
              }

              Spacer(modifier = Modifier.width(8.dp))

              IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Dismiss",
                  tint = LanternTextMuted,
                  modifier = Modifier.size(14.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(4.dp))
              .background(LanternObsidianBorder.copy(alpha = 0.6f))
              .padding(8.dp)
          ) {
            Text(
              text = suggestion,
              fontSize = 12.sp,
              fontFamily = FontFamily.Monospace,
              color = LanternTextPrimary,
              lineHeight = 18.sp
            )
          }
        }
      }
    }
  }
}
