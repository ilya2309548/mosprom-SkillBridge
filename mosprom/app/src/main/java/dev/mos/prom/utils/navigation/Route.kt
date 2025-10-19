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
    data object CreateClub : Route

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

    @Serializable
    data class Club(
        val id: Long,
        val name: String? = null,
        val logoUrl: String? = null,
        val description: String? = null,
        val directions: List<String> = emptyList(),
        val isCreator: Boolean = false,
    ) : Route

    @Serializable
    data class ClubChat(
        val id: Long,
        val name: String
    ) : Route

    @Serializable
    data class CreatePost(
        val clubId: Long,
        val clubName: String? = null,
    ) : Route

}