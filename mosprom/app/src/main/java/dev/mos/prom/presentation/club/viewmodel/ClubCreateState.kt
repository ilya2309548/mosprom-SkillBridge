package dev.mos.prom.presentation.club.viewmodel

import dev.mos.prom.presentation.ClubItem
import dev.mos.prom.utils.MosPromResult

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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClubCreateState

        if (created != other.created) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (directions != other.directions) return false
        if (selectedDirections != other.selectedDirections) return false
        if (existingClubs != other.existingClubs) return false
        if (!logoBytes.contentEquals(other.logoBytes)) return false
        if (logoFilename != other.logoFilename) return false
        if (logoMime != other.logoMime) return false
        if (status != other.status) return false
        if (error != other.error) return false

        return true
    }

    override fun hashCode(): Int {
        var result = created.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + directions.hashCode()
        result = 31 * result + selectedDirections.hashCode()
        result = 31 * result + existingClubs.hashCode()
        result = 31 * result + (logoBytes?.contentHashCode() ?: 0)
        result = 31 * result + (logoFilename?.hashCode() ?: 0)
        result = 31 * result + (logoMime?.hashCode() ?: 0)
        result = 31 * result + status.hashCode()
        result = 31 * result + (error?.hashCode() ?: 0)
        return result
    }
}
