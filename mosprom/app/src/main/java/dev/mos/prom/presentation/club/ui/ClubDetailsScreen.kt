package dev.mos.prom.presentation.club.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import dev.mos.prom.R
import dev.mos.prom.data.api.PostDto
import dev.mos.prom.data.api.PostService
import dev.mos.prom.data.api.UserDto
import dev.mos.prom.presentation.club.viewmodel.ClubDetailsEvent
import dev.mos.prom.presentation.club.viewmodel.ClubDetailsViewModel
import dev.mos.prom.presentation.ui.text.MosPromLoadingBar
import dev.mos.prom.utils.navigation.MosPromTopBar
import dev.mos.prom.utils.navigation.Route
import dev.mos.prom.utils.placeholderPainter
import org.koin.compose.koinInject
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ClubDetailsScreen(
    navController: NavController,
    innerPadding: PaddingValues,
    viewModel: ClubDetailsViewModel = koinViewModel(),
    id: Long,
    name: String? = null,
    logoUrl: String? = null,
    description: String? = null,
    directions: List<String> = emptyList(),
    isCreator: Boolean = false,
) {
    var tabIndex by remember { mutableIntStateOf(0) }
    val scroll = rememberScrollState()

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(id) {
        viewModel.onEvent(ClubDetailsEvent.Load(id))
    }

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
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Button(
                        onClick = {
                            navController.navigate(Route.ClubChat(id = id, name = clubName))
                        },
                        modifier = Modifier
                    ) {
                        Text("Чат", style = MaterialTheme.typography.labelLarge)
                    }

                    Spacer(Modifier.width(12.dp))

                    if (!isCreator && !state.subscribed) {
                        Button(
                            onClick = { viewModel.onEvent(ClubDetailsEvent.Subscribe(id)) },
                            enabled = !state.isSubscribing ,
                            modifier = Modifier
                        ) {
                            Text(
                                text = "Подписаться",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }


                    Spacer(Modifier.width(12.dp))

                    if (isCreator) {
                        Button(
                            onClick = { navController.navigate(dev.mos.prom.utils.navigation.Route.CreatePost(clubId = id, clubName = clubName)) },
                        ) {
                            Text("Добавить пост", style = MaterialTheme.typography.labelLarge)
                        }
                    }
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 12.dp)
            ) {
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

            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Участников: ${state.membersCount}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                )
                Spacer(Modifier.width(16.dp))

            }

            Spacer(Modifier.height(16.dp))

            Text("Описание", style = MaterialTheme.typography.titleMedium, color = Color.Black)

            Text(uiDescription, style = MaterialTheme.typography.bodyMedium, color = Color.Black, modifier = Modifier.padding(top = 6.dp))

            Spacer(Modifier.height(16.dp))

            val postTypes = listOf(
                "informational" to "Информационные",
                "project" to "Проекты",
                "internship" to "Стажировки",
                "educational" to "Образовательные",
                "activity" to "Мероприятия",
                "vacancy" to "Вакансии",
            )

            val pagerState = rememberPagerState(pageCount = { postTypes.size }, initialPage = tabIndex)

            ScrollableTabRow(
                modifier = Modifier
                    .fillMaxWidth(),
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                edgePadding = 8.dp
            ) {
                postTypes.forEachIndexed { index, (_, label) ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { tabIndex = index },
                        text = { Text(label, style = MaterialTheme.typography.labelLarge) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Sync tabIndex with pager
            LaunchedEffect(tabIndex) { if (pagerState.currentPage != tabIndex) pagerState.scrollToPage(tabIndex) }
            LaunchedEffect(pagerState.currentPage) { if (tabIndex != pagerState.currentPage) tabIndex = pagerState.currentPage }

            // Fetch all posts once for the club
            val postService: PostService = koinInject()
            var allPosts by remember { mutableStateOf<List<PostDto>>(emptyList()) }
            var loading by remember { mutableStateOf(false) }
            var error by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(id) {
                loading = true
                error = null
                try {
                    allPosts = postService.getPostsByClub(id)
                } catch (t: Throwable) {
                    error = t.message
                    allPosts = emptyList()
                } finally {
                    loading = false
                }
            }

            HorizontalPager(state = pagerState) { page ->
                val type = postTypes.getOrNull(page)?.first
                val filtered = remember(allPosts, type) { allPosts.filter { p -> type == null || p.type.equals(type, ignoreCase = true) } }

                if (loading) {
                    MosPromLoadingBar(
                        modifier = Modifier
                            .fillMaxWidth(),
                        containerColor = Color.White
                    )
                } else if (error != null) {
                    Text(error!!, color = Color(0xFFB00020))
                } else if (filtered.isEmpty()) {
                    Text("Нет постов", color = Color.Black, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        filtered.forEach { p: PostDto -> PostCard(p = p, isCreator = isCreator, currentClubId = id) }
                    }
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostCard(p: PostDto, isCreator: Boolean, currentClubId: Long) {
    // Hoisted state/services so they are available for menu and bottom sheet
    var menuExpanded by remember { mutableStateOf(false) }
    var showParticipants by remember { mutableStateOf(false) }
    var participants by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val postService: PostService = koinInject()
    val profileService: dev.mos.prom.data.api.ProfileService = koinInject()
    val profileRepo: dev.mos.prom.data.repo.ProfileRepository = koinInject()
    var joinLoading by remember { mutableStateOf(false) }
    var joined by remember { mutableStateOf(false) }
    androidx.compose.material3.Card(
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(p.title, color = Color.Black, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (isCreator && p.clubId == currentClubId) {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Ещё")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(text = { Text("Участники") }, onClick = {
                            menuExpanded = false
                            showParticipants = true
                            scope.launch {
                                loading = true
                                errorText = null
                                try {
                                    participants = postService.getParticipants(p.id)
                                } catch (t: Throwable) {
                                    errorText = t.message
                                    participants = emptyList()
                                } finally {
                                    loading = false
                                }
                            }
                        })
                    }
                }
            }
            
            if (!p.description.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(p.description!!, color = Color.Black, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(6.dp))
            // Type and Format
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.AssistChip(
                    onClick = {}, enabled = false,
                    label = { Text(p.type, color = Color.Black, style = MaterialTheme.typography.labelSmall) },
                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.25f))
                )
                p.format?.let {
                    androidx.compose.material3.AssistChip(
                        onClick = {}, enabled = false,
                        label = { Text(if (it == "in_person") "Очно" else "Онлайн", color = Color.Black, style = MaterialTheme.typography.labelSmall) },
                        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.25f))
                    )
                }
            }

            // Image (if provided as URL/path)
            p.image?.takeIf { it.isNotBlank() }?.let { img ->
                Spacer(Modifier.height(8.dp))
                AsyncImage(
                    model = img,
                    contentDescription = null,
                    placeholder = placeholderPainter(),
                    error = placeholderPainter(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
            }

            // Technologies
            if (p.technologies.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    p.technologies.forEach { t ->
                        androidx.compose.material3.AssistChip(
                            onClick = {}, enabled = false,
                            label = { Text(t.name, color = Color.Black, style = MaterialTheme.typography.labelSmall) },
                            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.25f))
                        )
                    }
                }
            }

            // Address
            p.address?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(8.dp))
                Text("Адрес: $it", color = Color.Black, style = MaterialTheme.typography.bodySmall)
            }

            // Join button for non-owners
            if (!isCreator && p.clubId == currentClubId && !joined) {
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    scope.launch {
                        try {
                            joinLoading = true
                            val myId = profileRepo.myId()
                            postService.joinPost(postId = p.id, userId = myId)
                            joined = true
                        } catch (t: Throwable) {
                            errorText = t.message
                        } finally {
                            joinLoading = false
                        }
                    }
                }, enabled = !joinLoading) {
                    Text(if (joinLoading) "Присоединяем..." else "Присоединиться")
                }
            }
        }
    }

    // Participants bottom sheet and achievement dialog
    var selectedUser by remember { mutableStateOf<dev.mos.prom.data.api.UserDto?>(null) }
    var achText by remember { mutableStateOf("") }

    if (showParticipants && isCreator && p.clubId == currentClubId) {
        ModalBottomSheet(onDismissRequest = { showParticipants = false }) {
            Column(Modifier.padding(16.dp)) {
                Text("Участники", color = Color.Black, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                if (loading) {
                    MosPromLoadingBar(modifier = Modifier.fillMaxWidth(), containerColor = Color.White)
                } else if (errorText != null) {
                    Text(errorText!!, color = Color(0xFFB00020))
                } else if (participants.isEmpty()) {
                    Text("Пока нет участников", color = Color.Black)
                } else {
                    participants.forEach { user ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            val displayName = user.name?.takeIf { it.isNotBlank() }
                                ?: user.telegramName.ifBlank { "ID: ${user.id}" }
                            Text(
                                text = displayName,
                                color = Color.Black,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )

                            if (isCreator) {
                                Button(onClick = {
                                    selectedUser = user
                                }) { Text("Ачивка") }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedUser != null && isCreator && p.clubId == currentClubId) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { selectedUser = null },
            confirmButton = {
                Button(onClick = {
                    val uid = selectedUser!!.id
                    val text = achText.trim()
                    if (text.isNotEmpty()) {
                        // fire and forget; in real app add error handling/Snackbar
                        scope.launch {
                            profileService.addAchievement(uid, text)
                            selectedUser = null
                            achText = ""
                        }
                    }
                }) { Text("Добавить") }
            },
            dismissButton = { Button(onClick = { selectedUser = null }) { Text("Отмена") } },
            title = { Text("Новая ачивка", color = Color.Black) },
            text = {
                OutlinedTextField(
                    value = achText,
                    onValueChange = { achText = it },
                    label = {
                        Text("Текст ачивки",
                            color = Color.Black)
                    },
                    textStyle = TextStyle(
                        color = Color.Black
                    )
                )
            }
        )
    }
}