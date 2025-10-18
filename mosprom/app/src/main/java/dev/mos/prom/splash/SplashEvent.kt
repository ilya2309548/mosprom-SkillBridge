package dev.mos.prom.splash

sealed class SplashEvent {
    data object OnLoadData : SplashEvent()
}
