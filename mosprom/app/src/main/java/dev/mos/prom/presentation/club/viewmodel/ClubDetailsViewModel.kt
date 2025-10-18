package dev.mos.prom.presentation.club.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mos.prom.data.repo.ClubRepository
import dev.mos.prom.data.repo.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClubDetailsState(
    val membersCount: Int = 0,
    val isSubscribing: Boolean = false,
    val subscribed: Boolean = false,
    val error: String? = null,
)

sealed interface ClubDetailsEvent {
    data class Load(val clubId: Long) : ClubDetailsEvent
    data class Subscribe(val clubId: Long) : ClubDetailsEvent
}

class ClubDetailsViewModel(
    private val repo: ClubRepository,
    private val profile: ProfileRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ClubDetailsState())
    val state = _state.asStateFlow()

    fun onEvent(e: ClubDetailsEvent) {
        when (e) {
            is ClubDetailsEvent.Load -> load(e.clubId)
            is ClubDetailsEvent.Subscribe -> subscribe(e.clubId)
        }
    }

    private fun load(id: Long) {
        viewModelScope.launch {
            try {
                val count = repo.subscribersCount(id)
                val myIds = profile.myClubIds()
                val isSubscribed = id in myIds
                _state.update {
                    it.copy(membersCount = count, subscribed = isSubscribed, error = null)
                }
            } catch (t: Throwable) {
                _state.update { it.copy(error = t.message) }
            }
        }
    }

    private fun subscribe(id: Long) {
        viewModelScope.launch {

            _state.update { it.copy(isSubscribing = true, error = null) }

            try {
                if (_state.value.subscribed) {
                    // Already subscribed; no-op unless we add unsubscribe flow
                    _state.update { it.copy(isSubscribing = false) }
                    return@launch
                }
                repo.subscribeToClub(id)

                val newCount = repo.subscribersCount(id)

                _state.update { it.copy(isSubscribing = false, subscribed = true, membersCount = newCount) }
            } catch (t: Throwable) {
                _state.update { it.copy(isSubscribing = false, error = t.message) }
            }
        }
    }
}
