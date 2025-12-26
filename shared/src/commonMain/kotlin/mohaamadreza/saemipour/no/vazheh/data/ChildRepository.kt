package mohaamadreza.saemipour.no.vazheh.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import mohaamadreza.saemipour.no.vazheh.network.ApiConfig

/**
 * Repository for child operations
 * مدیریت فرزندان - مادر می‌تواند ۱ یا ۲ فرزند اضافه کند
 */
class ChildRepository(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage
) {

    /**
     * Get all children of parent
     * دریافت لیست فرزندان
     */
    suspend fun getChildren(): Result<ListResponse<ChildDTO>> {
        return try {
            val token = tokenStorage.getToken()
                ?: return Result.failure(Exception("No token found"))

            val response: ListResponse<ChildDTO> =
                httpClient
                    .get("${ApiConfig.BASE_URL}${ApiConfig.CHILDREN}") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }
                    .body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get child by ID
     * دریافت فرزند با شناسه
     */
    suspend fun getChildById(childId: Int): Result<ApiResponse<ChildDTO>> {
        return try {
            val token = tokenStorage.getToken()
                ?: return Result.failure(Exception("No token found"))

            val response: ApiResponse<ChildDTO> =
                httpClient
                    .get("${ApiConfig.BASE_URL}${ApiConfig.CHILDREN}/$childId") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }
                    .body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create a new child (max 2)
     * ایجاد فرزند جدید (حداکثر ۲ فرزند)
     */
    suspend fun createChild(request: CreateChildRequest): Result<ApiResponse<ChildDTO>> {
        return try {
            val token = tokenStorage.getToken()
                ?: return Result.failure(Exception("No token found"))

            val response: ApiResponse<ChildDTO> =
                httpClient
                    .post("${ApiConfig.BASE_URL}${ApiConfig.CHILDREN}") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }
                    .body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update child
     * ویرایش فرزند
     */
    suspend fun updateChild(childId: Int, request: UpdateChildRequest): Result<ApiResponse<ChildDTO>> {
        return try {
            val token = tokenStorage.getToken()
                ?: return Result.failure(Exception("No token found"))

            val response: ApiResponse<ChildDTO> =
                httpClient
                    .put("${ApiConfig.BASE_URL}${ApiConfig.CHILDREN}/$childId") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }
                    .body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete child
     * حذف فرزند
     */
    suspend fun deleteChild(childId: Int): Result<MessageResponse> {
        return try {
            val token = tokenStorage.getToken()
                ?: return Result.failure(Exception("No token found"))

            val response: MessageResponse =
                httpClient
                    .delete("${ApiConfig.BASE_URL}${ApiConfig.CHILDREN}/$childId") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }
                    .body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


