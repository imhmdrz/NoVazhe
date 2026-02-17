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

/**
 * Repository for quiz operations
 * آزمون - صوت کلمه پخش می‌شود و فرزند کلمه درست را انتخاب می‌کند
 */
class QuizRepository(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage
) {

    /**
     * Generate a single quiz question for a category
     * تولید یک سوال آزمون برای یک دسته‌بندی
     *
     * @param categoryId شناسه دسته‌بندی
     * @param childId شناسه فرزند (اختیاری - برای اولویت‌بندی کلمات یادنگرفته)
     */
    suspend fun getQuizQuestion(
        categoryId: Int,
        childId: Int? = null
    ): Result<ApiResponse<QuizQuestionDTO>> {
        return try {
            val token = tokenStorage.getToken()
                ?: return Result.failure(Exception("No token found"))

            val url = buildString {
                append("${ApiConfig.BASE_URL}${ApiConfig.QUIZ_QUESTION}")
                append("?categoryId=$categoryId")
                if (childId != null) {
                    append("&childId=$childId")
                }
            }

            val response: ApiResponse<QuizQuestionDTO> =
                httpClient
                    .get(url) {
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }
                    .body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate multiple quiz questions for a category
     * تولید چند سوال آزمون
     *
     * @param categoryId شناسه دسته‌بندی
     * @param childId شناسه فرزند (اختیاری)
     * @param count تعداد سوالات (پیش‌فرض: 5)
     */
    suspend fun getQuizQuestions(
        categoryId: Int,
        childId: Int? = null,
        count: Int = 5
    ): Result<ListResponse<QuizQuestionDTO>> {
        return try {
            val token = tokenStorage.getToken()
                ?: return Result.failure(Exception("No token found"))

            val url = buildString {
                append("${ApiConfig.BASE_URL}${ApiConfig.QUIZ_QUESTIONS}")
                append("?categoryId=$categoryId")
                append("&count=$count")
                if (childId != null) {
                    append("&childId=$childId")
                }
            }

            val response: ListResponse<QuizQuestionDTO> =
                httpClient
                    .get(url) {
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }
                    .body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Submit quiz answer
     * ثبت پاسخ آزمون
     *
     * @param request درخواست شامل childId, wordId و selectedWordId
     */
    suspend fun submitQuizAnswer(request: SubmitQuizRequest): Result<ApiResponse<QuizResultDTO>> {
        return try {
            val token = tokenStorage.getToken()
                ?: return Result.failure(Exception("No token found"))

            val response: ApiResponse<QuizResultDTO> =
                httpClient
                    .post("${ApiConfig.BASE_URL}${ApiConfig.QUIZ_SUBMIT}") {
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
}






