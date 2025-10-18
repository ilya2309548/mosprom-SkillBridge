package dev.mos.prom.profile

import kotlinx.serialization.Serializable

@Serializable
data class UserModel (
    val name: String,
    val tg: String,
)