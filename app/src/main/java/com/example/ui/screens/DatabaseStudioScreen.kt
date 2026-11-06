package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
fun DatabaseStudioScreen(
  viewModel: WorkspaceViewModel,
  modifier: Modifier = Modifier
) {
  val sqlResult by viewModel.sqlQueryResult.collectAsStateWithLifecycle()
  var sqlInput by remember { mutableStateOf("SELECT * FROM projects LIMIT 10;") }

  val sampleQueries = listOf(
    "SELECT * FROM projects",
    "SELECT * FROM project_files",
    "SELECT * FROM git_commits",
    "SELECT * FROM audit_reports",
    "SELECT * FROM peer_sessions",
    "PRAGMA table_info(projects)"
  )

  val tables = listOf(
    "projects" to "Core workspace metadata",
    "project_files" to "Source files and code content",
    "git_commits" to "Local Git history & branches",
    "audit_reports" to "Code security & health scans",
    "peer_sessions" to "WebRTC mesh participant presence"
  )

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      // Header Card
      Card(
        modifier = Modifier.fillMaxWidth().testTag("sqlite_studio_header_card"),
        colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LanternCyan))
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Storage, contentDescription = null, tint = LanternCyan, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Room SQLite Database Studio",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = LanternBrightGreen
                )
              )
              Text(
                text = "File: lantern_code_workspace.db (WAL Mode)",
                style = MaterialTheme.typography.bodySmall.copy(color = LanternTextSecondary)
              )
            }
          }
        }
      }
    }

    item {
      // Schema Browser
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Active SQLite Tables",
            fontWeight = FontWeight.Bold,
            color = LanternTextPrimary,
            fontSize = 14.sp
          )
          Spacer(modifier = Modifier.height(10.dp))

          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tables) { (tbl, desc) ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(MaterialTheme.colorScheme.background)
                  .border(1.dp, LanternObsidianBorder, RoundedCornerShape(8.dp))
                  .clickable {
                    sqlInput = "SELECT * FROM $tbl LIMIT 20;"
                    viewModel.executeSql(sqlInput)
                  }
                  .padding(10.dp)
              ) {
                Column {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TableChart, contentDescription = null, tint = LanternCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(tbl, fontWeight = FontWeight.Bold, color = LanternCyan, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                  }
                  Text(desc, fontSize = 10.sp, color = LanternTextSecondary)
                }
              }
            }
          }
        }
      }
    }

    item {
      // SQL Query Editor
      Card(
        modifier = Modifier.fillMaxWidth().testTag("sql_query_card"),
        colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Interactive SQL Query Console",
            fontWeight = FontWeight.Bold,
            color = LanternTextPrimary,
            fontSize = 14.sp
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Quick Query Chips
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(sampleQueries) { query ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(LanternCyan.copy(alpha = 0.15f))
                  .clickable {
                    sqlInput = "$query;"
                    viewModel.executeSql(sqlInput)
                  }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = query,
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace,
                  color = LanternCyan
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = sqlInput,
            onValueChange = { sqlInput = it },
            placeholder = { Text("Enter SQL query (SELECT, INSERT, CREATE TABLE, PRAGMA)...") },
            modifier = Modifier
              .fillMaxWidth()
              .height(110.dp)
              .testTag("sql_query_input_field"),
            textStyle = androidx.compose.ui.text.TextStyle(
              fontFamily = FontFamily.Monospace,
              fontSize = 13.sp,
              color = LanternTextPrimary
            ),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = LanternCyan,
              focusedContainerColor = MaterialTheme.colorScheme.background,
              unfocusedContainerColor = MaterialTheme.colorScheme.background
            )
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Supports full SQLite syntax & DDL",
              fontSize = 11.sp,
              color = LanternTextMuted
            )

            Button(
              onClick = { viewModel.executeSql(sqlInput) },
              colors = ButtonDefaults.buttonColors(containerColor = LanternCyan, contentColor = Color(0xFF002F48)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("execute_sql_button")
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Run Query", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    item {
      // Results View
      if (sqlResult != null) {
        val res = sqlResult!!
        Card(
          modifier = Modifier.fillMaxWidth().testTag("sql_results_card"),
          colors = CardDefaults.cardColors(containerColor = LanternObsidianCard),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Query Results (${res.rowCount} rows)",
                fontWeight = FontWeight.Bold,
                color = LanternBrightGreen,
                fontSize = 13.sp
              )
              Text(
                text = "Execution time: ${res.executionTimeMs}ms",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = LanternCyan
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (res.error != null) {
              Text(
                text = "Error: ${res.error}",
                color = Color(0xFFEF4444),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
              )
            } else if (res.rows.isEmpty()) {
              Text(
                text = "0 rows returned.",
                color = LanternTextSecondary,
                fontSize = 12.sp
              )
            } else {
              // Tabular Grid Display
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState())
              ) {
                Column {
                  // Table Header
                  Row(
                    modifier = Modifier
                      .background(LanternObsidianBorder)
                      .padding(vertical = 6.dp)
                  ) {
                    res.columns.forEach { col ->
                      Text(
                        text = col,
                        fontWeight = FontWeight.Bold,
                        color = LanternBrightGreen,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                          .width(130.dp)
                          .padding(horizontal = 8.dp)
                      )
                    }
                  }

                  // Table Rows
                  res.rows.forEachIndexed { idx, row ->
                    Row(
                      modifier = Modifier
                        .background(if (idx % 2 == 0) MaterialTheme.colorScheme.background else LanternObsidianCard)
                        .padding(vertical = 6.dp)
                    ) {
                      row.forEach { cell ->
                        Text(
                          text = cell,
                          color = LanternTextPrimary,
                          fontSize = 11.sp,
                          fontFamily = FontFamily.Monospace,
                          maxLines = 1,
                          modifier = Modifier
                            .width(130.dp)
                            .padding(horizontal = 8.dp)
                        )
                      }
                    }
                  }
                }
              }
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
