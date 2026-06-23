package id.neotica.orpheum.uploader.data.local

import android.content.Context
import android.content.SharedPreferences
import id.neotica.orpheum.uploader.domain.local.TokenStorage

class AndroidTokenStorage(context: Context) : TokenStorage {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("orpheum_uploader", Context.MODE_PRIVATE)

    override fun saveToken(token: String, refreshToken: String) {
        prefs.edit()
            .putString(JWT_TOKEN, token)
            .putString(REFRESH_TOKEN, refreshToken)
            .apply()
    }

    override fun getToken(): String? = prefs.getString(JWT_TOKEN, null)

    override fun clearToken() {
        prefs.edit()
            .remove(JWT_TOKEN)
            .remove(REFRESH_TOKEN)
            .apply()
    }

    companion object {
        private const val JWT_TOKEN = "JWT_TOKEN"
        private const val REFRESH_TOKEN = "REFRESH_TOKEN"
    }
}
