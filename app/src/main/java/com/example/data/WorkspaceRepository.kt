package com.example.data

import android.content.Context
import android.database.Cursor
import com.example.data.entity.AuditReportEntity
import com.example.data.entity.GitCommitEntity
import com.example.data.entity.PeerSessionEntity
import com.example.data.entity.ProjectEntity
import com.example.data.entity.ProjectFileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

data class SqlQueryResult(
  val columns: List<String>,
  val rows: List<List<String>>,
  val executionTimeMs: Long,
  val rowCount: Int,
  val error: String? = null
)

enum class ProjectTemplate(val displayName: String, val description: String, val icon: String) {
  WEB_HTML_JS("Web App", "HTML5, CSS3 & JavaScript single page project", "🌐"),
  MARKDOWN_DOCS("Markdown & Docs", "Technical notes, guides, and specifications", "📝"),
  PYTHON("Python App", "Python scripts, data processing, and automation", "🐍"),
  NODE_TYPESCRIPT("Node & TypeScript", "TypeScript/JavaScript app with package.json", "⚡"),
  KOTLIN_ANDROID("Kotlin Engine", "Kotlin JVM logic and peer mesh handlers", "☕"),
  BLANK("Blank Workspace", "Empty workspace with custom starter file", "📄")
}

class WorkspaceRepository(private val database: LanternDatabase) {
  val allProjects: Flow<List<ProjectEntity>> = database.projectDao().getAllProjects()

  fun getFilesForProject(projectId: Long): Flow<List<ProjectFileEntity>> =
    database.projectFileDao().getFilesForProject(projectId)

  fun getCommitsForProject(projectId: Long): Flow<List<GitCommitEntity>> =
    database.gitCommitDao().getCommitsForProject(projectId)

  fun getReportsForProject(projectId: Long): Flow<List<AuditReportEntity>> =
    database.auditReportDao().getReportsForProject(projectId)

  fun getSessionsForRoom(roomId: String): Flow<List<PeerSessionEntity>> =
    database.peerSessionDao().getSessionsForRoom(roomId)

  suspend fun createProject(
    name: String,
    description: String,
    gitRemoteUrl: String = "",
    template: ProjectTemplate = ProjectTemplate.WEB_HTML_JS
  ): Long = withContext(Dispatchers.IO) {
    val project = ProjectEntity(
      name = name,
      description = description,
      gitRemoteUrl = gitRemoteUrl,
      currentBranch = "main"
    )
    val projectId = database.projectDao().insertProject(project)

    val starterFiles = generateStarterFiles(projectId, name, description, template)
    database.projectFileDao().insertFiles(starterFiles)

    // Initial Git commit
    database.gitCommitDao().insertCommit(
      GitCommitEntity(
        projectId = projectId,
        commitHash = "init-${System.currentTimeMillis().toString().takeLast(6)}",
        message = "Initial commit: scaffold $name (${template.displayName})",
        author = "LANtern Dev <dev@local>"
      )
    )

    projectId
  }

