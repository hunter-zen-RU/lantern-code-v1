package com.example.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.example.ui.theme.CodeComment
import com.example.ui.theme.CodeFunction
import com.example.ui.theme.CodeKeyword
import com.example.ui.theme.CodeNumber
import com.example.ui.theme.CodeString
import com.example.ui.theme.LanternCyan
import com.example.ui.theme.LanternBrightGreen

class CodeSyntaxVisualTransformation(private val language: String = "kotlin") : VisualTransformation {
  override fun filter(text: AnnotatedString): TransformedText {
    val raw = text.text
    if (raw.isEmpty()) {
      return TransformedText(text, OffsetMapping.Identity)
    }

    val cleanLang = language.lowercase()
    val annotated = buildAnnotatedString {
      append(raw)

      fun safeStyle(style: SpanStyle, start: Int, end: Int) {
        val s = start.coerceIn(0, raw.length)
        val e = end.coerceIn(0, raw.length)
        if (s < e) {
          addStyle(style, s, e)
        }
      }

      // 1. Language-Specific Keywords
      val keywords = when {
        cleanLang in listOf("py", "python") -> listOf(
          "def", "class", "import", "from", "return", "if", "elif", "else", "while", "for",
          "in", "try", "except", "finally", "with", "as", "lambda", "yield", "async", "await",
          "pass", "break", "continue", "True", "False", "None", "self", "assert"
        )
        cleanLang in listOf("js", "javascript", "ts", "typescript") -> listOf(
          "function", "const", "let", "var", "return", "if", "else", "for", "while", "class",
          "export", "import", "from", "default", "interface", "type", "async", "await", "new",
          "this", "extends", "implements", "true", "false", "null", "undefined", "typeof"
        )
        cleanLang in listOf("html", "htm") -> listOf(
          "html", "head", "body", "title", "meta", "link", "script", "div", "span", "p",
          "h1", "h2", "h3", "h4", "h5", "h6", "button", "input", "form", "main", "header",
          "footer", "section", "article", "table", "tr", "td", "th", "ul", "ol", "li", "a",
          "style", "img", "DOCTYPE"
        )
        cleanLang in listOf("sql") -> listOf(
          "SELECT", "FROM", "WHERE", "INSERT", "INTO", "UPDATE", "DELETE", "CREATE", "TABLE",
          "DROP", "ALTER", "PRIMARY", "KEY", "FOREIGN", "REFERENCES", "JOIN", "INNER", "LEFT",
          "RIGHT", "ON", "GROUP", "BY", "ORDER", "LIMIT", "PRAGMA", "VALUES", "DEFAULT", "NOT",
          "NULL", "INTEGER", "TEXT", "REAL", "BLOB", "AND", "OR", "AS", "SET"
        )
        cleanLang in listOf("md", "markdown") -> emptyList()
        else -> listOf(
          "package", "import", "class", "interface", "object", "val", "var", "fun",
          "return", "if", "else", "when", "for", "while", "data", "override", "suspend",
          "private", "public", "protected", "sealed", "abstract", "const", "true", "false",
          "null", "typealias", "enum", "companion", "constructor", "init"
        )
      }

      for (kw in keywords) {
        val regex = if (cleanLang == "sql") Regex("\\b$kw\\b", RegexOption.IGNORE_CASE) else Regex("\\b$kw\\b")
        regex.findAll(raw).forEach { match ->
          safeStyle(SpanStyle(color = CodeKeyword, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
        }
      }

      // 2. HTML Tags and Attributes
      if (cleanLang in listOf("html", "htm", "xml")) {
        Regex("</?[a-zA-Z0-9_-]+").findAll(raw).forEach { match ->
          safeStyle(SpanStyle(color = LanternBrightGreen, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
        }
        Regex("\\b[a-zA-Z_-]+(?==)").findAll(raw).forEach { match ->
          safeStyle(SpanStyle(color = LanternCyan), match.range.first, match.range.last + 1)
        }
      }

      // 3. Markdown Specific Elements
      if (cleanLang in listOf("md", "markdown")) {
        Regex("(?m)^#{1,6}\\s+.*$").findAll(raw).forEach { match ->
          safeStyle(SpanStyle(color = LanternBrightGreen, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
        }
        Regex("\\*\\*.*?\\*\\*").findAll(raw).forEach { match ->
          safeStyle(SpanStyle(color = CodeKeyword, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
        }
        Regex("`.*?`").findAll(raw).forEach { match ->
          safeStyle(SpanStyle(color = CodeNumber), match.range.first, match.range.last + 1)
        }
      }

      // 4. String Literals ("...", '...', `...`)
      Regex("\".*?\"|'.*?'|`.*?`").findAll(raw).forEach { match ->
        safeStyle(SpanStyle(color = CodeString), match.range.first, match.range.last + 1)
      }

      // 5. Comments
      val commentPatterns = when {
        cleanLang in listOf("py", "python", "sh", "bash") -> listOf("#.*")
        cleanLang == "sql" -> listOf("--.*")
        cleanLang in listOf("html", "htm", "xml") -> listOf("<!--.*?-->")
        else -> listOf("//.*", "/\\*.*?\\*/")
      }
      for (pat in commentPatterns) {
        Regex(pat).findAll(raw).forEach { match ->
          safeStyle(SpanStyle(color = CodeComment), match.range.first, match.range.last + 1)
        }
      }

      // 6. Numbers
      Regex("\\b\\d+(\\.\\d+)?\\b").findAll(raw).forEach { match ->
        safeStyle(SpanStyle(color = CodeNumber), match.range.first, match.range.last + 1)
      }

      // 7. Annotations / Decorators (@...)
      Regex("@[a-zA-Z0-9_]+").findAll(raw).forEach { match ->
        safeStyle(SpanStyle(color = CodeFunction, fontWeight = FontWeight.SemiBold), match.range.first, match.range.last + 1)
      }
    }

    return TransformedText(annotated, OffsetMapping.Identity)
  }
}
