package dev.mos.prom.presentation.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mos.prom.data.repo.AuthRepository
import dev.mos.prom.utils.MosPromResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterState(
    val telegram: String = "",
    val password: String = "",
    val name: String = "",
    val description: String = "",
    val university: String = "",
    val achievements: String = "",
    val status: MosPromResult = MosPromResult.Success,
    val errorMessage: String? = null,
    val isFinished: Boolean = false,
)

sealed interface RegisterEvent {
    data object Submit: RegisterEvent
    data class TelegramChanged(val v: String): RegisterEvent
    data class PasswordChanged(val v: String): RegisterEvent
    data class NameChanged(val v: String): RegisterEvent
    data class DescriptionChanged(val v: String): RegisterEvent
    data class UniversityChanged(val v: String): RegisterEvent
    data class AchievementsChanged(val v: String): RegisterEvent
}

class RegisterViewModel(private val repo: AuthRepository): ViewModel() {
    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()

    fun onEvent(e: RegisterEvent) {
        when (e) {
            RegisterEvent.Submit -> submit()
            is RegisterEvent.AchievementsChanged -> _state.update { it.copy(achievements = e.v) }
            is RegisterEvent.DescriptionChanged -> _state.update { it.copy(description = e.v) }
            is RegisterEvent.NameChanged -> _state.update { it.copy(name = e.v) }
            is RegisterEvent.PasswordChanged -> _state.update { it.copy(password = e.v) }
            is RegisterEvent.TelegramChanged -> _state.update { it.copy(telegram = e.v) }
            is RegisterEvent.UniversityChanged -> _state.update { it.copy(university = e.v) }
        }
    }

    private fun submit() {
        val s = state.value
        if (s.telegram.isBlank() || s.password.isBlank() || s.name.isBlank()) {
            _state.update { it.copy(status = MosPromResult.Error, errorMessage = "Telegram, Пароль и ФИО обязательны") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(status = MosPromResult.Loading, errorMessage = null) }
            try {
                repo.register(
                    telegram = s.telegram,
                    password = s.password,
                    name = s.name,
                    description = null,
                    university = null,
                    achievements = emptyList(),
                )
                _state.update { it.copy(status = MosPromResult.Success, isFinished = true) }
            } catch (t: Throwable) {
                _state.update { it.copy(status = MosPromResult.Error, errorMessage = t.message) }
            }
        }
    }
}
