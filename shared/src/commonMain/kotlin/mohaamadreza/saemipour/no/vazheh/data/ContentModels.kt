package mohaamadreza.saemipour.no.vazheh.data

import kotlinx.serialization.Serializable

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

/**
 * Create category request - درخواست ساخت دسته‌بندی جدید توسط مادر
 */
@Serializable
data class CreateCategoryRequest(
    val nameFa: String,
    val nameEn: String = "",
    val iconUrl: String? = null,
    val displayOrder: Int = 0
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

// ==================== Memory Progress DTOs - پیشرفت بازی حافظه ====================

/**
 * Current memory-game progression for a child
 * پیشرفت فعلی بازی حافظه (نردبان ابعاد 2x2 تا 4x4)
 */
@Serializable
data class MemoryProgressDTO(
    val dimensionIndex: Int,   // جایگاه در نردبان ابعاد (0 = 2x2 … 3 = 4x4)
    val successfulGames: Int   // برد‌های موفق در ابعاد فعلی (۰ تا ۳)
)

/**
 * Request to record one successful memory game at the given dimension
 */
@Serializable
data class CompleteMemoryGameRequest(
    val dimensionIndex: Int
)

// ==================== Custom Word DTOs - کلمات سفارشی ====================

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

// ==================== List Response ====================

@Serializable
data class ListResponse<T>(
    val success: Boolean,
    val data: List<T>,
    val total: Int,
    val message: String? = null
)

// ==================== Message Response ====================

@Serializable
data class MessageResponse(
    val success: Boolean,
    val message: String
)
