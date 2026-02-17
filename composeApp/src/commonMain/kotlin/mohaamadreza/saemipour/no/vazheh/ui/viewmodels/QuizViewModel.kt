package mohaamadreza.saemipour.no.vazheh.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mohaamadreza.saemipour.no.vazheh.AppLogger
import mohaamadreza.saemipour.no.vazheh.data.QuizRepository
import mohaamadreza.saemipour.no.vazheh.data.SubmitQuizRequest
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.QuizUiState

/**
 * Quiz ViewModel
 * مدیریت آزمون - صوت کلمه پخش می‌شود و فرزند کلمه درست را انتخاب می‌کند
 */
class QuizViewModel(
    private val quizRepository: QuizRepository
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
    fun startQuiz(categoryId: Int, childId: Int, questionCount: Int = 5) {
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
        val childId = currentState.childId ?: return
        val currentQuestion = currentState.currentQuestion ?: return
        val selectedOptionId = currentState.selectedOptionId ?: return

        _uiState.update { it.copy(isSubmittingAnswer = true) }

        viewModelScope.launch {
            val request = SubmitQuizRequest(
                childId = childId,
                wordId = currentQuestion.wordId,
                selectedWordId = selectedOptionId
            )

            quizRepository.submitQuizAnswer(request).fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        val result = response.data ?: return@launch
                        _uiState.update {
                            it.copy(
                                isSubmittingAnswer = false,
                                isAnswerSubmitted = true,
                                lastAnswerCorrect = result.isCorrect,
                                correctWordFa = result.correctWordFa,
                                totalCorrect = it.totalCorrect + if (result.isCorrect) 1 else 0,
                                totalAnswered = it.totalAnswered + 1
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isSubmittingAnswer = false,
                                errorMessage = response.message
                            )
                        }
                    }
                },
                onFailure = { exception ->
                    AppLogger.d("QuizViewModel", "Error submitting answer: ${exception.message}")
                    _uiState.update {
                        it.copy(
                            isSubmittingAnswer = false,
                            errorMessage = "خطا در ثبت پاسخ: ${exception.message ?: "خطای نامشخص"}"
                        )
                    }
                }
            )
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
        
        if (categoryId != null && childId != null) {
            startQuiz(categoryId, childId)
        }
    }
}






