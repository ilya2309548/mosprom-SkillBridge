package dev.mos.prom.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.ContentType

class AuthService(private val client: HttpClient) {
    suspend fun login(req: LoginRequest): LoginResponse =
        client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()

    suspend fun register(req: RegisterRequest): LoginResponse =
        client.post("/register") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()
}

class ProfileService(private val client: HttpClient) {
    suspend fun me(): UserDto = client.get("/me").body()

    suspend fun updateProfile(
        name: String?,
        telegramName: String?,
        description: String?,
        university: String?,
        directions: List<String>? = null,
    ): UserDto = client.put("/me") {
        contentType(ContentType.Application.Json)
        setBody(
            buildMap<String, Any?> {
                if (!name.isNullOrBlank()) put("name", name)
                if (!telegramName.isNullOrBlank()) put("telegram_name", telegramName)
                if (!description.isNullOrBlank()) put("description", description)
                if (!university.isNullOrBlank()) put("university", university)
                if (!directions.isNullOrEmpty()) put("directions", directions)
            }
        )
    }.body()

    suspend fun uploadPhoto(filename: String, bytes: ByteArray, mime: String = "image/jpeg"): UserDto =
        client.post("/me/photo") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            key = "photo",
                            value = bytes,
                            headers = Headers.build {
                                append(HttpHeaders.ContentType, mime)
                                append(HttpHeaders.ContentDisposition, "filename=$filename")
                            }
                        )
                    }
                )
            )
        }.body()
}

class ClubService(private val client: HttpClient) {
    suspend fun listDirections(): List<DirectionDto> = client.get("/directions").body()

    suspend fun listClubs(name: String?, directionNames: List<String>): List<ClubDto> {
        val directionsQuery = if (directionNames.isEmpty()) "" else directionNames.joinToString(",")
        return client.get("/clubs") {
            url {
                if (!name.isNullOrBlank()) parameters.append("name", name)
                if (directionsQuery.isNotBlank()) parameters.append("directions", directionsQuery)
            }
        }.body()
    }

    suspend fun createClub(req: CreateClubRequest): ClubDto =
        client.post("/clubs") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()

    suspend fun uploadClubLogo(clubId: Long, filename: String, bytes: ByteArray, mime: String = "image/jpeg"): String =
        client.post("/clubs/$clubId/logo") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            key = "logo",
                            value = bytes,
                            headers = Headers.build {
                                append(HttpHeaders.ContentType, mime)
                                append(HttpHeaders.ContentDisposition, "filename=$filename")
                            }
                        )
                    }
                )
            )
        }.body<Map<String, String>>()
            .getValue("logo")
}
