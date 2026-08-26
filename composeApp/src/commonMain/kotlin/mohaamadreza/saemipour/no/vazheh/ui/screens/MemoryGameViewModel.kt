package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mohaamadreza.saemipour.no.vazheh.data.ContentRepository
import mohaamadreza.saemipour.no.vazheh.data.ProgressRepository
import mohaamadreza.saemipour.no.vazheh.data.WordDTO

/** One rung of the memory-game board ladder - نردبان ابعاد بازی حافظه */
data class MemoryDimension(
    val rows: Int,
    val columns: Int,
    val pairCount: Int,
    /** Countdown budget (seconds) for a game played at this dimension. */
    val timeLimitSeconds: Int
)

/**
 * Client-side mirror of the server dimension ladder (MemoryProgressService on the server).
 * Keep both in sync. Every new child starts at index 0.
 */
val MEMORY_DIMENSION_LADDER = listOf(
    MemoryDimension(2, 2, 2, 30), // index 0 → 2×2
    MemoryDimension(2, 3, 3, 45), // index 1 → 2×3
    MemoryDimension(3, 4, 6, 60), // index 2 → 3×4 (today's default board)
    MemoryDimension(4, 4, 8, 90)  // index 3 → 4×4
)

data class MemoryCard(
    val id: Int,
    val word: WordDTO,
    val pairId: Int,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)

