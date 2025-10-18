package dev.mos.prom.presentation.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mos.prom.data.repo.ProfileRepository
import dev.mos.prom.data.storage.TokenStorage
import dev.mos.prom.utils.MosPromResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repo: ProfileRepository,
    private val tokens: TokenStorage,
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.OnLoadData -> loadData()
            is ProfileEvent.OnUpdateProfile -> update(event)
            is ProfileEvent.OnUploadAvatar -> upload(event)
            ProfileEvent.OnLogout -> logout()
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
}
