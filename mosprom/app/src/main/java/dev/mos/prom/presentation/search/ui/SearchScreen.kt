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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
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
import dev.mos.prom.utils.placeholderPainter
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    innerPadding: PaddingValues,
    viewModel: SearchViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDirectionsSheet by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()

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
                        .verticalScroll(scroll)
                        .padding(horizontal = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            MosTextField(
                                label = "Поиск",
                                value = state.query,
                                onValueChange = { viewModel.onEvent(SearchEvent.QueryChanged(it)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { viewModel.onEvent(SearchEvent.DoSearch) }) { Text("Искать") }
                    }

                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { showDirectionsSheet = true }) {
                        Text("Направления")
                    }
                    if (state.selectedDirections.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.selectedDirections.forEach { dir ->
                                androidx.compose.material3.AssistChip(
                                    onClick = { viewModel.onEvent(SearchEvent.ToggleDirection(dir)) },
                                    label = { Text(dir, color = Color.Black) },
                                    colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    ),
                                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.25f))
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    state.visibleClubs.forEach { club ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clickable { navController.navigate(Route.Club(id = club.id, name = club.name)) }
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
                                    placeholder = placeholderPainter(),
                                    error = placeholderPainter(),
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
                                                label = { Text(d, color = Color.Black) },
                                                enabled = false,
                                                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.25f))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (showDirectionsSheet) {
                        ModalBottomSheet(onDismissRequest = { showDirectionsSheet = false }) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Выберите направления", style = MaterialTheme.typography.titleMedium, color = Color.Black)
                                Spacer(Modifier.height(12.dp))
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
                                OutlinedButton(onClick = { showDirectionsSheet = false }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Готово")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
