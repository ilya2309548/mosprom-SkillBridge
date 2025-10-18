package dev.mos.prom.presentation.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mos.prom.data.repo.Club
import dev.mos.prom.data.repo.ClubRepository
import dev.mos.prom.utils.MosPromResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(private val repo: ClubRepository) : ViewModel() {
    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state

    private var loadJob: Job? = null
    private var queryJob: Job? = null

    fun onEvent(event: SearchEvent) {
        when (event) {
            SearchEvent.OnLoad -> initialLoad()
            is SearchEvent.QueryChanged -> applyQuery(event.value)
            is SearchEvent.ToggleDirection -> toggleDirection(event.name)
            SearchEvent.DoSearch -> doSearch()
            SearchEvent.Retry -> initialLoad()
        }
    }

    private fun initialLoad() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(status = MosPromResult.Loading, error = "") }
            try {
                val dirs = repo.directions()
                val clubs = repo.searchClubs(name = null, directions = emptyList())
                _state.update { s ->
                    s.copy(
                        status = MosPromResult.Success,
                        directions = dirs,
                        clubs = clubs,
                        visibleClubs = filter(clubs, s.query)
                    )
                }
            } catch (t: Throwable) {
                _state.update { it.copy(status = MosPromResult.Error, error = t.message ?: "Ошибка загрузки") }
            }
        }
    }

    private fun applyQuery(value: String) {
        _state.update { it.copy(query = value) }
    }

    private fun doSearch() {
        queryJob?.cancel()
        queryJob = viewModelScope.launch {
            try {
                _state.update { it.copy(status = MosPromResult.Loading, error = "") }
                val dirs = _state.value.selectedDirections.toList()
                val q = _state.value.query
                val clubs = repo.searchClubs(name = q, directions = dirs)
                _state.update { s ->
                    s.copy(
                        status = MosPromResult.Success,
                        clubs = clubs,
                        visibleClubs = filter(clubs, q)
                    )
                }
            } catch (t: Throwable) {
                _state.update { it.copy(status = MosPromResult.Error, error = t.message ?: "Ошибка загрузки") }
            }
        }
    }

    private fun toggleDirection(name: String) {
        val now = _state.value
        val newSet = now.selectedDirections.toMutableSet().also { set ->
            if (!set.add(name)) set.remove(name)
        }
        _state.update { it.copy(selectedDirections = newSet) }

        // Reload from backend using selected directions, then apply current query locally
        viewModelScope.launch {
            try {
                _state.update { it.copy(status = MosPromResult.Loading, error = "") }
                val clubs = repo.searchClubs(name = _state.value.query, directions = newSet.toList())
                _state.update { s ->
                    s.copy(
                        status = MosPromResult.Success,
                        clubs = clubs,
                        visibleClubs = filter(clubs, s.query)
                    )
                }
            } catch (t: Throwable) {
                _state.update { it.copy(status = MosPromResult.Error, error = t.message ?: "Ошибка загрузки") }
            }
        }
    }

    private fun filter(clubs: List<Club>, query: String): List<Club> {
        if (query.isBlank()) return clubs
        val q = query.trim().lowercase()
        return clubs.filter { it.name.lowercase().contains(q) }
    }
}
