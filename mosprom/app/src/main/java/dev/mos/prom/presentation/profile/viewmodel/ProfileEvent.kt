package dev.mos.prom.presentation.profile.viewmodel

sealed class ProfileEvent {
    data object OnLoadData : ProfileEvent()
    data class OnUpdateProfile(
        val name: String? = null,
        val telegram: String? = null,
        val description: String? = null,
        val university: String? = null,
        val directions: List<String>? = null,
    ) : ProfileEvent()
    data class OnUploadAvatar(
        val filename: String,
        val bytes: ByteArray,
        val mime: String = "image/jpeg",
    ) : ProfileEvent()
    data object OnLogout : ProfileEvent()
}
