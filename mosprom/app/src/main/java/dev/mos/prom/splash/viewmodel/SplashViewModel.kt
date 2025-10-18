package dev.mos.prom.splash.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mos.prom.utils.MosPromResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


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
                loadData()
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            delay(timeMillis = 2000L)

            _state.update {
                it.copy(
                    status = MosPromResult.Success
                )
            }
        }
    }

}
