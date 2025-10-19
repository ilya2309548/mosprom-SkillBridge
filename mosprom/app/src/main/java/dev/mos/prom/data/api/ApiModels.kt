package dev.mos.prom.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    @SerialName("telegram_name") val telegramName: String,
    val password: String,
)

@Serializable
data class LoginResponse(
    val token: String,
)

@Serializable
data class RegisterRequest(
    @SerialName("telegram_name") val telegramName: String,
    val name: String? = null,
    val password: String,
    val description: String? = null,
    val university: String? = null,
    val achievements: List<String> = emptyList(),
)

@Serializable
data class UserDto(
    val id: Long,
    @SerialName("telegram_name") val telegramName: String,
    val name: String? = null,
    val description: String? = null,
    val photo: String? = null,
    @SerialName("achievements") val achievements: List<String>? = emptyList(),
    @SerialName("events_count") val eventsCount: Int = 0,
    val university: String? = null,
    val technologies: List<TechnologyDto>? = emptyList(),
    val directions: List<DirectionDto>? = emptyList(),
)

@Serializable
data class TechnologyDto(
    val id: Long? = null,
    val name: String
)

@Serializable
data class DirectionDto(
    val id: Long? = null,
    val name: String
)

@Serializable
data class ClubDto(
    val id: Long,
    val name: String,
    val logo: String? = null,
    val description: String? = null,
    @SerialName("creator_id") val creatorId: Long? = null,
    val directions: List<DirectionDto> = emptyList(),
)

@Serializable
data class CreateClubRequest(
    val name: String,
    val description: String,
    val directions: List<String> = emptyList(),
)

@Serializable
data class PostUserTechnologiesRequest(
    @SerialName("user_id") val userId: Long,
    val technologies: List<String>,
)

@Serializable
data class CreatePostApiRequest(
    val title: String,
    val description: String = "",
    val type: String,
    @SerialName("end_date") val endDate: String? = null, // ISO8601 or RFC3339
    @SerialName("age_restriction") val ageRestriction: Int? = null,
    val format: String? = null,
    val address: String = "",
    @SerialName("club_id") val clubId: Long,
    val technologies: List<String> = emptyList(),
)

@Serializable
data class PostDto(
    val id: Long,
    val title: String,
    val description: String? = null,
    val type: String,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("age_restriction") val ageRestriction: Int? = null,
    val format: String? = null,
    val address: String? = null,
    @SerialName("club_id") val clubId: Long,
    @SerialName("participants_count") val participantsCount: Int = 0,
    val technologies: List<TechnologyDto> = emptyList(),
    val image: String? = null,
)

// Achievements
@Serializable
data class AddAchievementRequest(
    @SerialName("user_id") val userId: Long,
    val achievement: String,
)

// Join post
@Serializable
data class JoinPostRequest(
    @SerialName("post_id") val postId: Long,
    @SerialName("user_id") val userId: Long,
)
