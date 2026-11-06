package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.example.ui.theme.CodeComment
import com.example.ui.theme.CodeFunction
import com.example.ui.theme.CodeKeyword
import com.example.ui.theme.CodeNumber
import com.example.ui.theme.CodeString

class CodeSyntaxVisualTransformation(private val language: String = "kotlin") : VisualTransformation {
  override fun filter(text: AnnotatedString): TransformedText {
    val raw = text.text
    val annotated = buildAnnotatedString {
      append(raw)

      // 1. Keywords
      val keywords = listOf(
        "package", "import", "class", "interface", "object", "val", "var", "fun",
        "return", "if", "else", "when", "for", "while", "data", "override", "suspend",
        "private", "public", "protected", "sealed", "abstract", "const", "true", "false",
        "null", "SELECT", "FROM", "WHERE", "INSERT", "INTO", "CREATE", "TABLE", "PRIMARY",
        "KEY", "DEFAULT", "PRAGMA", "VALUES"
      )
      for (kw in keywords) {
        val regex = Regex("\\b$kw\\b")
        regex.findAll(raw).forEach { match ->
          addStyle(
            style = SpanStyle(color = CodeKeyword, fontWeight = FontWeight.Bold),
            start = match.range.first,
            end = match.range.last + 1
          )
        }
      }

      // 2. String Literals ("...")
      Regex("\".*?\"").findAll(raw).forEach { match ->
        addStyle(
          style = SpanStyle(color = CodeString),
          start = match.range.first,
          end = match.range.last + 1
        )
      }

      // 3. Comments (//... and /*...*/)
      Regex("//.*").findAll(raw).forEach { match ->
        addStyle(
          style = SpanStyle(color = CodeComment),
          start = match.range.first,
          end = match.range.last + 1
        )
      }

      // 4. Numbers
      Regex("\\b\\d+\\b").findAll(raw).forEach { match ->
        addStyle(
          style = SpanStyle(color = CodeNumber),
          start = match.range.first,
          end = match.range.last + 1
        )
      }

      // 5. Annotations (@...)
      Regex("@[a-zA-Z0-9_]+").findAll(raw).forEach { match ->
        addStyle(
          style = SpanStyle(color = CodeFunction, fontWeight = FontWeight.SemiBold),
          start = match.range.first,
          end = match.range.last + 1
        )
      }
    }

    return TransformedText(annotated, OffsetMapping.Identity)
  }
}
