package mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models

import mohaamadreza.saemipour.no.vazheh.data.CategoryDTO
import mohaamadreza.saemipour.no.vazheh.data.WordDTO

data class ChildUiState(
    val categories: List<CategoryDTO> = emptyList(),
    val words: List<WordDTO> = emptyList(),
    val selectedCategory: CategoryDTO? = null,
    val currentWordIndex: Int = 0,
    val isLoadingCategories: Boolean = false,
    val isLoadingWords: Boolean = false,
    val errorMessage: String? = null
)