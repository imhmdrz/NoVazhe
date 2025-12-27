package mohaamadreza.saemipour.no.vazheh.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mohaamadreza.saemipour.no.vazheh.AppLogger
import mohaamadreza.saemipour.no.vazheh.data.CategoryDTO
import mohaamadreza.saemipour.no.vazheh.data.ChildDTO
import mohaamadreza.saemipour.no.vazheh.data.ContentRepository
import mohaamadreza.saemipour.no.vazheh.data.WordDTO
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.ChildUiState

class ChildViewModel(
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        _uiState.update { it.copy(isLoadingCategories = true, errorMessage = null) }

        viewModelScope.launch {
            contentRepository.getAllCategories().fold(
                onSuccess = { response ->
                    if (response.success) {
                        _uiState.update {
                            it.copy(
                                categories = response.data,
                                isLoadingCategories = false
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoadingCategories = false,
                                errorMessage = "خطا در بارگذاری دسته‌بندی‌ها"
                            )
                        }
                    }
                },
                onFailure = { exception ->
                    AppLogger.d("ChildViewModel", "Error loading categories: ${exception.message}")
                    _uiState.update {
                        it.copy(
                            isLoadingCategories = false,
                            errorMessage = "خطا در اتصال: ${exception.message ?: "خطای نامشخص"}"
                        )
                    }
                }
            )
        }
    }

    fun onCategorySelected(category: CategoryDTO) {
        _uiState.update {
            it.copy(
                selectedCategory = category,
                currentWordIndex = 0,
                isLoadingWords = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            contentRepository.getWordsByCategory(category.id).fold(
                onSuccess = { response ->
                    if (response.success) {
                        _uiState.update {
                            it.copy(
                                words = response.data,
                                isLoadingWords = false
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoadingWords = false,
                                errorMessage = "خطا در بارگذاری کلمات"
                            )
                        }
                    }
                },
                onFailure = { exception ->
                    AppLogger.d("ChildViewModel", "Error loading words: ${exception.message}")
                    _uiState.update {
                        it.copy(
                            isLoadingWords = false,
                            errorMessage = "خطا در اتصال: ${exception.message ?: "خطای نامشخص"}"
                        )
                    }
                }
            )
        }
    }

    fun nextWord() {
        val currentState = _uiState.value
        if (currentState.currentWordIndex < currentState.words.size - 1) {
            _uiState.update { it.copy(currentWordIndex = it.currentWordIndex + 1) }
        }
    }

    fun previousWord() {
        val currentState = _uiState.value
        if (currentState.currentWordIndex > 0) {
            _uiState.update { it.copy(currentWordIndex = it.currentWordIndex - 1) }
        }
    }

    fun getCurrentWord(): WordDTO? {
        val state = _uiState.value
        return state.words.getOrNull(state.currentWordIndex)
    }

    fun canGoNext(): Boolean {
        val state = _uiState.value
        return state.currentWordIndex < state.words.size - 1
    }

    fun canGoPrevious(): Boolean {
        return _uiState.value.currentWordIndex > 0
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun retry() {
        loadCategories()
    }

    fun setSelectedChild(child: ChildDTO) {
        _uiState.update { it.copy(selectedChild = child) }
    }

    fun clearSelectedChild() {
        _uiState.update { it.copy(selectedChild = null) }
    }
}
