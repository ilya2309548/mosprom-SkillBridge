package dev.mos.prom.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn


class SplashViewModel() : ViewModel() {

    private val _state = MutableStateFlow(SplashState())
    val state = _state.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
        _state.value
    )

    fun onEvent(
        event: SplashEvent
    ) {
        when (event) {
            SplashEvent.OnLoadData -> {

            }
        }
    }

    fun loadData() {

    }
}
