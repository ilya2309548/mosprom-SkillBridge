package dev.mos.prom.presentation.club.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import dev.mos.prom.R
import dev.mos.prom.utils.navigation.MosPromTopBar
import dev.mos.prom.utils.placeholderPainter

@Composable
fun ClubDetailsScreen(
    navController: NavController,
    innerPadding: PaddingValues,
    id: Long,
    name: String? = null,
    logoUrl: String? = null,
    description: String? = null,
    directions: List<String> = emptyList(),
) {
    var tabIndex by remember { mutableIntStateOf(0) }
    val scroll = rememberScrollState()

    val clubName = name ?: "Клуб #$id"
    val uiLogo = logoUrl
    val uiDescription = description ?: "Описание клуба. Здесь краткая информация о деятельности, целях и задачах клуба."
    val uiDirections = directions

    Scaffold(
        topBar = {
            MosPromTopBar(
                title = clubName,
                navIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(painter = painterResource(R.drawable.ic_back), contentDescription = "Назад")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.onSurface,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
                    .navigationBarsPadding(),
            ) {
                Button(onClick = { /* TODO subscribe */ },
                    modifier = Modifier.align(Alignment.Center)) {
                    Text("Подписаться")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(scroll)
                .padding(horizontal = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
                if (!uiLogo.isNullOrEmpty()) {
                    AsyncImage(
                        model = uiLogo,
                        contentDescription = null,
                        placeholder = placeholderPainter(),
                        error = placeholderPainter(),
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Image(
                        painter = placeholderPainter(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                    )
                }
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(clubName, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.Black)
                    Spacer(Modifier.height(6.dp))
                    if (uiDirections.isNotEmpty()) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiDirections.forEach { d ->
                                androidx.compose.material3.AssistChip(
                                    onClick = {},
                                    label = { Text(d, color = Color.Black) },
                                    colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.25f))
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Описание", style = MaterialTheme.typography.titleMedium, color = Color.Black)
            Text(uiDescription, style = MaterialTheme.typography.bodyMedium, color = Color.Black, modifier = Modifier.padding(top = 6.dp))

            Spacer(Modifier.height(16.dp))

            TabRow(
                modifier = Modifier
                    .fillMaxWidth(),
                selectedTabIndex = tabIndex,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Tab(
                    selected = tabIndex == 0,
                    onClick = { tabIndex = 0 },
                    text = { Text("Посты", style = MaterialTheme.typography.labelLarge)
                })
                Tab(
                    selected = tabIndex == 1,
                    onClick = { tabIndex = 1 },
                    text = { Text("События", style = MaterialTheme.typography.labelLarge)
                })
            }

            Spacer(Modifier.height(12.dp))

            when (tabIndex) {
                0 -> {
                    Column (
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_no_posts),
                            contentDescription = null,
                            modifier = Modifier
                                .width(140.dp)
                                .padding(vertical = 24.dp),
                            contentScale = ContentScale.FillWidth
                        )
                    }

                }
                1 -> {
                    Column (
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_no_posts),
                            contentDescription = null,
                            modifier = Modifier
                                .width(140.dp)
                                .padding(vertical = 24.dp),
                            contentScale = ContentScale.FillWidth
                        )
                    }
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}