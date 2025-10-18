package dev.mos.prom.profile.viewmodel

sealed class ProfileEvent {
    data object OnLoadData : ProfileEvent()
}
