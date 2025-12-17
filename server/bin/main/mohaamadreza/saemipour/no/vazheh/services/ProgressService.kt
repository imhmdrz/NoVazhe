package mohaamadreza.saemipour.no.vazheh.services

import mohaamadreza.saemipour.no.vazheh.database.tables.*
import mohaamadreza.saemipour.no.vazheh.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.format.DateTimeFormatter

/**
 * Progress Service - سرویس پیشرفت
 * میزان پیشرفت فرزند
 */
object ProgressService {
    
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    /**
     * Get child progress stats
     * دریافت آمار پیشرفت فرزند
     */
    fun getChildProgressStats(parentId: Int, childId: Int): ApiResponse<ChildProgressStatsDTO> {
        return transaction {
            // Verify child belongs to parent
            val child = Children.selectAll()
                .where { (Children.id eq childId) and (Children.parentId eq parentId) and (Children.isActive eq true) }
                .singleOrNull()
                ?: return@transaction ApiResponse(false, "فرزند یافت نشد", null)
            
            // Get total words count
            val totalWords = Words.selectAll()
                .where { Words.isActive eq true }
                .count().toInt()
            
            // Get child's progress
            val progressList = ChildProgress.selectAll()
                .where { ChildProgress.childId eq childId }
                .toList()
            
            val learnedWords = progressList.count { it[ChildProgress.isLearned] }
            val inProgressWords = progressList.count { 
                !it[ChildProgress.isLearned] && it[ChildProgress.totalAttempts] > 0 
            }
            val totalAttempts = progressList.sumOf { it[ChildProgress.totalAttempts] }
            val correctAttempts = progressList.sumOf { it[ChildProgress.correctAttempts] }
            val overallAccuracy = if (totalAttempts > 0) {
                (correctAttempts.toDouble() / totalAttempts * 100).let { 
                    kotlin.math.round(it * 100) / 100 
                }
            } else 0.0
            
            // Get progress by category
            val categories = Categories.selectAll()
                .where { Categories.isActive eq true }
                .toList()
            
            val progressByCategory = categories.map { catRow ->
                val categoryId = catRow[Categories.id].value
                
                val wordIds = Words.selectAll()
                    .where { (Words.categoryId eq categoryId) and (Words.isActive eq true) }
                    .map { it[Words.id].value }
                
                val categoryTotalWords = wordIds.size
                
                val categoryLearnedWords = if (wordIds.isNotEmpty()) {
                    ChildProgress.selectAll()
                        .where { 
                            (ChildProgress.childId eq childId) and 
                            (ChildProgress.wordId inList wordIds) and 
                            (ChildProgress.isLearned eq true) 
                        }
                        .count().toInt()
                } else 0
                
                val progressPercent = if (categoryTotalWords > 0) {
                    (categoryLearnedWords.toDouble() / categoryTotalWords * 100).let {
                        kotlin.math.round(it * 100) / 100
                    }
                } else 0.0
                
                CategoryProgressDTO(
                    categoryId = categoryId,
                    categoryNameFa = catRow[Categories.nameFa],
                    categoryNameEn = catRow[Categories.nameEn],
                    totalWords = categoryTotalWords,
                    learnedWords = categoryLearnedWords,
                    progressPercent = progressPercent
                )
            }
            
            val stats = ChildProgressStatsDTO(
                childId = childId,
                childName = child[Children.name],
                totalWords = totalWords,
                learnedWords = learnedWords,
                inProgressWords = inProgressWords,
                totalAttempts = totalAttempts,
                correctAttempts = correctAttempts,
                overallAccuracyPercent = overallAccuracy,
                progressByCategory = progressByCategory
            )
            
            ApiResponse(true, "موفق", stats)
        }
    }
    
    /**
     * Get recent quiz attempts for a child
     * دریافت آزمون‌های اخیر فرزند
     */
    fun getRecentAttempts(parentId: Int, childId: Int, limit: Int = 20): ListResponse<RecentQuizAttemptDTO> {
        return transaction {
            // Verify child belongs to parent
            val childExists = Children.selectAll()
                .where { (Children.id eq childId) and (Children.parentId eq parentId) and (Children.isActive eq true) }
                .singleOrNull() != null
            
            if (!childExists) {
                return@transaction ListResponse(false, emptyList(), 0)
            }
            
            val attempts = (QuizAttempts innerJoin Words)
                .selectAll()
                .where { QuizAttempts.childId eq childId }
                .orderBy(QuizAttempts.attemptedAt, SortOrder.DESC)
                .limit(limit)
                .map { row ->
                    RecentQuizAttemptDTO(
                        wordFa = row[Words.wordFa],
                        wordEn = row[Words.wordEn],
                        isCorrect = row[QuizAttempts.isCorrect],
                        attemptedAt = row[QuizAttempts.attemptedAt].format(dateFormatter)
                    )
                }
            
            ListResponse(true, attempts, attempts.size)
        }
    }
    
    /**
     * Get word progress for a child
     * دریافت پیشرفت کلمه برای فرزند
     */
    fun getWordProgress(childId: Int, wordId: Int): ApiResponse<WordProgressDTO> {
        return transaction {
            val progress = ChildProgress.selectAll()
                .where { (ChildProgress.childId eq childId) and (ChildProgress.wordId eq wordId) }
                .singleOrNull()
            
            if (progress != null) {
                val correct = progress[ChildProgress.correctAttempts]
                val total = progress[ChildProgress.totalAttempts]
                
                ApiResponse(true, "موفق", WordProgressDTO(
                    wordId = wordId,
                    correctAttempts = correct,
                    totalAttempts = total,
                    isLearned = progress[ChildProgress.isLearned],
                    accuracyPercent = if (total > 0) (correct.toDouble() / total * 100) else 0.0
                ))
            } else {
                ApiResponse(true, "بدون پیشرفت", WordProgressDTO(
                    wordId = wordId,
                    correctAttempts = 0,
                    totalAttempts = 0,
                    isLearned = false,
                    accuracyPercent = 0.0
                ))
            }
        }
    }
}
