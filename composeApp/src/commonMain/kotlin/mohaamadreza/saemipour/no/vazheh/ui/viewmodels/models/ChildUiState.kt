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
    val errorMessage: String? = null
)