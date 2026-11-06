package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.AuditReportEntity
import com.example.data.entity.GitCommitEntity
import com.example.data.entity.PeerSessionEntity
import com.example.data.entity.ProjectEntity
import com.example.data.entity.ProjectFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
  @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
  fun getAllProjects(): Flow<List<ProjectEntity>>

  @Query("SELECT * FROM projects WHERE id = :id")
  suspend fun getProjectById(id: Long): ProjectEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProject(project: ProjectEntity): Long

  @Update
  suspend fun updateProject(project: ProjectEntity)

  @Query("DELETE FROM projects WHERE id = :id")
  suspend fun deleteProjectById(id: Long)
}

@Dao
interface ProjectFileDao {
  @Query("SELECT * FROM project_files WHERE projectId = :projectId ORDER BY path ASC")
  fun getFilesForProject(projectId: Long): Flow<List<ProjectFileEntity>>

  @Query("SELECT * FROM project_files WHERE id = :id")
  suspend fun getFileById(id: Long): ProjectFileEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFile(file: ProjectFileEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFiles(files: List<ProjectFileEntity>)

  @Update
  suspend fun updateFile(file: ProjectFileEntity)

  @Query("DELETE FROM project_files WHERE id = :id")
  suspend fun deleteFileById(id: Long)

  @Query("DELETE FROM project_files WHERE projectId = :projectId")
  suspend fun deleteFilesForProject(projectId: Long)
}

@Dao
interface GitCommitDao {
  @Query("SELECT * FROM git_commits WHERE projectId = :projectId ORDER BY timestamp DESC")
  fun getCommitsForProject(projectId: Long): Flow<List<GitCommitEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCommit(commit: GitCommitEntity): Long
}

@Dao
interface AuditReportDao {
  @Query("SELECT * FROM audit_reports WHERE projectId = :projectId ORDER BY timestamp DESC")
  fun getReportsForProject(projectId: Long): Flow<List<AuditReportEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReport(report: AuditReportEntity): Long
}

@Dao
interface PeerSessionDao {
  @Query("SELECT * FROM peer_sessions WHERE roomId = :roomId ORDER BY lastPing DESC")
  fun getSessionsForRoom(roomId: String): Flow<List<PeerSessionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSession(session: PeerSessionEntity): Long

  @Query("DELETE FROM peer_sessions WHERE id = :id")
  suspend fun deleteSessionById(id: Long)

  @Query("DELETE FROM peer_sessions WHERE roomId = :roomId")
  suspend fun deleteSessionsForRoom(roomId: String)
}
