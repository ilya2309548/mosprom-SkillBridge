package dev.mos.prom.profile.viewmodel

import dev.mos.prom.profile.UserModel
import dev.mos.prom.utils.MosPromResult

data class ProfileState (
    val userModel: UserModel = UserModel(
        name = "Кирюшин Алексей Александрович",
        tg = "@lkey"
    ),
    val status: MosPromResult = MosPromResult.Loading,
    val error: String? = null,
)
