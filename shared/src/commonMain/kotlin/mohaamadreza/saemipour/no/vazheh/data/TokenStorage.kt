package mohaamadreza.saemipour.no.vazheh.data

import com.russhwolf.settings.Settings

/** Token storage using multiplatform-settings Works on Android, iOS, and JVM */
expect fun createSettings(): Settings

class TokenStorage(private val settings: Settings) {

    companion object {
        private const val KEY_JWT_TOKEN = "jwt_token"
        private const val KEY_PARENT_ID = "parent_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_DISPLAY_NAME = "display_name"
    }

    fun saveToken(token: String) {
        settings.putString(KEY_JWT_TOKEN, token)
    }

    fun getToken(): String? {
        return settings.getStringOrNull(KEY_JWT_TOKEN)
    }

    fun clearToken() {
        settings.remove(KEY_JWT_TOKEN)
    }

    fun saveParentInfo(parentId: Int, username: String, displayName: String) {
        settings.putInt(KEY_PARENT_ID, parentId)
        settings.putString(KEY_USERNAME, username)
        settings.putString(KEY_DISPLAY_NAME, displayName)
    }

    fun getParentId(): Int? {
        return if (settings.hasKey(KEY_PARENT_ID)) {
            settings.getInt(KEY_PARENT_ID, -1).takeIf { it != -1 }
        } else null
    }

    fun getUsername(): String? {
        return settings.getStringOrNull(KEY_USERNAME)
    }

    fun getDisplayName(): String? {
        return settings.getStringOrNull(KEY_DISPLAY_NAME)
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    fun clearAll() {
        settings.remove(KEY_JWT_TOKEN)
        settings.remove(KEY_PARENT_ID)
        settings.remove(KEY_USERNAME)
        settings.remove(KEY_DISPLAY_NAME)
    }
}
