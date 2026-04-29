package com.privatefamily.broadcastsms.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.privatefamily.broadcastsms.data.dao.BroadcastDao
import com.privatefamily.broadcastsms.data.dao.BroadcastRecipientDao
import com.privatefamily.broadcastsms.data.dao.DistributionListDao
import com.privatefamily.broadcastsms.data.dao.DistributionListMemberDao
import com.privatefamily.broadcastsms.data.dao.MessageTemplateDao
import com.privatefamily.broadcastsms.data.model.BroadcastEntity
import com.privatefamily.broadcastsms.data.model.BroadcastRecipientEntity
import com.privatefamily.broadcastsms.data.model.DistributionListEntity
import com.privatefamily.broadcastsms.data.model.DistributionListMemberEntity
import com.privatefamily.broadcastsms.data.model.MessageTemplateEntity

@Database(
    entities = [
        DistributionListEntity::class,
        DistributionListMemberEntity::class,
        MessageTemplateEntity::class,
        BroadcastEntity::class,
        BroadcastRecipientEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun distributionListDao(): DistributionListDao
    abstract fun distributionListMemberDao(): DistributionListMemberDao
    abstract fun messageTemplateDao(): MessageTemplateDao
    abstract fun broadcastDao(): BroadcastDao
    abstract fun broadcastRecipientDao(): BroadcastRecipientDao
}
