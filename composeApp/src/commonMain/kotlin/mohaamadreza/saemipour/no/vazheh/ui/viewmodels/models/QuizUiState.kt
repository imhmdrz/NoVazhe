package mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models

import androidx.compose.runtime.Stable
import mohaamadreza.saemipour.no.vazheh.data.QuizOptionDTO
import mohaamadreza.saemipour.no.vazheh.data.QuizQuestionDTO

@Stable
data class QuizUiState(
    // Current quiz session
    val childId: Int? = null,
    val categoryId: Int? = null,
    
    // Questions
    val questions: List<QuizQuestionDTO> = emptyList(),
    val currentQuestionIndex: Int = 0,
    
    // Current question state
    val selectedOptionId: Int? = null,
    val isAnswerSubmitted: Boolean = false,
    val lastAnswerCorrect: Boolean? = null,
    val correctWordFa: String? = null,
    
    // Loading states
    val isLoadingQuestions: Boolean = false,
    val isSubmittingAnswer: Boolean = false,
    
    // Error
    val errorMessage: String? = null,
    
    // Quiz completion
    val isQuizCompleted: Boolean = false,
    val totalCorrect: Int = 0,
    val totalAnswered: Int = 0
) {
    // Current question helper
    val currentQuestion: QuizQuestionDTO?
        get() = questions.getOrNull(currentQuestionIndex)
    
    // Progress helpers
    val progressText: String
        get() = "${currentQuestionIndex + 1} / ${questions.size}"
    
    val hasMoreQuestions: Boolean
        get() = currentQuestionIndex < questions.size - 1
    
    val canSubmitAnswer: Boolean
        get() = selectedOptionId != null && !isAnswerSubmitted && !isSubmittingAnswer
    
    val canGoToNextQuestion: Boolean
        get() = isAnswerSubmitted && hasMoreQuestions
    
    // Score calculation
    val scorePercent: Int
        get() = if (totalAnswered > 0) (totalCorrect * 100) / totalAnswered else 0
    
    val scoreText: String
        get() = "$totalCorrect از $totalAnswered"
}

// ==================== Quiz States ====================

@Stable
sealed class QuizSessionState {
    data object Idle : QuizSessionState()
    data object Loading : QuizSessionState()
    data class Ready(val questionCount: Int) : QuizSessionState()
    data class InProgress(val currentIndex: Int, val total: Int) : QuizSessionState()
    data class Completed(val correct: Int, val total: Int) : QuizSessionState()
    data class Error(val message: String) : QuizSessionState()
}






