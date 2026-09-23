package com.walangkaninbossing.eshcat.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.walangkaninbossing.eshcat.data.local.dao.AuditDao
import com.walangkaninbossing.eshcat.data.local.dao.AuthDao
import com.walangkaninbossing.eshcat.data.local.dao.ReferenceDao
import com.walangkaninbossing.eshcat.data.local.dao.RequestDao
import com.walangkaninbossing.eshcat.data.local.entity.AnnouncementEntity
import com.walangkaninbossing.eshcat.data.local.entity.ApplicationAssignmentEntity
import com.walangkaninbossing.eshcat.data.local.entity.ApplicationEntity
import com.walangkaninbossing.eshcat.data.local.entity.ApplicationHistoryEntity
import com.walangkaninbossing.eshcat.data.local.entity.AppointmentEntity
import com.walangkaninbossing.eshcat.data.local.entity.AuditLogEntity
import com.walangkaninbossing.eshcat.data.local.entity.CommunityReportEntity
import com.walangkaninbossing.eshcat.data.local.entity.DepartmentEntity
import com.walangkaninbossing.eshcat.data.local.entity.OfficeEntity
import com.walangkaninbossing.eshcat.data.local.entity.PermissionEntity
import com.walangkaninbossing.eshcat.data.local.entity.RoleEntity
import com.walangkaninbossing.eshcat.data.local.entity.RolePermissionEntity
import com.walangkaninbossing.eshcat.data.local.entity.ServiceEntity
import com.walangkaninbossing.eshcat.data.local.entity.ServiceRequirementEntity
import com.walangkaninbossing.eshcat.data.local.entity.StaffUserEntity

@Database(
    entities = [
        DepartmentEntity::class,
        OfficeEntity::class,
        ServiceEntity::class,
        ServiceRequirementEntity::class,
        RoleEntity::class,
        PermissionEntity::class,
        RolePermissionEntity::class,
        StaffUserEntity::class,
        ApplicationEntity::class,
        ApplicationHistoryEntity::class,
        ApplicationAssignmentEntity::class,
        AppointmentEntity::class,
        CommunityReportEntity::class,
        AnnouncementEntity::class,
        AuditLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun referenceDao(): ReferenceDao
    abstract fun authDao(): AuthDao
    abstract fun requestDao(): RequestDao
    abstract fun auditDao(): AuditDao

    companion object {
        private const val DB_NAME = "eshcat.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE applications ADD COLUMN submissionKey TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE appointments ADD COLUMN submissionKey TEXT NOT NULL DEFAULT ''")
                // Existing rows must receive a unique legacy key before the index is created.
                db.execSQL("UPDATE applications SET submissionKey = 'legacy-application-' || id WHERE submissionKey = ''")
                db.execSQL("UPDATE appointments SET submissionKey = 'legacy-appointment-' || id WHERE submissionKey = ''")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_applications_submissionKey ON applications (submissionKey)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_appointments_submissionKey ON appointments (submissionKey)")
            }
        }
    }
}
