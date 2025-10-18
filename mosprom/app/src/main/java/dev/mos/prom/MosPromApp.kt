package dev.mos.prom

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.mos.prom.navigation.Route
import dev.mos.prom.profile.ProfileScreen
import dev.mos.prom.splash.ui.SplashScreen
import dev.mos.prom.splash.viewmodel.SplashViewModel
import dev.mos.prom.ui.theme.MospromTheme

@Composable
fun MosPromApp() {

    val navController = rememberNavController()

    MospromTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Route.Splash,
            ) {
                composable<Route.Splash>(
                    exitTransition = { slideOutHorizontally() },
                    popEnterTransition = { slideInHorizontally() }
                ) {
                    SplashScreen(
                        navController = navController,
                        viewModel = SplashViewModel(),
                        innerPadding = innerPadding,
                    )
                }

                composable<Route.Profile>(
                    exitTransition = { slideOutHorizontally() },
                    popEnterTransition = { slideInHorizontally() }
                ) {
                    ProfileScreen(
                        innerPadding = innerPadding,
                        navController = navController,
                    )
                }

            }
        }
    }


}
