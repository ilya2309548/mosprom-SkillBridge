package dev.mos.prom.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mos.prom.profile.UserModel
import dev.mos.prom.utils.MosPromResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel() : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    fun onEvent(
        event: ProfileEvent
    ) {
        when (event) {
            ProfileEvent.OnLoadData -> {
                loadData()
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            delay(timeMillis = 2000L)
            _state.update {
                it.copy(
                    userModel = UserModel(
                        name = "Иван Иванов",
                        tg = "ivan@example.com"
                    ),
                    status = MosPromResult.Success
                )
            }
        }
    }

}
