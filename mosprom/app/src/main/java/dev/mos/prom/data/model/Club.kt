package dev.mos.prom.data.model

data class Club(
    val id: Long,
    val name: String,
    val description: String,
    val logoUrl: String?,
    val directions: List<String>,
    val creatorId: Long? = null,
)
