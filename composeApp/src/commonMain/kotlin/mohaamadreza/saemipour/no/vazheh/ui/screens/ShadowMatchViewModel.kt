package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mohaamadreza.saemipour.no.vazheh.data.ContentRepository
import mohaamadreza.saemipour.no.vazheh.data.WordDTO

/**
 * Shadow Match game - یک سایه نمایش داده می‌شود و کودک باید
 * تصویر صحیح را از بین چند گزینه پیدا کند.
 */
class ShadowMatchViewModel(
    private val contentRepository: ContentRepository
) : ViewModel() {

    /** کلمه‌ای که سایه‌اش نشان داده می‌شود */
    var currentTarget by mutableStateOf<WordDTO?>(null)
        private set

    /** ۴ گزینه قابل انتخاب (شامل گزینه صحیح) */
    var options = mutableStateListOf<WordDTO>()
        private set

    /** شماره دور فعلی (۱-بِیس) */
    var currentRound by mutableStateOf(0)
        private set

    /** تعداد کل دورها */
    var totalRounds by mutableStateOf(0)
        private set

    /** تعداد ستاره‌های کسب‌شده (دورهایی که در اولین تلاش درست شدند) */
    var stars by mutableStateOf(0)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isWin by mutableStateOf(false)
        private set

    /** کلمه‌ای که الان درست انتخاب شده و باید صدایش پخش شود */
    var lastCorrectWord by mutableStateOf<WordDTO?>(null)
        private set

    /** فلگ نمایش بازخورد اشتباه */
    var showWrongFeedback by mutableStateOf(false)
        private set

    /** id گزینه‌ای که اشتباه انتخاب شده تا غیرفعال شود */
    var wrongOptionId by mutableStateOf<Int?>(null)
        private set

    /** فلگ نمایش بازخورد درست (روی گزینه‌ی انتخاب‌شده) */
    var correctOptionId by mutableStateOf<Int?>(null)
        private set

    private var roundHasMistake = false
    private var allWords: List<WordDTO> = emptyList()
    private var lastMode: LoadMode = LoadMode.None

    fun loadWords(categoryId: Int, rounds: Int = 5) {
        lastMode = LoadMode.Single(categoryId, rounds)
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            contentRepository.getWordsByCategory(categoryId).fold(
                onSuccess = { response ->
                    val withImages = response.data.filter { !it.imageUrl.isNullOrBlank() }
                    if (response.success && withImages.size >= MIN_WORDS_REQUIRED) {
                        allWords = withImages
                        setupGame(rounds)
                    } else {
                        errorMessage = "تصاویر کافی برای این بازی در این دسته‌بندی موجود نیست"
                    }
                    isLoading = false
                },
                onFailure = { exception ->
                    errorMessage = "خطا در بارگذاری: ${exception.message ?: "خطای نامشخص"}"
                    isLoading = false
                }
            )
        }
    }

    /**
     * بارگذاری کلمات از همه دسته‌بندی‌ها برای حالت ترکیبی
     */
    fun loadCombinedWords(rounds: Int = 5) {
        lastMode = LoadMode.Combined(rounds)
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            contentRepository.getAllCategories().fold(
                onSuccess = { categoriesResponse ->
                    val categories = categoriesResponse.data
                    if (!categoriesResponse.success || categories.isEmpty()) {
                        errorMessage = "دسته‌بندی‌ای برای بازی ترکیبی یافت نشد"
                        isLoading = false
                        return@fold
                    }

                    val collected = mutableListOf<WordDTO>()
                    for (category in categories) {
                        contentRepository.getWordsByCategory(category.id).onSuccess { resp ->
                            if (resp.success) collected.addAll(resp.data)
                        }
                    }

                    val withImages = collected
                        .distinctBy { it.id }
                        .filter { !it.imageUrl.isNullOrBlank() }

                    if (withImages.size < MIN_WORDS_REQUIRED) {
                        errorMessage = "تصاویر کافی برای بازی ترکیبی موجود نیست"
                    } else {
                        allWords = withImages
                        setupGame(rounds)
                    }
                    isLoading = false
                },
                onFailure = { exception ->
                    errorMessage = "خطا در بارگذاری: ${exception.message ?: "خطای نامشخص"}"
                    isLoading = false
                }
            )
        }
    }

    private fun setupGame(rounds: Int) {
        val effectiveRounds = rounds.coerceAtMost(allWords.size)
        totalRounds = effectiveRounds
        currentRound = 0
        stars = 0
        isWin = false
        lastCorrectWord = null
        showWrongFeedback = false
        wrongOptionId = null
        correctOptionId = null
        roundHasMistake = false

        startNextRound()
    }

    private fun startNextRound() {
        if (currentRound >= totalRounds) {
            isWin = true
            return
        }

        val target = allWords.shuffled().first()
        val distractors = allWords
            .filter { it.id != target.id }
            .shuffled()
            .take(OPTIONS_COUNT - 1)

        val newOptions = (distractors + target).shuffled()

        currentTarget = target
        options.clear()
        options.addAll(newOptions)
        currentRound++
        wrongOptionId = null
        correctOptionId = null
        roundHasMistake = false
    }

    fun onOptionSelected(option: WordDTO) {
        if (correctOptionId != null) return
        val target = currentTarget ?: return

        if (option.id == target.id) {
            correctOptionId = option.id
            lastCorrectWord = option
            if (!roundHasMistake) {
                stars++
            }
        } else {
            roundHasMistake = true
            wrongOptionId = option.id
            showWrongFeedback = true
        }
    }

    fun advanceToNextRound() {
        startNextRound()
    }

    fun clearLastCorrectWord() {
        lastCorrectWord = null
    }

    fun clearWrongFeedback() {
        showWrongFeedback = false
        wrongOptionId = null
    }

    fun resetGame() {
        when (val mode = lastMode) {
            is LoadMode.Combined -> setupGame(mode.rounds)
            is LoadMode.Single -> setupGame(mode.rounds)
            LoadMode.None -> { /* nothing */ }
        }
    }

    fun retry() {
        errorMessage = null
        when (val mode = lastMode) {
            is LoadMode.Combined -> loadCombinedWords(mode.rounds)
            is LoadMode.Single -> loadWords(mode.categoryId, mode.rounds)
            LoadMode.None -> { /* nothing to retry */ }
        }
    }

    private sealed class LoadMode {
        data object None : LoadMode()
        data class Single(val categoryId: Int, val rounds: Int) : LoadMode()
        data class Combined(val rounds: Int) : LoadMode()
    }

    companion object {
        private const val OPTIONS_COUNT = 4
        private const val MIN_WORDS_REQUIRED = OPTIONS_COUNT
    }
}
