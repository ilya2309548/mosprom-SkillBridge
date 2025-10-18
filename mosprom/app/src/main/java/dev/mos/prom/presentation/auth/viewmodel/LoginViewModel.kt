package dev.mos.prom.presentation.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mos.prom.data.repo.AuthRepository
import dev.mos.prom.utils.MosPromResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginState(
    val telegram: String = "",
    val password: String = "",
    val status: MosPromResult = MosPromResult.Success,
    val errorMessage: String? = null,
    val isFinished: Boolean = false,
)

sealed interface LoginEvent {
    data object Submit: LoginEvent
    data class TelegramChanged(val v: String): LoginEvent
    data class PasswordChanged(val v: String): LoginEvent
}

class LoginViewModel(private val repo: AuthRepository): ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun onEvent(e: LoginEvent) {
        when (e) {
            LoginEvent.Submit -> submit()
            is LoginEvent.PasswordChanged -> _state.update { it.copy(password = e.v) }
            is LoginEvent.TelegramChanged -> _state.update { it.copy(telegram = e.v) }
        }
    }

    private fun submit() {
        val s = state.value
        if (s.telegram.isBlank() || s.password.isBlank()) {
            _state.update { it.copy(status = MosPromResult.Error, errorMessage = "Заполните все поля") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(status = MosPromResult.Loading, errorMessage = null) }
            try {
                repo.login(s.telegram, s.password)
                _state.update { it.copy(status = MosPromResult.Success, isFinished = true) }
            } catch (t: Throwable) {
                _state.update { it.copy(status = MosPromResult.Error, errorMessage = t.message) }
            }
        }
    }
}
