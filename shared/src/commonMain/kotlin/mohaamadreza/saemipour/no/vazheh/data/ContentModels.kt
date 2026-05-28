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
