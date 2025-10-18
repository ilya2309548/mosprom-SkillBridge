package dev.mos.prom.profile.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.mos.prom.R
import dev.mos.prom.profile.ui.components.AchievementChip
import dev.mos.prom.profile.ui.components.SectionHeader
import dev.mos.prom.profile.viewmodel.ProfileState

@Composable
fun ProfileView(
    modifier: Modifier = Modifier,
    state: ProfileState,
    innerPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        // Аватар и имя
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_avatar_placeholder),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "Венедиктов Павел",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("3 проекта", style = MaterialTheme.typography.bodySmall)
                    Text("12 событий", style = MaterialTheme.typography.bodySmall)
                    Text("102 клуба", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Блоки
        SectionHeader("О себе")
        Text(
            "Активно участвую в развитии классических машиностроительных производств.",
            style = MaterialTheme.typography.bodyMedium
        )

        SectionHeader("Работа")
        Text("Инженер-исследователь,", fontWeight = FontWeight.Bold)
        Text("Современные Технологии Газовых Турбин")

        SectionHeader("Образование")
        Text("МТИ  •  бакалавриат", fontWeight = FontWeight.Bold)
        Text("Технологические машины и оборудование в промышленности")

        SectionHeader("Направления")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Конструкторско-технологическое обеспечение",
                "Документация",
                "Роботизация производства",
                "ЧПУ",
                "Авиационная техника",
                "Развернуть..."
            ).forEach {
                AssistChip(
                    onClick = { },
                    label = { Text(it) }
                )
            }
        }

        SectionHeader("Достижения")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AchievementChip("Хакатон от МАИ", Color(0xFF964B00))
            AchievementChip("Лекция от МТИ", Color(0xFFB8860B))
            AchievementChip("Воркшоп от МЭИ", Color(0xFF4169E1))
            AchievementChip("Кейс от МАИ", Color(0xFF4B8B3B))
        }

        SectionHeader("Проекты")
        Text("Ускоритель заряженных частиц", fontWeight = FontWeight.Bold)

        TextButton(onClick = { /* читать подробнее */ }) {
            Text("Читать")
        }
    }
}
