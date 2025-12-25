package mohaamadreza.saemipour.no.vazheh.services

import mohaamadreza.saemipour.no.vazheh.database.tables.*
import mohaamadreza.saemipour.no.vazheh.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

/**
 * Quiz Service - سرویس آزمون
 * صوت کلمه پخش می‌شود و فرزند کلمه درست را انتخاب می‌کند
 */
object QuizService {
    
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private const val OPTIONS_COUNT = 4 // تعداد گزینه‌ها در هر سوال
    private const val LEARNED_THRESHOLD = 3 // تعداد پاسخ درست برای یادگیری
    
    /**
     * Generate quiz question for a category
     * تولید سوال آزمون برای یک دسته‌بندی
     */
    fun generateQuizQuestion(categoryId: Int, childId: Int?): ApiResponse<QuizQuestionDTO> {
        return transaction {
            // Get all active words in category
            val allWords = Words.selectAll()
                .where { (Words.categoryId eq categoryId) and (Words.isActive eq true) }
                .toList()
            
            if (allWords.size < OPTIONS_COUNT) {
                return@transaction ApiResponse(false, "تعداد کلمات این دسته‌بندی کافی نیست (حداقل $OPTIONS_COUNT کلمه نیاز است)", null)
            }
            
            // If childId provided, prioritize words not yet learned
            val targetWord = if (childId != null) {
                val learnedWordIds = ChildProgress.selectAll()
                    .where { (ChildProgress.childId eq childId) and (ChildProgress.isLearned eq true) }
                    .map { it[ChildProgress.wordId].value }
                
                val unlearnedWords = allWords.filter { it[Words.id].value !in learnedWordIds }
                unlearnedWords.randomOrNull() ?: allWords.random()
            } else {
                allWords.random()
            }
            
            val targetWordId = targetWord[Words.id].value
            
            // Select random wrong options
            val wrongOptions = allWords
                .filter { it[Words.id].value != targetWordId }
                .shuffled()
                .take(OPTIONS_COUNT - 1)
            
            // Build options (correct + wrong, shuffled)
            val options = (listOf(targetWord) + wrongOptions).shuffled().map { row ->
                QuizOptionDTO(
                    wordId = row[Words.id].value,
                    wordFa = row[Words.wordFa],
                    wordEn = row[Words.wordEn],
                    imageUrl = row[Words.imageUrl]
                )
            }
            
            val question = QuizQuestionDTO(
                wordId = targetWordId,
                audioUrl = targetWord[Words.audioUrl] ?: "",
                options = options
            )
            
            ApiResponse(true, "موفق", question)
        }
    }
    
    /**
     * Generate multiple quiz questions for a category
     * تولید چند سوال آزمون
     */
    fun generateQuizQuestions(categoryId: Int, childId: Int?, count: Int = 5): ListResponse<QuizQuestionDTO> {
        return transaction {
            val allWords = Words.selectAll()
                .where { (Words.categoryId eq categoryId) and (Words.isActive eq true) }
                .toList()
            
            if (allWords.size < OPTIONS_COUNT) {
                return@transaction ListResponse(false, emptyList(), 0)
            }
            
            // Get words to quiz on
            val wordsToQuiz = if (childId != null) {
                val learnedWordIds = ChildProgress.selectAll()
                    .where { (ChildProgress.childId eq childId) and (ChildProgress.isLearned eq true) }
                    .map { it[ChildProgress.wordId].value }
                
                val unlearnedWords = allWords.filter { it[Words.id].value !in learnedWordIds }
                if (unlearnedWords.size >= count) {
                    unlearnedWords.shuffled().take(count)
                } else {
                    (unlearnedWords + allWords.shuffled()).distinctBy { it[Words.id] }.take(count)
                }
            } else {
                allWords.shuffled().take(count)
            }
            
            val questions = wordsToQuiz.map { targetWord ->
                val targetWordId = targetWord[Words.id].value
                
                val wrongOptions = allWords
                    .filter { it[Words.id].value != targetWordId }
                    .shuffled()
                    .take(OPTIONS_COUNT - 1)
                
                val options = (listOf(targetWord) + wrongOptions).shuffled().map { row ->
                    QuizOptionDTO(
                        wordId = row[Words.id].value,
                        wordFa = row[Words.wordFa],
                        wordEn = row[Words.wordEn],
                        imageUrl = row[Words.imageUrl]
                    )
                }
                
                QuizQuestionDTO(
                    wordId = targetWordId,
                    audioUrl = targetWord[Words.audioUrl] ?: "",
                    options = options
                )
            }
            
            ListResponse(true, questions, questions.size)
        }
    }
    
    /**
     * Submit quiz answer
     * ثبت پاسخ آزمون
     */
    fun submitQuizAnswer(request: SubmitQuizRequest): ApiResponse<QuizResultDTO> {
        return transaction {
            // Get correct word
            val correctWord = Words.selectAll()
                .where { Words.id eq request.wordId }
                .singleOrNull()
                ?: return@transaction ApiResponse(false, "کلمه یافت نشد", null)
            
            val isCorrect = request.wordId == request.selectedWordId
            val now = LocalDateTime.now()
            
            // Record attempt
            QuizAttempts.insert {
                it[childId] = request.childId
                it[wordId] = request.wordId
                it[QuizAttempts.isCorrect] = isCorrect
                it[responseTimeMs] = request.responseTimeMs
                it[attemptedAt] = now
            }
            
            // Update progress
            updateChildProgress(request.childId, request.wordId, isCorrect, now)
            
            val result = QuizResultDTO(
                isCorrect = isCorrect,
                correctWordId = request.wordId,
                correctWordFa = correctWord[Words.wordFa],
                message = if (isCorrect) "آفرین! پاسخ درست بود 🎉" else "اشتباه بود. پاسخ درست: ${correctWord[Words.wordFa]}"
            )
            
            ApiResponse(true, if (isCorrect) "پاسخ درست!" else "پاسخ اشتباه", result)
        }
    }
    
    /**
     * Update child progress for a word
     */
    private fun updateChildProgress(childId: Int, wordId: Int, isCorrect: Boolean, now: LocalDateTime) {
        val existing = ChildProgress.selectAll()
            .where { (ChildProgress.childId eq childId) and (ChildProgress.wordId eq wordId) }
            .singleOrNull()
        
        if (existing != null) {
            val newCorrect = existing[ChildProgress.correctAttempts] + if (isCorrect) 1 else 0
            val newTotal = existing[ChildProgress.totalAttempts] + 1
            val isLearned = newCorrect >= LEARNED_THRESHOLD
            
            ChildProgress.update({ (ChildProgress.childId eq childId) and (ChildProgress.wordId eq wordId) }) {
                it[correctAttempts] = newCorrect
                it[totalAttempts] = newTotal
                it[ChildProgress.isLearned] = isLearned
                it[lastAttemptAt] = now
                if (isLearned && existing[ChildProgress.learnedAt] == null) {
                    it[learnedAt] = now
                }
                it[updatedAt] = now
            }
        } else {
            ChildProgress.insert {
                it[ChildProgress.childId] = childId
                it[ChildProgress.wordId] = wordId
                it[correctAttempts] = if (isCorrect) 1 else 0
                it[totalAttempts] = 1
                it[isLearned] = false
                it[lastAttemptAt] = now
                it[createdAt] = now
                it[updatedAt] = now
            }
        }
    }
}






