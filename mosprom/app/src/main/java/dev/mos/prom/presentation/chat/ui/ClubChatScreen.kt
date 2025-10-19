package dev.mos.prom.presentation.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.mos.prom.R
import dev.mos.prom.utils.navigation.MosPromTopBar

@Composable
fun ClubChatScreen(
    navController: NavController,
    innerPadding: PaddingValues,
    clubName: String,
) {
    Scaffold(
        topBar = {
            MosPromTopBar(
                title = "Чат — $clubName",
                navIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(painter = painterResource(R.drawable.ic_back), contentDescription = "Назад")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.onSurface
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 12.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Text("Непрочитанные сообщения", color = Color.Gray, style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                MessageRow(
                    avatarUrl = null,
                    author = "Тёма Игнатьев",
                    text = "Привет! Подскажите, кто завтра пойдет на лекцию?",
                    time = "20:04",
                    isOutgoing = false
                )

                MessageRow(
                    avatarUrl = null,
                    author = "Ксения Бондаренко",
                    text = "Привет, я пойду",
                    time = "20:07",
                    isOutgoing = false
                )

                MessageRow(
                    avatarUrl = null,
                    author = "Венедиктов Павел",
                    text = "я тоже",
                    time = "20:10",
                    isOutgoing = true
                )

                MessageRow(
                    avatarUrl = null,
                    author = "",
                    text = "пойду",
                    time = "20:10",
                    isOutgoing = true
                )

                MessageRow(
                    avatarUrl = null,
                    author = "Джеймис Харрисон",
                    text = "я пропускаю в этот раз",
                    time = "20:15",
                    isOutgoing = false
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Сообщение…", color = Color.Gray, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "send",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

