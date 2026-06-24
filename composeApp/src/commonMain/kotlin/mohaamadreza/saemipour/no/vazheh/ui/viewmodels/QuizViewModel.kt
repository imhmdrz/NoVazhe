package mohaamadreza.saemipour.no.vazheh.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mohaamadreza.saemipour.no.vazheh.AppLogger
import mohaamadreza.saemipour.no.vazheh.data.ContentRepository
import mohaamadreza.saemipour.no.vazheh.data.QuizQuestionDTO
import mohaamadreza.saemipour.no.vazheh.data.QuizRepository
import mohaamadreza.saemipour.no.vazheh.data.SubmitQuizRequest
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.QuizUiState

/**
 * Quiz ViewModel
 * مدیریت آزمون - صوت کلمه پخش می‌شود و فرزند کلمه درست را انتخاب می‌کند
 */
class QuizViewModel(
    private val quizRepository: QuizRepository,
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState = _uiState.asStateFlow()

    // ==================== Quiz Session ====================

    /**
     * Start a new quiz session
     * شروع یک جلسه آزمون جدید
     *
     * @param categoryId شناسه دسته‌بندی
     * @param childId شناسه فرزند
     * @param questionCount تعداد سوالات (پیش‌فرض: 5)
     */
    fun startQuiz(categoryId: Int, childId: Int?, questionCount: Int = 5) {
        _uiState.update {
            it.copy(
                childId = childId,
                categoryId = categoryId,
                questions = emptyList(),
                currentQuestionIndex = 0,
                selectedOptionId = null,
                isAnswerSubmitted = false,
                lastAnswerCorrect = null,
                correctWordFa = null,
                isLoadingQuestions = true,
                isQuizCompleted = false,
                totalCorrect = 0,
                totalAnswered = 0,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            quizRepository.getQuizQuestions(categoryId, childId, questionCount).fold(
                onSuccess = { response ->
                    if (response.success && response.data.isNotEmpty()) {
                        _uiState.update {
                            it.copy(
                                questions = response.data,
                                isLoadingQuestions = false
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoadingQuestions = false,
                                errorMessage = "سوالی برای این دسته‌بندی یافت نشد"
                            )
                        }
                    }
                },
                onFailure = { exception ->
                    AppLogger.d("QuizViewModel", "Error loading questions: ${exception.message}")
                    _uiState.update {
                        it.copy(
                            isLoadingQuestions = false,
                            errorMessage = "خطا در بارگذاری سوالات: ${exception.message ?: "خطای نامشخص"}"
                        )
                    }
                }
            )
        }
    }

    /**
     * Start a combined quiz session pulling questions from all categories
     * شروع یک جلسه آزمون ترکیبی - سوالات از همه دسته‌بندی‌ها
     *
     * @param childId شناسه فرزند
     * @param questionCount تعداد سوالات (پیش‌فرض: 5)
     */
    fun startCombinedQuiz(childId: Int?, questionCount: Int = 5) {
        _uiState.update {
            it.copy(
                childId = childId,
                categoryId = COMBINED_CATEGORY_ID,
                questions = emptyList(),
                currentQuestionIndex = 0,
                selectedOptionId = null,
                isAnswerSubmitted = false,
                lastAnswerCorrect = null,
                correctWordFa = null,
                isLoadingQuestions = true,
                isQuizCompleted = false,
                totalCorrect = 0,
                totalAnswered = 0,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            contentRepository.getAllCategories().fold(
                onSuccess = { categoriesResponse ->
                    val categories = categoriesResponse.data
                    if (!categoriesResponse.success || categories.isEmpty()) {
                        _uiState.update {
                            it.copy(
                                isLoadingQuestions = false,
                                errorMessage = "دسته‌بندی‌ای برای آزمون ترکیبی یافت نشد"
                            )
                        }
                        return@fold
                    }

                    val perCategory = (questionCount / categories.size).coerceAtLeast(1)
                    val collected = mutableListOf<QuizQuestionDTO>()

                    for (category in categories) {
                        val result = quizRepository.getQuizQuestions(
                            categoryId = category.id,
                            childId = childId,
                            count = perCategory
                        )
                        result.onSuccess { listResp ->
                            if (listResp.success) collected.addAll(listResp.data)
                        }
                    }

                    val finalQuestions = collected.shuffled().take(questionCount)

                    if (finalQuestions.isEmpty()) {
                        _uiState.update {
                            it.copy(
                                isLoadingQuestions = false,
                                errorMessage = "سوالی برای آزمون ترکیبی یافت نشد"
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                questions = finalQuestions,
                                isLoadingQuestions = false
                            )
                        }
                    }
                },
                onFailure = { exception ->
                    AppLogger.d("QuizViewModel", "Error loading categories for combined quiz: ${exception.message}")
                    _uiState.update {
                        it.copy(
                            isLoadingQuestions = false,
                            errorMessage = "خطا در بارگذاری دسته‌بندی‌ها: ${exception.message ?: "خطای نامشخص"}"
                        )
                    }
                }
            )
        }
    }

    /**
     * Load a single question (for continuous quiz mode)
     * بارگذاری یک سوال
     */
    fun loadSingleQuestion(categoryId: Int, childId: Int) {
        _uiState.update {
            it.copy(
                childId = childId,
                categoryId = categoryId,
                isLoadingQuestions = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            quizRepository.getQuizQuestion(categoryId, childId).fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        _uiState.update {
                            it.copy(
                                questions = listOf(response.data ?: return@launch),
                                currentQuestionIndex = 0,
                                selectedOptionId = null,
                                isAnswerSubmitted = false,
                                lastAnswerCorrect = null,
                                correctWordFa = null,
                                isLoadingQuestions = false
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoadingQuestions = false,
                                errorMessage = response.message
                            )
                        }
                    }
                },
                onFailure = { exception ->
                    AppLogger.d("QuizViewModel", "Error loading question: ${exception.message}")
                    _uiState.update {
                        it.copy(
                            isLoadingQuestions = false,
                            errorMessage = "خطا در بارگذاری سوال: ${exception.message ?: "خطای نامشخص"}"
                        )
                    }
                }
            )
        }
    }

    // ==================== Answer Selection ====================

    /**
     * Select an answer option
     * انتخاب یک گزینه
     */
    fun selectOption(optionWordId: Int) {
        if (_uiState.value.isAnswerSubmitted) return
        
        _uiState.update { it.copy(selectedOptionId = optionWordId) }
    }

    /**
     * Submit the selected answer
     * ثبت پاسخ انتخاب شده
     */
    fun submitAnswer() {
        val currentState = _uiState.value
        val currentQuestion = currentState.currentQuestion ?: return
        val selectedOptionId = currentState.selectedOptionId ?: return
        val childId = currentState.childId

        // Correctness is known client-side (the question carries the correct wordId), so the
        // result shows instantly and the game works for guests without any server round-trip.
        val isCorrect = selectedOptionId == currentQuestion.wordId
        val correctWordFa = currentQuestion.options
            .firstOrNull { it.wordId == currentQuestion.wordId }?.wordFa

        _uiState.update {
            it.copy(
                isSubmittingAnswer = false,
                isAnswerSubmitted = true,
                lastAnswerCorrect = isCorrect,
                correctWordFa = correctWordFa,
                totalCorrect = it.totalCorrect + if (isCorrect) 1 else 0,
                totalAnswered = it.totalAnswered + 1
            )
        }

        // Record progress on the server only for a logged-in child; best-effort, never blocks play.
        if (childId != null) {
            viewModelScope.launch {
                val request = SubmitQuizRequest(
                    childId = childId,
                    wordId = currentQuestion.wordId,
                    selectedWordId = selectedOptionId
                )
                quizRepository.submitQuizAnswer(request).onFailure { exception ->
                    AppLogger.d("QuizViewModel", "Error recording quiz progress: ${exception.message}")
                }
            }
        }
    }

    // ==================== Navigation ====================

    /**
     * Go to next question
     * رفتن به سوال بعدی
     */
    fun nextQuestion() {
        val currentState = _uiState.value
        
        if (currentState.hasMoreQuestions) {
            _uiState.update {
                it.copy(
                    currentQuestionIndex = it.currentQuestionIndex + 1,
                    selectedOptionId = null,
                    isAnswerSubmitted = false,
                    lastAnswerCorrect = null,
                    correctWordFa = null
                )
            }
        } else {
            // Quiz completed
            _uiState.update { it.copy(isQuizCompleted = true) }
        }
    }

    /**
     * Finish quiz and show results
     * پایان آزمون و نمایش نتایج
     */
    fun finishQuiz() {
        _uiState.update { it.copy(isQuizCompleted = true) }
    }

    // ==================== Utilities ====================

    /**
     * Clear error message
     * پاک کردن پیام خطا
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Reset quiz to initial state
     * بازنشانی آزمون به حالت اولیه
     */
    fun resetQuiz() {
        _uiState.update { QuizUiState() }
    }

    /**
     * Retry loading questions
     * تلاش مجدد برای بارگذاری سوالات
     */
    fun retry() {
        val categoryId = _uiState.value.categoryId
        val childId = _uiState.value.childId

        // childId may be null for guests — that's fine, the quiz still loads.
        if (categoryId == COMBINED_CATEGORY_ID) {
            startCombinedQuiz(childId)
        } else if (categoryId != null) {
            startQuiz(categoryId, childId)
        }
    }

    companion object {
        /** Sentinel categoryId indicating a combined (cross-category) quiz session */
        const val COMBINED_CATEGORY_ID = -1
    }
}






