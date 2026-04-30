package com.privatefamily.broadcastsms.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.privatefamily.broadcastsms.R
import com.privatefamily.broadcastsms.ui.theme.LogoGreen
import com.privatefamily.broadcastsms.ui.theme.LogoNavy
import com.privatefamily.broadcastsms.ui.theme.LogoSky

@Composable
fun SplashScreen(
    onContinue: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LogoNavy)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.clan_cast_logo),
                contentDescription = "Clan Cast logo",
                modifier = Modifier.size(220.dp),
                contentScale = ContentScale.Fit,
            )
            Text(
                text = "Clan Cast",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Private family SMS broadcasts with a careful, review-first send flow.",
                style = MaterialTheme.typography.bodyLarge,
                color = LogoSky,
                textAlign = TextAlign.Center,
            )
            SectionCard(title = "Responsible Use") {
                Text(
                    text = "Copyright 2026 Numerius Engineering LLC.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Clan Cast is intended for thoughtful, permission-based communication. Please send responsibly, respect recipients' preferences, and avoid unwanted or excessive messaging.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                )
            }
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Continue")
            }
            Text(
                text = "Connect • Share • Stay Close",
                style = MaterialTheme.typography.bodyMedium,
                color = LogoGreen,
            )
        }
    }
}
