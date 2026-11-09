package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AuditSeverity
import com.example.ui.theme.LanternCyan
import com.example.ui.theme.LanternNeonGreen
import com.example.ui.theme.LanternObsidianBorder
import com.example.ui.theme.LanternObsidianCard
import com.example.ui.theme.LanternTextMuted
import kotlinx.coroutines.launch

@Composable
fun EditorMinimap(
  lines: List<String>,
  activeLine: Int,
  scrollState: ScrollState,
  issueLines: Map<Int, AuditSeverity>,
  onLineClick: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val coroutineScope = rememberCoroutineScope()
  val lineCount = maxOf(1, lines.size)

  Column(
    modifier = modifier
      .width(54.dp)
      .fillMaxHeight()
      .background(LanternObsidianCard)
      .border(width = 1.dp, color = LanternObsidianBorder)
      .testTag("editor_minimap"),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Top map label
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(LanternObsidianBorder.copy(alpha = 0.5f))
        .padding(vertical = 3.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "MAP",
        fontSize = 8.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        color = LanternCyan
      )
    }

    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .pointerInput(lineCount, scrollState.maxValue) {
          detectTapGestures { offset ->
            val height = size.height.toFloat()
            if (height > 0) {
              val fraction = (offset.y / height).coerceIn(0f, 1f)
              val targetLine = (fraction * lineCount).toInt().coerceIn(1, lineCount)
              onLineClick(targetLine)

              if (scrollState.maxValue > 0) {
                val targetPx = (fraction * scrollState.maxValue).toInt()
                coroutineScope.launch {
                  scrollState.scrollTo(targetPx)
                }
              }
            }
          }
        }
        .pointerInput(lineCount, scrollState.maxValue) {
          detectVerticalDragGestures { change, dragAmount ->
            change.consume()
            val height = size.height.toFloat()
            if (height > 0 && scrollState.maxValue > 0) {
              val currentFraction = (scrollState.value.toFloat() / scrollState.maxValue.toFloat()).coerceIn(0f, 1f)
              val deltaFraction = dragAmount / height
              val newFraction = (currentFraction + deltaFraction).coerceIn(0f, 1f)
              val targetPx = (newFraction * scrollState.maxValue).toInt()
              val targetLine = (newFraction * lineCount).toInt().coerceIn(1, lineCount)
              onLineClick(targetLine)
              coroutineScope.launch {
                scrollState.scrollTo(targetPx)
              }
            }
          }
        }
    ) {
      Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 4.dp)) {
        val totalLines = lines.size
        val availableHeight = size.height
        val availableWidth = size.width

        if (totalLines > 0 && availableHeight > 0) {
          val lineHeightPx = (availableHeight / totalLines).coerceIn(1f, 6f)
          val step = if (totalLines > 300) totalLines / 300 else 1

          var y = 0f
          for (i in 0 until totalLines step step) {
            if (y > availableHeight) break
            val lineText = lines[i]
            val lineNum = i + 1
            val trimmed = lineText.trim()
            val indent = (lineText.length - lineText.trimStart().length).coerceAtMost(20)

            val color = when {
              issueLines.containsKey(lineNum) -> {
                when (issueLines[lineNum]) {
                  AuditSeverity.CRITICAL -> Color(0xFFEF4444)
                  AuditSeverity.WARNING -> Color(0xFFF59E0B)
                  else -> Color(0xFF3B82F6)
                }
              }
              lineNum == activeLine -> Color.White
              trimmed.startsWith("//") || trimmed.startsWith("/*") -> Color(0xFF64748B)
              trimmed.startsWith("fun ") || trimmed.startsWith("class ") || trimmed.startsWith("def ") || trimmed.startsWith("import ") -> LanternNeonGreen
              trimmed.contains("val ") || trimmed.contains("var ") || trimmed.contains("const ") || trimmed.contains("let ") -> LanternCyan
              else -> Color(0xFF475569)
            }

            val startX = (indent * 1.5f).coerceAtMost(availableWidth * 0.4f)
            val barWidth = (trimmed.length * 1.2f).coerceIn(4f, availableWidth - startX)

            drawRect(
              color = color,
              topLeft = Offset(startX, y),
              size = Size(barWidth, (lineHeightPx * 0.75f).coerceAtLeast(1f))
            )

            y += lineHeightPx
          }

          // Viewport highlight overlay box representing visible scroll area
          val maxScroll = scrollState.maxValue.toFloat()
          val currentScroll = scrollState.value.toFloat()
          val scrollFraction = if (maxScroll > 0) (currentScroll / maxScroll).coerceIn(0f, 1f) else 0f

          val minHeight = 16f.coerceAtMost(availableHeight)
          val maxHeight = (availableHeight * 0.5f).coerceAtLeast(minHeight)
          val viewportHeight = (availableHeight * 0.25f).coerceIn(minHeight, maxHeight)
          val viewportTop = scrollFraction * (availableHeight - viewportHeight).coerceAtLeast(0f)

          drawRect(
            color = LanternCyan.copy(alpha = 0.25f),
            topLeft = Offset(0f, viewportTop),
            size = Size(availableWidth, viewportHeight)
          )

          drawRect(
            color = LanternCyan,
            topLeft = Offset(0f, viewportTop),
            size = Size(2.dp.toPx(), viewportHeight)
          )
        }
      }
    }
  }
}
