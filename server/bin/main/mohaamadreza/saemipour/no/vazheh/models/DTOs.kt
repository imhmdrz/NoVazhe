package mohaamadreza.saemipour.no.vazheh.models

import kotlinx.serialization.Serializable

// ==================== Authentication DTOs - احراز هویت ====================

@Serializable
data class RegisterRequest(val username: String, val password: String, val displayName: String)

@Serializable data class LoginRequest(val username: String, val password: String)

@Serializable
data class AuthResponse(
        val success: Boolean,
        val message: String,
        val token: String? = null,
        val parent: ParentDTO? = null
)

// ==================== Parent DTOs - والدین ====================

@Serializable
data class ParentDTO(
        val id: Int,
        val username: String,
        val displayName: String,
        val isActive: Boolean,
        val createdAt: String
)

// ==================== Child DTOs - فرزندان ====================

/**
 * Gender enum for children
 * جنسیت فرزند - پسر یا دختر
 */
@Serializable
enum class Gender {
    BOY,   // پسر
    GIRL   // دختر
}

@Serializable
data class ChildDTO(
        val id: Int,
        val parentId: Int,
        val name: String,
        val age: Int,
        val gender: Gender,
        val avatarUrl: String?,
        val isActive: Boolean,
        val createdAt: String
)

@Serializable
data class CreateChildRequest(
        val name: String,
        val age: Int,
        val gender: Gender,
        val avatarUrl: String? = null
)

@Serializable
data class UpdateChildRequest(
        val name: String? = null,
        val age: Int? = null,
        val gender: Gender? = null,
        val avatarUrl: String? = null
)

// ==================== Category DTOs - دسته‌بندی‌ها ====================

@Serializable
data class CategoryDTO(
        val id: Int,
        val nameFa: String,
        val nameEn: String,
        val iconUrl: String?,
        val displayOrder: Int,
        val wordCount: Int = 0
)

// ==================== Word DTOs - کلمات ====================

@Serializable
data class WordDTO(
        val id: Int,
        val categoryId: Int,
        val wordFa: String,
        val wordEn: String,
        val imageUrl: String?,
        val audioUrl: String?,
        val displayOrder: Int
)

@Serializable
data class WordWithProgressDTO(
        val id: Int,
        val categoryId: Int,
        val wordFa: String,
        val wordEn: String,
        val imageUrl: String?,
        val audioUrl: String?,
        val displayOrder: Int,
        val progress: WordProgressDTO? = null
)

// ==================== Custom Word DTOs - کلمات سفارشی مادر ====================

@Serializable
data class CustomWordDTO(
        val id: Int,
        val parentId: Int,
        val wordFa: String,
        val wordEn: String?,
        val imageUrl: String?,
        val audioUrl: String,
        val categoryId: Int?,
        val createdAt: String
)

@Serializable
data class CreateCustomWordRequest(
        val wordFa: String,
        val wordEn: String? = null,
        val imageUrl: String? = null,
        val audioUrl: String,
        val categoryId: Int? = null
)

// ==================== Quiz DTOs - آزمون ====================

@Serializable
data class QuizQuestionDTO(
        val wordId: Int,
        val audioUrl: String,
        val options: List<QuizOptionDTO> // گزینه‌ها (یکی درست، بقیه غلط)
)

@Serializable
data class QuizOptionDTO(
        val wordId: Int,
        val wordFa: String,
        val wordEn: String,
        val imageUrl: String?
)

@Serializable
data class SubmitQuizRequest(
        val childId: Int,
        val wordId: Int, // کلمه اصلی (صوتی که پخش شد)
        val selectedWordId: Int, // کلمه‌ای که فرزند انتخاب کرد
        val responseTimeMs: Int? = null
)

@Serializable
data class QuizResultDTO(
        val isCorrect: Boolean,
        val correctWordId: Int,
        val correctWordFa: String,
        val message: String
)

// ==================== Progress DTOs - پیشرفت ====================

@Serializable
data class WordProgressDTO(
        val wordId: Int,
        val correctAttempts: Int,
        val totalAttempts: Int,
        val isLearned: Boolean,
        val accuracyPercent: Double
)

@Serializable
data class ChildProgressStatsDTO(
        val childId: Int,
        val childName: String,
        val totalWords: Int,
        val learnedWords: Int,
        val inProgressWords: Int,
        val totalAttempts: Int,
        val correctAttempts: Int,
        val overallAccuracyPercent: Double,
        val progressByCategory: List<CategoryProgressDTO>
)

@Serializable
data class CategoryProgressDTO(
        val categoryId: Int,
        val categoryNameFa: String,
        val categoryNameEn: String,
        val totalWords: Int,
        val learnedWords: Int,
        val progressPercent: Double
)

@Serializable
data class RecentQuizAttemptDTO(
        val wordFa: String,
        val wordEn: String,
        val isCorrect: Boolean,
        val attemptedAt: String
)

// ==================== Generic Response DTOs ====================

@Serializable
data class ApiResponse<T>(val success: Boolean, val message: String, val data: T? = null)

@Serializable data class ListResponse<T>(val success: Boolean, val data: List<T>, val total: Int)

@Serializable data class MessageResponse(val success: Boolean, val message: String)
