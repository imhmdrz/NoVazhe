package mohaamadreza.saemipour.no.vazheh.data

import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import mohaamadreza.saemipour.no.vazheh.network.ApiConfig

/**
 * Repository for child progress / quiz history (all endpoints require auth).
 * مخزن پیشرفت فرزند و تاریخچه آزمون
 */
class ProgressRepository(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage,
    private val settings: Settings
) {

    /**
     * Single slot for one pending memory-game completion, stored as
     * "<childId>|<dimensionIndex>|<successfulGamesBefore>". Kept in device storage so a win
     * whose submission failed (temporary network unavailability) survives app restarts and is
     * retried on the next memory-progress interaction.
     */
    private companion object {
        const val PENDING_MEMORY_KEY = "memory_progress_pending"
        const val PENDING_COLOR_SORTING_KEY = "color_sorting_progress_pending"
    }

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

    // ==================== Memory Game progression ====================

    /**
     * Current Memory Game progression for a child - پیشرفت فعلی بازی حافظه
     * Retries any earlier failed submission first, then fetches the fresh authoritative state.
     */
    suspend fun getMemoryProgress(childId: Int): Result<ApiResponse<MemoryProgressDTO>> {
        retryPendingMemoryCompletion()
        return try {
            val token = tokenStorage.getToken()
                ?: return Result.failure(Exception("No token found"))

            val response: ApiResponse<MemoryProgressDTO> =
                httpClient
                    .get("${ApiConfig.BASE_URL}${ApiConfig.MEMORY_PROGRESS}/$childId") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }
                    .body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Record one successful Memory Game at [dimensionIndex] - ثبت یک برد موفق بازی حافظه
     *
     * [successfulGamesBefore] is the counter value read before this game; it lets a later retry
     * tell "the submission already landed (response was lost)" apart from "it truly failed",
     * so duplicate submissions never advance progression twice. If the network fails, the win
     * is kept pending and retried on the next memory-progress interaction.
     */
    suspend fun recordMemoryGameComplete(
        childId: Int,
        dimensionIndex: Int,
        successfulGamesBefore: Int
    ): Result<ApiResponse<MemoryProgressDTO>> {
        // Apply any older pending win first so ordering stays correct.
        retryPendingMemoryCompletion()

        val token = tokenStorage.getToken()
            ?: return Result.failure(Exception("No token found"))

        return try {
            val response: ApiResponse<MemoryProgressDTO> =
                httpClient
                    .post("${ApiConfig.BASE_URL}${ApiConfig.MEMORY_PROGRESS}/$childId/complete") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(CompleteMemoryGameRequest(dimensionIndex))
                    }
                    .body()

            // A rejected submission (stale dimension etc.) must never be retried — only a
            // successful one clears the pending slot.
            if (response.success) {
                clearPendingMemoryCompletion()
            }
            Result.success(response)
        } catch (e: Exception) {
            // Temporary network unavailability — keep this win pending for a later retry.
            storePendingMemoryCompletion(childId, dimensionIndex, successfulGamesBefore)
            Result.failure(e)
        }
    }

    /**
     * Retry the single stored memory completion, if any - تلاش مجدد برای ثبت ناقص‌شده
     *
     * Reads the current server state first and only resends when it proves the earlier
     * submission did NOT land. The retry is not scoped to a specific child on purpose: a win
     * pending for one child must still get its chance even if the parent switches children.
     * Ownership of each request is enforced by the server per request.
     */
    private suspend fun retryPendingMemoryCompletion() {
        val pending = readPendingMemoryCompletion() ?: return
        val (childId, dimensionIndex, gamesBefore) = pending
        val token = tokenStorage.getToken() ?: return // logged out — keep pending until a later chance

        try {
            val current: ApiResponse<MemoryProgressDTO> =
                httpClient
                    .get("${ApiConfig.BASE_URL}${ApiConfig.MEMORY_PROGRESS}/$childId") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }
                    .body()
            if (!current.success || current.data == null) return // keep pending, retry next time
            val state = current.data!!

            // If the earlier submission already landed (its response was lost), drop it without resending.
            val alreadyApplied = state.dimensionIndex > dimensionIndex ||
                (state.dimensionIndex == dimensionIndex && state.successfulGames > gamesBefore)
            if (alreadyApplied) {
                clearPendingMemoryCompletion()
                return
            }

            val retry: ApiResponse<MemoryProgressDTO> =
                httpClient
                    .post("${ApiConfig.BASE_URL}${ApiConfig.MEMORY_PROGRESS}/$childId/complete") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(CompleteMemoryGameRequest(dimensionIndex))
                    }
                    .body()

            // A failed retry keeps the pending entry for the next opportunity.
            if (retry.success) clearPendingMemoryCompletion()
        } catch (_: Exception) {
            // Network still unavailable — the pending entry stays until a later retry.
        }
    }

    private fun readPendingMemoryCompletion(): Triple<Int, Int, Int>? {
        val raw = settings.getStringOrNull(PENDING_MEMORY_KEY) ?: return null
        val parts = raw.split("|")
        if (parts.size != 3) return null
        return try {
            Triple(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        } catch (_: NumberFormatException) {
            null
        }
    }

    private fun storePendingMemoryCompletion(childId: Int, dimensionIndex: Int, successfulGamesBefore: Int) {
        settings.putString(PENDING_MEMORY_KEY, "$childId|$dimensionIndex|$successfulGamesBefore")
    }

    private fun clearPendingMemoryCompletion() {
        settings.remove(PENDING_MEMORY_KEY)
    }

    // ==================== Color Sorting progression ====================

    /**
     * Current Color Sorting progression for a child - پیشرفت فعلی بازی رنگ‌ها
     * Retries any earlier failed submission first, then fetches the fresh authoritative state.
     */
    suspend fun getColorSortingProgress(childId: Int): Result<ApiResponse<ColorSortingProgressDTO>> {
        retryPendingColorSortingCompletion()
        return try {
            val token = tokenStorage.getToken()
                ?: return Result.failure(Exception("No token found"))

            val response: ApiResponse<ColorSortingProgressDTO> =
                httpClient
                    .get("${ApiConfig.BASE_URL}${ApiConfig.COLOR_SORTING_PROGRESS}/$childId") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }
                    .body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Record one successful Color Sorting game at [levelIndex] - ثبت یک برد موفق بازی رنگ‌ها
     */
    suspend fun recordColorSortingGameComplete(
        childId: Int,
        levelIndex: Int,
        successfulGamesBefore: Int
    ): Result<ApiResponse<ColorSortingProgressDTO>> {
        retryPendingColorSortingCompletion()

        val token = tokenStorage.getToken()
            ?: return Result.failure(Exception("No token found"))

        return try {
            val response: ApiResponse<ColorSortingProgressDTO> =
                httpClient
                    .post("${ApiConfig.BASE_URL}${ApiConfig.COLOR_SORTING_PROGRESS}/$childId/complete") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(CompleteColorSortingGameRequest(levelIndex))
                    }
                    .body()

            if (response.success) {
                clearPendingColorSortingCompletion()
            }
            Result.success(response)
        } catch (e: Exception) {
            storePendingColorSortingCompletion(childId, levelIndex, successfulGamesBefore)
            Result.failure(e)
        }
    }

    /**
     * Retry the single stored Color Sorting completion, if any.
     */
    private suspend fun retryPendingColorSortingCompletion() {
        val pending = readPendingColorSortingCompletion() ?: return
        val (childId, levelIndex, gamesBefore) = pending
        val token = tokenStorage.getToken() ?: return

        try {
            val current: ApiResponse<ColorSortingProgressDTO> =
                httpClient
                    .get("${ApiConfig.BASE_URL}${ApiConfig.COLOR_SORTING_PROGRESS}/$childId") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }
                    .body()
            if (!current.success || current.data == null) return
            val state = current.data!!

            val alreadyApplied = state.levelIndex > levelIndex ||
                (state.levelIndex == levelIndex && state.successfulGames > gamesBefore)
            if (alreadyApplied) {
                clearPendingColorSortingCompletion()
                return
            }

            val retry: ApiResponse<ColorSortingProgressDTO> =
                httpClient
                    .post("${ApiConfig.BASE_URL}${ApiConfig.COLOR_SORTING_PROGRESS}/$childId/complete") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(CompleteColorSortingGameRequest(levelIndex))
                    }
                    .body()

            if (retry.success) clearPendingColorSortingCompletion()
        } catch (_: Exception) {
            // Network still unavailable — the pending entry stays until a later retry.
        }
    }

    private fun readPendingColorSortingCompletion(): Triple<Int, Int, Int>? {
        val raw = settings.getStringOrNull(PENDING_COLOR_SORTING_KEY) ?: return null
        val parts = raw.split("|")
        if (parts.size != 3) return null
        return try {
            Triple(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        } catch (_: NumberFormatException) {
            null
        }
    }

    private fun storePendingColorSortingCompletion(childId: Int, levelIndex: Int, successfulGamesBefore: Int) {
        settings.putString(PENDING_COLOR_SORTING_KEY, "$childId|$levelIndex|$successfulGamesBefore")
    }

    private fun clearPendingColorSortingCompletion() {
        settings.remove(PENDING_COLOR_SORTING_KEY)
    }
}
