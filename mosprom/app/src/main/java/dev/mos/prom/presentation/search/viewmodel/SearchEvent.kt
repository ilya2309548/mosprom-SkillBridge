package dev.mos.prom.presentation.search.viewmodel

sealed interface SearchEvent {
    data object OnLoad : SearchEvent
    data class QueryChanged(val value: String) : SearchEvent
    data class ToggleDirection(val name: String) : SearchEvent
    data object DoSearch : SearchEvent
    data object Retry : SearchEvent
}