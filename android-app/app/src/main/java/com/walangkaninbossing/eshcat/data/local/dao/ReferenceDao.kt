package com.walangkaninbossing.eshcat.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.walangkaninbossing.eshcat.data.local.entity.DepartmentEntity
import com.walangkaninbossing.eshcat.data.local.entity.OfficeEntity
import com.walangkaninbossing.eshcat.data.local.entity.ServiceEntity
import com.walangkaninbossing.eshcat.data.local.entity.ServiceRequirementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReferenceDao {

    @Query("SELECT * FROM departments ORDER BY name")
    fun allDepartments(): Flow<List<DepartmentEntity>>

    @Query("SELECT * FROM departments ORDER BY name")
    suspend fun allDepartmentsOnce(): List<DepartmentEntity>

    @Insert
    suspend fun insertDepartments(list: List<DepartmentEntity>): List<Long>

    @Query("SELECT COUNT(*) FROM departments")
    suspend fun countDepartments(): Int

    @Query("SELECT * FROM offices ORDER BY name")
    fun allOffices(): Flow<List<OfficeEntity>>

    @Query("SELECT * FROM offices ORDER BY name")
    suspend fun allOfficesOnce(): List<OfficeEntity>

    @Query("SELECT * FROM offices WHERE id = :id")
    fun officeById(id: Int): Flow<OfficeEntity?>

    @Query("SELECT * FROM offices WHERE id = :id")
    suspend fun officeByIdOnce(id: Int): OfficeEntity?

    @Insert
    suspend fun insertOffice(office: OfficeEntity): Long

    @Insert
    suspend fun insertOffices(list: List<OfficeEntity>): List<Long>

    @Update
    suspend fun updateOffice(office: OfficeEntity)

    @Query("SELECT COUNT(*) FROM offices")
    suspend fun countOffices(): Int

    @Query("SELECT * FROM services ORDER BY name")
    fun allServices(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services ORDER BY name")
    suspend fun allServicesOnce(): List<ServiceEntity>

    @Query("SELECT * FROM services WHERE officeId = :officeId ORDER BY name")
    fun servicesByOffice(officeId: Int): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE id = :id")
    fun serviceById(id: Int): Flow<ServiceEntity?>

    @Query("SELECT * FROM services WHERE id = :id")
    suspend fun serviceByIdOnce(id: Int): ServiceEntity?

    @Insert
    suspend fun insertService(service: ServiceEntity): Long

    @Insert
    suspend fun insertServices(list: List<ServiceEntity>): List<Long>

    @Update
    suspend fun updateService(service: ServiceEntity)

    @Query("SELECT COUNT(*) FROM services")
    suspend fun countServices(): Int

    @Query("SELECT * FROM service_requirements WHERE serviceId = :serviceId ORDER BY id")
    fun requirementsFor(serviceId: Int): Flow<List<ServiceRequirementEntity>>

    @Query("SELECT * FROM service_requirements WHERE serviceId = :serviceId")
    suspend fun requirementsForOnce(serviceId: Int): List<ServiceRequirementEntity>

    @Insert
    suspend fun insertRequirements(list: List<ServiceRequirementEntity>)

    @Insert
    suspend fun insertRequirement(req: ServiceRequirementEntity): Long

    @Update
    suspend fun updateRequirement(req: ServiceRequirementEntity)

    @Delete
    suspend fun deleteRequirement(req: ServiceRequirementEntity)
}