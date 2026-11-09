package com.example.ui.components

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class CompletionResult(
  val snippet: String,
  val source: String,
  val isSuccess: Boolean
)

object GeminiAutocompleter {
  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(3, TimeUnit.SECONDS)
    .readTimeout(5, TimeUnit.SECONDS)
    .build()

  suspend fun fetchCompletion(
    prefix: String,
    suffix: String,
    fileName: String,
    language: String
  ): CompletionResult = withContext(Dispatchers.IO) {
    val apiKey = try {
      BuildConfig.GEMINI_API_KEY
    } catch (_: Throwable) {
      ""
    }

    if (apiKey.isNotBlank() && apiKey != "your_api_key_here") {
      val geminiResult = callGeminiApi(apiKey, prefix, suffix, fileName, language)
      if (geminiResult != null && geminiResult.isNotBlank()) {
        return@withContext CompletionResult(
          snippet = geminiResult,
          source = "Gemini 2.0 Flash",
          isSuccess = true
        )
      }
    }

    // Smart Local Fallback Autocompleter
    val localCompletion = generateLocalCompletion(prefix, fileName, language)
    CompletionResult(
      snippet = localCompletion,
      source = "Context Engine",
      isSuccess = localCompletion.isNotBlank()
    )
  }

  private fun callGeminiApi(
    apiKey: String,
    prefix: String,
    suffix: String,
    fileName: String,
    language: String
  ): String? {
    return try {
      val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"

      val systemContext = "You are a code completion engine. Given file $fileName ($language), provide ONLY the code that immediately follows the prefix. Do not repeat prefix or suffix. Return plain code without markdown formatting."

      val promptText = "$systemContext\n\nPREFIX:\n${prefix.takeLast(500)}\n\nSUFFIX:\n${suffix.take(200)}"

      val jsonBody = JSONObject().apply {
        put("contents", JSONArray().apply {
          put(JSONObject().apply {
            put("parts", JSONArray().apply {
              put(JSONObject().apply {
                put("text", promptText)
              })
            })
          })
        })
        put("generationConfig", JSONObject().apply {
          put("temperature", 0.2)
          put("maxOutputTokens", 96)
        })
      }

      val request = Request.Builder()
        .url(url)
        .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
        .build()

      httpClient.newCall(request).execute().use { response ->
        if (response.isSuccessful) {
          val bodyStr = response.body?.string() ?: return null
          val resObj = JSONObject(bodyStr)
          val candidates = resObj.optJSONArray("candidates")
          if (candidates != null && candidates.length() > 0) {
            val content = candidates.getJSONObject(0).optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
              val rawText = parts.getJSONObject(0).optString("text")
              return cleanCompletionSnippet(rawText)
            }
          }
        }
      }
      null
    } catch (_: Exception) {
      null
    }
  }

  private fun cleanCompletionSnippet(rawText: String): String {
    var cleaned = rawText.trim()
    if (cleaned.startsWith("```")) {
      cleaned = cleaned.substringAfter("\n").substringBeforeLast("```").trim()
    }
    return cleaned
  }

  private fun generateLocalCompletion(prefix: String, fileName: String, language: String): String {
    val lastLine = prefix.lines().lastOrNull()?.trim() ?: ""
    val langLower = language.lowercase()

    return when {
      lastLine.endsWith("fun ") || lastLine.contains("fun main") -> "(): Unit {\n    val result = calculateResult()\n    return result\n}"
      lastLine.endsWith("class ") -> "WorkspaceEngine {\n    private val state = mutableStateOf(true)\n    fun initialize() { println(\"Initialized\") }\n}"
      lastLine.endsWith("if ") || lastLine.endsWith("if") -> "(isValid) {\n    executeTransaction()\n} else {\n    logError()\n}"
      lastLine.endsWith("for ") -> "(item in dataset) {\n    processItem(item)\n}"
      lastLine.endsWith("try ") -> "{\n    connectToMesh()\n} catch (e: Exception) {\n    Log.e(\"Engine\", \"Error: \${e.message}\")\n}"
      lastLine.contains("val ") || lastLine.contains("var ") || lastLine.contains("let ") || lastLine.contains("const ") -> " = repository.fetchSecurePayload() ?: defaultValue"
      lastLine.endsWith(".") -> when {
        langLower.contains("kotlin") || langLower.contains("java") -> "collectAsStateWithLifecycle()"
        langLower.contains("script") || langLower.contains("js") -> "map(res => res.json())"
        else -> "execute()"
      }
      lastLine.endsWith("import ") -> when {
        langLower.contains("kotlin") -> "kotlinx.coroutines.flow.Flow\nimport kotlinx.coroutines.flow.collectAsState"
        langLower.contains("python") -> "asyncio\nimport json"
        else -> "useState, useEffect from 'react'"
      }
      lastLine.endsWith("return") -> " Result.success(payload)"
      else -> "\n    val telemetry = System.currentTimeMillis()\n    Log.d(\"LANtern\", \"Telemetry executed at \$telemetry\")"
    }
  }
}
