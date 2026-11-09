package com.example.data
import android.content.Context
import com.example.data.entity.GitBlameEntity
import com.example.data.entity.GitDiffEntity
import com.example.data.entity.ProjectEntity
import com.example.data.entity.ProjectFileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object LanternDiskStorage {
  private const val TAG = "LanternDiskStorage"

  /**
   * Resolves the primary root directory for LANtern disk storage:
   * Prefer '/home/LANtern' (the requested directory in Linux container / mobile environment),
   * falling back to internal storage 'context.filesDir/home/LANtern' if '/home' is read-only.
   */
  fun getLanternBaseDir(context: Context): File {
    val systemHome = File("/home/LANtern")
    return try {
      if (!systemHome.exists()) {
        systemHome.mkdirs()
      }
      if (systemHome.exists() && systemHome.canWrite()) {
        systemHome
      } else {
        File(context.filesDir, "home/LANtern").apply { mkdirs() }
      }
    } catch (_: Exception) {
      File(context.filesDir, "home/LANtern").apply { mkdirs() }
    }
  }

  /**
   * Returns directory 'home/LANtern/projects/{projectName}'
   */
  fun getProjectDir(context: Context, projectName: String): File {
    val base = getLanternBaseDir(context)
    val sanitized = projectName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    return File(base, "projects/$sanitized").apply { mkdirs() }
  }

  /**
   * Returns directory 'home/LANtern/.diffs/{diffId}'
   */
  fun getDiffDir(context: Context, diffId: String): File {
    val base = getLanternBaseDir(context)
    return File(base, ".diffs/$diffId").apply { mkdirs() }
  }

  /**
   * Saves and organizes project files directly to 'home/LANtern/projects/{project-name}'
   */
  suspend fun saveProjectToDisk(
    context: Context,
    project: ProjectEntity,
    files: List<ProjectFileEntity>
  ): File = withContext(Dispatchers.IO) {
    val projectDir = getProjectDir(context, project.name)

    for (file in files) {
      val targetFile = File(projectDir, file.path)
      targetFile.parentFile?.mkdirs()
      targetFile.writeText(file.content)
    }

    // Write a project descriptor file
    val metaFile = File(projectDir, ".lantern-meta.json")
    val json = """
      {
        "projectId": ${project.id},
        "name": "${project.name}",
        "branch": "${project.currentBranch}",
        "exportedAt": "${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}",
        "filesCount": ${files.size}
      }
    """.trimIndent()
    metaFile.writeText(json)

    projectDir
  }

  /**
   * Computes a unified diff between oldCode and newCode, saves code artifacts
   * in 'home/LANtern/.diffs/{diff-id}/', and records the diff in the database.
   */
  suspend fun recordDiffAndBlame(
    context: Context,
    db: LanternDatabase,
    project: ProjectEntity,
    file: ProjectFileEntity,
    oldContent: String,
    newContent: String,
    commitHash: String,
    author: String
  ): GitDiffEntity = withContext(Dispatchers.IO) {
    val diffId = "diff-" + UUID.randomUUID().toString().take(8)
    val diffDir = getDiffDir(context, diffId)

    val oldLines = oldContent.lines()
    val newLines = newContent.lines()

    var linesAdded = 0
    var linesDeleted = 0
    val patchBuilder = StringBuilder()
    patchBuilder.append("--- a/${file.path}\n")
    patchBuilder.append("+++ b/${file.path}\n")
    patchBuilder.append("@@ -1,${oldLines.size} +1,${newLines.size} @@\n")

    val maxLines = maxOf(oldLines.size, newLines.size)
    for (i in 0 until maxLines) {
      val oldLine = oldLines.getOrNull(i)
      val newLine = newLines.getOrNull(i)
      if (oldLine != null && newLine != null) {
        if (oldLine == newLine) {
          patchBuilder.append(" $newLine\n")
        } else {
          patchBuilder.append("-$oldLine\n")
          patchBuilder.append("+$newLine\n")
          linesDeleted++
          linesAdded++
        }
      } else if (oldLine != null) {
        patchBuilder.append("-$oldLine\n")
        linesDeleted++
      } else if (newLine != null) {
        patchBuilder.append("+$newLine\n")
        linesAdded++
      }
    }

    val patchText = patchBuilder.toString()

    // Write code snapshots and patch to disk in 'home/LANtern/.diffs/{diff-id}/'
    File(diffDir, "before.code").writeText(oldContent)
    File(diffDir, "after.code").writeText(newContent)
    File(diffDir, "diff.patch").writeText(patchText)
    File(diffDir, "metadata.json").writeText(
      """
      {
        "diffId": "$diffId",
        "projectId": ${project.id},
        "filePath": "${file.path}",
        "commitHash": "$commitHash",
        "author": "$author",
        "linesAdded": $linesAdded,
        "linesDeleted": $linesDeleted,
        "timestamp": ${System.currentTimeMillis()}
      }
      """.trimIndent()
    )

    // Save in Room Database
    val summary = if (linesAdded == 0 && linesDeleted == 0) {
      "Clean update on ${file.name}"
    } else {
      "+$linesAdded / -$linesDeleted on ${file.name}"
    }

    val diffEntity = GitDiffEntity(
      diffId = diffId,
      projectId = project.id,
      fileId = file.id,
      filePath = file.path,
      commitHash = commitHash,
      author = author,
      summary = summary,
      linesAdded = linesAdded,
      linesDeleted = linesDeleted,
      rawPatch = patchText,
      diskPath = diffDir.absolutePath,
      timestamp = System.currentTimeMillis()
    )
    db.gitDiffDao().insertDiff(diffEntity)

    // Compute and record Git Blame for each line of the file in the database
    val blameList = newLines.mapIndexed { idx, line ->
      GitBlameEntity(
        projectId = project.id,
        fileId = file.id,
        filePath = file.path,
        lineNumber = idx + 1,
        lineContent = line,
        commitHash = commitHash.take(7),
        author = author,
        timestamp = System.currentTimeMillis()
      )
    }
    db.gitBlameDao().deleteBlameForFile(file.id)
    db.gitBlameDao().insertBlameRecords(blameList)

    diffEntity
  }
}
