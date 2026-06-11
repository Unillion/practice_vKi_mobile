package ci.nsu.mobile.main.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class TokenManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val TOKEN_KEY = "jwt_token"
    }

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        PREFS_NAME,
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var token: String?
        get() = sharedPreferences.getString(TOKEN_KEY, null)
        set(value) {
            sharedPreferences.edit().putString(TOKEN_KEY, value).apply()
        }

    fun clearToken() {
        sharedPreferences.edit().remove(TOKEN_KEY).apply()
    }

    fun hasToken(): Boolean = !token.isNullOrEmpty()
}