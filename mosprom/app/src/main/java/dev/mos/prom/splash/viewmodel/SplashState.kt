package dev.mos.prom.splash.viewmodel

import dev.mos.prom.utils.MosPromResult

data class SplashState (
    val status: MosPromResult = MosPromResult.Loading,
    val hasToken: Boolean = false,
)
