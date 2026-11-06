package com.example.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.entity.ProjectFileEntity
import com.example.ui.theme.CodeEditorBg
import com.example.ui.theme.LanternBrightGreen
import com.example.ui.theme.LanternCyan
import com.example.ui.theme.LanternNeonGreen
import com.example.ui.theme.LanternObsidianBorder
import com.example.ui.theme.LanternObsidianCard
import com.example.ui.theme.LanternTextMuted
import com.example.ui.theme.LanternTextPrimary
import com.example.ui.theme.LanternTextSecondary

@Composable
fun LiveWebPreviewDialog(
  fileName: String,
  content: String,
  projectFiles: List<ProjectFileEntity> = emptyList(),
  onDismiss: () -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  var reloadTrigger by remember { mutableIntStateOf(0) }
  val tabs = listOf("Interactive Preview", "Document Info", "Raw Source")

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

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = { reloadTrigger++ }, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = LanternCyan, modifier = Modifier.size(16.dp))
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = LanternTextMuted, modifier = Modifier.size(16.dp))
          }
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
            .height(380.dp),
          colors = CardDefaults.cardColors(containerColor = CodeEditorBg),
          shape = RoundedCornerShape(8.dp),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LanternObsidianBorder))
        ) {
          when (selectedTab) {
            0 -> {
              DocumentWebPreview(
                fileName = fileName,
                content = content,
                projectFiles = projectFiles,
                reloadKey = reloadTrigger,
                modifier = Modifier.fillMaxSize()
              )
            }
            1 -> {
              DocumentStatisticsView(
                fileName = fileName,
                content = content,
                modifier = Modifier.fillMaxSize()
              )
            }
            2 -> {
              LazyColumn(
                modifier = Modifier
                  .fillMaxSize()
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

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DocumentWebPreview(
  fileName: String,
  content: String,
  projectFiles: List<ProjectFileEntity> = emptyList(),
  reloadKey: Int = 0,
  modifier: Modifier = Modifier
) {
  val cleanExt = fileName.substringAfterLast(".", "").lowercase()
  val htmlPayload = remember(fileName, content, projectFiles, reloadKey) {
    when (cleanExt) {
      "html", "htm" -> {
        // Embed project styles and scripts if referenced
        var processedHtml = content
        val cssFile = projectFiles.firstOrNull { it.name.endsWith(".css") }
        val jsFile = projectFiles.firstOrNull { it.name.endsWith(".js") }

        if (cssFile != null && !processedHtml.contains("<style>")) {
          processedHtml = processedHtml.replace("</head>", "<style>\n${cssFile.content}\n</style>\n</head>")
        }
        if (jsFile != null && !processedHtml.contains("<script>") && !processedHtml.contains(jsFile.name)) {
          processedHtml = processedHtml.replace("</body>", "<script>\n${jsFile.content}\n</script>\n</body>")
        }
        processedHtml
      }
      "css" -> {
        """<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <style>
  $content
  </style>
</head>
<body>
  <h1>CSS Stylesheet Preview: $fileName</h1>
  <p>Testing typography, cards, and buttons applied from this stylesheet.</p>
  <button class="btn">Primary Action</button>
  <div class="card">Sample Component Surface</div>
</body>
</html>"""
      }
      "js", "javascript" -> {
        """<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <style>
    body { background: #0b0f17; color: #f0f6fc; font-family: monospace; padding: 16px; }
    #console-out { background: #161b22; border: 1px solid #30363d; padding: 12px; border-radius: 8px; }
  </style>
</head>
<body>
  <h3>JavaScript Sandbox: $fileName</h3>
  <div id="console-out">Executing script...</div>
  <script>
    const out = document.getElementById('console-out');
    const log = (msg) => { out.innerHTML += '<br>> ' + msg; };
    console.log = log;
    try {
      $content
      out.innerHTML += '<br><span style="color:#00e676;">[SUCCESS] Script executed without uncaught exceptions</span>';
    } catch(e) {
      out.innerHTML += '<br><span style="color:#ef4444;">[ERROR] ' + e.message + '</span>';
    }
  </script>
</body>
</html>"""
      }
      "md", "markdown" -> {
        convertMarkdownToStyledHtml(fileName, content)
      }
      else -> {
        """<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <style>
    body { background: #0b0f17; color: #c9d1d9; font-family: monospace; padding: 16px; white-space: pre-wrap; }
  </style>
</head>
<body>${content.replace("<", "&lt;").replace(">", "&gt;")}</body>
</html>"""
      }
    }
  }

  AndroidView(
    factory = { ctx ->
      WebView(ctx).apply {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true
        webViewClient = WebViewClient()
        webChromeClient = WebChromeClient()
        setBackgroundColor(android.graphics.Color.parseColor("#0B0F17"))
      }
    },
    update = { webView ->
      webView.loadDataWithBaseURL("https://localhost/", htmlPayload, "text/html", "UTF-8", null)
    },
    modifier = modifier.clip(RoundedCornerShape(8.dp))
  )
}

@Composable
fun DocumentStatisticsView(
  fileName: String,
  content: String,
  modifier: Modifier = Modifier
) {
  val lines = content.lines()
  val words = content.split(Regex("\\s+")).filter { it.isNotBlank() }.size
  val chars = content.length
  val headings = lines.filter { it.trimStart().startsWith("#") }
  val readingTimeMinutes = (words / 200).coerceAtLeast(1)

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(14.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Text(
        text = "Document Metrics",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = LanternBrightGreen
      )
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        StatBadge(label = "Lines", value = "${lines.size}", modifier = Modifier.weight(1f))
        StatBadge(label = "Words", value = "$words", modifier = Modifier.weight(1f))
        StatBadge(label = "Chars", value = "$chars", modifier = Modifier.weight(1f))
        StatBadge(label = "Read Time", value = "${readingTimeMinutes}m", modifier = Modifier.weight(1f))
      }
    }

    if (headings.isNotEmpty()) {
      item {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Headings Outline (${headings.size})",
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = LanternCyan
        )
      }

      items(headings) { h ->
        val level = h.takeWhile { it == '#' }.length
        val title = h.removePrefix("#".repeat(level)).trim()
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(start = ((level - 1) * 12).dp)
        ) {
          Text("H$level", fontSize = 10.sp, color = LanternTextMuted, fontFamily = FontFamily.Monospace)
          Spacer(modifier = Modifier.width(6.dp))
          Text(title, fontSize = 12.sp, color = LanternTextPrimary, fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }
}

@Composable
private fun StatBadge(label: String, value: String, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(LanternObsidianCard)
      .border(1.dp, LanternObsidianBorder, RoundedCornerShape(8.dp))
      .padding(8.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(value, fontWeight = FontWeight.Bold, color = LanternNeonGreen, fontSize = 14.sp)
      Text(label, fontSize = 10.sp, color = LanternTextMuted)
    }
  }
}

/**
 * High-performance parser that transforms standard and GitHub-flavored Markdown
 * into polished, dark-mode themed HTML ready for Android WebView preview.
 */
fun convertMarkdownToStyledHtml(fileName: String, markdown: String): String {
  val lines = markdown.lines()
  val bodyHtml = StringBuilder()
  var inCodeBlock = false
  var inTable = false

  for (rawLine in lines) {
    val line = rawLine.trimEnd()

    // 1. Code blocks (```)
    if (line.startsWith("```")) {
      if (inCodeBlock) {
        bodyHtml.append("</code></pre>\n")
        inCodeBlock = false
      } else {
        val lang = line.removePrefix("```").trim()
        bodyHtml.append("<pre><code class=\"language-$lang\">")
        inCodeBlock = true
      }
      continue
    }

    if (inCodeBlock) {
      bodyHtml.append(line.replace("<", "&lt;").replace(">", "&gt;")).append("\n")
      continue
    }

    // 2. Table rows (| ... |)
    if (line.startsWith("|") && line.endsWith("|")) {
      if (!inTable) {
        bodyHtml.append("<table>\n")
        inTable = true
      }
      if (line.contains("---")) {
        // Divider row, skip
        continue
      }
      val cells = line.split("|").filter { it.isNotBlank() }
      bodyHtml.append("<tr>")
      for (cell in cells) {
        bodyHtml.append("<td>").append(parseInlineMarkdown(cell.trim())).append("</td>")
      }
      bodyHtml.append("</tr>\n")
      continue
    } else if (inTable) {
      bodyHtml.append("</table>\n")
      inTable = false
    }

    // 3. Headings
    when {
      line.startsWith("# ") -> {
        bodyHtml.append("<h1>").append(parseInlineMarkdown(line.substring(2))).append("</h1>\n")
      }
      line.startsWith("## ") -> {
        bodyHtml.append("<h2>").append(parseInlineMarkdown(line.substring(3))).append("</h2>\n")
      }
      line.startsWith("### ") -> {
        bodyHtml.append("<h3>").append(parseInlineMarkdown(line.substring(4))).append("</h3>\n")
      }
      line.startsWith("#### ") -> {
        bodyHtml.append("<h4>").append(parseInlineMarkdown(line.substring(5))).append("</h4>\n")
      }
      // 4. Horizontal rules
      line == "---" || line == "***" -> {
        bodyHtml.append("<hr>\n")
      }
      // 5. Blockquotes
      line.startsWith("> ") -> {
        bodyHtml.append("<blockquote>").append(parseInlineMarkdown(line.substring(2))).append("</blockquote>\n")
      }
      // 6. Checkboxes / Task lists
      line.startsWith("- [x] ") || line.startsWith("* [x] ") -> {
        bodyHtml.append("<div class=\"task-item checked\"><span class=\"chk\">✔</span> ").append(parseInlineMarkdown(line.substring(6))).append("</div>\n")
      }
      line.startsWith("- [ ] ") || line.startsWith("* [ ] ") -> {
        bodyHtml.append("<div class=\"task-item unchecked\"><span class=\"chk-box\"></span> ").append(parseInlineMarkdown(line.substring(6))).append("</div>\n")
      }
      // 7. Bullet lists
      line.startsWith("- ") || line.startsWith("* ") -> {
        bodyHtml.append("<li>").append(parseInlineMarkdown(line.substring(2))).append("</li>\n")
      }
      // 8. Numbered lists
      Regex("^\\d+\\.\\s+.*").matches(line) -> {
        val content = line.substringAfter(". ")
        bodyHtml.append("<li>").append(parseInlineMarkdown(content)).append("</li>\n")
      }
      // 9. Standard paragraphs
      line.isNotBlank() -> {
        bodyHtml.append("<p>").append(parseInlineMarkdown(line)).append("</p>\n")
      }
    }
  }

  if (inCodeBlock) bodyHtml.append("</code></pre>\n")
  if (inTable) bodyHtml.append("</table>\n")

  return """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>$fileName Preview</title>
  <style>
    :root {
      --bg: #0b0f17;
      --surface: #161b22;
      --border: #30363d;
      --text: #c9d1d9;
      --text-bright: #f0f6fc;
      --primary: #00e676;
      --accent: #00e5ff;
    }
    body {
      background-color: var(--bg);
      color: var(--text);
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
      line-height: 1.6;
      padding: 16px;
      margin: 0;
      font-size: 14px;
    }
    h1, h2, h3, h4 {
      color: var(--text-bright);
      margin-top: 20px;
      margin-bottom: 10px;
      font-weight: 700;
    }
    h1 {
      font-size: 24px;
      color: var(--primary);
      border-bottom: 1px solid var(--border);
      padding-bottom: 8px;
    }
    h2 {
      font-size: 18px;
      color: var(--accent);
      border-bottom: 1px solid rgba(48, 54, 61, 0.5);
      padding-bottom: 6px;
    }
    h3 { font-size: 15px; }
    p { margin: 8px 0; }
    code {
      font-family: ui-monospace, SFMono-Regular, SF Mono, Menlo, Consolas, monospace;
      background: rgba(110, 118, 129, 0.2);
      color: #79c0ff;
      padding: 2px 6px;
      border-radius: 4px;
      font-size: 12px;
    }
    pre {
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: 8px;
      padding: 12px;
      overflow-x: auto;
    }
    pre code {
      background: transparent;
      padding: 0;
      color: #e6edf3;
      font-size: 12px;
    }
    blockquote {
      border-left: 4px solid var(--primary);
      margin: 12px 0;
      padding: 6px 14px;
      background: rgba(0, 230, 118, 0.05);
      color: #8b949e;
      border-radius: 0 6px 6px 0;
    }
    table {
      border-collapse: collapse;
      width: 100%;
      margin: 12px 0;
    }
    th, td {
      border: 1px solid var(--border);
      padding: 8px 12px;
      text-align: left;
    }
    tr:nth-child(even) { background-color: rgba(22, 27, 34, 0.7); }
    hr {
      border: 0;
      height: 1px;
      background: var(--border);
      margin: 20px 0;
    }
    li { margin: 4px 0; }
    a { color: var(--accent); text-decoration: none; }
    a:hover { text-decoration: underline; }
    .task-item {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 4px 0;
    }
    .chk { color: var(--primary); font-weight: bold; }
    .chk-box {
      width: 12px;
      height: 12px;
      border: 1.5px solid #8b949e;
      border-radius: 3px;
      display: inline-block;
    }
  </style>
</head>
<body>
  $bodyHtml
</body>
</html>"""
}

private fun parseInlineMarkdown(text: String): String {
  var res = text
    .replace("<", "&lt;")
    .replace(">", "&gt;")

  // Inline code `code`
  res = res.replace(Regex("`([^`]+)`"), "<code>$1</code>")
  // Bold **text**
  res = res.replace(Regex("\\*\\*([^*]+)\\*\\*"), "<strong>$1</strong>")
  // Italic *text*
  res = res.replace(Regex("\\*([^*]+)\\*"), "<em>$1</em>")
  // Strikethrough ~~text~~
  res = res.replace(Regex("~~([^~]+)~~"), "<del>$1</del>")
  // Links [text](url)
  res = res.replace(Regex("\\[([^\\]]+)\\]\\(([^)]+)\\)"), "<a href=\"$2\">$1</a>")

  return res
}
