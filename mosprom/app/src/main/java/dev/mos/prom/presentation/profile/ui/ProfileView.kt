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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.mos.prom.R
import dev.mos.prom.presentation.ui.text.AchievementChip
import dev.mos.prom.presentation.ui.text.SectionHeader
import dev.mos.prom.presentation.profile.viewmodel.ProfileState
import java.io.InputStream

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
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { pickLauncher.launch("image/*") }
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_avatar_placeholder),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp)
                        .clickable { pickLauncher.launch("image/*") },
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = state.userModel.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.Black)
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("3 проекта", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                    Text("12 событий", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                    Text("102 клуба", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Блоки
        SectionHeader("О себе")
        Text(state.userModel.description.ifBlank { "—" }, style = MaterialTheme.typography.bodyMedium, color = Color.Black)

    SectionHeader("Образование")
    Text(state.userModel.education.ifBlank { "—" }, fontWeight = FontWeight.Bold, color = Color.Black)

        SectionHeader("Направления")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (state.userModel.directions.ifEmpty { listOf("—") }).forEach {
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
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.userModel.achievements.take(4).forEachIndexed { idx, a ->
                AchievementChip(a, Color(0xFF964B00 + (idx * 1000)))
            }
        }

        SectionHeader("Проекты")
        Text("—", fontWeight = FontWeight.Bold, color = Color.Black)


    }
}
