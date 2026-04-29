package com.privatefamily.broadcastsms

import android.app.Application
import androidx.room.Room
import com.privatefamily.broadcastsms.data.AppDatabase
import com.privatefamily.broadcastsms.data.contacts.ContactReader
import com.privatefamily.broadcastsms.data.contacts.PhoneNumberNormalizer
import com.privatefamily.broadcastsms.data.repository.BroadcastRepository
import com.privatefamily.broadcastsms.data.repository.ContactRepository
import com.privatefamily.broadcastsms.data.repository.DistributionListRepository
import com.privatefamily.broadcastsms.data.repository.TemplateRepository
import com.privatefamily.broadcastsms.domain.preflight.BroadcastDraftStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ClanCastApp : Application() {
    lateinit var container: AppContainer
        private set
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "clan-cast.db",
        ).build()

        container = AppContainer(
            distributionListRepository = DistributionListRepository(
                listDao = database.distributionListDao(),
                memberDao = database.distributionListMemberDao(),
            ),
            templateRepository = TemplateRepository(database.messageTemplateDao()),
            broadcastRepository = BroadcastRepository(
                database = database,
                broadcastDao = database.broadcastDao(),
                recipientDao = database.broadcastRecipientDao(),
            ),
            contactRepository = ContactRepository(
                contactReader = ContactReader(
                    context = applicationContext,
                    contentResolver = applicationContext.contentResolver,
                    phoneNumberNormalizer = PhoneNumberNormalizer(),
                ),
            ),
            broadcastDraftStore = BroadcastDraftStore(),
        )

        appScope.launch {
            container.broadcastRepository.recoverInterruptedBroadcasts()
        }
    }
}

data class AppContainer(
    val distributionListRepository: DistributionListRepository,
    val templateRepository: TemplateRepository,
    val broadcastRepository: BroadcastRepository,
    val contactRepository: ContactRepository,
    val broadcastDraftStore: BroadcastDraftStore,
)
