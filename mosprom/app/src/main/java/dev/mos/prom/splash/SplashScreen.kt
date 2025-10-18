package dev.mos.prom.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SplashScreen (
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues,
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("splash.json"))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onSurfaceVariant)
    ) {
        LottieAnimation(
            composition = composition,
            progress = {
                progress
            },
            modifier = Modifier
                .fillMaxSize()
        )
    }
}