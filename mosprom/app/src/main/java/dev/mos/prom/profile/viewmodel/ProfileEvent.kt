package dev.mos.prom.profile.viewmodel

sealed class ProfileEvent {
    data object OnLoadData : ProfileEvent()
    data class OnUpdateProfile(
        val name: String? = null,
        val telegram: String? = null,
        val description: String? = null,
        val university: String? = null,
    ) : ProfileEvent()
    data class OnUploadAvatar(
        val filename: String,
        val bytes: ByteArray,
        val mime: String = "image/jpeg",
    ) : ProfileEvent()
}
