package com.privatefamily.broadcastsms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.privatefamily.broadcastsms.ui.ClanCastAppRoot
import com.privatefamily.broadcastsms.ui.theme.ClanCastTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as ClanCastApp).container

        setContent {
            ClanCastTheme {
                ClanCastAppRoot(container = container)
            }
        }
    }
}