class MemoryGameViewModel(
    private val contentRepository: ContentRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    var cards = mutableStateListOf<MemoryCard>()
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isWin by mutableStateOf(false)
        private set

    var moves by mutableStateOf(0)
        private set

    var matchedPairs by mutableStateOf(0)
        private set

    var totalPairs by mutableStateOf(0)
        private set

    /** Current board dimension from MEMORY_DIMENSION_LADDER (authoritative value comes from the server). */
    var dimensionIndex by mutableStateOf(0)
        private set

    /** Successful games completed at the current dimension. */
    var successfulGames by mutableStateOf(0)
        private set

    /** Child whose progression is tracked; null for guests (no persistence). */
    private var childId: Int? = null

    /** Seconds left on the current game's countdown. */
    var timeRemainingSeconds by mutableStateOf(0)
        private set

    /** Whether the countdown coroutine is currently active. */
    var isTimerRunning by mutableStateOf(false)
        private set

    /** True once the countdown reached zero before all pairs were matched. Final until the next board. */
    var hasTimedOut by mutableStateOf(false)
        private set

    /** The single active countdown job — never two at once (see [startTimer]). */
    private var timerJob: Job? = null

    private var firstFlippedCard: MemoryCard? = null
    private var isProcessing by mutableStateOf(false)
    
    var currentMatchedWord by mutableStateOf<WordDTO?>(null)
        private set

    private var lastMode: LoadMode = LoadMode.None

    fun loadWords(categoryId: Int, childId: Int? = null) {
        lastMode = LoadMode.Single(categoryId)
        this.childId = childId
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            if (childId != null) {
                fetchProgression(childId)
            } else {
                useFallbackDimension()
            }

            loadSingleBoard(
                categoryId,
                MEMORY_DIMENSION_LADDER[dimensionIndex].pairCount
            )
        }
    }

    /**
     * Load words from all categories combined for a mixed memory game
     * بارگذاری کلمات از همه دسته‌بندی‌ها برای بازی حافظه ترکیبی
     */
    fun loadCombinedWords(childId: Int? = null) {
        lastMode = LoadMode.Combined
        this.childId = childId
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            if (childId != null) {
                fetchProgression(childId)
            } else {
                useFallbackDimension()
            }

            loadCombinedBoard(MEMORY_DIMENSION_LADDER[dimensionIndex].pairCount)
        }
    }

    /**
     * Fetch the authoritative progression for a child from the server - دریافت پیشرفت رسمی از سرور
     * On any failure (network error, missing token, rejected response) fall back to dimension 0
     * so the game always stays playable.
     */
    private suspend fun fetchProgression(childId: Int) {
        progressRepository.getMemoryProgress(childId).fold(
            onSuccess = { response ->
                val progress = response.data
                if (response.success && progress != null) {
                    dimensionIndex = progress.dimensionIndex.coerceIn(0, MEMORY_DIMENSION_LADDER.lastIndex)
                    successfulGames = progress.successfulGames.coerceIn(0, 3)
                } else {
                    useFallbackDimension()
                }
            },
            onFailure = { useFallbackDimension() }
        )
    }

    private fun useFallbackDimension() {
        dimensionIndex = 0
        successfulGames = 0
    }

    /**
     * Starts a fresh countdown for the current dimension.
     *
     * Cancels any previous job first, so at most one timer is ever active. It also clears
     * stale timeout state so an expired game can never leak into a freshly loaded board.
     */
    private fun startTimer() {
        stopTimer()

        val limit = MEMORY_DIMENSION_LADDER[dimensionIndex].timeLimitSeconds
        hasTimedOut = false
        timeRemainingSeconds = limit
        isTimerRunning = true

        timerJob = viewModelScope.launch {
            while (timeRemainingSeconds > 0) {
                delay(1000)
                timeRemainingSeconds--
            }

            // The countdown reached zero on its own. A completed game never gets here: it
            // calls [stopTimer] the moment the final pair is matched, which cancels this
            // coroutine before the post-loop code can run.
            stopTimer()
            if (matchedPairs < totalPairs) {
                hasTimedOut = true
            }
        }
    }

    /** Cancels the active countdown, if any. Never clears [hasTimedOut] — an expired game stays expired until the next board. */
    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        isTimerRunning = false
    }

    /** Load a single-category board without touching progression state. */
    private suspend fun loadSingleBoard(categoryId: Int, pairCount: Int) {
        isLoading = true
        errorMessage = null

        contentRepository.getWordsByCategory(categoryId).fold(
            onSuccess = { response ->
                if (response.success && response.data.isNotEmpty()) {
                    val words = response.data.shuffled().take(pairCount)
                    setupGame(words)
                } else {
                    errorMessage = "کلمه‌ای در این دسته‌بندی یافت نشد"
                }
                isLoading = false
            },
            onFailure = { exception ->
                errorMessage = "خطا در بارگذاری: ${exception.message ?: "خطای نامشخص"}"
                isLoading = false
            }
        )
    }

    /** Load a combined (cross-category) board without touching progression state. */
    private suspend fun loadCombinedBoard(pairCount: Int) {
        isLoading = true
        errorMessage = null

        contentRepository.getAllCategories().fold(
            onSuccess = { categoriesResponse ->
                val categories = categoriesResponse.data
                if (!categoriesResponse.success || categories.isEmpty()) {
                    errorMessage = "دسته‌بندی‌ای برای بازی ترکیبی یافت نشد"
                    isLoading = false
                    return@fold
                }

                val allWords = mutableListOf<WordDTO>()
                for (category in categories) {
                    contentRepository.getWordsByCategory(category.id).onSuccess { resp ->
                        if (resp.success) allWords.addAll(resp.data)
                    }
                }

                if (allWords.isEmpty()) {
                    errorMessage = "کلمه‌ای برای بازی ترکیبی یافت نشد"
                } else {
                    val words = allWords.distinctBy { it.id }.shuffled().take(pairCount)
                    setupGame(words)
                }
                isLoading = false
            },
            onFailure = { exception ->
                errorMessage = "خطا در بارگذاری: ${exception.message ?: "خطای نامشخص"}"
                isLoading = false
            }
        )
    }

    private sealed class LoadMode {
        data object None : LoadMode()
        data class Single(val categoryId: Int) : LoadMode()
        data object Combined : LoadMode()
    }

    private fun setupGame(words: List<WordDTO>) {
        cards.clear()
        isWin = false
        moves = 0
        matchedPairs = 0
        totalPairs = words.size
        firstFlippedCard = null
        isProcessing = false
        currentMatchedWord = null

        var cardId = 0
        val newCards = mutableListOf<MemoryCard>()
        
        words.forEachIndexed { index, word ->
            newCards.add(MemoryCard(id = cardId++, word = word, pairId = index))
            newCards.add(MemoryCard(id = cardId++, word = word, pairId = index))
        }

        cards.addAll(newCards.shuffled())

        // The board is playable now — begin the countdown for this dimension. This single
        // call site covers every new board: initial load, retry after error, and reset.
        startTimer()
    }

    fun onCardClick(card: MemoryCard) {
        // A timed-out game is final: no further card input can change its outcome.
        if (hasTimedOut || isProcessing || card.isFlipped || card.isMatched) return

        val cardIndex = cards.indexOfFirst { it.id == card.id }
        if (cardIndex == -1) return

        cards[cardIndex] = cards[cardIndex].copy(isFlipped = true)

        val firstCard = firstFlippedCard
        if (firstCard == null) {
            firstFlippedCard = cards[cardIndex]
        } else {
            moves++
            isProcessing = true

            viewModelScope.launch {
                delay(800)

                // The countdown may have expired while this pair was being evaluated; a
                // timeout is final — never count a match or declare a win after it.
                if (hasTimedOut) {
                    firstFlippedCard = null
                    isProcessing = false
                    return@launch
                }

                if (firstCard.pairId == cards[cardIndex].pairId) {
                    val firstIndex = cards.indexOfFirst { it.id == firstCard.id }
                    val secondIndex = cardIndex

                    if (firstIndex != -1) {
                        cards[firstIndex] = cards[firstIndex].copy(isMatched = true)
                    }
                    cards[secondIndex] = cards[secondIndex].copy(isMatched = true)

                    matchedPairs++
                    currentMatchedWord = cards[secondIndex].word

                    if (matchedPairs == totalPairs) {
                        // Final pair matched — freeze the countdown immediately so a timeout
                        // can never be declared for an already completed game.
                        stopTimer()
                        delay(500)
                        // Ignore a reset that happened during the delay. Completion is handled
                        // inline in this coroutine (no nested launch) and only celebrates once
                        // the server confirms the dimension was finished.
                        if (matchedPairs == totalPairs) {
                            completeSuccessfulGame()
                        }
                    }
                } else {
                    val firstIndex = cards.indexOfFirst { it.id == firstCard.id }
                    val secondIndex = cardIndex

                    if (firstIndex != -1) {
                        cards[firstIndex] = cards[firstIndex].copy(isFlipped = false)
                    }
                    cards[secondIndex] = cards[secondIndex].copy(isFlipped = false)
                }

                firstFlippedCard = null
                isProcessing = false
            }
        }
    }

    /**
     * Handles one successfully completed individual game - ثبت و بررسی برد موفق بازی حافظه
     *
     * Guest (no child): keeps the existing per-game celebration; nothing is persisted.
     * Logged-in child: submits through ProgressRepository and lets the AUTHORITATIVE server
     * response decide everything — local progression state is taken from it, never recomputed.
     * The dimension-completion celebration fires only when that response shows this completion
     * finished the current dimension (dimension advanced, or third success at the final one).
     * Network failures leave ProgressRepository's pending-retry mechanism in charge: no state
     * change and no celebration until the server confirms.
     */
    private suspend fun completeSuccessfulGame() {
        val currentChildId = childId
        if (currentChildId == null) {
            // Guest — unchanged behavior: celebrate each successful game, nothing persisted.
            isWin = true
            return
        }

        val completedDimension = dimensionIndex
        val gamesBefore = successfulGames

        progressRepository
            .recordMemoryGameComplete(
                childId = currentChildId,
                dimensionIndex = completedDimension,
                successfulGamesBefore = gamesBefore
            )
            .fold(
                onSuccess = { response ->
                    val progress = response.data
                    if (response.success && progress != null) {
                        val returnedDimension = progress.dimensionIndex.coerceIn(0, MEMORY_DIMENSION_LADDER.lastIndex)
                        dimensionIndex = returnedDimension
                        successfulGames = progress.successfulGames.coerceIn(0, 3)

                        // Dimension completion is detected from the authoritative response —
                        // never from local counters:
                        // - advancement happened (returned index greater than completed one), or
                        // - final dimension: this was exactly its third success (the server caps
                        //   the counter there at 3, so [gamesBefore] == 2 identifies it).
                        val finishedCurrentDimension =
                            returnedDimension > completedDimension ||
                                (completedDimension == MEMORY_DIMENSION_LADDER.lastIndex &&
                                    gamesBefore == 2 && progress.successfulGames >= 3)

                        if (finishedCurrentDimension) {
                            isWin = true
                        }
                    }
                },
                onFailure = { /* pending retry is owned by ProgressRepository — no advancement assumed */ }
            )
    }

    fun clearMatchedWord() {
        currentMatchedWord = null
    }

    fun resetGame() {
        // Cancel any running countdown first so rebuilding/loading consumes no game time;
        // setupGame starts a fresh timer once the new board is playable.
        stopTimer()

        // Restart at the CURRENT dimension — progression is never reset by a restart.
        val pairCount = MEMORY_DIMENSION_LADDER[dimensionIndex].pairCount
        when (val mode = lastMode) {
            is LoadMode.Combined -> viewModelScope.launch { loadCombinedBoard(pairCount) }
            is LoadMode.Single -> {
                if (totalPairs != pairCount) {
                    // Dimension advanced after a win — fetch fresh words at the new size.
                    viewModelScope.launch { loadSingleBoard(mode.categoryId, pairCount) }
                } else {
                    val currentWords = cards.map { it.word }.distinctBy { it.id }
                    if (currentWords.isNotEmpty()) {
                        setupGame(currentWords)
                    }
                }
            }
            LoadMode.None -> { /* nothing to reset */ }
        }
    }

    fun retry() {
        stopTimer()
        errorMessage = null
        val pairCount = MEMORY_DIMENSION_LADDER[dimensionIndex].pairCount
        when (val mode = lastMode) {
            is LoadMode.Combined -> viewModelScope.launch { loadCombinedBoard(pairCount) }
            is LoadMode.Single -> viewModelScope.launch { loadSingleBoard(mode.categoryId, pairCount) }
            LoadMode.None -> { /* nothing to retry */ }
        }
    }
}
