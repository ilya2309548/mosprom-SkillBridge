package dev.mos.prom.presentation.club.ui

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import dev.mos.prom.R
import dev.mos.prom.utils.placeholderPainter
import dev.mos.prom.presentation.club.viewmodel.ClubCreateEvent
import dev.mos.prom.presentation.club.viewmodel.ClubCreateViewModel
import dev.mos.prom.presentation.ui.components.MosTextField
import dev.mos.prom.presentation.ui.text.MosPromErrorMessage
import dev.mos.prom.presentation.ui.text.MosPromLoadingBar
import dev.mos.prom.utils.MosPromResult
import dev.mos.prom.utils.navigation.MosPromTopBar
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubCreateScreen(
    navController: NavController,
    innerPadding: PaddingValues,
    viewModel: ClubCreateViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDirectionsSheet by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()

    LaunchedEffect(Unit) { viewModel.onEvent(ClubCreateEvent.OnLoad) }

    when (state.status) {
        MosPromResult.Loading -> MosPromLoadingBar(modifier = Modifier.padding(innerPadding))
        MosPromResult.Error -> MosPromErrorMessage(modifier = Modifier.padding(innerPadding), text = state.error, onUpdate = { viewModel.onEvent(ClubCreateEvent.OnLoad) })
        MosPromResult.Success -> {
            if (state.created) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
            Scaffold(
                topBar = {
                    MosPromTopBar(
                        title = "Новый клуб",
                        navIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(painter = painterResource(R.drawable.ic_back), contentDescription = "Назад")
                            }
                        }
                    )
                },
                containerColor = MaterialTheme.colorScheme.onSurface
            ) { padding ->
                val ctx = androidx.compose.ui.platform.LocalContext.current
                val pickLogo = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                    if (uri != null) {
                        val mime = ctx.contentResolver.getType(uri) ?: "image/jpeg"
                        val name = uri.lastPathSegment?.substringAfterLast('/') ?: "logo.jpg"
                        val bytes = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        if (bytes != null) viewModel.onEvent(ClubCreateEvent.SetLogo(name, bytes, mime))
                    }
                }

        Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .background(Color.White)
            .verticalScroll(scroll)
            .padding(horizontal = 16.dp)
                ) {
                    // Title & Logo row
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { pickLogo.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            val bytes = state.logoBytes
                            if (bytes != null) {
                                val bmp = remember(bytes) { BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }
                                if (bmp != null) {
                                    androidx.compose.foundation.Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            } else {
                                    androidx.compose.foundation.Image(
                                        painter = placeholderPainter(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize()
                                    )
                            }
                        }
                        Spacer(Modifier.width(12.dp))

                        Column(Modifier.weight(1f)) {
                            Text("Название клуба", color = Color.Black, fontWeight = FontWeight.SemiBold, style = androidx.compose.material3.MaterialTheme.typography.titleSmall)
                            MosTextField(label = "", value = state.name, onValueChange = { viewModel.onEvent(ClubCreateEvent.NameChanged(it)) })
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Text("Описание клуба", color = Color.Black, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    MosTextField(
                        label = "",
                        value = state.description,
                        onValueChange = { viewModel.onEvent(ClubCreateEvent.DescriptionChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showDirectionsSheet = true }
                    ) {
                        Text("Выбрать направления", style = MaterialTheme.typography.labelLarge)
                    }

                    if (
                        state.selectedDirections.isNotEmpty()
                    ) {
                        Spacer(Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            state.selectedDirections.forEach { dir ->
                                androidx.compose.material3.AssistChip(
                                    onClick = { viewModel.onEvent(ClubCreateEvent.ToggleDirection(dir)) },
                                    label = { Text(dir, color = Color.Black, style = MaterialTheme.typography.labelSmall) },
                                    colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    ),
                                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.25f))
                                )
                            }
                        }
                    }

                    if (
                        state.selectedDirections.isNotEmpty()
                    ) {

                        Spacer(Modifier.height(16.dp))

                        if (state.existingClubs.isEmpty()) {
                            Text("Вы будете первый, кто откроет клуб по этому направлению", color = Color.Black, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                        } else {
                            state.existingClubs.forEach { c ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(12.dp)
                                ) {

                                    AsyncImage(
                                        model = c.logoUrl,
                                        contentDescription = null,
                                        placeholder = placeholderPainter(),
                                        error = placeholderPainter(),
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .size(44.dp)
                                    )

                                    Spacer(Modifier.width(12.dp))

                                    Column(Modifier.weight(1f)) {
                                        Text(c.name, fontWeight = FontWeight.SemiBold, color = Color.Black, style = androidx.compose.material3.MaterialTheme.typography.titleSmall)
                                        Text(c.description, color = Color.Black.copy(alpha = 0.7f), style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { viewModel.onEvent(ClubCreateEvent.Submit) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("Создать", style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
                    }
                    if (showDirectionsSheet) {
                        ModalBottomSheet(onDismissRequest = { showDirectionsSheet = false }) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Выберите направления", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = Color.Black)
                                Spacer(Modifier.height(12.dp))
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    state.directions.forEach { dir ->
                                        val selected = dir in state.selectedDirections
                                        androidx.compose.material3.AssistChip(
                                            onClick = { viewModel.onEvent(ClubCreateEvent.ToggleDirection(dir)) },
                                            label = { Text(dir, color = if (selected) Color.White else Color.Black, style = androidx.compose.material3.MaterialTheme.typography.labelSmall) },
                                            colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                                                containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            )
                                        )
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                OutlinedButton(onClick = { showDirectionsSheet = false }, modifier = Modifier.fillMaxWidth()) { Text("Готово", style = MaterialTheme.typography.labelLarge) }
                            }
                        }
                    }
                }
            }
        }
    }
}
