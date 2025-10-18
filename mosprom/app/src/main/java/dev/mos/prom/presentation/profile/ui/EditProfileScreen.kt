package dev.mos.prom.presentation.profile.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import dev.mos.prom.presentation.profile.viewmodel.ProfileEvent
import dev.mos.prom.presentation.profile.viewmodel.ProfileViewModel
import dev.mos.prom.presentation.ui.components.MosTextField
import dev.mos.prom.presentation.ui.text.MosPromErrorMessage
import dev.mos.prom.presentation.ui.text.MosPromLoadingBar
import dev.mos.prom.utils.MosPromResult
import dev.mos.prom.utils.navigation.MosPromTopBar
import dev.mos.prom.R
import org.koin.compose.viewmodel.koinViewModel
import java.io.InputStream
import dev.mos.prom.presentation.ui.util.placeholderPainter

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
                topBar = {
                    MosPromTopBar(
                        title = "Редактировать профиль",
                        navIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(painter = painterResource(R.drawable.ic_back), contentDescription = "Назад")
                            }
                        }
                    )
                },
                containerColor = MaterialTheme.colorScheme.onSurface
            ) { padding ->

                var name by remember(state.userModel.name) { mutableStateOf(state.userModel.name) }
                var description by remember(state.userModel.description) { mutableStateOf(state.userModel.description) }
                var university by remember(state.userModel.education) { mutableStateOf(state.userModel.education) }
                var lastSubmitted by remember { mutableStateOf(false) }
                val context = LocalContext.current
                val pickLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                    if (uri != null) {
                        val bytes = context.contentResolver.openInputStream(uri)?.use(InputStream::readBytes)
                        if (bytes != null) {
                            val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                            val nameF = uri.lastPathSegment?.substringAfterLast('/') ?: "avatar.jpg"
                            viewModel.onEvent(ProfileEvent.OnUploadAvatar(nameF, bytes, mime))
                        }
                    }
                }

                Column(
                    Modifier
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    // Avatar section with placeholder
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (state.userModel.photoUrl != null) {
                            AsyncImage(
                                model = state.userModel.photoUrl,
                                contentDescription = null,
                                placeholder = placeholderPainter(),
                                error = placeholderPainter(),
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { pickLauncher.launch("image/*") }
                            )
                        } else {
                            androidx.compose.foundation.Image(
                                painter = placeholderPainter(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { pickLauncher.launch("image/*") }
                            )
                        }
                        Spacer(Modifier.size(12.dp))
                        Text("Сменить фото", color = Color.Black, modifier = Modifier.clickable { pickLauncher.launch("image/*") })
                    }

                    Spacer(Modifier.size(16.dp))
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
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
