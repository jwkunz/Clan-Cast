package com.privatefamily.broadcastsms.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.privatefamily.broadcastsms.data.model.DistributionListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DistributionListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(distributionList: DistributionListEntity)

    @Update
    suspend fun update(distributionList: DistributionListEntity)

    @Delete
    suspend fun delete(distributionList: DistributionListEntity)

    @Query("SELECT * FROM distribution_lists ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<DistributionListEntity>>

    @Query("SELECT * FROM distribution_lists WHERE id = :listId LIMIT 1")
    fun observeById(listId: String): Flow<DistributionListEntity?>

    @Query("SELECT * FROM distribution_lists WHERE id = :listId LIMIT 1")
    suspend fun getById(listId: String): DistributionListEntity?
}
