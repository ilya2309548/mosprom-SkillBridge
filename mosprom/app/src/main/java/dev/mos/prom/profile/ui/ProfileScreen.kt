package dev.mos.prom.profile.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import dev.mos.prom.navigation.MosPromBottomBar
import dev.mos.prom.navigation.MosPromTopBar
import dev.mos.prom.navigation.Route
import dev.mos.prom.profile.viewmodel.ProfileEvent
import dev.mos.prom.profile.viewmodel.ProfileViewModel
import dev.mos.prom.ui.text.MosPromErrorMessage
import dev.mos.prom.ui.text.MosPromLoadingBar
import dev.mos.prom.utils.MosPromResult
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun ProfileScreen (
    viewModel: ProfileViewModel,
    navController: NavController,
    innerPadding: PaddingValues,
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(ProfileEvent.OnLoadData)
    }

    Scaffold (
        bottomBar = {
            MosPromBottomBar(
                navController = navController
            )
        },
        topBar = {
            MosPromTopBar(
                title = state.userModel.name,
//                actions = {
//                    IconButton(
//                        onClick = {
//                            navController.navigate(Route)
//                        }
//                    ) {
//                        Icon(
//                            painter = painterResource(R.drawable.ic_history),
//                            contentDescription = "История",
//                            tint = MaterialTheme.colorScheme.surfaceContainer
//                        )
//                    }
//                }
            )
        },
        modifier = Modifier
            .fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.onSurface,
    ) { padding ->

        when (state.status) {
            MosPromResult.Error -> {
                MosPromErrorMessage(
                    modifier = Modifier
                        .padding(padding),
                    text = "Ошибка загрузки",
                    onUpdate = {
                        viewModel.onEvent(ProfileEvent.OnLoadData)
                    }
                )
            }
            MosPromResult.Loading -> {
                MosPromLoadingBar(
                    modifier = Modifier
                        .padding(padding)
                )
            }
            MosPromResult.Success -> {
                ProfileView(
                    modifier = Modifier
                        .padding(padding),
                    state = state,
                    innerPadding = innerPadding,
                )
            }
        }
    }

}
