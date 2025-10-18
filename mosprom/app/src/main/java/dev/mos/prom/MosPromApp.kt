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
import dev.mos.prom.utils.navigation.Route
import dev.mos.prom.presentation.auth.ui.LoginScreen
import dev.mos.prom.presentation.auth.ui.RegisterScreen
import dev.mos.prom.presentation.profile.ui.ProfileScreen
import dev.mos.prom.presentation.profile.ui.EditProfileScreen
import dev.mos.prom.presentation.club.ui.ClubCreateScreen
import dev.mos.prom.presentation.search.ui.SearchScreen
import dev.mos.prom.presentation.splash.ui.SplashScreen
import dev.mos.prom.presentation.ui.theme.MospromTheme

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
                        innerPadding = innerPadding,
                    )
                }

                composable<Route.Search> {
                    SearchScreen(
                        navController = navController,
                        innerPadding = innerPadding,
                    )
                }

                composable<Route.Login> {
                    LoginScreen(
                        navController = navController,
                        innerPadding = innerPadding,
                    )
                }

                composable<Route.Register> {
                    RegisterScreen(
                        navController = navController,
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

                composable<Route.EditProfile>(
                    exitTransition = { slideOutHorizontally() },
                    popEnterTransition = { slideInHorizontally() }
                ) {
                    EditProfileScreen(
                        innerPadding = innerPadding,
                        navController = navController,
                    )
                }

                composable<Route.CreatePost>(
                    exitTransition = { slideOutHorizontally() },
                    popEnterTransition = { slideInHorizontally() }
                ) {
                    ClubCreateScreen(
                        innerPadding = innerPadding,
                        navController = navController,
                    )
                }

            }
        }
    }


}
