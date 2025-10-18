package dev.mos.prom.profile

import kotlinx.serialization.Serializable

@Serializable
data class UserModel (
    val name: String,
    val tg: String,
    val description: String = "",
    val university: String = "",
    val achievements: List<String> = emptyList(),
    val photoUrl: String? = null,
)