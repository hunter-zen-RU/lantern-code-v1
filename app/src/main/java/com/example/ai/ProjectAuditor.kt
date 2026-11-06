package com.example.ai

import com.example.data.entity.AuditReportEntity
import com.example.data.entity.ProjectFileEntity
import org.json.JSONArray
import org.json.JSONObject

enum class AuditSeverity { CRITICAL, WARNING, INFO }

data class AuditIssue(
  val title: String,
  val description: String,
  val fileName: String,
  val lineNumber: Int,
  val severity: AuditSeverity,
  val fixSuggestion: String
)

data class AuditAnalysisResult(
  val healthScore: Int,
  val criticalCount: Int,
  val warningCount: Int,
  val infoCount: Int,
  val cyclomaticComplexity: Int,
  val issues: List<AuditIssue>,
  val timestamp: Long = System.currentTimeMillis()
)

object ProjectAuditor {

  fun auditFiles(projectId: Long, files: List<ProjectFileEntity>): AuditAnalysisResult {
    val issues = mutableListOf<AuditIssue>()
    var totalComplexity = 0

    for (file in files) {
      val lines = file.content.lines()

      lines.forEachIndexed { index, line ->
        val lineNum = index + 1
        val trimmed = line.trim()

        // 1. Hardcoded Secret / Token Check
        if (trimmed.contains("sk-", ignoreCase = true) ||
          trimmed.contains("AKIA", ignoreCase = true) ||
          (trimmed.contains("password", ignoreCase = true) && trimmed.contains("=") && !trimmed.contains("val password:")) ||
          (trimmed.contains("api_key", ignoreCase = true) && trimmed.contains("\"") && !trimmed.contains("BuildConfig"))
        ) {
          issues.add(
            AuditIssue(
              title = "Hardcoded Credential / Secret Exposed",
              description = "Sensitive token or credentials detected in source file. Hardcoding secrets breaches Google Play policy and security standards.",
              fileName = file.name,
              lineNumber = lineNum,
              severity = AuditSeverity.CRITICAL,
              fixSuggestion = "Move secrets to BuildConfig / Secrets Panel in AI Studio."
            )
          )
        }

        // 2. Insecure Cleartext HTTP Protocol Check
        if (trimmed.contains("http://") && !trimmed.contains("10.0.2.2") && !trimmed.contains("localhost")) {
          issues.add(
            AuditIssue(
              title = "Insecure Cleartext HTTP Endpoint",
              description = "Cleartext HTTP traffic is intercepted easily on public LAN/WAN networks.",
              fileName = file.name,
              lineNumber = lineNum,
              severity = AuditSeverity.WARNING,
              fixSuggestion = "Upgrade endpoint URI to HTTPS or wrap with encrypted TLS channel."
            )
          )
        }

        // 3. SQL Injection risk check
        if (file.language.equals("kotlin", ignoreCase = true) &&
          trimmed.contains("SELECT", ignoreCase = true) &&
          trimmed.contains("+") &&
          trimmed.contains("FROM", ignoreCase = true)
        ) {
          issues.add(
            AuditIssue(
              title = "Possible SQL Injection in Raw Query",
              description = "Unparameterized SQL string concatenation detected.",
              fileName = file.name,
              lineNumber = lineNum,
              severity = AuditSeverity.CRITICAL,
              fixSuggestion = "Use parameterized queries ('?1') or Room @Query with bind arguments."
            )
          )
        }

        // 4. Empty Exception Catch Block
        if (trimmed.startsWith("catch") && (trimmed.endsWith("{}") || trimmed.contains("{ }"))) {
          issues.add(
            AuditIssue(
              title = "Silent Exception Swallowing",
              description = "Empty catch block conceals runtime failures, deadlocks, and corrupted states.",
              fileName = file.name,
              lineNumber = lineNum,
              severity = AuditSeverity.WARNING,
              fixSuggestion = "Log the exception via Log.e or propagate to UI error state."
            )
          )
        }

        // 5. Complexity Estimation
        if (trimmed.startsWith("if ") || trimmed.startsWith("when ") || trimmed.startsWith("for ") || trimmed.startsWith("while ")) {
          totalComplexity++
        }
      }

      // File-level checks
      if (file.content.length > 15000) {
        issues.add(
          AuditIssue(
            title = "Oversized Monolithic Source File",
            description = "File '${file.name}' exceeds recommended size limits. Large files impair mobile IDE performance and reviewability.",
            fileName = file.name,
            lineNumber = 1,
            severity = AuditSeverity.INFO,
            fixSuggestion = "Modularize functions into distinct focused modules."
          )
        )
      }
    }

    val criticalCount = issues.count { it.severity == AuditSeverity.CRITICAL }
    val warningCount = issues.count { it.severity == AuditSeverity.WARNING }
    val infoCount = issues.count { it.severity == AuditSeverity.INFO }

    // Calculate health score (0 - 100)
    val deductions = (criticalCount * 25) + (warningCount * 8) + (infoCount * 2)
    val healthScore = (100 - deductions).coerceIn(10, 100)

    return AuditAnalysisResult(
      healthScore = healthScore,
      criticalCount = criticalCount,
      warningCount = warningCount,
      infoCount = infoCount,
      cyclomaticComplexity = totalComplexity.coerceAtLeast(1),
      issues = issues
    )
  }

  fun toEntity(projectId: Long, result: AuditAnalysisResult): AuditReportEntity {
    val detailsJson = JSONObject().apply {
      put("complexity", result.cyclomaticComplexity)
      val issuesArray = JSONArray()
      for (issue in result.issues) {
        issuesArray.put(JSONObject().apply {
          put("title", issue.title)
          put("description", issue.description)
          put("fileName", issue.fileName)
          put("lineNumber", issue.lineNumber)
          put("severity", issue.severity.name)
          put("fix", issue.fixSuggestion)
        })
      }
      put("issues", issuesArray)
    }

    return AuditReportEntity(
      projectId = projectId,
      score = result.healthScore,
      securityIssuesCount = result.criticalCount,
      warningsCount = result.warningCount,
      detailsJson = detailsJson.toString()
    )
  }
}
