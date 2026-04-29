package com.privatefamily.broadcastsms.data.repository

import com.privatefamily.broadcastsms.data.dao.DistributionListDao
import com.privatefamily.broadcastsms.data.dao.DistributionListMemberDao
import com.privatefamily.broadcastsms.data.model.DistributionListEntity
import com.privatefamily.broadcastsms.data.model.DistributionListMemberEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class DistributionListRepository(
    private val listDao: DistributionListDao,
    private val memberDao: DistributionListMemberDao,
) {
    fun observeLists(): Flow<List<DistributionListEntity>> = listDao.observeAll()

    fun observeList(listId: String): Flow<DistributionListEntity?> = listDao.observeById(listId)

    fun observeMembers(listId: String): Flow<List<DistributionListMemberEntity>> = memberDao.observeByListId(listId)

    suspend fun createList(name: String) {
        val now = System.currentTimeMillis()
        listDao.insert(
            DistributionListEntity(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            ),
        )
    }

    suspend fun renameList(listId: String, name: String) {
        val existing = listDao.getById(listId) ?: return
        listDao.update(existing.copy(name = name.trim(), updatedAtEpochMillis = System.currentTimeMillis()))
    }

    suspend fun deleteList(listId: String) {
        val existing = listDao.getById(listId) ?: return
        listDao.delete(existing)
    }

    suspend fun addMember(member: DistributionListMemberEntity) {
        memberDao.insert(member)
    }

    suspend fun updateMember(member: DistributionListMemberEntity) {
        memberDao.update(member.copy(updatedAtEpochMillis = System.currentTimeMillis()))
    }

    suspend fun removeMember(memberId: String) {
        memberDao.deleteById(memberId)
    }
}
