package mohaamadreza.saemipour.no.vazheh.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mohaamadreza.saemipour.no.vazheh.AppLogger
import mohaamadreza.saemipour.no.vazheh.data.CategoryDTO
import mohaamadreza.saemipour.no.vazheh.data.ChildDTO
import mohaamadreza.saemipour.no.vazheh.data.ContentRepository
import mohaamadreza.saemipour.no.vazheh.data.ProgressRepository
import mohaamadreza.saemipour.no.vazheh.data.TokenStorage
import mohaamadreza.saemipour.no.vazheh.data.WordDTO
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.models.ChildUiState

class ChildViewModel(
    private val contentRepository: ContentRepository,
    private val progressRepository: ProgressRepository,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildUiState())
    val uiState = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadSavedTimerDuration()
        loadCategories()
    }

    private fun loadSavedTimerDuration() {
        val savedDuration = tokenStorage.getTimerDuration()
        _uiState.update { 
            it.copy(
                timerDurationMinutes = savedDuration,
                remainingTimeSeconds = savedDuration * 60L
            ) 
        }
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
                        // Refresh per-category/overall progress for the selected child
                        loadProgressStats()
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

    // ==================== Progress (logged-in child only) ====================

    /**
     * Load progress for a single word (attempts, accuracy, learned flag) and show it on the
     * current word. No-op for guests / when no child is selected.
     * پیشرفت کلمه‌ی فعلی برای فرزند
     */
    fun loadWordProgress(wordId: Int) {
        val childId = _uiState.value.selectedChild?.id ?: return
        // Clear stale data while the new word's progress loads
        _uiState.update { it.copy(wordProgress = null) }
        viewModelScope.launch {
            progressRepository.getWordProgress(childId, wordId).onSuccess { response ->
                if (response.success) {
                    _uiState.update { it.copy(wordProgress = response.data) }
                }
            }.onFailure { exception ->
                AppLogger.d("ChildViewModel", "Error loading word progress: ${exception.message}")
            }
        }
    }

    /**
     * Load the child's overall progress stats (learned words, accuracy, per-category).
     * No-op for guests / when no child is selected.
     * آمار کلی پیشرفت فرزند
     */
    fun loadProgressStats() {
        val childId = _uiState.value.selectedChild?.id ?: return
        viewModelScope.launch {
            progressRepository.getChildProgressStats(childId).onSuccess { response ->
                if (response.success) {
                    _uiState.update { it.copy(progressStats = response.data) }
                }
            }.onFailure { exception ->
                AppLogger.d("ChildViewModel", "Error loading progress stats: ${exception.message}")
            }
        }
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

    // ==================== Timer Operations ====================

    /**
     * Set the timer duration in minutes and persist to storage
     * تنظیم مدت زمان تایمر به دقیقه و ذخیره در حافظه
     */
    fun setTimerDuration(minutes: Int) {
        tokenStorage.saveTimerDuration(minutes)
        _uiState.update { 
            it.copy(
                timerDurationMinutes = minutes,
                remainingTimeSeconds = minutes * 60L,
                isTimerFinished = false
            ) 
        }
    }

    /**
     * Start the countdown timer
     * شروع تایمر شمارش معکوس
     */
    fun startTimer() {
        val currentState = _uiState.value
        if (currentState.timerDurationMinutes <= 0) return

        // Initialize remaining time if not set
        if (currentState.remainingTimeSeconds <= 0) {
            _uiState.update { 
                it.copy(remainingTimeSeconds = currentState.timerDurationMinutes * 60L) 
            }
        }

        timerJob?.cancel()
        _uiState.update { it.copy(isTimerRunning = true, isTimerFinished = false) }

        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingTimeSeconds > 0 && _uiState.value.isTimerRunning) {
                delay(1000L)
                _uiState.update { 
                    it.copy(remainingTimeSeconds = it.remainingTimeSeconds - 1) 
                }
            }
            
            // Timer finished
            if (_uiState.value.remainingTimeSeconds <= 0) {
                _uiState.update { 
                    it.copy(
                        isTimerRunning = false, 
                        isTimerFinished = true
                    ) 
                }
            }
        }
    }

    /**
     * Pause the timer
     * توقف موقت تایمر
     */
    fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isTimerRunning = false) }
    }

    /**
     * Stop and reset the timer
     * توقف و بازنشانی تایمر
     */
    fun stopTimer() {
        timerJob?.cancel()
        _uiState.update { 
            it.copy(
                isTimerRunning = false,
                remainingTimeSeconds = it.timerDurationMinutes * 60L,
                isTimerFinished = false
            ) 
        }
    }

    /**
     * Clear the timer finished flag after navigation
     * پاک کردن پرچم پایان تایمر پس از ناوبری
     */
    fun clearTimerFinished() {
        _uiState.update { it.copy(isTimerFinished = false) }
    }

    /**
     * Reset timer to initial state with duration
     * بازنشانی تایمر به حالت اولیه
     */
    fun resetTimerForNewSession() {
        timerJob?.cancel()
        _uiState.update { 
            it.copy(
                remainingTimeSeconds = it.timerDurationMinutes * 60L,
                isTimerRunning = false,
                isTimerFinished = false
            ) 
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
