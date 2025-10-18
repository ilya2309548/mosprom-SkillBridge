package dev.mos.prom.profile.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import dev.mos.prom.navigation.MosPromBottomBar
import dev.mos.prom.navigation.MosPromTopBar
import dev.mos.prom.profile.viewmodel.ProfileEvent
import dev.mos.prom.profile.viewmodel.ProfileViewModel
import dev.mos.prom.ui.text.MosPromErrorMessage
import dev.mos.prom.ui.text.MosPromLoadingBar
import dev.mos.prom.utils.MosPromResult
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen (
    viewModel: ProfileViewModel = koinViewModel<ProfileViewModel>(),
    navController: NavController,
    innerPadding: PaddingValues,
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(ProfileEvent.OnLoadData)
    }

    when (state.status) {

        MosPromResult.Error -> {
            MosPromErrorMessage(
                modifier = Modifier
                    .padding(innerPadding),
                text = "Ошибка загрузки",
                onUpdate = {
                    viewModel.onEvent(ProfileEvent.OnLoadData)
                }
            )
        }
        MosPromResult.Loading -> {
            MosPromLoadingBar(
                modifier = Modifier
                    .padding(innerPadding)
            )
        }
        MosPromResult.Success -> {
            Scaffold (
                bottomBar = {
                    MosPromBottomBar(
                        navController = navController
                    )
                },
                topBar = {
                    MosPromTopBar(
                        title = "Профиль",
                    )
                },
                modifier = Modifier
                    .fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.onSurface,
            ) { padding ->

                ProfileView(
                    modifier = Modifier
                        .padding(padding),
                    state = state,
                    innerPadding = innerPadding,
                )
            }
        }
    }

}
