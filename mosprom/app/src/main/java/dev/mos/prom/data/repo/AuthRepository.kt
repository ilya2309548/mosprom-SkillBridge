package dev.mos.prom.data.repo

import dev.mos.prom.data.api.AuthService
import dev.mos.prom.data.api.LoginRequest
import dev.mos.prom.data.api.RegisterRequest
import dev.mos.prom.data.storage.TokenStorage

class AuthRepository(
    private val auth: AuthService,
    private val tokens: TokenStorage,
) {
    suspend fun login(telegram: String, password: String): String {
        val normalizedTg = telegram.trim().removePrefix("@")
        val res = auth.login(LoginRequest(telegramName = normalizedTg, password = password))
        tokens.saveAccessToken(res.token)
        return res.token
    }

    suspend fun register(
        telegram: String,
        password: String,
        name: String?,
        description: String?,
        university: String?,
        achievements: List<String>,
    ): String {
        val normalizedTg = telegram.trim().removePrefix("@")
        val req = RegisterRequest(
            telegramName = normalizedTg,
            password = password,
            name = name?.takeIf { it.isNotBlank() },
            description = null,
            university = null,
            achievements = emptyList(),
        )
        val res = auth.register(req)
        tokens.saveAccessToken(res.token)
        return res.token
    }
}
