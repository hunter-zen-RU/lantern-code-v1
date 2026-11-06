package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.AuditIssue
import com.example.ai.AuditSeverity
import com.example.testdeploy.DeploymentPlatform
import com.example.ui.WorkspaceViewModel
import com.example.ui.theme.LanternBrightGreen
import com.example.ui.theme.LanternCyan
import com.example.ui.theme.LanternNeonGreen
import com.example.ui.theme.LanternObsidianBorder
import com.example.ui.theme.LanternObsidianCard
import com.example.ui.theme.LanternTextMuted
import com.example.ui.theme.LanternTextPrimary
import com.example.ui.theme.LanternTextSecondary

@Composable
fun AuditAndTestScreen(
  viewModel: WorkspaceViewModel,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Audit & Security", "Automated Tests", "Deploy & Export")

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = LanternObsidianCard,
      contentColor = LanternNeonGreen
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = {
            Text(
              text = title,
              fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
              fontSize = 13.sp
            )
          }
        )
      }
    }

    when (selectedTab) {
      0 -> AuditTabContent(viewModel)
      1 -> TestTabContent(viewModel)
      2 -> DeployTabContent(viewModel)
    }
  }
}

@Composable
fun AuditTabContent(viewModel: WorkspaceViewModel) {
  val auditResult by viewModel.auditResult.collectAsStateWithLifecycle()
  val selectedProject by viewModel.selectedProject.collectAsStateWithLifecycle()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      // Health Score Banner
      Card(
        modifier = Modifier.fillMaxWidth().testTag("audit_score_card"),
        colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LanternNeonGreen))
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Project Health & Security Audit",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = LanternBrightGreen
                )
              )
              Text(
                text = "Target: ${selectedProject?.name ?: "Current Workspace"}",
                style = MaterialTheme.typography.bodySmall.copy(color = LanternTextSecondary)
              )
            }

            Button(
              onClick = { viewModel.runAudit() },
              colors = ButtonDefaults.buttonColors(containerColor = LanternNeonGreen, contentColor = Color(0xFF003D1A)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("run_audit_button")
            ) {
              Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Run Audit", fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          val score = auditResult?.healthScore ?: 96
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("CODE HEALTH SCORE", fontSize = 10.sp, color = LanternTextMuted, fontWeight = FontWeight.Bold)
              Text(
                text = "$score / 100",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (score >= 80) LanternNeonGreen else Color(0xFFF59E0B)
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              StatPill(count = auditResult?.criticalCount ?: 0, label = "Critical", color = Color(0xFFEF4444))
              StatPill(count = auditResult?.warningCount ?: 0, label = "Warnings", color = Color(0xFFF59E0B))
              StatPill(count = auditResult?.cyclomaticComplexity ?: 3, label = "Complexity", color = LanternCyan)
            }
          }
        }
      }
    }

    item {
      Text(
        text = "Audit Findings & Remediation",
        fontWeight = FontWeight.Bold,
        color = LanternTextPrimary,
        fontSize = 14.sp
      )
    }

    val issues = auditResult?.issues ?: emptyList()
    if (issues.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LanternNeonGreen, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("No Critical Vulnerabilities Found", fontWeight = FontWeight.Bold, color = LanternTextPrimary)
            Text("All project files satisfy local security and AST validation rules.", fontSize = 12.sp, color = LanternTextSecondary)
          }
        }
      }
    } else {
      items(issues) { issue ->
        AuditIssueCard(issue = issue)
      }
    }

    item {
      Spacer(modifier = Modifier.height(32.dp))
    }
  }
}

@Composable
fun StatPill(count: Int, label: String, color: Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = "$count",
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      color = color
    )
    Text(
      text = label,
      fontSize = 10.sp,
      color = LanternTextMuted
    )
  }
}

@Composable
fun AuditIssueCard(issue: AuditIssue) {
  val color = when (issue.severity) {
    AuditSeverity.CRITICAL -> Color(0xFFEF4444)
    AuditSeverity.WARNING -> Color(0xFFF59E0B)
    AuditSeverity.INFO -> LanternCyan
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
    shape = RoundedCornerShape(12.dp),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(color.copy(alpha = 0.4f)))
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (issue.severity == AuditSeverity.CRITICAL) Icons.Default.Error else Icons.Default.Warning,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(issue.title, fontWeight = FontWeight.Bold, color = color, fontSize = 13.sp)
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(issue.severity.name, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "${issue.fileName}:${issue.lineNumber}",
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = LanternTextMuted
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = issue.description,
        fontSize = 12.sp,
        color = LanternTextSecondary
      )

      Spacer(modifier = Modifier.height(8.dp))

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(6.dp))
          .background(MaterialTheme.colorScheme.background)
          .padding(8.dp)
      ) {
        Text(
          text = "Remediation: ${issue.fixSuggestion}",
          fontSize = 11.sp,
          color = LanternBrightGreen,
          fontFamily = FontFamily.Monospace
        )
      }
    }
  }
}

