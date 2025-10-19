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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import dev.mos.prom.R
import dev.mos.prom.presentation.chat.viewmodel.ChatEvent
import dev.mos.prom.presentation.chat.viewmodel.ChatViewModel
import dev.mos.prom.utils.navigation.MosPromTopBar
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ClubChatScreen(
    navController: NavController,
    innerPadding: PaddingValues,
    clubName: String,
) {
    val viewModel: ChatViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(clubName) {
        viewModel.onEvent(ChatEvent.Connect(clubName))
    }
    DisposableEffect(Unit) {
        onDispose { viewModel.onEvent(ChatEvent.Disconnect) }
    }

    Scaffold(
        topBar = {
            MosPromTopBar(
                title = "Чат — ${state.title.ifBlank { clubName }}",
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
            if (state.status == dev.mos.prom.utils.MosPromResult.Error) {
                Text(
                    state.statusText ?: "Ошибка подключения к чату",
                    color = Color(0xFFB00020),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(8.dp))
            } else if (!state.connected) {
                Text(
                    "Подключение к чату…",
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                Text(
                    "Непрочитанные сообщения",
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f, fill = true)) {
                state.messages.forEach { m ->
                    MessageRow(
                        avatarUrl = null,
                        author = m.author ?: "",
                        text = m.text,
                        time = m.timestamp,
                        isOutgoing = m.isOutgoing,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                TextField(
                    value = state.input,
                    onValueChange = { viewModel.onEvent(ChatEvent.InputChanged(it)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { viewModel.onEvent(ChatEvent.Send) }),
                    placeholder = { Text("Сообщение…") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(color = Color.Black),
                )

                val sendEnabled = state.connected && state.input.isNotBlank()

                IconButton(
                    onClick = { viewModel.onEvent(ChatEvent.Send) },
                    enabled = sendEnabled,
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Отправить",
                        tint = if (sendEnabled) MaterialTheme.colorScheme.primary else Color.Gray,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

