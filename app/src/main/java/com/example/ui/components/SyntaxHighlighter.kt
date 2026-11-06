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

class CodeSyntaxVisualTransformation(
  private val language: String = "",
  private val fileName: String = ""
) : VisualTransformation {

  override fun filter(text: AnnotatedString): TransformedText {
    val raw = text.text
    if (raw.isEmpty()) {
      return TransformedText(text, OffsetMapping.Identity)
    }

    val cleanLang = resolveLanguage(language, fileName)
    val annotated = buildAnnotatedString {
      append(raw)

      fun safeStyle(style: SpanStyle, start: Int, end: Int) {
        val s = start.coerceIn(0, raw.length)
        val e = end.coerceIn(0, raw.length)
        if (s < e) {
          addStyle(style, s, e)
        }
      }

      // 1. Language Keywords
      val keywords = getKeywordsForLanguage(cleanLang)
      for (kw in keywords) {
        val regex = if (cleanLang == "sql") Regex("\\b$kw\\b", RegexOption.IGNORE_CASE) else Regex("\\b$kw\\b")
        regex.findAll(raw).forEach { match ->
          safeStyle(SpanStyle(color = CodeKeyword, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
        }
      }

      // 2. HTML / XML / SVG Tags and Attributes
      if (cleanLang in listOf("html", "xml")) {
        Regex("</?[a-zA-Z0-9_\\-:]+").findAll(raw).forEach { match ->
          safeStyle(SpanStyle(color = LanternBrightGreen, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
        }
        Regex("\\b[a-zA-Z_\\-:]+(?=\\s*=)").findAll(raw).forEach { match ->
          safeStyle(SpanStyle(color = LanternCyan), match.range.first, match.range.last + 1)
        }
      }

      // 3. CSS / SCSS Selectors and Properties
      if (cleanLang == "css") {
        Regex("[\\.#][a-zA-Z0-9_\\-]+").findAll(raw).forEach { match ->
          safeStyle(SpanStyle(color = LanternBrightGreen, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
        }
        Regex("\\b[a-zA-Z\\-]+(?=\\s*:)").findAll(raw).forEach { match ->
          safeStyle(SpanStyle(color = LanternCyan), match.range.first, match.range.last + 1)
        }
        Regex("#[0-9a-fA-F]{3,8}\\b").findAll(raw).forEach { match ->
          safeStyle(SpanStyle(color = CodeNumber, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
        }
      }

      // 4. JSON / YAML / Config Keys
      if (cleanLang in listOf("json", "yaml", "config")) {
        Regex("\"([^\"]|\\\\.)*\"\\s*(?=:)|^[a-zA-Z0-9_\\-\\.\\s]+(?=\\s*=)|^[a-zA-Z0-9_\\-\\.\\s]+(?=\\s*:)").findAll(raw).forEach { match ->
          safeStyle(SpanStyle(color = LanternCyan, fontWeight = FontWeight.SemiBold), match.range.first, match.range.last + 1)
        }
      }

      // 5. Shell Variables
      if (cleanLang == "shell") {
        Regex("\\$[a-zA-Z0-9_]+|\\$\\{[^\\}]+\\}").findAll(raw).forEach { match ->
          safeStyle(SpanStyle(color = LanternCyan, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
        }
      }

      // 6. Markdown Specific Elements
      if (cleanLang == "markdown") {
        Regex("(?m)^#{1,6}\\s+.*$").findAll(raw).forEach { match ->
          safeStyle(SpanStyle(color = LanternBrightGreen, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
        }
        Regex("\\*\\*.*?\\*\\*").findAll(raw).forEach { match ->
          safeStyle(SpanStyle(color = CodeKeyword, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
        }
        Regex("`.*?`").findAll(raw).forEach { match ->
          safeStyle(SpanStyle(color = CodeNumber), match.range.first, match.range.last + 1)
        }
        Regex("(?m)^[\\s]*[\\-\\*\\+]\\s+|(?m)^[\\s]*\\d+\\.\\s+").findAll(raw).forEach { match ->
          safeStyle(SpanStyle(color = LanternCyan, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
        }
      }

      // 7. String Literals ("...", '...', `...`)
      if (cleanLang != "markdown") {
        Regex("\".*?\"|'.*?'|`.*?`").findAll(raw).forEach { match ->
          safeStyle(SpanStyle(color = CodeString), match.range.first, match.range.last + 1)
        }
      }

      // 8. Comments
      val commentPatterns = when (cleanLang) {
        "python", "shell", "yaml", "config" -> listOf("#.*")
        "sql" -> listOf("--.*", "/\\*.*?\\*/")
        "html", "xml" -> listOf("<!--.*?-->")
        else -> listOf("//.*", "/\\*.*?\\*/")
      }
      for (pat in commentPatterns) {
        Regex(pat).findAll(raw).forEach { match ->
          safeStyle(SpanStyle(color = CodeComment), match.range.first, match.range.last + 1)
        }
      }

      // 9. Numbers
      Regex("\\b\\d+(\\.\\d+)?(px|em|rem|%|vh|vw|s|ms|pt|deg)?\\b").findAll(raw).forEach { match ->
        safeStyle(SpanStyle(color = CodeNumber), match.range.first, match.range.last + 1)
      }

      // 10. Annotations / Decorators / Macros (@..., #include)
      Regex("@[a-zA-Z0-9_]+|#[a-zA-Z0-9_]+").findAll(raw).forEach { match ->
        safeStyle(SpanStyle(color = CodeFunction, fontWeight = FontWeight.SemiBold), match.range.first, match.range.last + 1)
      }
    }

    return TransformedText(annotated, OffsetMapping.Identity)
  }

  private fun resolveLanguage(language: String, fileName: String): String {
    val ext = fileName.substringAfterLast(".", "").lowercase()
    val lang = language.lowercase()

    return when {
      ext in listOf("kt", "kts") || lang in listOf("kotlin", "kt") -> "kotlin"
      ext in listOf("java") || lang == "java" -> "java"
      ext in listOf("js", "jsx", "mjs", "cjs") || lang in listOf("javascript", "js") -> "javascript"
      ext in listOf("ts", "tsx") || lang in listOf("typescript", "ts") -> "typescript"
      ext in listOf("html", "htm") || lang in listOf("html", "htm") -> "html"
      ext in listOf("css", "scss", "less") || lang == "css" -> "css"
      ext in listOf("json") || lang == "json" -> "json"
      ext in listOf("yaml", "yml") || lang in listOf("yaml", "yml") -> "yaml"
      ext in listOf("py", "pyw") || lang in listOf("python", "py") -> "python"
      ext in listOf("c", "h") || lang == "c" -> "c"
      ext in listOf("cpp", "hpp", "cc", "cxx") || lang in listOf("cpp", "c++") -> "cpp"
      ext in listOf("rs") || lang in listOf("rust", "rs") -> "rust"
      ext in listOf("go") || lang == "go" -> "go"
      ext in listOf("sql", "sqlite", "db") || lang == "sql" -> "sql"
      ext in listOf("sh", "bash", "zsh") || lang in listOf("sh", "bash") -> "shell"
      ext in listOf("xml", "svg", "plist") || lang == "xml" -> "xml"
      ext in listOf("md", "markdown") || lang in listOf("markdown", "md") -> "markdown"
      ext in listOf("php") || lang == "php" -> "php"
      ext in listOf("rb") || lang in listOf("ruby", "rb") -> "ruby"
      ext in listOf("properties", "env", "ini", "toml") || lang in listOf("config", "env", "properties") -> "config"
      else -> if (lang.isNotEmpty()) lang else if (ext.isNotEmpty()) ext else "kotlin"
    }
  }

  private fun getKeywordsForLanguage(lang: String): List<String> {
    return when (lang) {
      "kotlin" -> listOf(
        "package", "import", "class", "interface", "object", "val", "var", "fun",
        "return", "if", "else", "when", "for", "while", "data", "override", "suspend",
        "private", "public", "protected", "sealed", "abstract", "const", "true", "false",
        "null", "typealias", "enum", "companion", "constructor", "init", "try", "catch",
        "finally", "throw", "this", "super", "in", "is", "as", "by"
      )
      "java" -> listOf(
        "package", "import", "public", "private", "protected", "class", "interface", "extends",
        "implements", "void", "int", "boolean", "double", "float", "long", "short", "char",
        "static", "final", "abstract", "synchronized", "volatile", "transient", "return", "if",
        "else", "for", "while", "do", "switch", "case", "default", "try", "catch", "finally",
        "throw", "throws", "new", "this", "super", "null", "true", "false"
      )
      "javascript", "typescript" -> listOf(
        "function", "const", "let", "var", "return", "if", "else", "for", "while", "class",
        "export", "import", "from", "default", "interface", "type", "async", "await", "new",
        "this", "extends", "implements", "true", "false", "null", "undefined", "typeof",
        "instanceof", "try", "catch", "finally", "throw", "yield", "of", "in", "as", "enum"
      )
      "python" -> listOf(
        "def", "class", "import", "from", "return", "if", "elif", "else", "while", "for",
        "in", "try", "except", "finally", "with", "as", "lambda", "yield", "async", "await",
        "pass", "break", "continue", "True", "False", "None", "self", "assert", "raise",
        "global", "nonlocal", "is", "not", "and", "or"
      )
      "c", "cpp" -> listOf(
        "int", "char", "float", "double", "void", "auto", "const", "static", "extern",
        "struct", "union", "enum", "typedef", "sizeof", "return", "if", "else", "for",
        "while", "do", "switch", "case", "default", "break", "continue", "goto", "class",
        "public", "private", "protected", "virtual", "override", "template", "typename",
        "namespace", "using", "try", "catch", "throw", "new", "delete", "this", "true", "false"
      )
      "rust" -> listOf(
        "fn", "let", "mut", "const", "static", "struct", "enum", "impl", "trait", "pub",
        "use", "mod", "crate", "super", "self", "Self", "return", "if", "else", "match",
        "while", "for", "in", "loop", "break", "continue", "async", "await", "where",
        "type", "unsafe", "dyn", "ref", "move", "true", "false", "None", "Some"
      )
      "go" -> listOf(
        "package", "import", "func", "type", "struct", "interface", "var", "const",
        "return", "if", "else", "for", "range", "switch", "case", "default", "select",
        "go", "defer", "chan", "map", "break", "continue", "fallthrough", "true", "false", "nil"
      )
      "sql" -> listOf(
        "SELECT", "FROM", "WHERE", "INSERT", "INTO", "UPDATE", "DELETE", "CREATE", "TABLE",
        "DROP", "ALTER", "PRIMARY", "KEY", "FOREIGN", "REFERENCES", "JOIN", "INNER", "LEFT",
        "RIGHT", "ON", "GROUP", "BY", "ORDER", "LIMIT", "PRAGMA", "VALUES", "DEFAULT", "NOT",
        "NULL", "INTEGER", "TEXT", "REAL", "BLOB", "AND", "OR", "AS", "SET", "DATABASE", "INDEX"
      )
      "shell" -> listOf(
        "if", "then", "else", "elif", "fi", "for", "in", "do", "done", "while", "case",
        "esac", "function", "return", "exit", "echo", "export", "local", "set", "unset"
      )
      "json", "yaml", "config" -> listOf("true", "false", "null")
      else -> listOf(
        "class", "function", "var", "let", "const", "val", "fun", "def", "return", "if",
        "else", "for", "while", "import", "export", "public", "private", "true", "false", "null"
      )
    }
  }
}
