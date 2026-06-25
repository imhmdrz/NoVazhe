package mohaamadreza.saemipour.no.vazheh.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import mohaamadreza.saemipour.no.vazheh.network.ApiConfig

/**
 * Repository for child progress / quiz history (all endpoints require auth).
 * مخزن پیشرفت فرزند و تاریخچه آزمون
 */
class ProgressRepository(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage
) {

    /** Overall progress stats for a child (learned words, accuracy, per-category). */
    suspend fun getChildProgressStats(childId: Int): Result<ApiResponse<ChildProgressStatsDTO>> {
        return try {
            val token = tokenStorage.getToken()
                ?: return Result.failure(Exception("No token found"))

            val response: ApiResponse<ChildProgressStatsDTO> =
                httpClient
                    .get("${ApiConfig.BASE_URL}${ApiConfig.PROGRESS_CHILD}/$childId") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }
                    .body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Most recent quiz attempts for a child. */
    suspend fun getRecentAttempts(
        childId: Int,
        limit: Int = 20
    ): Result<ListResponse<RecentQuizAttemptDTO>> {
        return try {
            val token = tokenStorage.getToken()
                ?: return Result.failure(Exception("No token found"))

            val response: ListResponse<RecentQuizAttemptDTO> =
                httpClient
                    .get("${ApiConfig.BASE_URL}${ApiConfig.PROGRESS_CHILD}/$childId/recent?limit=$limit") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }
                    .body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Progress for a single word (attempts, accuracy, learned flag). */
    suspend fun getWordProgress(
        childId: Int,
        wordId: Int
    ): Result<ApiResponse<WordProgressDTO>> {
        return try {
            val token = tokenStorage.getToken()
                ?: return Result.failure(Exception("No token found"))

            val response: ApiResponse<WordProgressDTO> =
                httpClient
                    .get("${ApiConfig.BASE_URL}${ApiConfig.PROGRESS_CHILD}/$childId/word/$wordId") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }
                    .body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
