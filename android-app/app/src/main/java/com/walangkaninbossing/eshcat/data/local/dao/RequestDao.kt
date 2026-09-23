package com.walangkaninbossing.eshcat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.walangkaninbossing.eshcat.data.local.entity.AnnouncementEntity
import com.walangkaninbossing.eshcat.data.local.entity.ApplicationAssignmentEntity
import com.walangkaninbossing.eshcat.data.local.entity.ApplicationEntity
import com.walangkaninbossing.eshcat.data.local.entity.ApplicationHistoryEntity
import com.walangkaninbossing.eshcat.data.local.entity.AppointmentEntity
import com.walangkaninbossing.eshcat.data.local.entity.CommunityReportEntity
import kotlinx.coroutines.flow.Flow

data class StatusCount(
    val status: String,
    val count: Int,
)

@Dao
interface RequestDao {

    @Query("SELECT * FROM applications ORDER BY createdDate DESC")
    fun allApplications(): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications ORDER BY createdDate DESC")
    suspend fun allApplicationsOnce(): List<ApplicationEntity>

    @Query("SELECT * FROM applications WHERE referenceNumber = :ref LIMIT 1")
    suspend fun applicationByRef(ref: String): ApplicationEntity?

    @Query("SELECT * FROM applications WHERE id = :id")
    fun applicationById(id: Int): Flow<ApplicationEntity?>

    @Query("SELECT * FROM applications WHERE id = :id")
    suspend fun applicationByIdOnce(id: Int): ApplicationEntity?

    @Query("SELECT COUNT(*) FROM applications")
    suspend fun countApplications(): Int

    @Query("SELECT COUNT(*) FROM applications WHERE status = :status")
    suspend fun countApplicationsByStatus(status: String): Int

    @Query("SELECT status, COUNT(*) AS count FROM applications GROUP BY status")
    suspend fun applicationStatusCounts(): List<StatusCount>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertApplication(app: ApplicationEntity): Long

    @Query("SELECT * FROM applications WHERE submissionKey = :submissionKey LIMIT 1")
    suspend fun applicationBySubmissionKey(submissionKey: String): ApplicationEntity?

    @Update
    suspend fun updateApplication(app: ApplicationEntity)

    @Query("SELECT * FROM application_history WHERE applicationId = :applicationId ORDER BY changedDate ASC, id ASC")
    fun historyFor(applicationId: Int): Flow<List<ApplicationHistoryEntity>>

    @Query("SELECT * FROM application_history WHERE applicationId = :applicationId ORDER BY changedDate ASC, id ASC")
    suspend fun historyForOnce(applicationId: Int): List<ApplicationHistoryEntity>

    @Insert
    suspend fun insertHistory(hist: ApplicationHistoryEntity): Long

    @Query("SELECT * FROM application_assignments WHERE applicationId = :applicationId")
    suspend fun assignmentsFor(applicationId: Int): List<ApplicationAssignmentEntity>

    @Query("SELECT * FROM application_assignments WHERE staffUserId = :staffUserId AND applicationId = :applicationId LIMIT 1")
    suspend fun assignmentFor(staffUserId: Int, applicationId: Int): ApplicationAssignmentEntity?

    @Query("SELECT * FROM application_assignments")
    suspend fun allAssignments(): List<ApplicationAssignmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: ApplicationAssignmentEntity): Long

    @Query("SELECT * FROM appointments ORDER BY createdDate DESC")
    fun allAppointments(): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments ORDER BY createdDate DESC")
    suspend fun allAppointmentsOnce(): List<AppointmentEntity>

    @Query("SELECT * FROM appointments WHERE id = :id")
    suspend fun appointmentById(id: Int): AppointmentEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAppointment(appointment: AppointmentEntity): Long

    @Query("SELECT * FROM appointments WHERE submissionKey = :submissionKey LIMIT 1")
    suspend fun appointmentBySubmissionKey(submissionKey: String): AppointmentEntity?

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)

    @Query("SELECT COUNT(*) FROM appointments")
    suspend fun countAppointments(): Int

    @Query("SELECT COUNT(*) FROM appointments WHERE status = :status")
    suspend fun countAppointmentsByStatus(status: String): Int

    @Query("SELECT status, COUNT(*) AS count FROM appointments GROUP BY status")
    suspend fun appointmentStatusCounts(): List<StatusCount>

    @Query("SELECT * FROM community_reports ORDER BY createdDate DESC")
    fun allReports(): Flow<List<CommunityReportEntity>>

    @Query("SELECT * FROM community_reports ORDER BY createdDate DESC")
    suspend fun allReportsOnce(): List<CommunityReportEntity>

    @Query("SELECT * FROM community_reports WHERE id = :id")
    suspend fun reportById(id: Int): CommunityReportEntity?

    @Insert
    suspend fun insertReport(report: CommunityReportEntity): Long

    @Update
    suspend fun updateReport(report: CommunityReportEntity)

    @Query("SELECT COUNT(*) FROM community_reports")
    suspend fun countReports(): Int

    @Query("SELECT COUNT(*) FROM community_reports WHERE status = :status")
    suspend fun countReportsByStatus(status: String): Int

    @Query("SELECT status, COUNT(*) AS count FROM community_reports GROUP BY status")
    suspend fun reportStatusCounts(): List<StatusCount>

    @Query("SELECT * FROM announcements ORDER BY pinned DESC, date DESC")
    fun allAnnouncements(): Flow<List<AnnouncementEntity>>

    @Query("SELECT * FROM announcements ORDER BY pinned DESC, date DESC")
    suspend fun allAnnouncementsOnce(): List<AnnouncementEntity>

    @Query("SELECT * FROM announcements WHERE id = :id")
    suspend fun announcementById(id: Int): AnnouncementEntity?

    @Insert
    suspend fun insertAnnouncements(list: List<AnnouncementEntity>): List<Long>

    @Insert
    suspend fun insertAnnouncement(announcement: AnnouncementEntity): Long

    @Update
    suspend fun updateAnnouncement(announcement: AnnouncementEntity)
}
