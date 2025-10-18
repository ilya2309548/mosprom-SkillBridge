package dev.mos.prom.di

import dev.mos.prom.profile.viewmodel.ProfileViewModel
import dev.mos.prom.splash.viewmodel.SplashViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val modules = module {

    /* View Model*/
    viewModelOf(::SplashViewModel)
    viewModelOf(::ProfileViewModel)
}