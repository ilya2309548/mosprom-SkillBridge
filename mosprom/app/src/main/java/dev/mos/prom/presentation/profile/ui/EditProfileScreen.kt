package dev.mos.prom.presentation.profile.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import dev.mos.prom.presentation.profile.viewmodel.ProfileEvent
import dev.mos.prom.presentation.profile.viewmodel.ProfileViewModel
import dev.mos.prom.presentation.ui.components.MosTextField
import dev.mos.prom.presentation.ui.text.MosPromErrorMessage
import dev.mos.prom.presentation.ui.text.MosPromLoadingBar
import dev.mos.prom.utils.MosPromResult
import dev.mos.prom.utils.navigation.MosPromTopBar
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EditProfileScreen(
    navController: NavController,
    innerPadding: PaddingValues,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(ProfileEvent.OnLoadData)
    }

    when (state.status) {
        MosPromResult.Loading -> MosPromLoadingBar(modifier = Modifier.padding(innerPadding))
        MosPromResult.Error -> MosPromErrorMessage(
            modifier = Modifier.padding(innerPadding),
            text = state.error ?: "Ошибка загрузки",
            onUpdate = { viewModel.onEvent(ProfileEvent.OnLoadData) }
        )
        MosPromResult.Success -> {
            Scaffold(
                topBar = { MosPromTopBar(title = "Редактировать профиль") },
                containerColor = MaterialTheme.colorScheme.onSurface
            ) { padding ->

                var name by remember(state.userModel.name) { mutableStateOf(state.userModel.name) }
                var description by remember(state.userModel.description) { mutableStateOf(state.userModel.description) }
                var university by remember(state.userModel.education) { mutableStateOf(state.userModel.education) }
                var lastSubmitted by remember { mutableStateOf(false) }

                Column(
                    Modifier
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    MosTextField(label = "ФИО", value = name, onValueChange = { name = it })

                    MosTextField(label = "О себе", value = description, onValueChange = { description = it }, modifier = Modifier.padding(top = 12.dp))

                    MosTextField(label = "Образование", value = university, onValueChange = { university = it }, modifier = Modifier.padding(top = 12.dp))

                    Button(
                        onClick = {
                            lastSubmitted = true
                            viewModel.onEvent(
                                ProfileEvent.OnUpdateProfile(
                                    name = name,
                                    description = description,
                                    university = university,
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) { Text("Сохранить") }

                    LaunchedEffect(state.status, lastSubmitted) {
                        if (lastSubmitted) {
                            lastSubmitted = false
                            navController.popBackStack()
                        }
                    }
                }
            }
        }
    }
}
