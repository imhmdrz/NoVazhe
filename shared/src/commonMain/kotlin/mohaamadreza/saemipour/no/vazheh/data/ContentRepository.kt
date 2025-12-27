package mohaamadreza.saemipour.no.vazheh.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import mohaamadreza.saemipour.no.vazheh.network.ApiConfig

/**
 * Repository for content operations
 * دسته‌بندی‌ها، کلمات و کلمات سفارشی
 */
class ContentRepository(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage
) {

    // ==================== Categories - دسته‌بندی‌ها ====================

    /**
     * Get all categories
     * دریافت همه دسته‌بندی‌ها
     */
    suspend fun getAllCategories(): Result<ListResponse<CategoryDTO>> {
        return try {
            val response: ListResponse<CategoryDTO> =
                httpClient
                    .get("${ApiConfig.BASE_URL}${ApiConfig.CATEGORIES}")
                    .body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get category by ID
     * دریافت دسته‌بندی با شناسه
     */
    suspend fun getCategoryById(categoryId: Int): Result<ApiResponse<CategoryDTO>> {
        return try {
            val response: ApiResponse<CategoryDTO> =
                httpClient
                    .get("${ApiConfig.BASE_URL}${ApiConfig.CATEGORIES}/$categoryId")
                    .body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== Words - کلمات ====================

    /**
     * Get words by category
     * دریافت کلمات یک دسته‌بندی
     */
    suspend fun getWordsByCategory(categoryId: Int): Result<ListResponse<WordDTO>> {
        return try {
            val response: ListResponse<WordDTO> =
                httpClient
                    .get("${ApiConfig.BASE_URL}${ApiConfig.CATEGORIES}/$categoryId/words")
                    .body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get words by category with child progress
     * دریافت کلمات با پیشرفت فرزند
     */
    suspend fun getWordsByCategoryWithProgress(
        categoryId: Int,
        childId: Int
    ): Result<ListResponse<WordWithProgressDTO>> {
        return try {
            val token = tokenStorage.getToken()
            val response: ListResponse<WordWithProgressDTO> =
                httpClient
                    .get("${ApiConfig.BASE_URL}${ApiConfig.CATEGORIES}/$categoryId/words/progress?childId=$childId") {
                        token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                    }
                    .body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get word by ID
     * دریافت کلمه با شناسه
     */
    suspend fun getWordById(wordId: Int): Result<ApiResponse<WordDTO>> {
        return try {
            val response: ApiResponse<WordDTO> =
                httpClient
                    .get("${ApiConfig.BASE_URL}${ApiConfig.WORDS}/$wordId")
                    .body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== Custom Words - کلمات سفارشی ====================

    /**
     * Get custom words created by parent
     * دریافت کلمات سفارشی مادر
     */
    suspend fun getCustomWords(): Result<ListResponse<CustomWordDTO>> {
        return try {
            val token = tokenStorage.getToken()
                ?: return Result.failure(Exception("No token found"))

            val response: ListResponse<CustomWordDTO> =
                httpClient
                    .get("${ApiConfig.BASE_URL}${ApiConfig.CUSTOM_WORDS}") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }
                    .body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create a custom word
     * ایجاد کلمه سفارشی
     */
    suspend fun createCustomWord(request: CreateCustomWordRequest): Result<ApiResponse<CustomWordDTO>> {
        return try {
            val token = tokenStorage.getToken()
                ?: return Result.failure(Exception("No token found"))

            val response: ApiResponse<CustomWordDTO> =
                httpClient
                    .post("${ApiConfig.BASE_URL}${ApiConfig.CUSTOM_WORDS}") {
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
     * Delete a custom word
     * حذف کلمه سفارشی
     */
    suspend fun deleteCustomWord(wordId: Int): Result<MessageResponse> {
        return try {
            val token = tokenStorage.getToken()
                ?: return Result.failure(Exception("No token found"))

            val response: MessageResponse =
                httpClient
                    .delete("${ApiConfig.BASE_URL}${ApiConfig.CUSTOM_WORDS}/$wordId") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }
                    .body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}




