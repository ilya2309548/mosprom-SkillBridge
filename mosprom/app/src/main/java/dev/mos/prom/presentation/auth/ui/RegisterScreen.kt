package dev.mos.prom.presentation.auth.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import dev.mos.prom.presentation.auth.viewmodel.RegisterEvent
import dev.mos.prom.presentation.auth.viewmodel.RegisterViewModel
import dev.mos.prom.utils.navigation.Route
import dev.mos.prom.utils.MosPromResult
import org.koin.compose.viewmodel.koinViewModel
import dev.mos.prom.presentation.ui.components.MosTextField
import androidx.compose.ui.text.input.PasswordVisualTransformation

@Composable
fun RegisterScreen(
    navController: NavController,
    innerPadding: PaddingValues,
    viewModel: RegisterViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isFinished) {
        if (state.isFinished) {
            navController.navigate(Route.Profile) {
                popUpTo(Route.Splash) { inclusive = true }
            }
        }
    }

    Column(Modifier.padding(innerPadding).padding(16.dp)) {
        MosTextField(
            label = "Telegram*",
            value = state.telegram,
            onValueChange = { viewModel.onEvent(RegisterEvent.TelegramChanged(it)) })

        Spacer(Modifier.height(12.dp))

        MosTextField(label = "ФИО*", value = state.name, onValueChange = { viewModel.onEvent(RegisterEvent.NameChanged(it)) })

        Spacer(Modifier.height(12.dp))

        MosTextField(
            label = "Пароль*",
            value = state.password,
            onValueChange = { viewModel.onEvent(RegisterEvent.PasswordChanged(it)) },
            visualTransformation = PasswordVisualTransformation()
        )

        Button(onClick = { viewModel.onEvent(RegisterEvent.Submit) }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Text("Создать аккаунт", style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
        }

        if (state.status == MosPromResult.Error) {
            Text("Ошибка: ${state.errorMessage ?: "неизвестно"}", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
        }
    }
}
