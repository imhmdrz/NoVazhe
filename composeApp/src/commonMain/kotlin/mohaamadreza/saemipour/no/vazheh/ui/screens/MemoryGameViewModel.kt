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
import mohaamadreza.saemipour.no.vazheh.data.MemoryProgressDTO
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

    /**
     * Identity of the current board - شناسه‌ی نسخه‌ی فعلی صفحه‌ی بازی
     *
     * Incremented every time a board is replaced (setupGame) or abandoned (reset/retry/entry).
     * Every coroutine that outlives a user interaction (pair resolution, win submission)
     * captures the generation it started on and must re-check it after each suspension point
     * before touching board state. Because card/pair ids restart from 0 on every board, a
     * stale coroutine would otherwise find "valid-looking" cards on the NEW board and corrupt
     * it (unflipping cards, bumping matchedPairs, killing the new timer, or even submitting a
     * phantom completion).
     */
    private var gameGeneration = 0

    /**
     * The currently running win-submission pipeline, if any - ثبت برد در حال انجام
     *
     * Completions are strictly serialized: a new game's submission joins the previous one
     * before reading dimensionIndex/successfulGames, so it always reports against fresh,
     * authoritative state and an older response can never overwrite a newer game's result.
     */
    private var completionInFlight: Job? = null

    fun loadWords(categoryId: Int, childId: Int? = null) {
        gameGeneration++ // entering the game invalidates anything left over from a previous session
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
        gameGeneration++ // entering the game invalidates anything left over from a previous session
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
        // A new board gets a new identity: every coroutine still working for a previous board
        // becomes stale at this exact moment and will bail at its next resume point.
        gameGeneration++
        log("setupGame gen=$gameGeneration pairs=${words.size}")

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
            val generation = gameGeneration

            viewModelScope.launch {
                delay(800)

                // A reset/reload while this pair was being evaluated replaced the board. This
                // coroutine's captured indices and card references belong to the OLD board —
                // touching live state now would corrupt the new one.
                if (generation != gameGeneration) return@launch

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
                        log("final match gen=$generation")

                        // The board is fully matched, so the input lock is released BEFORE the
                        // async completion pipeline. Otherwise this coroutine's tail would
                        // still clear firstFlippedCard/isProcessing AFTER a reset rebuilt the
                        // board — silently eating the first tap of the new game.
                        firstFlippedCard = null
                        isProcessing = false

                        // Cosmetic pause so the player sees the final match flip over before
                        // any celebration. The win itself is already committed at this point:
                        // even if the player resets during the pause, the completion is still
                        // submitted exactly once (only the celebration is generation-gated).
                        delay(500)

                        // Serialize submissions: join the previous game's pipeline so this one
                        // reads progression state that already includes its confirmed result.
                        val previous = completionInFlight
                        completionInFlight = viewModelScope.launch {
                            previous?.join()
                            completeSuccessfulGame(generation)
                        }
                        return@launch
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
     *
     * [generation] identifies the board that earned this completion. The submission itself is
     * unconditional (the game was genuinely completed), but the celebration is only shown if
     * that board is still the current one — a response must never pop a celebration over a
     * newer game the player has already moved on to.
     */
    private suspend fun completeSuccessfulGame(generation: Int) {
        val currentChildId = childId
        if (currentChildId == null) {
            // Guest — unchanged behavior: celebrate each successful game, nothing persisted.
            if (generation == gameGeneration) isWin = true
            return
        }

        // Submissions are serialized by the caller (completionInFlight), so the state read
        // here already includes every earlier game's confirmed server result.
        submitCompletion(currentChildId, generation, dimensionIndex, successfulGames, isResubmission = false)
    }

    /**
     * Submit one completed game and apply the authoritative response - ارسال نتیجه و اعمال پاسخ سرور
     *
     * If the server rejects the submission (e.g. a stored pending win was retried inside the
     * repository and advanced the server past our dimension first, or the server's progress
     * row was wiped), the client re-syncs from the authoritative GET endpoint and resubmits
     * this win exactly once against the fresh state. A second rejection is accepted as final:
     * the win is not recorded (no guessing), and the next progression GET restores the true state.
     */
    private suspend fun submitCompletion(
        childId: Int,
        generation: Int,
        completedDimension: Int,
        gamesBefore: Int,
        isResubmission: Boolean
    ) {
        log("submit gen=$generation dim=$completedDimension gamesBefore=$gamesBefore resubmission=$isResubmission")

        progressRepository
            .recordMemoryGameComplete(
                childId = childId,
                dimensionIndex = completedDimension,
                successfulGamesBefore = gamesBefore
            )
            .fold(
                onSuccess = { response ->
                    val progress = response.data
                    if (response.success && progress != null) {
                        applyProgressResponse(generation, completedDimension, gamesBefore, progress)
                    } else if (!isResubmission) {
                        log("submit rejected gen=$generation — re-syncing before one resubmission")
                        // Re-sync from the authoritative server, then resubmit exactly once.
                        // On GET failure keep the current state and drop the resubmission —
                        // no state is guessed.
                        val fresh = progressRepository.getMemoryProgress(childId).getOrNull()
                            ?.takeIf { it.success }?.data
                        if (fresh != null) {
                            dimensionIndex = fresh.dimensionIndex.coerceIn(0, MEMORY_DIMENSION_LADDER.lastIndex)
                            successfulGames = fresh.successfulGames.coerceIn(0, 3)
                            submitCompletion(childId, generation, dimensionIndex, successfulGames, isResubmission = true)
                        }
                    } else {
                        log("resubmission rejected gen=$generation — win left to the next progression sync")
                    }
                },
                onFailure = { /* pending retry is owned by ProgressRepository — no advancement assumed */ }
            )
    }

    /**
     * Apply the server's authoritative progression after a confirmed completion.
     *
     * dimensionIndex/successfulGames are GLOBAL progression state (not per-board state), so
     * they are synced from every confirmed response, even if the board that earned it has
     * already been replaced. The celebration, however, belongs to the board that earned it:
     * it is shown only when that board is still current. A confirmed game that did NOT finish
     * its dimension instead deals the next board right away (same generation gate), so the
     * player never sits on a fully matched board waiting for a button.
     */
    private fun applyProgressResponse(
        generation: Int,
        completedDimension: Int,
        gamesBefore: Int,
        progress: MemoryProgressDTO
    ) {
        val returnedDimension = progress.dimensionIndex.coerceIn(0, MEMORY_DIMENSION_LADDER.lastIndex)
        dimensionIndex = returnedDimension
        successfulGames = progress.successfulGames.coerceIn(0, 3)
        log("response gen=$generation dim=$returnedDimension games=${progress.successfulGames}")

        // Dimension completion is detected from the authoritative response — never from local
        // counters:
        // - advancement happened (returned index greater than completed one), or
        // - final dimension: this was exactly its third success (the server caps the counter
        //   there at 3, so [gamesBefore] == 2 identifies it).
        val finishedCurrentDimension =
            returnedDimension > completedDimension ||
                (completedDimension == MEMORY_DIMENSION_LADDER.lastIndex &&
                    gamesBefore == 2 && progress.successfulGames >= 3)

        if (finishedCurrentDimension && generation == gameGeneration) {
            isWin = true
            log("dimension completed gen=$generation → celebration")
        } else if (!finishedCurrentDimension && generation == gameGeneration) {
            // Plain successful game (counter advanced, dimension unchanged): no celebration —
            // deal the next board immediately so the player never waits on a fully matched
            // board. resetGame() builds it at the current authoritative dimension and its
            // generation bump retires any stale async work from this board.
            log("game completed gen=$generation → next board at dim=$dimensionIndex")
            resetGame()
        }
    }

    fun clearMatchedWord() {
        currentMatchedWord = null
    }

    fun resetGame() {
        // Abandon the current board immediately: any in-flight pair-resolution or win pipeline
        // becomes stale now (not only once the replacement board is ready), so it can never
        // mutate the next game. A genuinely completed game's submission still finishes — only
        // its celebration is suppressed (see completeSuccessfulGame).
        gameGeneration++
        log("resetGame gen=$gameGeneration")

        // Cancel any running countdown first so rebuilding/loading consumes no game time;
        // setupGame starts a fresh timer once the new board is playable.
        stopTimer()

        // Dismiss any visible celebration now, not only once the new board is ready.
        isWin = false

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
        gameGeneration++ // same invalidation rule as resetGame
        stopTimer()
        errorMessage = null
        val pairCount = MEMORY_DIMENSION_LADDER[dimensionIndex].pairCount
        when (val mode = lastMode) {
            is LoadMode.Combined -> viewModelScope.launch { loadCombinedBoard(pairCount) }
            is LoadMode.Single -> viewModelScope.launch { loadSingleBoard(mode.categoryId, pairCount) }
            LoadMode.None -> { /* nothing to retry */ }
        }
    }

    /**
     * Focused diagnostic logging for the completion/reset/timer ordering - لاگ تشخیصی
     * Temporary: safe to keep (one line per state transition), or strip once the flow is
     * verified on device. Filter logcat by the tag to observe ordering.
     */
    private fun log(message: String) {
        println("MemoryGame[$LOG_TAG] $message")
    }

    private companion object {
        const val LOG_TAG = "flow"
    }
}
