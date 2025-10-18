package dev.mos.prom.presentation.splash.viewmodel

sealed class SplashEvent {
    data object OnLoadData : SplashEvent()
}
