package com.walangkaninbossing.eshcat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.walangkaninbossing.eshcat.data.local.entity.AuditLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditDao {

    @Insert
    suspend fun insertLog(log: AuditLogEntity): Long

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC, id DESC")
    fun allLogs(): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC, id DESC")
    suspend fun allLogsOnce(): List<AuditLogEntity>

    @Query("SELECT COUNT(*) FROM audit_logs")
    suspend fun countLogs(): Int

    @Query("SELECT COUNT(*) FROM audit_logs WHERE userId = :userId")
    suspend fun countLogsByUser(userId: Int): Int

    @Query("SELECT COUNT(*) FROM audit_logs WHERE action = :action")
    suspend fun countLogsByAction(action: String): Int
}