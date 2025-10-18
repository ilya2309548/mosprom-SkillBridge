package dev.mos.prom.presentation.profile

import kotlinx.serialization.Serializable

@Serializable
data class UserModel (
    val name: String,
    val tg: String,
    val description: String = "",
    val education: String = "",
    val achievements: List<String> = emptyList(),
    val photoUrl: String? = null,
    val directions: List<String> = emptyList(),
)