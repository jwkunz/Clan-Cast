package com.privatefamily.broadcastsms.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.privatefamily.broadcastsms.AppContainer
import com.privatefamily.broadcastsms.ui.navigation.ClanCastNavHost

@Composable
fun ClanCastAppRoot(container: AppContainer) {
    val navController = rememberNavController()
    Surface(modifier = Modifier.fillMaxSize()) {
        ClanCastNavHost(
            navController = navController,
            container = container,
        )
    }
}
