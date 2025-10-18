package dev.mos.prom.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mos.prom.splash.viewmodel.SplashEvent
import dev.mos.prom.splash.viewmodel.SplashState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class ProfileViewModel() : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state = _state.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
        _state.value
    )

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

    }

}
