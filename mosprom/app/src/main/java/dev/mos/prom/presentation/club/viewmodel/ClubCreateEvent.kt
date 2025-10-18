package dev.mos.prom.presentation.club.viewmodel

sealed interface ClubCreateEvent {
    data object OnLoad : ClubCreateEvent
    data class NameChanged(val v: String): ClubCreateEvent
    data class DescriptionChanged(val v: String): ClubCreateEvent
    data class ToggleDirection(val name: String): ClubCreateEvent
    data class SetLogo(val filename: String, val bytes: ByteArray, val mime: String): ClubCreateEvent
    data object Submit: ClubCreateEvent
}
