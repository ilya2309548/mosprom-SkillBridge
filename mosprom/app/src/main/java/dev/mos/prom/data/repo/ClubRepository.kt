package dev.mos.prom.data.repo

import dev.mos.prom.data.model.Club
import dev.mos.prom.data.api.ClubDto
import dev.mos.prom.data.api.ClubService
import dev.mos.prom.data.api.CreateClubRequest
import dev.mos.prom.data.api.DirectionDto
import dev.mos.prom.data.api.TechnologyDto



class ClubRepository(private val api: ClubService) {
    suspend fun directions(): List<String> = api.listDirections().map(DirectionDto::name)

    suspend fun directionsFull(): List<DirectionDto> = api.listDirections()

    suspend fun clubsByDirections(directions: List<String>): List<Club> =
        api.listClubs(name = null, directionNames = directions).map { it.toClub() }

    suspend fun searchClubs(name: String?, directions: List<String>): List<Club> =
        api.listClubs(name = name, directionNames = directions).map { it.toClub() }

    suspend fun createClub(name: String, description: String, directions: List<String>, logo: ByteArray?, logoFilename: String?, logoMime: String?): Club {
        val created = api.createClub(CreateClubRequest(name = name, description = description, directions = directions))
        if (logo != null && logoFilename != null) {
            api.uploadClubLogo(created.id, logoFilename, logo, logoMime ?: "image/jpeg")
        }
        return created.toClub()
    }

    suspend fun subscribeToClub(clubId: Long) {
        api.subscribe(clubId)
    }

    suspend fun subscribersCount(clubId: Long): Int = api.subscribers(clubId).size

    suspend fun technologiesByDirection(directionId: Long): List<TechnologyDto> =
        api.technologiesByDirection(directionId)
}

private fun ClubDto.toClub(): Club = Club(
    id = id,
    name = name,
    description = description.orEmpty(),
    logoUrl = logo?.let { if (it.startsWith("http")) it else "http://81.29.146.35:8080/photos/$it" },
    directions = directions.map { it.name },
    creatorId = creatorId,
)
