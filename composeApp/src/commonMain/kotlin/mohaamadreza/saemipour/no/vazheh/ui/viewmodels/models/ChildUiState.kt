package mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models

import androidx.compose.runtime.Stable
import mohaamadreza.saemipour.no.vazheh.data.CategoryDTO
import mohaamadreza.saemipour.no.vazheh.data.CategoryProgressDTO
import mohaamadreza.saemipour.no.vazheh.data.ChildDTO
import mohaamadreza.saemipour.no.vazheh.data.ChildProgressStatsDTO
import mohaamadreza.saemipour.no.vazheh.data.WordDTO
import mohaamadreza.saemipour.no.vazheh.data.WordProgressDTO

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

    // Progress (only when a logged-in parent's child is selected)
    val wordProgress: WordProgressDTO? = null,
    val progressStats: ChildProgressStatsDTO? = null,

    // Timer settings
    val timerDurationMinutes: Int = 10, // Default 10 minutes
    val remainingTimeSeconds: Long = 0,
    val isTimerRunning: Boolean = false,
    val isTimerFinished: Boolean = false
) {
    /** True when progress can be tracked: a (logged-in) child is selected. */
    val hasChildId: Boolean
        get() = selectedChild != null

    /** Overall progress for the currently selected category, if loaded. */
    val selectedCategoryProgress: CategoryProgressDTO?
        get() = progressStats?.progressByCategory?.firstOrNull { it.categoryId == selectedCategory?.id }

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