package dev.mos.prom.presentation.club.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mos.prom.data.repo.ClubRepository
import dev.mos.prom.utils.MosPromResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClubCreateState(
    val name: String = "",
    val description: String = "",
    val directions: List<String> = emptyList(), // all
    val selectedDirections: Set<String> = emptySet(),
    val existingClubs: List<ClubItem> = emptyList(),
    val logoBytes: ByteArray? = null,
    val logoFilename: String? = null,
    val logoMime: String? = null,
    val status: MosPromResult = MosPromResult.Loading,
    val error: String? = null,
    val created: Boolean = false,
)

data class ClubItem(
    val id: Long,
    val name: String,
    val description: String,
    val logoUrl: String?,
)

sealed interface ClubCreateEvent {
    data object OnLoad : ClubCreateEvent
    data class NameChanged(val v: String): ClubCreateEvent
    data class DescriptionChanged(val v: String): ClubCreateEvent
    data class ToggleDirection(val name: String): ClubCreateEvent
    data class SetLogo(val filename: String, val bytes: ByteArray, val mime: String): ClubCreateEvent
    data object Submit: ClubCreateEvent
}

class ClubCreateViewModel(private val repo: ClubRepository): ViewModel() {
    private val _state = MutableStateFlow(ClubCreateState())
    val state = _state.asStateFlow()

    fun onEvent(e: ClubCreateEvent) {
        when (e) {
            ClubCreateEvent.OnLoad -> load()
            is ClubCreateEvent.NameChanged -> _state.update { it.copy(name = e.v) }
            is ClubCreateEvent.DescriptionChanged -> _state.update { it.copy(description = e.v) }
            is ClubCreateEvent.ToggleDirection -> toggleDir(e.name)
            is ClubCreateEvent.SetLogo -> _state.update { it.copy(logoFilename = e.filename, logoBytes = e.bytes, logoMime = e.mime) }
            ClubCreateEvent.Submit -> submit()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(status = MosPromResult.Loading, error = null) }
            try {
                val dirs = repo.directions()
                _state.update { it.copy(status = MosPromResult.Success, directions = dirs) }
            } catch (t: Throwable) {
                _state.update { it.copy(status = MosPromResult.Error, error = t.message) }
            }
        }
    }

    private fun toggleDir(name: String) {
        viewModelScope.launch {
            val newSel = _state.value.selectedDirections.toMutableSet().apply {
                if (contains(name)) remove(name) else add(name)
            }
            _state.update { it.copy(selectedDirections = newSel) }
            try {
                val clubs = repo.clubsByDirections(newSel.toList())
                _state.update { it.copy(existingClubs = clubs.map { c -> ClubItem(c.id, c.name, c.description, c.logoUrl) }) }
            } catch (t: Throwable) {
                _state.update { it.copy(error = t.message) }
            }
        }
    }

    private fun submit() {
        val s = _state.value
        if (s.name.isBlank()) {
            _state.update { it.copy(error = "Введите название клуба") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(status = MosPromResult.Loading, error = null) }
            try {
                repo.createClub(
                    name = s.name,
                    description = s.description,
                    directions = s.selectedDirections.toList(),
                    logo = s.logoBytes,
                    logoFilename = s.logoFilename,
                    logoMime = s.logoMime,
                )
                _state.update { it.copy(status = MosPromResult.Success, created = true) }
            } catch (t: Throwable) {
                _state.update { it.copy(status = MosPromResult.Error, error = t.message) }
            }
        }
    }
}
