package dev.mos.prom.presentation.splash.viewmodel

import dev.mos.prom.utils.MosPromResult

data class SplashState (
    val status: MosPromResult = MosPromResult.Loading,
    val hasToken: Boolean = false,
)
