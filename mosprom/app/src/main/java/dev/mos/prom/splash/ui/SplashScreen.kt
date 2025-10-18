package dev.mos.prom.splash.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import dev.mos.prom.navigation.Route
import dev.mos.prom.splash.viewmodel.SplashEvent
import dev.mos.prom.splash.viewmodel.SplashViewModel
import dev.mos.prom.ui.text.MosPromErrorMessage
import dev.mos.prom.utils.MosPromResult
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashScreen (
    navController: NavController,
    viewModel : SplashViewModel = koinViewModel<SplashViewModel>(),
    innerPadding: PaddingValues,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(SplashEvent.OnLoadData)
    }

    when (state.status) {
        is MosPromResult.Error -> {
            MosPromErrorMessage(
                text = "Не удалось загрузить данные",
                onUpdate = {
                    viewModel.onEvent(SplashEvent.OnLoadData)
                },
                modifier = Modifier,
            )
        }
        is MosPromResult.Loading -> {
            SplashView(
                innerPadding = innerPadding
            )
        }
        is MosPromResult.Success -> {
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
