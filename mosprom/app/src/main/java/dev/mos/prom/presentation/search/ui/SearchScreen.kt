package dev.mos.prom.presentation.search.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import dev.mos.prom.presentation.search.viewmodel.SearchEvent
import dev.mos.prom.presentation.search.viewmodel.SearchViewModel
import dev.mos.prom.presentation.ui.components.MosTextField
import dev.mos.prom.presentation.ui.text.MosPromErrorMessage
import dev.mos.prom.presentation.ui.text.MosPromLoadingBar
import dev.mos.prom.utils.MosPromResult
import dev.mos.prom.utils.navigation.MosPromBottomBar
import dev.mos.prom.utils.navigation.MosPromTopBar
import dev.mos.prom.utils.navigation.Route
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchScreen(
    navController: NavController,
    innerPadding: PaddingValues,
    viewModel: SearchViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.onEvent(SearchEvent.OnLoad) }

    when (state.status) {
        MosPromResult.Loading -> MosPromLoadingBar(modifier = Modifier.padding(innerPadding))
        MosPromResult.Error -> MosPromErrorMessage(
            modifier = Modifier.padding(innerPadding),
            text = state.error,
            onUpdate = { viewModel.onEvent(SearchEvent.Retry) }
        )
        MosPromResult.Success -> {
            Scaffold(
                topBar = { MosPromTopBar(title = "Поиск") },
                bottomBar = { MosPromBottomBar(navController) },
                containerColor = MaterialTheme.colorScheme.onSurface
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(horizontal = 16.dp)
                ) {
                    MosTextField(
                        label = "Поиск",
                        value = state.query,
                        onValueChange = { viewModel.onEvent(SearchEvent.QueryChanged(it)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))
                    Text("Направления", color = Color.Black, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.directions.forEach { dir ->
                            val selected = dir in state.selectedDirections
                            androidx.compose.material3.AssistChip(
                                onClick = { viewModel.onEvent(SearchEvent.ToggleDirection(dir)) },
                                label = { Text(dir, color = if (selected) Color.White else Color.Black) },
                                colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                                    containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    state.visibleClubs.forEach { club ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clickable { navController.navigate("club/${'$'}{club.id}") }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                AsyncImage(
                                    model = club.logoUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .size(44.dp)
                                )

                                Spacer(Modifier.width(12.dp))

                                Column(Modifier.weight(1f)) {
                                    Text(club.name, color = Color.Black, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(6.dp))
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        club.directions.forEach { d ->
                                            androidx.compose.material3.AssistChip(
                                                onClick = {},
                                                label = { Text(d) },
                                                enabled = false
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
