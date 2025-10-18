package dev.mos.prom.profile.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
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
                    text = state.error ?: "Ошибка загрузки",
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
                var name by remember(state.userModel.name) { mutableStateOf(state.userModel.name) }
                var description by remember(state.userModel.description) { mutableStateOf(state.userModel.description) }
                var university by remember(state.userModel.university) { mutableStateOf(state.userModel.university) }

                ProfileView(
                    modifier = Modifier
                        .padding(padding),
                    state = state,
                    innerPadding = innerPadding,
                    onUploadAvatar = { filename, bytes, mime ->
                        viewModel.onEvent(ProfileEvent.OnUploadAvatar(filename, bytes, mime))
                    }
                )
                // Minimal inline edit actions (could be a FAB/dialog in real app)
                /* Example of update trigger:
                Button(onClick = {
                    viewModel.onEvent(ProfileEvent.OnUpdateProfile(
                        name = name,
                        telegram = null,
                        description = description,
                        university = university
                    ))
                }) { Text("Сохранить") }
                */
            }
        }
    }

}
