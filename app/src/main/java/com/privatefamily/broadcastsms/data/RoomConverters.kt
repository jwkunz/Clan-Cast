package com.privatefamily.broadcastsms.data

import androidx.room.TypeConverter
import com.privatefamily.broadcastsms.data.model.BroadcastStatus
import com.privatefamily.broadcastsms.data.model.DuplicatePolicy
import com.privatefamily.broadcastsms.data.model.InvalidNumberPolicy
import com.privatefamily.broadcastsms.data.model.RecipientStatus

class RoomConverters {
    @TypeConverter
    fun toBroadcastStatus(value: String?): BroadcastStatus? = value?.let(BroadcastStatus::valueOf)

    @TypeConverter
    fun fromBroadcastStatus(value: BroadcastStatus?): String? = value?.name

    @TypeConverter
    fun toRecipientStatus(value: String?): RecipientStatus? = value?.let(RecipientStatus::valueOf)

    @TypeConverter
    fun fromRecipientStatus(value: RecipientStatus?): String? = value?.name

    @TypeConverter
    fun toDuplicatePolicy(value: String?): DuplicatePolicy? = value?.let(DuplicatePolicy::valueOf)

    @TypeConverter
    fun fromDuplicatePolicy(value: DuplicatePolicy?): String? = value?.name

    @TypeConverter
    fun toInvalidNumberPolicy(value: String?): InvalidNumberPolicy? = value?.let(InvalidNumberPolicy::valueOf)

    @TypeConverter
    fun fromInvalidNumberPolicy(value: InvalidNumberPolicy?): String? = value?.name
}
