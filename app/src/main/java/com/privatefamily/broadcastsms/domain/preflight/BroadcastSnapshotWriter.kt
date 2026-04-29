package com.privatefamily.broadcastsms.domain.preflight

import com.privatefamily.broadcastsms.data.model.BroadcastEntity
import com.privatefamily.broadcastsms.data.model.BroadcastRecipientEntity

interface BroadcastSnapshotWriter {
    suspend fun createBroadcastSnapshot(
        broadcast: BroadcastEntity,
        recipients: List<BroadcastRecipientEntity>,
    )
}
