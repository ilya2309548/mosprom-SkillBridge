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
    ) : ProfileEvent() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as OnUploadAvatar

            if (filename != other.filename) return false
            if (!bytes.contentEquals(other.bytes)) return false
            if (mime != other.mime) return false

            return true
        }

        override fun hashCode(): Int {
            var result = filename.hashCode()
            result = 31 * result + bytes.contentHashCode()
            result = 31 * result + mime.hashCode()
            return result
        }
    }

    data object OnLogout : ProfileEvent()
}
