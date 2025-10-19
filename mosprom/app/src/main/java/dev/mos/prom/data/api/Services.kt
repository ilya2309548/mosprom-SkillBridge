package dev.mos.prom.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.path
import io.ktor.http.path

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
    suspend fun myClubs(): List<ClubDto> = client.get("/me/clubs").body()

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

    // Replace user's technologies with provided list of technology NAMES (backend expects names and user_id)
    suspend fun setTechnologies(userId: Long, technologyNames: List<String>) {
        client.post("/users/technologies") {
            contentType(ContentType.Application.Json)
            setBody(PostUserTechnologiesRequest(userId = userId, technologies = technologyNames))
        }
    }
}

class ClubService(private val client: HttpClient) {
    suspend fun listDirections(): List<DirectionDto> = client.get("/directions").body()

    // List technologies for a direction by its id
    suspend fun technologiesByDirection(directionId: Long): List<TechnologyDto> =
        client.get("/directions/$directionId/technologies").body()

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

    suspend fun subscribe(clubId: Long) {
        client.post("/clubs/id/$clubId/subscribe") {}
    }

    suspend fun subscribers(clubId: Long): List<UserDto> =
        client.get("/clubs/id/$clubId/subscribers").body()
}

class ChatService(private val client: HttpClient) {
    suspend fun getChatIdByClubName(name: String): ChatIdResponse =
        client.get {
            url {
                path("clubs", name, "chat")
            }
        }.body()

    // Open a websocket session and provide the session to the caller for send/receive
    suspend fun openSession(chatId: String, block: suspend DefaultClientWebSocketSession.() -> Unit) {
        val safeId = chatId.trim().trim('{', '}').trim('"')
        client.webSocket(
            request = {
                url {
                    path("ws")
                    parameters.append("chat_id", safeId)
                }
            }
        ) { block() }
    }
}

class PostService(private val client: HttpClient) {
    suspend fun createPost(req: CreatePostApiRequest): PostDto =
        client.post("/posts") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()

    suspend fun getPostsByClub(clubId: Long, type: String? = null): List<PostDto> =
        client.get("/posts/club") {
            url {
                parameters.append("club_id", clubId.toString())
                if (!type.isNullOrBlank()) parameters.append("type", type)
            }
        }.body()
}
