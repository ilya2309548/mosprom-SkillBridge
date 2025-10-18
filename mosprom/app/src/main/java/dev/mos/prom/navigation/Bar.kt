package dev.mos.prom.navigation

import androidx.annotation.DrawableRes
import dev.mos.prom.R

/**
 Все табы приложения
 */

sealed class Bar (
    val route: Route,
    val title: String,
    @DrawableRes val icon: Int
) {

    data object Main : Bar(
        route = Route.Main,
        title = "Главная",
        icon = R.drawable.ic_home
    )

    data object Search : Bar(
        route = Route.Search,
        title = "Поиск",
        icon = R.drawable.ic_search
    )

    data object CreatePost : Bar(
        route = Route.CreatePost,
        title = "Создать пост",
        icon = R.drawable.ic_add
    )

    data object Notifications : Bar(
        route = Route.Notifications,
        title = "Нотификация",
        icon = R.drawable.ic_notification
    )

    data object Profile : Bar(
        route = Route.Profile,
        title = "Профиль",
        icon = R.drawable.ic_profile
    )

    companion object {
        val items = listOf(Main, Search, CreatePost, Notifications, Profile)
    }
}
