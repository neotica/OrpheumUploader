package id.neotica.orpheum.uploader.domain.local

interface TokenStorage {
    fun saveToken(token: String, refreshToken: String)
    fun getToken(): String?
    fun clearToken()
}