package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val description: String,
  val gitRemoteUrl: String = "",
  val currentBranch: String = "main",
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "project_files")
data class ProjectFileEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val projectId: Long,
  val path: String,
  val name: String,
  val content: String,
  val language: String,
  val isModified: Boolean = false,
  val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "git_commits")
data class GitCommitEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val projectId: Long,
  val commitHash: String,
  val message: String,
  val author: String,
  val timestamp: Long = System.currentTimeMillis(),
  val branch: String = "main"
)

@Entity(tableName = "audit_reports")
data class AuditReportEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val projectId: Long,
  val score: Int,
  val securityIssuesCount: Int,
  val warningsCount: Int,
  val detailsJson: String,
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "peer_sessions")
data class PeerSessionEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val roomId: String,
  val peerName: String,
  val role: String = "Editor",
  val status: String = "Connected",
  val connectionType: String = "WebRTC P2P",
  val lastPing: Long = System.currentTimeMillis()
)

@Entity(tableName = "git_diffs")
data class GitDiffEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val diffId: String,
  val projectId: Long,
  val fileId: Long,
  val filePath: String,
  val commitHash: String,
  val author: String,
  val summary: String,
  val linesAdded: Int,
  val linesDeleted: Int,
  val rawPatch: String,
  val diskPath: String,
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "git_blame")
data class GitBlameEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val projectId: Long,
  val fileId: Long,
  val filePath: String,
  val lineNumber: Int,
  val lineContent: String,
  val commitHash: String,
  val author: String,
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "team_chat_messages")
data class TeamChatMessageEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val messageId: String,
  val roomId: String,
  val senderId: String,
  val senderName: String,
  val text: String,
  val hasCodeReference: Boolean = false,
  val referencedFilePath: String = "",
  val referencedLineNumber: Int = 0,
  val referencedCodeSnippet: String = "",
  val timestamp: Long = System.currentTimeMillis()
)

