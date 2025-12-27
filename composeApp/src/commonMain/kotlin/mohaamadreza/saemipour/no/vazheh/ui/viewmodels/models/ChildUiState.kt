package mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models

import androidx.compose.runtime.Stable
import mohaamadreza.saemipour.no.vazheh.data.CategoryDTO
import mohaamadreza.saemipour.no.vazheh.data.ChildDTO
import mohaamadreza.saemipour.no.vazheh.data.WordDTO

@Stable
data class ChildUiState(
    val selectedChild: ChildDTO? = null,
    val categories: List<CategoryDTO> = emptyList(),
    val words: List<WordDTO> = emptyList(),
    val selectedCategory: CategoryDTO? = null,
    val currentWordIndex: Int = 0,
    val isLoadingCategories: Boolean = false,
    val isLoadingWords: Boolean = false,
    val errorMessage: String? = null,
    
    // Timer settings
    val timerDurationMinutes: Int = 10, // Default 10 minutes
    val remainingTimeSeconds: Long = 0,
    val isTimerRunning: Boolean = false,
    val isTimerFinished: Boolean = false
) {
    // Timer helpers
    val remainingMinutes: Int
        get() = (remainingTimeSeconds / 60).toInt()
    
    val remainingSeconds: Int
        get() = (remainingTimeSeconds % 60).toInt()
    
    val formattedRemainingTime: String
        get() = "${remainingMinutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}"
    
    val isTimerEnabled: Boolean
        get() = timerDurationMinutes > 0
}