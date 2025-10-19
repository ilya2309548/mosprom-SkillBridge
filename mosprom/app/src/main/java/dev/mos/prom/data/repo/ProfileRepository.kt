package dev.mos.prom.data.repo

import dev.mos.prom.data.api.ProfileService
import dev.mos.prom.presentation.profile.UserModel

class ProfileRepository(private val api: ProfileService) {
    suspend fun me(): UserModel {
        val dto = api.me()
        return dto.toUserModel()
    }

    suspend fun myId(): Long = api.me().id

    suspend fun update(
        name: String?, telegram: String?, description: String?, university: String?, directions: List<String>? = null
    ): UserModel {
        val dto = api.updateProfile(name, telegram, description, university, directions)
        return dto.toUserModel()
    }

    suspend fun uploadPhoto(filename: String, bytes: ByteArray, mime: String): UserModel {
        val dto = api.uploadPhoto(filename, bytes, mime)
        return dto.toUserModel()
    }

    suspend fun myClubsCount(): Int = api.myClubs().size

    suspend fun myClubIds(): List<Long> = api.myClubs().map { it.id }
}

private fun dev.mos.prom.data.api.UserDto.toUserModel(): UserModel =
    UserModel(
        name = this.name ?: this.telegramName,
        tg = this.telegramName,
        description = this.description ?: "",
    education = this.university ?: "", // временно используем university как education
    achievements = this.achievements ?: emptyList(),
        photoUrl = this.photo?.let { 
            // Backend serves photos by /photos/{filename}
            if (it.startsWith("http")) it else "http://81.29.146.35:8080/photos/$it"
        },
        directions = (this.directions ?: emptyList()).mapNotNull { it.name }
    )
