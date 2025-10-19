package dev.mos.prom.presentation.profile.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.mos.prom.utils.navigation.Route
import dev.mos.prom.utils.navigation.MosPromBottomBar
import dev.mos.prom.utils.navigation.MosPromTopBar
import dev.mos.prom.presentation.profile.viewmodel.ProfileEvent
import dev.mos.prom.presentation.profile.viewmodel.ProfileViewModel
import dev.mos.prom.presentation.ui.text.MosPromErrorMessage
import dev.mos.prom.presentation.ui.text.MosPromLoadingBar
import dev.mos.prom.utils.MosPromResult
import org.koin.compose.viewmodel.koinViewModel
import dev.mos.prom.R
import kotlin.random.Random

@Composable
fun ProfileScreen (
    viewModel: ProfileViewModel = koinViewModel<ProfileViewModel>(),
    navController: NavController,
    innerPadding: PaddingValues,
) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    val profileService: dev.mos.prom.data.api.ProfileService = koinInject()
    var myAchievements by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedAchievement by remember { mutableStateOf<String?>(null) }
    var overlayColor by remember { mutableStateOf<Color?>(null) }
    var overlayImageRes by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        viewModel.onEvent(ProfileEvent.OnLoadData)
        try {
            val list: List<String> = profileService.myAchievements()
            myAchievements = list
        } catch (_: Throwable) {
            // ignore for now; could log or show a snackbar if needed
        }
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

                if (myAchievements.isNotEmpty()) {
                    androidx.compose.material3.Text(
                        text = "Мои ачивки",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        val bgChoices = listOf(Color(0xFF2A54B9), Color(0xFFA3286D), Color(0xFF537E70))
                        val imgChoices = listOf(R.drawable.achieve_1, R.drawable.achieve_2, R.drawable.achieve_3)
                        myAchievements.forEach { a ->
                            androidx.compose.material3.AssistChip(
                                onClick = {
                                    selectedAchievement = a
                                    overlayColor = bgChoices[Random.nextInt(bgChoices.size)]
                                    overlayImageRes = imgChoices[Random.nextInt(imgChoices.size)]
                                },
                                enabled = true,
                                label = { androidx.compose.material3.Text(a) }
                            )
                        }
                    }
                }

                // Full-screen achievement overlay
                if (selectedAchievement != null && overlayColor != null && overlayImageRes != null) {
                    Dialog(
                        onDismissRequest = { selectedAchievement = null },
                        properties = DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(overlayColor!!)
                        ) {
                            // Top-left back button
                            IconButton(
                                onClick = { selectedAchievement = null },
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp)
                            ) {
                                Icon(painter = painterResource(id = R.drawable.ic_back), contentDescription = "Назад", tint = Color.White)
                            }

                            // Centered achievement image
                            Image(
                                painter = painterResource(id = overlayImageRes!!),
                                contentDescription = selectedAchievement,
                                modifier = Modifier
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
                
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
