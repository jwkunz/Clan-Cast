package com.privatefamily.broadcastsms.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.privatefamily.broadcastsms.data.model.DistributionListMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DistributionListMemberDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: DistributionListMemberEntity)

    @Update
    suspend fun update(member: DistributionListMemberEntity)

    @Query("DELETE FROM distribution_list_members WHERE id = :memberId")
    suspend fun deleteById(memberId: String)

    @Query("SELECT * FROM distribution_list_members WHERE distributionListId = :listId ORDER BY displayName COLLATE NOCASE ASC")
    fun observeByListId(listId: String): Flow<List<DistributionListMemberEntity>>

    @Query("SELECT * FROM distribution_list_members WHERE distributionListId = :listId ORDER BY displayName COLLATE NOCASE ASC")
    suspend fun getByListId(listId: String): List<DistributionListMemberEntity>

    @Query("SELECT * FROM distribution_list_members WHERE id = :memberId LIMIT 1")
    suspend fun getById(memberId: String): DistributionListMemberEntity?
}
