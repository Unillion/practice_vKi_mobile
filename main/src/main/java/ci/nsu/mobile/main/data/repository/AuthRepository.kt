package ci.nsu.mobile.main.data.repository

import ci.nsu.mobile.main.data.models.ErrorResponse
import ci.nsu.mobile.main.data.models.GroupDto
import ci.nsu.mobile.main.data.models.RegisterRequest
import ci.nsu.mobile.main.data.models.UserDto
import ci.nsu.mobile.main.data.network.ApiService
import ci.nsu.mobile.main.data.storage.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState

    suspend fun login(login: String, password: String): Result<Unit> {
        return try {
            val credentials = mapOf("login" to login, "password" to password)
            val response = apiService.login(credentials)

            if (response.isSuccessful && response.body() != null) {
                val token = response.body()!!.token
                tokenManager.token = token
                _authState.value = AuthState.Authenticated
                Result.success(Unit)
            } else {
                val errorMsg = parseError(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Ошибка сети: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(Exception("Ошибка сервера: ${e.code()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка: ${e.message}"))
        }
    }

    suspend fun register(request: RegisterRequest): Result<Unit> {
        return try {
            val response = apiService.register(request)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = parseError(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Ошибка сети: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(Exception("Ошибка сервера: ${e.code()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка: ${e.message}"))
        }
    }

    suspend fun getUsers(): Result<List<UserDto>> {
        return try {
            val response = apiService.getUsers()

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Не удалось получить пользователей"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Ошибка сети: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(Exception("Ошибка сервера: ${e.code()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка: ${e.message}"))
        }
    }

    suspend fun getGroups(): Result<List<GroupDto>> {
        return try {
            val response = apiService.getGroups()

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Не удалось получить группы"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Ошибка сети: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(Exception("Ошибка сервера: ${e.code()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка: ${e.message}"))
        }
    }

    fun logout() {
        tokenManager.clearToken()
        _authState.value = AuthState.Unauthenticated
    }

    fun checkAuth(): Boolean {
        val hasToken = tokenManager.hasToken()
        _authState.value = if (hasToken) AuthState.Authenticated else AuthState.Unauthenticated
        return hasToken
    }

    private fun parseError(errorBody: String?): String {
        return try {
            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            val error = json.decodeFromString<ErrorResponse>(errorBody ?: "")
            error.message
        } catch (e: Exception) {
            "Неизвестная ошибка"
        }
    }
}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
}