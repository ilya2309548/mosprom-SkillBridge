package dev.mos.prom.splash.viewmodel

sealed class SplashEvent {
    data object OnLoadData : SplashEvent()
}
