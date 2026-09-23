package com.walangkaninbossing.eshcat

import android.app.Application
import android.content.Context
import com.walangkaninbossing.eshcat.auth.AuthManager
import com.walangkaninbossing.eshcat.auth.PermissionManager
import com.walangkaninbossing.eshcat.auth.SessionManager
import com.walangkaninbossing.eshcat.data.local.AppDatabase
import com.walangkaninbossing.eshcat.data.repository.ApplicationRepository
import com.walangkaninbossing.eshcat.data.repository.AuditRepository
import com.walangkaninbossing.eshcat.data.repository.AuthRepository
import com.walangkaninbossing.eshcat.data.repository.CatalogRepository
import com.walangkaninbossing.eshcat.data.repository.CommunityRepository
import com.walangkaninbossing.eshcat.data.seed.DatabaseSeeder
import com.walangkaninbossing.eshcat.util.AppSettingsManager
import com.walangkaninbossing.eshcat.util.EmailService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
    val database: AppDatabase = AppDatabase.get(context)
    val catalog: CatalogRepository = CatalogRepository(database)
    val auth: AuthRepository = AuthRepository(database)
    val applications: ApplicationRepository = ApplicationRepository(database)
    val community: CommunityRepository = CommunityRepository(database)
    val audit: AuditRepository = AuditRepository(database)
    val session: SessionManager = SessionManager(context)
    val settings: AppSettingsManager = AppSettingsManager(context)
    val authManager: AuthManager = AuthManager(auth, session, audit)
    val permissionManager: PermissionManager = PermissionManager(auth)

    private val _seedingDone = MutableStateFlow(false)
    val seedingDone: StateFlow<Boolean> = _seedingDone.asStateFlow()

    fun markSeeded() {
        _seedingDone.value = true
    }

    suspend fun awaitSeeded() {
        _seedingDone.first { it }
    }
}

class ESHCATApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        EmailService.initialize(this)
        container = AppContainer(this)
        appScope.launch {
            DatabaseSeeder(container.database).seedIfEmpty()
            container.markSeeded()
        }
    }
}
