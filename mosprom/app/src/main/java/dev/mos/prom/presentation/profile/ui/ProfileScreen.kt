package dev.mos.prom.presentation.profile.ui

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.unit.dp
import dev.mos.prom.utils.navigation.Route
import dev.mos.prom.utils.navigation.MosPromBottomBar
import dev.mos.prom.utils.navigation.MosPromTopBar
import dev.mos.prom.presentation.profile.viewmodel.ProfileEvent
import dev.mos.prom.presentation.profile.viewmodel.ProfileViewModel
import dev.mos.prom.presentation.ui.text.MosPromErrorMessage
import dev.mos.prom.presentation.ui.text.MosPromLoadingBar
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
                    var expanded by remember { mutableStateOf(false) }
                    MosPromTopBar(
                        title = "Профиль",
                        actions = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "Меню")
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                DropdownMenuItem(
                                    text = { androidx.compose.material3.Text("Редактировать") },
                                    onClick = {
                                        expanded = false
                                        navController.navigate(Route.EditProfile)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { androidx.compose.material3.Text("Выйти") },
                                    onClick = {
                                        expanded = false
                                        viewModel.onEvent(ProfileEvent.OnLogout)
                                    }
                                )
                            }
                        }
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
                    onUploadAvatar = { filename, bytes, mime ->
                        viewModel.onEvent(ProfileEvent.OnUploadAvatar(filename, bytes, mime))
                    }
                )
                
                if (state.loggedOut) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Route.Login) {
                            popUpTo(Route.Profile) { inclusive = true }
                        }
                    }
                }
            }
        }
    }

}
