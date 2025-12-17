package mohaamadreza.saemipour.no.vazheh.services

import mohaamadreza.saemipour.no.vazheh.database.tables.*
import mohaamadreza.saemipour.no.vazheh.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Content Service - سرویس محتوا
 * دسته‌بندی‌ها و کلمات
 */
object ContentService {
    
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    // ==================== Categories - دسته‌بندی‌ها ====================
    
    /**
     * Get all categories with word counts
     * دریافت همه دسته‌بندی‌ها
     */
    fun getAllCategories(): ListResponse<CategoryDTO> {
        return transaction {
            val categories = Categories.selectAll()
                .where { Categories.isActive eq true }
                .orderBy(Categories.displayOrder)
                .map { row ->
                    val wordCount = Words.selectAll()
                        .where { (Words.categoryId eq row[Categories.id]) and (Words.isActive eq true) }
                        .count().toInt()
                    
                    CategoryDTO(
                        id = row[Categories.id].value,
                        nameFa = row[Categories.nameFa],
                        nameEn = row[Categories.nameEn],
                        iconUrl = row[Categories.iconUrl],
                        displayOrder = row[Categories.displayOrder],
                        wordCount = wordCount
                    )
                }
            
            ListResponse(true, categories, categories.size)
        }
    }
    
    /**
     * Get category by ID
     */
    fun getCategoryById(categoryId: Int): ApiResponse<CategoryDTO> {
        return transaction {
            val row = Categories.selectAll()
                .where { (Categories.id eq categoryId) and (Categories.isActive eq true) }
                .singleOrNull()
                ?: return@transaction ApiResponse(false, "دسته‌بندی یافت نشد", null)
            
            val wordCount = Words.selectAll()
                .where { (Words.categoryId eq categoryId) and (Words.isActive eq true) }
                .count().toInt()
            
            val category = CategoryDTO(
                id = row[Categories.id].value,
                nameFa = row[Categories.nameFa],
                nameEn = row[Categories.nameEn],
                iconUrl = row[Categories.iconUrl],
                displayOrder = row[Categories.displayOrder],
                wordCount = wordCount
            )
            
            ApiResponse(true, "موفق", category)
        }
    }
    
    // ==================== Words - کلمات ====================
    
    /**
     * Get words by category
     * دریافت کلمات یک دسته‌بندی
     */
    fun getWordsByCategory(categoryId: Int): ListResponse<WordDTO> {
        return transaction {
            val words = Words.selectAll()
                .where { (Words.categoryId eq categoryId) and (Words.isActive eq true) }
                .orderBy(Words.displayOrder)
                .map { row ->
                    WordDTO(
                        id = row[Words.id].value,
                        categoryId = row[Words.categoryId].value,
                        wordFa = row[Words.wordFa],
                        wordEn = row[Words.wordEn],
                        imageUrl = row[Words.imageUrl],
                        audioUrl = row[Words.audioUrl],
                        displayOrder = row[Words.displayOrder]
                    )
                }
            
            ListResponse(true, words, words.size)
        }
    }
    
    /**
     * Get words by category with child progress
     * دریافت کلمات با پیشرفت فرزند
     */
    fun getWordsByCategoryWithProgress(categoryId: Int, childId: Int): ListResponse<WordWithProgressDTO> {
        return transaction {
            val words = Words.selectAll()
                .where { (Words.categoryId eq categoryId) and (Words.isActive eq true) }
                .orderBy(Words.displayOrder)
                .map { row ->
                    val wordId = row[Words.id].value
                    
                    // Get progress for this word
                    val progressRow = ChildProgress.selectAll()
                        .where { (ChildProgress.childId eq childId) and (ChildProgress.wordId eq wordId) }
                        .singleOrNull()
                    
                    val progress = progressRow?.let {
                        val correct = it[ChildProgress.correctAttempts]
                        val total = it[ChildProgress.totalAttempts]
                        WordProgressDTO(
                            wordId = wordId,
                            correctAttempts = correct,
                            totalAttempts = total,
                            isLearned = it[ChildProgress.isLearned],
                            accuracyPercent = if (total > 0) (correct.toDouble() / total * 100) else 0.0
                        )
                    }
                    
                    WordWithProgressDTO(
                        id = wordId,
                        categoryId = row[Words.categoryId].value,
                        wordFa = row[Words.wordFa],
                        wordEn = row[Words.wordEn],
                        imageUrl = row[Words.imageUrl],
                        audioUrl = row[Words.audioUrl],
                        displayOrder = row[Words.displayOrder],
                        progress = progress
                    )
                }
            
            ListResponse(true, words, words.size)
        }
    }
    
