package dev.mos.prom.presentation.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mos.prom.data.repo.ProfileRepository
import dev.mos.prom.data.repo.ClubRepository
import dev.mos.prom.data.storage.TokenStorage
import dev.mos.prom.utils.MosPromResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repo: ProfileRepository,
    private val tokens: TokenStorage,
    private val clubs: ClubRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.OnLoadData -> loadData()
            is ProfileEvent.OnUpdateProfile -> update(event)
            is ProfileEvent.OnUploadAvatar -> upload(event)
            ProfileEvent.OnLogout -> logout()
            ProfileEvent.LoadDirections -> loadDirections()
            is ProfileEvent.SelectDirection -> selectDirection(event.id)
            is ProfileEvent.ToggleTechnology -> toggleTechnology(event.id)
            ProfileEvent.SaveTechnologies -> saveTechnologies()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(status = MosPromResult.Loading, error = null) }
            try {
                val me = repo.me()
                val clubs = repo.myClubsCount()
                _state.update { it.copy(userModel = me, clubsCount = clubs, status = MosPromResult.Success) }
            } catch (t: Throwable) {
                _state.update { it.copy(status = MosPromResult.Error, error = t.message) }
            }
        }
    }

    private fun update(e: ProfileEvent.OnUpdateProfile) {
        viewModelScope.launch {
            _state.update { it.copy(status = MosPromResult.Loading, error = null) }
            try {
                val updated = repo.update(e.name, e.telegram, e.description, e.university, e.directions)
                _state.update { it.copy(userModel = updated, status = MosPromResult.Success) }
            } catch (t: Throwable) {
                _state.update { it.copy(status = MosPromResult.Error, error = t.message) }
            }
        }
    }

    private fun upload(e: ProfileEvent.OnUploadAvatar) {
        viewModelScope.launch {
            _state.update { it.copy(status = MosPromResult.Loading, error = null) }
            try {
                val updated = repo.uploadPhoto(e.filename, e.bytes, e.mime)
                _state.update { it.copy(userModel = updated, status = MosPromResult.Success) }
            } catch (t: Throwable) {
                _state.update { it.copy(status = MosPromResult.Error, error = t.message) }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            try {
                tokens.clear()
                _state.update { it.copy(loggedOut = true) }
            } catch (t: Throwable) {
                _state.update { it.copy(status = MosPromResult.Error, error = t.message) }
            }
        }
    }

    private fun loadDirections() {
        viewModelScope.launch {
            try {
                val dirs = clubs.directionsFull()
                _state.update { it.copy(directions = dirs.mapNotNull { d -> d.id?.let { DirectionItem(id = it, name = d.name) } }) }
            } catch (t: Throwable) {
                _state.update { it.copy(error = t.message) }
            }
        }
    }

    private fun selectDirection(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(selectedDirectionId = id, technologies = emptyList()) }
            try {
                val techs = clubs.technologiesByDirection(directionId = id)
                _state.update { it.copy(technologies = techs.mapNotNull { t -> t.id?.let { TechnologyItem(id = it, name = t.name) } }) }
            } catch (t: Throwable) {
                _state.update { it.copy(error = t.message) }
            }
        }
    }

    private fun toggleTechnology(id: Long) {
        val set = _state.value.selectedTechnologyIds.toMutableSet()
        if (!set.add(id)) set.remove(id)
        _state.update { it.copy(selectedTechnologyIds = set) }
    }

    private fun saveTechnologies() {
        viewModelScope.launch {
            try {
                // Backend expects technology names and explicit user_id
                val userId = repo.myId()
                val selectedIds = _state.value.selectedTechnologyIds
                val nameById = _state.value.technologies.associate { it.id to it.name }
                val names = selectedIds.mapNotNull { nameById[it] }
                repo.setTechnologies(userId, names)
            } catch (t: Throwable) {
                _state.update { it.copy(error = t.message) }
            }
        }
    }
}
