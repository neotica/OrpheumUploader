package id.neotica.orpheum.uploader.domain.remote

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<String>
}