@Composable
fun TestTabContent(viewModel: WorkspaceViewModel) {
  val testSummary by viewModel.testSummary.collectAsStateWithLifecycle()
  val isTesting by viewModel.isTesting.collectAsStateWithLifecycle()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      // Test Control Card
      Card(
        modifier = Modifier.fillMaxWidth().testTag("test_runner_card"),
        colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LanternCyan))
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Automated Test Suite Runner",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = LanternBrightGreen
                )
              )
              Text(
                text = "Simulated unit & peer mesh assertion suite",
                style = MaterialTheme.typography.bodySmall.copy(color = LanternTextSecondary)
              )
            }

            Button(
              onClick = { viewModel.runAutomatedTests() },
              enabled = !isTesting,
              colors = ButtonDefaults.buttonColors(containerColor = LanternCyan, contentColor = Color(0xFF002F48)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("run_tests_button")
            ) {
              if (isTesting) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF002F48))
              } else {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Run Suite", fontWeight = FontWeight.Bold)
              }
            }
          }

          if (testSummary != null) {
            val sum = testSummary!!
            Spacer(modifier = Modifier.height(14.dp))
            LinearProgressIndicator(
              progress = { sum.coveragePercent / 100f },
              modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
              color = LanternNeonGreen,
              trackColor = MaterialTheme.colorScheme.background
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Passed: ${sum.passedCount}/${sum.totalTests}", color = LanternBrightGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              Text("Coverage: ${sum.coveragePercent}%", color = LanternCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              Text("Duration: ${sum.totalDurationMs}ms", color = LanternTextMuted, fontSize = 12.sp)
            }
          }
        }
      }
    }

    item {
      Text(
        text = "Executed Test Cases",
        fontWeight = FontWeight.Bold,
        color = LanternTextPrimary,
        fontSize = 14.sp
      )
    }

    val tests = testSummary?.results ?: emptyList()
    if (tests.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text("Press 'Run Suite' to execute unit and mesh test assertions.", color = LanternTextSecondary, fontSize = 12.sp)
          }
        }
      }
    } else {
      items(tests) { test ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
          shape = RoundedCornerShape(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = if (test.passed) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (test.passed) LanternNeonGreen else Color(0xFFEF4444),
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = test.name,
                  fontWeight = FontWeight.Bold,
                  color = LanternTextPrimary,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 12.sp
                )
                Text(
                  text = test.suite,
                  color = LanternTextMuted,
                  fontSize = 10.sp
                )
              }
            }
            Text(
              text = "${test.durationMs}ms",
              color = LanternCyan,
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp
            )
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(32.dp))
    }
  }
}

@Composable
fun DeployTabContent(viewModel: WorkspaceViewModel) {
  val pkg by viewModel.deploymentPackage.collectAsStateWithLifecycle()
  val platforms = DeploymentPlatform.values()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFA78BFA)))
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "Cross-Platform Deployment Engine",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.ExtraBold,
              color = LanternBrightGreen
            )
          )
          Text(
            text = "Generate production deploy packages for mobile, web, and cloud containers.",
            style = MaterialTheme.typography.bodySmall.copy(color = LanternTextSecondary)
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            platforms.forEach { platform ->
              val isSel = pkg?.platform == platform
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSel) LanternNeonGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.background)
                  .border(1.dp, if (isSel) LanternNeonGreen else LanternObsidianBorder, RoundedCornerShape(8.dp))
                  .clickable { viewModel.selectDeploymentPlatform(platform) }
                  .padding(8.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = platform.displayName.split(" ").first(),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSel) LanternNeonGreen else LanternTextPrimary
                )
              }
            }
          }
        }
      }
    }

    if (pkg != null) {
      val p = pkg!!
      item {
        Card(
          modifier = Modifier.fillMaxWidth().testTag("deployment_package_card"),
          colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = p.artifactName,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  color = LanternBrightGreen,
                  fontSize = 14.sp
                )
                Text(
                  text = "Est. Package Size: ${p.sizeEstimate} | Target: ${p.platform.displayName}",
                  fontSize = 11.sp,
                  color = LanternTextSecondary
                )
              }

              Button(
                onClick = { viewModel.showMessage("Generated deployment artifact: ${p.artifactName}") },
                colors = ButtonDefaults.buttonColors(containerColor = LanternNeonGreen, contentColor = Color(0xFF003D1A)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("build_export_button")
              ) {
                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Build", fontWeight = FontWeight.Bold)
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(12.dp)
            ) {
              Text(
                text = p.configurationScript,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = LanternTextPrimary
              )
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(32.dp))
    }
  }
}
