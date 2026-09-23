package com.walangkaninbossing.eshcat.auth

import com.walangkaninbossing.eshcat.data.local.entity.StaffUserEntity
import com.walangkaninbossing.eshcat.data.repository.AuthRepository
import com.walangkaninbossing.eshcat.data.repository.AuditRepository
import com.walangkaninbossing.eshcat.util.TimeUtil

class AuthManager(
    private val authRepository: AuthRepository,
    private val session: SessionManager,
    private val auditRepository: AuditRepository
) {

    sealed class LoginResult {
        data class Success(val user: StaffUserEntity) : LoginResult()
        data class Error(val message: String) : LoginResult()
    }

    suspend fun login(username: String, password: String): LoginResult {
        val normalized = username.trim().lowercase()
        val user = authRepository.userByUsername(normalized)
            ?: return LoginResult.Error("Incorrect username or password.")
        if (!PasswordHasher.verify(password, user.passwordHash)) {
            return LoginResult.Error("Incorrect username or password.")
        }
        if (user.status != "ACTIVE") {
            return LoginResult.Error(
                "This account is ${user.status.lowercase()}. Contact the administrator."
            )
        }
        authRepository.updateLastLogin(user.id, TimeUtil.now())
        auditRepository.logLogin(user.id, user.username)
        session.login(user.id)
        return LoginResult.Success(user)
    }

    fun logout() = session.logout()
}