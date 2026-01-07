package mohaamadreza.saemipour.no.vazheh.data

import kotlinx.serialization.Serializable

// ==================== Quiz DTOs - آزمون ====================

/**
 * Quiz question DTO
 * سوال آزمون - شامل صوت و گزینه‌ها
 */
@Serializable
data class QuizQuestionDTO(
    val wordId: Int,
    val audioUrl: String,
    val options: List<QuizOptionDTO>
)

/**
 * Quiz option DTO
 * گزینه آزمون
 */
@Serializable
data class QuizOptionDTO(
    val wordId: Int,
    val wordFa: String,
    val wordEn: String,
    val imageUrl: String?
)

/**
 * Submit quiz answer request
 * درخواست ثبت پاسخ آزمون
 */
@Serializable
data class SubmitQuizRequest(
    val childId: Int,
    val wordId: Int,           // کلمه اصلی (صوتی که پخش شد)
    val selectedWordId: Int,   // کلمه‌ای که فرزند انتخاب کرد
    val responseTimeMs: Int? = null
)

/**
 * Quiz result DTO
 * نتیجه آزمون
 */
@Serializable
data class QuizResultDTO(
    val isCorrect: Boolean,
    val correctWordId: Int,
    val correctWordFa: String,
    val message: String
)





