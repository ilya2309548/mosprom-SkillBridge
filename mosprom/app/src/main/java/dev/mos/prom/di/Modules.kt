package dev.mos.prom.di

import dev.mos.prom.data.api.AuthService
import dev.mos.prom.data.api.KtorClientProvider
import dev.mos.prom.data.api.ProfileService
import dev.mos.prom.data.api.ClubService
import dev.mos.prom.data.api.ChatService
import dev.mos.prom.data.api.PostService
import dev.mos.prom.data.repo.AuthRepository
import dev.mos.prom.data.repo.ProfileRepository
import dev.mos.prom.data.repo.ClubRepository
import dev.mos.prom.presentation.search.viewmodel.SearchViewModel
import dev.mos.prom.data.storage.TokenStorage
import dev.mos.prom.presentation.auth.viewmodel.LoginViewModel
import dev.mos.prom.presentation.auth.viewmodel.RegisterViewModel
import dev.mos.prom.presentation.profile.viewmodel.ProfileViewModel
import dev.mos.prom.presentation.splash.viewmodel.SplashViewModel
import dev.mos.prom.presentation.club.viewmodel.ClubCreateViewModel
import dev.mos.prom.presentation.club.viewmodel.ClubDetailsViewModel
import dev.mos.prom.presentation.chat.viewmodel.ChatViewModel
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val modules = module {

    // Storage
    single { TokenStorage(androidContext()) }

    // Network
    single { KtorClientProvider(baseHost = "81.29.146.35", basePort = 8080, tokenStorage = get()) }
    single { get<KtorClientProvider>().client }

    // Services
    single { AuthService(get()) }
    single { ProfileService(get()) }
    single { ClubService(get()) }
    single { ChatService(get()) }
    single { PostService(get()) }

    // JSON
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = false
            encodeDefaults = true
            coerceInputValues = true
        }
    }

    // Repositories
    single { AuthRepository(auth = get(), tokens = get()) }
    single { ProfileRepository(api = get()) }
    single { ClubRepository(api = get()) }

    /* View Models */
    viewModelOf(::SplashViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::ClubCreateViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::ClubDetailsViewModel)
    viewModelOf(::ChatViewModel)
}