package mohaamadreza.saemipour.no.vazheh.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import mohaamadreza.saemipour.no.vazheh.network.ApiConfig
import mohaamadreza.saemipour.no.vazheh.network.createHttpClient

/** Repository for authentication operations */
class AuthRepository(
        private val httpClient: HttpClient = createHttpClient(),
        private val tokenStorage: TokenStorage = TokenStorage(createSettings())
) {

    /**
     * Login with username and password
     * @return AuthResponse with success status and token if successful
     */
    suspend fun login(username: String, password: String): Result<AuthResponse> {
        return try {
            val response: AuthResponse =
                    httpClient
                            .post("${ApiConfig.BASE_URL}${ApiConfig.AUTH_LOGIN}") {
                                contentType(ContentType.Application.Json)
                                setBody(LoginRequest(username, password))
                            }
                            .body()

            if (response.success && response.token != null) {
                tokenStorage.saveToken(response.token)
                response.parent?.let {
                    tokenStorage.saveParentInfo(it.id, it.username, it.displayName)
                }
                // Notify successful login - اطلاع‌رسانی ورود موفق
                AuthStateManager.notifyLoggedIn()
            }

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Test without login - create/get test user for demo purposes
     * @return AuthResponse with success status and token if successful
     */
    suspend fun testWithoutLogin(): Result<AuthResponse> {
        return try {
            val response: AuthResponse =
                    httpClient
                            .post("${ApiConfig.BASE_URL}${ApiConfig.AUTH_TEST}") {}
                            .body()

            if (response.success && response.token != null) {
                tokenStorage.saveToken(response.token)
                response.parent?.let {
                    tokenStorage.saveParentInfo(it.id, it.username, it.displayName)
                }
                // Notify successful login - اطلاع‌رسانی ورود موفق
                AuthStateManager.notifyLoggedIn()
            }

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Register a new user
     * @return AuthResponse with success status and token if successful
     */
    suspend fun register(
            username: String,
            password: String,
            displayName: String
    ): Result<AuthResponse> {
        return try {
            val response: AuthResponse =
                    httpClient
                            .post("${ApiConfig.BASE_URL}${ApiConfig.AUTH_REGISTER}") {
                                contentType(ContentType.Application.Json)
                                setBody(RegisterRequest(username, password, displayName))
                            }
                            .body()

            if (response.success && response.token != null) {
                tokenStorage.saveToken(response.token)
                response.parent?.let {
                    tokenStorage.saveParentInfo(it.id, it.username, it.displayName)
                }
                // Notify successful login - اطلاع‌رسانی ورود موفق
                AuthStateManager.notifyLoggedIn()
            }

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get current authenticated user info
     * @return ApiResponse with ParentDTO if successful
     */
    suspend fun getCurrentUser(): Result<ApiResponse<ParentDTO>> {
        return try {
            val token =
                    tokenStorage.getToken() ?: return Result.failure(Exception("No token found"))

            val response: ApiResponse<ParentDTO> =
                    httpClient
                            .get("${ApiConfig.BASE_URL}${ApiConfig.AUTH_ME}") {
                                header(HttpHeaders.Authorization, "Bearer $token")
                            }
                            .body()

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Check if user is logged in */
    fun isLoggedIn(): Boolean {
        return tokenStorage.isLoggedIn()
    }

    /** Get stored token */
    fun getToken(): String? {
        return tokenStorage.getToken()
    }

    /** Get stored display name */
    fun getDisplayName(): String? {
        return tokenStorage.getDisplayName()
    }

    /** Get stored username */
    fun getUsername(): String? {
        return tokenStorage.getUsername()
    }

    /** Logout - clear all stored data */
    fun logout() {
        tokenStorage.clearAll()
        // Notify logout - اطلاع‌رسانی خروج
        AuthStateManager.notifyLoggedOut()
    }

    /**
     * Force logout without notifying (used when handling 401 to avoid infinite loop)
     * خروج اجباری بدون اطلاع‌رسانی (برای مدیریت خطای 401)
     */
    fun forceLogout() {
        tokenStorage.clearAll()
    }
}
