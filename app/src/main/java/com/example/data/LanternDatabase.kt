package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AuditReportDao
import com.example.data.dao.GitBlameDao
import com.example.data.dao.GitCommitDao
import com.example.data.dao.GitDiffDao
import com.example.data.dao.PeerSessionDao
import com.example.data.dao.ProjectDao
import com.example.data.dao.ProjectFileDao
import com.example.data.dao.TeamChatMessageDao
import com.example.data.entity.AuditReportEntity
import com.example.data.entity.GitBlameEntity
import com.example.data.entity.GitCommitEntity
import com.example.data.entity.GitDiffEntity
import com.example.data.entity.PeerSessionEntity
import com.example.data.entity.ProjectEntity
import com.example.data.entity.ProjectFileEntity
import com.example.data.entity.TeamChatMessageEntity

@Database(
  entities = [
    ProjectEntity::class,
    ProjectFileEntity::class,
    GitCommitEntity::class,
    AuditReportEntity::class,
    PeerSessionEntity::class,
    GitDiffEntity::class,
    GitBlameEntity::class,
    TeamChatMessageEntity::class
  ],
  version = 2,
  exportSchema = false
)
abstract class LanternDatabase : RoomDatabase() {
  abstract fun projectDao(): ProjectDao
  abstract fun projectFileDao(): ProjectFileDao
  abstract fun gitCommitDao(): GitCommitDao
  abstract fun auditReportDao(): AuditReportDao
  abstract fun peerSessionDao(): PeerSessionDao
  abstract fun gitDiffDao(): GitDiffDao
  abstract fun gitBlameDao(): GitBlameDao
  abstract fun teamChatMessageDao(): TeamChatMessageDao

  companion object {
    @Volatile
    private var INSTANCE: LanternDatabase? = null

    fun getDatabase(context: Context): LanternDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          LanternDatabase::class.java,
          "lantern_code_workspace.db"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
        INSTANCE = instance
        instance
      }
    }
  }
}
