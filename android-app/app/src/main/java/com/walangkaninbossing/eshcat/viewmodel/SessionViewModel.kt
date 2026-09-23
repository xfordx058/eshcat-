package com.walangkaninbossing.eshcat.viewmodel

import androidx.lifecycle.viewModelScope
import com.walangkaninbossing.eshcat.auth.AuthManager
import com.walangkaninbossing.eshcat.data.local.entity.RoleEntity
import com.walangkaninbossing.eshcat.data.local.entity.StaffUserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SessionState(
    val user: StaffUserEntity? = null,
    val role: RoleEntity? = null,
    val permissions: Set<String> = emptySet(),
    val loading: Boolean = false,
    val error: String? = null,
    val ready: Boolean = false,
)

class StaffSessionViewModel(app: com.walangkaninbossing.eshcat.AppContainer) : AppViewModel(app) {

    private val _state = MutableStateFlow(SessionState())
    val state: StateFlow<SessionState> = _state.asStateFlow()

    val isLoggedIn: Boolean
        get() = app.session.isLoggedIn

    init {
        refreshSession()
    }

    fun refreshSession() {
        viewModelScope.launch {
            app.awaitSeeded()
            val uid = app.session.currentUserId.first()
            if (uid <= 0) {
                _state.value = SessionState(ready = true)
                return@launch
            }
            val user = app.auth.userById(uid)
            if (user == null) {
                app.session.logout()
                _state.value = SessionState(ready = true)
                return@launch
            }
            val role = app.auth.roleById(user.roleId)
            val perms = app.permissionManager.permissionsFor(user)
            _state.value = SessionState(user = user, role = role, permissions = perms, ready = true)
        }
    }

    fun login(username: String, password: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val result = app.authManager.login(username, password)
            when (result) {
                is AuthManager.LoginResult.Success -> {
                    val user = result.user
                    val role = app.auth.roleById(user.roleId)
                    val perms = app.permissionManager.permissionsFor(user)
                    _state.value = SessionState(
                        user = user,
                        role = role,
                        permissions = perms,
                        loading = false,
                        ready = true,
                    )
                    onResult(null)
                }

                is AuthManager.LoginResult.Error -> {
                    _state.value = _state.value.copy(loading = false, error = result.message)
                    onResult(result.message)
                }
            }
        }
    }

    fun logout() {
        app.authManager.logout()
        clearPermissionCache()
        _state.value = SessionState(ready = true)
    }

    fun has(permission: String): Boolean = permission in _state.value.permissions

    private fun clearPermissionCache() {
        app.permissionManager.clear(_state.value.user?.id ?: -1)
    }
}