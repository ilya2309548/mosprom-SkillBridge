package dev.mos.prom.presentation.auth.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import dev.mos.prom.presentation.auth.viewmodel.LoginEvent
import dev.mos.prom.presentation.auth.viewmodel.LoginViewModel
import dev.mos.prom.utils.navigation.Route
import dev.mos.prom.utils.MosPromResult
import org.koin.compose.viewmodel.koinViewModel
import dev.mos.prom.presentation.ui.components.MosTextField
import androidx.compose.ui.text.input.PasswordVisualTransformation

@Composable
fun LoginScreen(
    navController: NavController,
    innerPadding: PaddingValues,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snack: SnackbarHostState = SnackbarHostState()

    LaunchedEffect(state.isFinished) {
        if (state.isFinished) {
            navController.navigate(Route.Profile) {
                popUpTo(Route.Splash) { inclusive = true }
            }
        }
    }

    Column(Modifier.padding(innerPadding).padding(16.dp)) {
        SnackbarHost(hostState = snack)
        MosTextField(label = "Telegram", value = state.telegram, onValueChange = { viewModel.onEvent(LoginEvent.TelegramChanged(it)) })
        Spacer(Modifier.height(12.dp))
        MosTextField(label = "Пароль", value = state.password, onValueChange = { viewModel.onEvent(LoginEvent.PasswordChanged(it)) }, visualTransformation = PasswordVisualTransformation())
        Button(onClick = { viewModel.onEvent(LoginEvent.Submit) }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Text("Войти")
        }
        Button(onClick = { navController.navigate(Route.Register) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("Регистрация")
        }
        if (state.status == MosPromResult.Error) {
            Text("Ошибка: ${state.errorMessage ?: "неизвестно"}")
        }
    }
}
