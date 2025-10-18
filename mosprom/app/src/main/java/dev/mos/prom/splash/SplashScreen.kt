package dev.mos.prom.splash

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import dev.mos.prom.navigation.Route
import dev.mos.prom.ui.text.MosPromErrorMessage
import dev.mos.prom.utils.MosPromResult

@Composable
fun SplashScreen (
    navController: NavController,
    viewModel : SplashViewModel,
    innerPadding: PaddingValues,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(SplashEvent.OnLoadData)
    }

    when (state.status) {
        MosPromResult.Error -> {
            MosPromErrorMessage(
                text = "Не удалось загрузить данные",
                onUpdate = {
                    viewModel.onEvent(SplashEvent.OnLoadData)
                },
                modifier = Modifier,
            )
        }
        MosPromResult.Loading -> {
            SplashView(
                innerPadding = innerPadding
            )
        }
        MosPromResult.Success -> {
            LaunchedEffect(Unit) {
                navController.navigate(Route.Profile) {
                    popUpTo(Route.Splash) {
                        inclusive = true
                    }
                }
            }
        }
    }
}
