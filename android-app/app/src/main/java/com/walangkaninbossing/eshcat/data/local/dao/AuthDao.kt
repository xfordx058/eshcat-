package com.walangkaninbossing.eshcat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.walangkaninbossing.eshcat.data.local.entity.PermissionEntity
import com.walangkaninbossing.eshcat.data.local.entity.RoleEntity
import com.walangkaninbossing.eshcat.data.local.entity.RolePermissionEntity
import com.walangkaninbossing.eshcat.data.local.entity.StaffUserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthDao {

    @Query("SELECT * FROM roles ORDER BY name")
    fun allRoles(): Flow<List<RoleEntity>>

    @Query("SELECT * FROM roles ORDER BY name")
    suspend fun allRolesOnce(): List<RoleEntity>

    @Query("SELECT * FROM roles WHERE id = :id")
    suspend fun roleById(id: Int): RoleEntity?

    @Insert
    suspend fun insertRoles(list: List<RoleEntity>): List<Long>

    @Insert
    suspend fun insertRole(role: RoleEntity): Long

    @Update
    suspend fun updateRole(role: RoleEntity)

    @Query("SELECT COUNT(*) FROM roles")
    suspend fun countRoles(): Int

    @Query("SELECT * FROM permissions ORDER BY name")
    fun allPermissions(): Flow<List<PermissionEntity>>

    @Query("SELECT * FROM permissions ORDER BY name")
    suspend fun allPermissionsOnce(): List<PermissionEntity>

    @Insert
    suspend fun insertPermissions(list: List<PermissionEntity>)

    @Query("SELECT COUNT(*) FROM permissions")
    suspend fun countPermissions(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRolePermissions(list: List<RolePermissionEntity>)

    @Query("DELETE FROM role_permissions WHERE roleId = :roleId")
    suspend fun deleteRolePermissions(roleId: Int)

    @Query("SELECT permissionId FROM role_permissions WHERE roleId = :roleId")
    suspend fun permissionIdsForRole(roleId: Int): List<String>

    @Query("SELECT * FROM staff_users ORDER BY fullName")
    fun allUsers(): Flow<List<StaffUserEntity>>

    @Query("SELECT * FROM staff_users ORDER BY fullName")
    suspend fun allUsersOnce(): List<StaffUserEntity>

    @Query("SELECT * FROM staff_users WHERE username = :username LIMIT 1")
    suspend fun userByUsername(username: String): StaffUserEntity?

    @Query("SELECT * FROM staff_users WHERE id = :id")
    suspend fun userById(id: Int): StaffUserEntity?

    @Query("SELECT * FROM staff_users WHERE id = :id")
    fun userByIdFlow(id: Int): Flow<StaffUserEntity?>

    @Insert
    suspend fun insertUser(user: StaffUserEntity): Long

    @Insert
    suspend fun insertUsers(list: List<StaffUserEntity>): List<Long>

    @Update
    suspend fun updateUser(user: StaffUserEntity)

    @Query("UPDATE staff_users SET lastLogin = :lastLogin WHERE id = :id")
    suspend fun updateLastLogin(id: Int, lastLogin: String)

    @Query("UPDATE staff_users SET status = :status WHERE id = :id")
    suspend fun updateUserStatus(id: Int, status: String)

    @Query("SELECT COUNT(*) FROM staff_users")
    suspend fun countUsers(): Int
}