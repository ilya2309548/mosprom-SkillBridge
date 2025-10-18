package dev.mos.prom.presentation.search.viewmodel

import dev.mos.prom.data.model.Club
import dev.mos.prom.utils.MosPromResult

data class SearchState(
    val status: MosPromResult = MosPromResult.Loading,
    val error: String = "",
    val query: String = "",
    val directions: List<String> = emptyList(),
    val selectedDirections: Set<String> = emptySet(),
    val clubs: List<Club> = emptyList(),
    val visibleClubs: List<Club> = emptyList(),
)