  private fun generateStarterFiles(
    projectId: Long,
    name: String,
    description: String,
    template: ProjectTemplate
  ): List<ProjectFileEntity> {
    return when (template) {
      ProjectTemplate.WEB_HTML_JS -> listOf(
        ProjectFileEntity(
          projectId = projectId,
          path = "index.html",
          name = "index.html",
          content = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>$name - LANtern Studio</title>
  <link rel="stylesheet" href="styles.css">
</head>
<body>
  <div class="container">
    <header>
      <div class="badge">💡 LANtern Code Web Studio</div>
      <h1>$name</h1>
      <p class="subtitle">$description</p>
    </header>
    <main>
      <div class="card">
        <h2>Live Web Application</h2>
        <p>Edit HTML, CSS, and JS with instant real-time live preview in LANtern Code.</p>
        <div class="interactive-demo">
          <button id="counter-btn" class="btn">Clicks: <span id="count">0</span></button>
          <button id="glow-btn" class="btn secondary">Pulse Accent</button>
        </div>
        <div id="output-log" class="console-box">Ready. Tap buttons to interact!</div>
      </div>
    </main>
  </div>
  <script src="app.js"></script>
</body>
</html>""",
          language = "html"
        ),
        ProjectFileEntity(
          projectId = projectId,
          path = "styles.css",
          name = "styles.css",
          content = """/* $name Stylesheet */
:root {
  --bg-color: #0b0f17;
  --card-bg: #161b22;
  --card-border: #30363d;
  --text-main: #f0f6fc;
  --text-muted: #8b949e;
  --primary: #00e676;
  --accent: #00e5ff;
}

body {
  margin: 0;
  padding: 20px;
  background-color: var(--bg-color);
  color: var(--text-main);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  display: flex;
  justify-content: center;
}

.container {
  width: 100%;
  max-width: 580px;
}

header {
  margin-bottom: 20px;
  text-align: center;
}

.badge {
  display: inline-block;
  padding: 4px 10px;
  background: rgba(0, 230, 118, 0.15);
  border: 1px solid var(--primary);
  color: var(--primary);
  border-radius: 12px;
  font-size: 11px;
  font-weight: bold;
  margin-bottom: 10px;
}

h1 {
  margin: 0 0 6px 0;
  font-size: 26px;
  background: linear-gradient(90deg, #00e676, #00e5ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.subtitle {
  color: var(--text-muted);
  font-size: 13px;
  margin: 0;
}

.card {
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.35);
}

.card h2 {
  margin-top: 0;
  font-size: 18px;
  color: var(--primary);
}

.interactive-demo {
  display: flex;
  gap: 10px;
  margin: 16px 0;
}

.btn {
  background: var(--primary);
  color: #003d1a;
  border: none;
  border-radius: 8px;
  padding: 10px 18px;
  font-size: 13px;
  font-weight: bold;
  cursor: pointer;
}

.btn.secondary {
  background: rgba(0, 229, 255, 0.2);
  color: var(--accent);
  border: 1px solid var(--accent);
}

.console-box {
  background: #090d13;
  border: 1px solid #21262d;
  border-radius: 6px;
  padding: 10px;
  font-family: monospace;
  font-size: 12px;
  color: var(--accent);
}
""",
          language = "css"
        ),
        ProjectFileEntity(
          projectId = projectId,
          path = "app.js",
          name = "app.js",
          content = """// $name - Client Interactive Logic
console.log("💡 LANtern Code Studio: $name running");

let clicks = 0;
const countEl = document.getElementById("count");
const counterBtn = document.getElementById("counter-btn");
const glowBtn = document.getElementById("glow-btn");
const logBox = document.getElementById("output-log");

if (counterBtn && countEl) {
  counterBtn.addEventListener("click", () => {
    clicks++;
    countEl.textContent = clicks;
    if (logBox) {
      logBox.textContent = `[EVENT] Counter incremented to ${'$'}{clicks} at ${'$'}{new Date().toLocaleTimeString()}`;
    }
  });
}

if (glowBtn && logBox) {
  glowBtn.addEventListener("click", () => {
    document.body.style.backgroundColor = document.body.style.backgroundColor === "rgb(15, 23, 42)" ? "#0b0f17" : "#0f172a";
    logBox.textContent = `[THEME] Toggled background shade`;
  });
}
""",
          language = "javascript"
        ),
        ProjectFileEntity(
          projectId = projectId,
          path = "README.md",
          name = "README.md",
          content = """# $name
$description

## 🌐 Web Project Overview
Created in **LANtern Code Studio**.
- **Preview**: Toggle the **Preview** button in the studio toolbar to view the live responsive rendering.
- **Languages**: HTML5, CSS3, JavaScript
""",
          language = "markdown"
        )
      )

      ProjectTemplate.MARKDOWN_DOCS -> listOf(
        ProjectFileEntity(
          projectId = projectId,
          path = "README.md",
          name = "README.md",
          content = """# $name
$description

## 📖 Technical Documentation Hub
Welcome to the documentation for **$name**.

### 🚀 Key Highlights
- [x] Full Markdown preview with dark mode styling
- [x] Supports GitHub-flavored Markdown
- [x] Real-time live side-by-side or tab preview

### 🛠️ Architecture Specification
> *This documentation is stored locally in device-backed SQLite via Room and syncable over WebRTC peer mesh.*

```kotlin
// Code blocks are fully supported
fun onDocumentPublished(docId: String) {
    println("Document published: ${'$'}docId")
}
```

| Module | Status | Owner |
| :--- | :--- | :--- |
| Core Specification | ✅ Done | @lead |
| API Contracts | ⚡ In Progress | @dev |
| Deployment Guide | ⏳ Planned | @mesh |

---
*Created with LANtern Code Studio*
""",
          language = "markdown"
        ),
        ProjectFileEntity(
          projectId = projectId,
          path = "docs/architecture.md",
          name = "architecture.md",
          content = """# Architecture Overview: $name

## System Components
1. **Decentralized Storage**: Local SQLite Room engine
2. **Peer-to-Peer Mesh**: WebRTC data channels
3. **Smart Live Preview**: Instant Markdown & Web engine
""",
          language = "markdown"
        ),
        ProjectFileEntity(
          projectId = projectId,
          path = "docs/notes.md",
          name = "notes.md",
          content = """# Developer Notes

- Zero-cleartext secret compliance verified
- Markdown headings and code blocks validated
""",
          language = "markdown"
        )
      )

      ProjectTemplate.PYTHON -> listOf(
        ProjectFileEntity(
          projectId = projectId,
          path = "main.py",
          name = "main.py",
          content = """\"\"\"
$name - Python Application
$description
\"\"\"
import sys
import time

def compute_metrics(data_points: list[int]) -> dict:
    \"\"\"Calculates basic statistics for numerical records.\"\"\"
    if not data_points:
        return {"count": 0, "average": 0.0}
    return {
        "count": len(data_points),
        "total": sum(data_points),
        "average": sum(data_points) / len(data_points),
        "timestamp": time.time()
    }

def main():
    print("====================================")
    print("💡 LANtern Code Python Studio: $name")
    print("====================================")
    sample_records = [14, 28, 42, 56, 70, 84]
    metrics = compute_metrics(sample_records)
    print(f"Processed {metrics['count']} data points. Average: {metrics['average']:.2f}")

if __name__ == "__main__":
    main()
""",
          language = "python"
        ),
        ProjectFileEntity(
          projectId = projectId,
          path = "requirements.txt",
          name = "requirements.txt",
          content = """requests>=2.31.0
pydantic>=2.5.0
pytest>=7.4.0
""",
          language = "text"
        ),
        ProjectFileEntity(
          projectId = projectId,
          path = "test_main.py",
          name = "test_main.py",
          content = """from main import compute_metrics

def test_compute_metrics():
    res = compute_metrics([10, 20, 30])
    assert res["count"] == 3
    assert res["total"] == 60
    assert res["average"] == 20.0
""",
          language = "python"
        ),
        ProjectFileEntity(
          projectId = projectId,
          path = "README.md",
          name = "README.md",
          content = """# $name
$description

Python project scaffolded in LANtern Code Studio.
""",
          language = "markdown"
        )
      )

      ProjectTemplate.NODE_TYPESCRIPT -> listOf(
        ProjectFileEntity(
          projectId = projectId,
          path = "src/index.ts",
          name = "index.ts",
          content = """/**
 * $name - TypeScript Application
 * $description
 */

export interface ServiceConfig {
  serviceName: string;
  port: number;
  environment: 'development' | 'production';
}

export class AppService {
  constructor(private readonly config: ServiceConfig) {}

  public start(): void {
    console.log(`💡 ${'$'}{this.config.serviceName} running on port ${'$'}{this.config.port}`);
  }

  public getHealth(): { status: string; uptime: number } {
    return {
      status: "UP",
      uptime: process.uptime ? process.uptime() : 0
    };
  }
}

const app = new AppService({
  serviceName: "$name",
  port: 3000,
  environment: 'development'
});

app.start();
""",
          language = "typescript"
        ),
        ProjectFileEntity(
          projectId = projectId,
          path = "package.json",
          name = "package.json",
          content = """{
  "name": "${name.lowercase().replace(" ", "-")}",
  "version": "1.0.0",
  "description": "$description",
  "main": "dist/index.js",
  "scripts": {
    "build": "tsc",
    "start": "node dist/index.js",
    "test": "jest"
  },
  "dependencies": {
    "express": "^4.19.2"
  },
  "devDependencies": {
    "typescript": "^5.4.0",
    "@types/node": "^20.11.0"
  }
}
""",
          language = "json"
        ),
        ProjectFileEntity(
          projectId = projectId,
          path = "README.md",
          name = "README.md",
          content = """# $name
$description

Node.js & TypeScript project managed in LANtern Code Studio.
""",
          language = "markdown"
        )
      )

      ProjectTemplate.KOTLIN_ANDROID -> listOf(
        ProjectFileEntity(
          projectId = projectId,
          path = "src/Main.kt",
          name = "Main.kt",
          content = """package $name.core

import kotlinx.coroutines.runBlocking

/**
 * LANtern Code Node Engine
 * Decentralized peer-to-peer workspace execution
 */
fun main() = runBlocking {
    println("💡 LANtern Code session initiated for $name")
    val status = verifyNetworkMesh()
    println("Mesh status: " + status)
}

fun verifyNetworkMesh(): String {
    val nodeActive = true
    return if (nodeActive) "PEER_ONLINE_READY" else "OFFLINE"
}
""",
          language = "kotlin"
        ),
        ProjectFileEntity(
          projectId = projectId,
          path = "README.md",
          name = "README.md",
          content = """# $name
$description

Kotlin engine workspace managed in LANtern Code.
""",
          language = "markdown"
        )
      )

      ProjectTemplate.BLANK -> listOf(
        ProjectFileEntity(
          projectId = projectId,
          path = "README.md",
          name = "README.md",
          content = """# $name
$description

Blank workspace ready for custom file creation.
""",
          language = "markdown"
        )
      )
    }
  }

  suspend fun updateFile(file: ProjectFileEntity) = withContext(Dispatchers.IO) {
    database.projectFileDao().updateFile(file.copy(updatedAt = System.currentTimeMillis()))
  }

  suspend fun addFile(projectId: Long, path: String, name: String, content: String, language: String): Long =
    withContext(Dispatchers.IO) {
      val file = ProjectFileEntity(
        projectId = projectId,
        path = path,
        name = name,
        content = content,
        language = language,
        isModified = true
      )
      database.projectFileDao().insertFile(file)
    }

  suspend fun deleteFile(fileId: Long) = withContext(Dispatchers.IO) {
    database.projectFileDao().deleteFileById(fileId)
  }

  suspend fun deleteProject(projectId: Long) = withContext(Dispatchers.IO) {
    database.projectFileDao().deleteFilesForProject(projectId)
    database.projectDao().deleteProjectById(projectId)
  }

  suspend fun addCommit(projectId: Long, message: String, author: String, branch: String): Long =
    withContext(Dispatchers.IO) {
      val hash = "git-${System.currentTimeMillis().toString().takeLast(7)}"
      database.gitCommitDao().insertCommit(
        GitCommitEntity(
          projectId = projectId,
          commitHash = hash,
          message = message,
          author = author,
          branch = branch
        )
      )
    }

  suspend fun addAuditReport(report: AuditReportEntity) = withContext(Dispatchers.IO) {
    database.auditReportDao().insertReport(report)
  }

  suspend fun addPeerSession(session: PeerSessionEntity) = withContext(Dispatchers.IO) {
    database.peerSessionDao().insertSession(session)
  }

  suspend fun clearSessionsForRoom(roomId: String) = withContext(Dispatchers.IO) {
    database.peerSessionDao().deleteSessionsForRoom(roomId)
  }

  // Interactive SQLite Raw Query Executor for the database management studio
  suspend fun executeRawSqlQuery(sql: String): SqlQueryResult = withContext(Dispatchers.IO) {
    val startTime = System.currentTimeMillis()
    try {
      val cleanSql = sql.trim()
      val db = database.openHelper.writableDatabase

      if (cleanSql.startsWith("SELECT", ignoreCase = true) ||
        cleanSql.startsWith("PRAGMA", ignoreCase = true) ||
        cleanSql.startsWith("EXPLAIN", ignoreCase = true)
      ) {
        val cursor: Cursor = db.query(cleanSql)
        val columns = cursor.columnNames.toList()
        val rows = mutableListOf<List<String>>()

        cursor.use {
          while (it.moveToNext()) {
            val row = mutableListOf<String>()
            for (i in 0 until it.columnCount) {
              row.add(it.getString(i) ?: "NULL")
            }
            rows.add(row)
          }
        }
        val elapsed = System.currentTimeMillis() - startTime
        SqlQueryResult(
          columns = columns,
          rows = rows,
          executionTimeMs = elapsed,
          rowCount = rows.size
        )
      } else {
        db.execSQL(cleanSql)
        val elapsed = System.currentTimeMillis() - startTime
        SqlQueryResult(
          columns = listOf("Status"),
          rows = listOf(listOf("Query executed successfully: affected rows updated")),
          executionTimeMs = elapsed,
          rowCount = 1
        )
      }
    } catch (e: Exception) {
      val elapsed = System.currentTimeMillis() - startTime
      SqlQueryResult(
        columns = listOf("Error"),
        rows = emptyList(),
        executionTimeMs = elapsed,
        rowCount = 0,
        error = e.localizedMessage ?: "Unknown SQL execution error"
      )
    }
  }

  suspend fun seedSampleProjectIfEmpty() = withContext(Dispatchers.IO) {
    val db = database.openHelper.readableDatabase
    val cursor = db.query("SELECT COUNT(*) FROM projects")
    var count = 0
    cursor.use {
      if (it.moveToNext()) {
        count = it.getInt(0)
      }
    }

    if (count == 0) {
      val projId = createProject(
        name = "NeonLantern-Core",
        description = "High-performance collaborative edge IDE & P2P WebRTC engine",
        gitRemoteUrl = "https://github.com/lantern-mesh/neonlantern-core.git"
      )

      // Add additional starter files
      addFile(
        projectId = projId,
        path = "src/MeshPeer.kt",
        name = "MeshPeer.kt",
        content = """package com.lantern.mesh

/**
 * WebRTC DataChannel connection state handler
 */
data class PeerInfo(
    val peerId: String,
    val role: String = "Editor",
    val latencyMs: Long = 18,
    val isEncrypted: Boolean = true
)

class MeshPeer(val info: PeerInfo) {
    fun sendDelta(delta: String): Boolean {
        // Broadcasts delta changes across peer data channels
        return true
    }
}
""",
        language = "kotlin"
      )

      addFile(
        projectId = projId,
        path = "db/schema.sql",
        name = "schema.sql",
        content = """-- LANtern Code SQLite local table definition
CREATE TABLE IF NOT EXISTS local_cache (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL,
    synced_at INTEGER DEFAULT (strftime('%s', 'now'))
);

INSERT INTO local_cache (key, value) VALUES ('session_token', 'rtc-sec-9842');
INSERT INTO local_cache (key, value) VALUES ('peer_name', 'NeonCoder-Mobile');
""",
        language = "sql"
      )

      addFile(
        projectId = projId,
        path = "tests/MeshUnitTest.kt",
        name = "MeshUnitTest.kt",
        content = """package com.lantern.tests

fun testPeerHandshake(): Boolean {
    val peerReady = true
    val encryptionKeySet = true
    return peerReady && encryptionKeySet
}

fun testDeltaSynchronization(): Boolean {
    val initialText = "LANtern Code"
    val delta = " 💡"
    return (initialText + delta).length == 14
}
""",
        language = "kotlin"
      )
    }
  }
}