    /**
     * Get word by ID
     * دریافت کلمه با شناسه
     */
    fun getWordById(wordId: Int): ApiResponse<WordDTO> {
        return transaction {
            val row = Words.selectAll()
                .where { (Words.id eq wordId) and (Words.isActive eq true) }
                .singleOrNull()
                ?: return@transaction ApiResponse(false, "کلمه یافت نشد", null)
            
            val word = WordDTO(
                id = row[Words.id].value,
                categoryId = row[Words.categoryId].value,
                wordFa = row[Words.wordFa],
                wordEn = row[Words.wordEn],
                imageUrl = row[Words.imageUrl],
                audioUrl = row[Words.audioUrl],
                displayOrder = row[Words.displayOrder]
            )
            
            ApiResponse(true, "موفق", word)
        }
    }
    
    // ==================== Custom Words - کلمات سفارشی ====================
    
    /**
     * Get custom words by parent
     * دریافت کلمات سفارشی مادر
     */
    fun getCustomWords(parentId: Int): ListResponse<CustomWordDTO> {
        return transaction {
            val words = CustomWords.selectAll()
                .where { CustomWords.parentId eq parentId }
                .orderBy(CustomWords.createdAt, SortOrder.DESC)
                .map { row ->
                    CustomWordDTO(
                        id = row[CustomWords.id].value,
                        parentId = row[CustomWords.parentId].value,
                        wordFa = row[CustomWords.wordFa],
                        wordEn = row[CustomWords.wordEn],
                        imageUrl = row[CustomWords.imageUrl],
                        audioUrl = row[CustomWords.audioUrl],
                        categoryId = row[CustomWords.categoryId]?.value,
                        createdAt = row[CustomWords.createdAt].format(dateFormatter)
                    )
                }
            
            ListResponse(true, words, words.size)
        }
    }
    
    /**
     * Create custom word
     * ایجاد کلمه سفارشی توسط مادر
     */
    fun createCustomWord(parentId: Int, request: CreateCustomWordRequest): ApiResponse<CustomWordDTO> {
        return transaction {
            val now = LocalDateTime.now()
            val wordId = CustomWords.insertAndGetId {
                it[CustomWords.parentId] = parentId
                it[wordFa] = request.wordFa
                it[wordEn] = request.wordEn
                it[imageUrl] = request.imageUrl
                it[audioUrl] = request.audioUrl
                it[categoryId] = request.categoryId
                it[createdAt] = now
                it[updatedAt] = now
            }
            
            val word = CustomWordDTO(
                id = wordId.value,
                parentId = parentId,
                wordFa = request.wordFa,
                wordEn = request.wordEn,
                imageUrl = request.imageUrl,
                audioUrl = request.audioUrl,
                categoryId = request.categoryId,
                createdAt = now.format(dateFormatter)
            )
            
            ApiResponse(true, "کلمه سفارشی با موفقیت اضافه شد", word)
        }
    }
    
    /**
     * Delete custom word
     * حذف کلمه سفارشی
     */
    fun deleteCustomWord(parentId: Int, wordId: Int): MessageResponse {
        return transaction {
            val deleted = CustomWords.deleteWhere {
                (CustomWords.id eq wordId) and (CustomWords.parentId eq parentId)
            }
            
            if (deleted > 0) {
                MessageResponse(true, "کلمه سفارشی با موفقیت حذف شد")
            } else {
                MessageResponse(false, "کلمه یافت نشد")
            }
        }
    }
}
