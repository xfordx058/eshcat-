package com.walangkaninbossing.eshcat.auth

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("eshcat_session", Context.MODE_PRIVATE)

    private val _currentUserId = MutableStateFlow(
        prefs.getInt(KEY_USER_ID, -1).takeIf { it > 0 } ?: -1
    )

    val currentUserId: StateFlow<Int> = _currentUserId.asStateFlow()

    val isLoggedIn: Boolean
        get() = _currentUserId.value > 0

    fun login(userId: Int) {
        prefs.edit().putInt(KEY_USER_ID, userId).apply()
        _currentUserId.value = userId
    }

    fun logout() {
        prefs.edit().remove(KEY_USER_ID).apply()
        _currentUserId.value = -1
    }

    private companion object {
        const val KEY_USER_ID = "staff_user_id"
    }
}