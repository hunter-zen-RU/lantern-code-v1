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
