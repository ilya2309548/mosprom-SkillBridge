package dev.mos.prom.presentation.post.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.mos.prom.R
import dev.mos.prom.data.api.CreatePostApiRequest
import dev.mos.prom.data.api.DirectionDto
import dev.mos.prom.data.api.PostService
import dev.mos.prom.data.api.TechnologyDto
import dev.mos.prom.data.api.ClubService
import dev.mos.prom.utils.navigation.MosPromTopBar
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.util.Calendar

private fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item
// Post types (should match backend enums)
private enum class UiPostType(val code: String, val label: String) {
    Informational("informational", "Информационный пост"),
    Project("project", "Проект"),
    Internship("internship", "Стажировка"),
    Educational("educational", "Образовательный"),
    Activity("activity", "Мероприятие"),
    Vacancy("vacancy", "Вакансия"),
}

private enum class UiPostFormat(val code: String, val label: String) {
    InPerson("in_person", "Очно"),
    Online("online", "Онлайн"),
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    navController: NavController,
    innerPadding: PaddingValues,
    clubId: Long,
    clubName: String? = null,
) {
    // TODO: Replace with real admin check (e.g., from Profile or ClubDetails state)
    val isAdmin = remember(clubId) { true }

    if (!isAdmin) {
        NotAllowedScreen(navController, innerPadding)
        return
    }

    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    var typeExpanded by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf(UiPostType.Informational) }

    var formatExpanded by remember { mutableStateOf(false) }
    var format by remember { mutableStateOf(UiPostFormat.InPerson) }

    var dateText by remember { mutableStateOf("") }
    val ctx = LocalContext.current
    val cal = remember { Calendar.getInstance() }
    val datePicker = remember {
        DatePickerDialog(
            ctx,
            { _, y, m, d ->
                dateText = String.format("%02d.%02d.%04d", d, m + 1, y)
            },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    val postService: PostService = koinInject()
    val clubService: ClubService = koinInject()
    val scope = rememberCoroutineScope()

    var isSubmitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }

    // Directions and Technologies state
    var directions by remember { mutableStateOf<List<DirectionDto>>(emptyList()) }
    var selectedDirectionId by remember { mutableStateOf<Long?>(null) }
    var technologies by remember { mutableStateOf<List<TechnologyDto>>(emptyList()) }
    var selectedTechIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

    LaunchedEffect(Unit) {
        directions = clubService.listDirections()
    }
    LaunchedEffect(selectedDirectionId) {
        val id = selectedDirectionId
        technologies = if (id != null) clubService.technologiesByDirection(id) else emptyList()
        // reset techs on direction change
        selectedTechIds = emptySet()
    }

    Scaffold(
        topBar = {
            MosPromTopBar(
                title = "Новый пост",
                navIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(painter = painterResource(R.drawable.ic_back), contentDescription = "Назад")
                    }
                }
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Заголовок поста", color = Color.Black) },
                placeholder = { Text("Например: воркшоп по пайке…", color = Color.Black) },
                textStyle = TextStyle(color = Color.Black),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text("Комментарий", color = Color.Black) },
                placeholder = { Text("Например: мастер-класс…", color = Color.Black) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = Color.Black),
                minLines = 3
            )

            // Post type dropdown
            ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                OutlinedTextField(
                    value = type.label,
                    onValueChange = {},
                    readOnly = true,
                    textStyle = TextStyle(color = Color.Black),
                    label = { Text("Тип поста", color = Color.Black) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                    UiPostType.entries.forEach { option ->
                        DropdownMenuItem(text = { Text(option.label, color = Color.Black) }, onClick = {
                            type = option
                            typeExpanded = false
                        })
                    }
                }
            }

            // Date picker
            OutlinedTextField(
                value = dateText,
                onValueChange = {},
                label = { Text("До какого числа актуально", color = Color.Black) },
                placeholder = { Text("Дата", color = Color.Black) },
                readOnly = true,
                textStyle = TextStyle(color = Color.Black),
                trailingIcon = {
                    TextButton(onClick = { datePicker.show() }) { Text("📅", color = Color.Black) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Age restriction
            OutlinedTextField(
                value = age,
                onValueChange = { if (it.all { ch -> ch.isDigit() }) age = it },
                label = { Text("Возрастное ограничение", color = Color.Black) },
                placeholder = { Text("0+", color = Color.Black) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = Color.Black),

                )

            // Format dropdown
            ExposedDropdownMenuBox(expanded = formatExpanded, onExpandedChange = { formatExpanded = it }) {
                OutlinedTextField(
                    value = format.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Мероприятие пройдёт в формате", color = Color.Black) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formatExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    textStyle = TextStyle(color = Color.Black),

                    )
                DropdownMenu(expanded = formatExpanded, onDismissRequest = { formatExpanded = false }) {
                    UiPostFormat.entries.forEach { option ->
                        DropdownMenuItem(text = { Text(option.label, color = Color.Black) }, onClick = {
                            format = option
                            formatExpanded = false
                        })
                    }
                }
            }

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                textStyle = TextStyle(color = Color.Black),

                label = { Text("Адрес", color = Color.Black) },
                placeholder = { Text("Введите адрес", color = Color.Black) },
                modifier = Modifier.fillMaxWidth()
            )

            // Directions
            if (directions.isNotEmpty()) {
                androidx.compose.material3.Text("Направления", color = Color.Black, style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp), verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                    directions.forEach { d ->
                        val id = d.id
                        val selected = id != null && id == selectedDirectionId
                        androidx.compose.material3.AssistChip(
                            onClick = { if (id != null) selectedDirectionId = id },
                            label = { Text(d.name, color = if (selected) Color.White else Color.Black) },
                            colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }

            // Technologies
            if (technologies.isNotEmpty()) {
                androidx.compose.material3.Text("Технологии", color = Color.Black, style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp), verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                    technologies.forEach { t ->
                        val tid = t.id
                        val selected = tid != null && tid in selectedTechIds
                        androidx.compose.material3.AssistChip(
                            onClick = { if (tid != null) selectedTechIds = selectedTechIds.toggle(tid) },
                            label = { Text(t.name, color = if (selected) Color.White else Color.Black) },
                            colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (submitError != null) {
                Text(submitError!!, color = Color(0xFFB00020))
            }

            Button(
                enabled = !isSubmitting && title.isNotBlank(),
                onClick = {
                    isSubmitting = true
                    submitError = null
                    scope.launch {
                        try {
                            val isoEnd = dateText.takeIf { it.isNotBlank() }?.let { d ->
                                // Backend expects RFC3339: 2006-01-02T15:04:05Z07:00
                                // Parse dd.MM.yyyy and emit yyyy-MM-ddT00:00:00Z
                                val parts = d.split('.')
                                if (parts.size == 3) {
                                    val day = parts[0].toIntOrNull()
                                    val month = parts[1].toIntOrNull()
                                    val year = parts[2].toIntOrNull()
                                    if (day != null && month != null && year != null) {
                                        val ld = java.time.LocalDate.of(year, month, day)
                                        java.time.OffsetDateTime.of(ld, java.time.LocalTime.MIDNIGHT, java.time.ZoneOffset.UTC)
                                            .format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                                    } else null
                                } else null
                            }
                            val technologiesNames = technologies.filter { it.id != null && it.id in selectedTechIds }.map { it.name }
                            val req = CreatePostApiRequest(
                                title = title.trim(),
                                description = desc.trim(),
                                type = type.code,
                                endDate = isoEnd,
                                ageRestriction = age.toIntOrNull(),
                                format = format.code,
                                address = address.trim(),
                                clubId = clubId,
                                technologies = technologiesNames,
                            )
                            val created = postService.createPost(req)
                            // Navigate back to club page
                            navController.popBackStack()
                        } catch (t: Throwable) {
                            submitError = t.message ?: t.toString()
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isSubmitting) "Создание…" else "Создать", color = Color.Black)
            }
        }
    }
}

@Composable
private fun NotAllowedScreen(navController: NavController, innerPadding: PaddingValues) {
    Scaffold(
        topBar = {
            MosPromTopBar(
                title = "Нет доступа",
                navIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(painter = painterResource(R.drawable.ic_back), contentDescription = "Назад")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.onSurface
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.White),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Экран доступен только администраторам клуба")
        }
    }
}
