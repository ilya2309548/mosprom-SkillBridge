package dev.mos.prom.presentation.profile.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import dev.mos.prom.R
import dev.mos.prom.presentation.profile.viewmodel.ProfileState
import dev.mos.prom.presentation.ui.text.AchievementChip
import dev.mos.prom.presentation.ui.text.SectionHeader
import dev.mos.prom.utils.placeholderPainter
import java.io.InputStream
import kotlin.random.Random

@Composable
fun ProfileView(
    modifier: Modifier = Modifier,
    state: ProfileState,
    innerPadding: PaddingValues,
    onUploadAvatar: (filename: String, bytes: ByteArray, mime: String) -> Unit = { _, _, _ -> },
) {
    val context = LocalContext.current
    val pickLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use(InputStream::readBytes)
            if (bytes != null) {
                val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                val name = uri.lastPathSegment?.substringAfterLast('/') ?: "avatar.jpg"
                onUploadAvatar(name, bytes, mime)
            }
        }
    }
    Column(
        modifier = modifier
            .background(Color.White)
            .padding(innerPadding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        // Аватар и имя
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = state.userModel.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("3 проекта", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                    Text("12 событий", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                    Text("${state.clubsCount} клуба", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Блоки
        SectionHeader("О себе")
        Text(state.userModel.description.ifBlank { "—" }, style = MaterialTheme.typography.bodyMedium, color = Color.Black)

        SectionHeader("Образование")
        Text(state.userModel.education.ifBlank { "—" }, style = MaterialTheme.typography.bodyMedium, color = Color.Black)

        SectionHeader("Технологии")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (state.userModel.technologies.ifEmpty { listOf("—") }).forEach {
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = it,
                            color = Color.Black
                        )
                    }
                )
            }
        }

        SectionHeader("Достижения")
        var selectedAchievement by remember { mutableStateOf<String?>(null) }
        var overlayColor by remember { mutableStateOf<Color?>(null) }
        var overlayImageRes by remember { mutableStateOf<Int?>(null) }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val bgChoices = listOf(Color(0xFF2A54B9), Color(0xFFA3286D), Color(0xFF537E70))
            val imgChoices = listOf(R.drawable.achieve_1, R.drawable.achieve_2, R.drawable.achieve_3)
            state.userModel.achievements.take(4).forEach { a ->
                AchievementChip(a, Color(0xFF537E70)) {
                    selectedAchievement = a
                    overlayColor = bgChoices[Random.nextInt(bgChoices.size)]
                    overlayImageRes = imgChoices[Random.nextInt(imgChoices.size)]
                }
            }
        }

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
                    IconButton(
                        onClick = { selectedAchievement = null },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_back), contentDescription = "Назад", tint = Color.White)
                    }
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier
                            .align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = overlayImageRes!!),
                            contentDescription = selectedAchievement
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = selectedAchievement ?: "",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        SectionHeader("Проекты")
    Text("—", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.Black)


    }
}
