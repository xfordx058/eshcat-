package com.walangkaninbossing.eshcat.auth

import com.walangkaninbossing.eshcat.data.local.entity.StaffUserEntity
import com.walangkaninbossing.eshcat.data.repository.AuthRepository

class PermissionManager(private val authRepository: AuthRepository) {

    private val cache = mutableMapOf<Int, Set<String>>()

    suspend fun permissionsFor(user: StaffUserEntity): Set<String> {
        cache[user.id]?.let { return it }
        val ids = authRepository.permissionIdsForRole(user.roleId).toSet()
        cache[user.id] = ids
        return ids
    }

    suspend fun has(user: StaffUserEntity, permission: String): Boolean =
        permission in permissionsFor(user)

    fun clear(userId: Int) {
        cache.remove(userId)
    }
}