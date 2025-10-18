package dev.mos.prom.utils.navigation

import kotlinx.serialization.Serializable

/**
 * Интерфейс для всех вложенных навигаций
 * */

sealed interface Route {

    @Serializable
    data object Splash : Route

    @Serializable
    data object Main : Route

    @Serializable
    data object Search : Route

    @Serializable
    data object CreatePost : Route

    @Serializable
    data object Notifications : Route

    @Serializable
    data object Profile : Route

    @Serializable
    data object EditProfile : Route

    @Serializable
    data object Login : Route

    @Serializable
    data object Register : Route

    // Club details with id to load data and optional name for title fallback
    @Serializable
    data class Club(val id: Long, val name: String? = null) : Route

}