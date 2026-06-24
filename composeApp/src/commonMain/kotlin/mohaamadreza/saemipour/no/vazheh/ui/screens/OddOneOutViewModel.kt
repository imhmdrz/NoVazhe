package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mohaamadreza.saemipour.no.vazheh.data.CategoryDTO
import mohaamadreza.saemipour.no.vazheh.data.ContentRepository
import mohaamadreza.saemipour.no.vazheh.data.WordDTO

/**
 * Odd One Out game - بازی «کدام متفاوت است؟»
 *
 * در هر دور ۳ کلمه از یک دسته‌بندی + ۱ کلمه از دسته‌ای متفاوت
 * نمایش داده می‌شود و کودک باید کلمه‌ی «متفاوت» را پیدا کند.
 */
class OddOneOutViewModel(
    private val contentRepository: ContentRepository
) : ViewModel() {

    /** ۴ گزینه قابل انتخاب */
    var options = mutableStateListOf<WordDTO>()
        private set

    /** id کلمه‌ی «متفاوت» (پاسخ صحیح) */
    var oddWordId by mutableStateOf<Int?>(null)
        private set

    /** نام دسته‌بندی اصلی (برای نمایش به صورت hint اختیاری) */
    var mainCategoryNameFa by mutableStateOf<String?>(null)
        private set

    var currentRound by mutableStateOf(0)
        private set

    var totalRounds by mutableStateOf(0)
        private set

    var stars by mutableStateOf(0)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isWin by mutableStateOf(false)
        private set

    /** کلمه‌ای که الان درست انتخاب شده تا صدایش پخش شود */
    var lastCorrectWord by mutableStateOf<WordDTO?>(null)
        private set

    var showWrongFeedback by mutableStateOf(false)
        private set

    var wrongOptionId by mutableStateOf<Int?>(null)
        private set

    var correctOptionId by mutableStateOf<Int?>(null)
        private set

    private var roundHasMistake = false
    private var lastRequestedRounds: Int = 5

    /** map از categoryId به لیست کلمات آن دسته */
    private var wordsByCategory: Map<Int, List<WordDTO>> = emptyMap()
    private var categoriesById: Map<Int, CategoryDTO> = emptyMap()

    fun load(rounds: Int = 5) {
        lastRequestedRounds = rounds
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            contentRepository.getAllCategories().fold(
                onSuccess = { categoriesResponse ->
                    val categories = categoriesResponse.data.filter { category ->
                        val isNumbersCategory = category.nameEn.equals("Numbers", ignoreCase = true) ||
                                category.nameEn.equals("Number", ignoreCase = true) ||
                                category.nameFa.contains("عدد") ||
                                category.nameFa.contains("اعداد")
                        !isNumbersCategory
                    }
                    if (!categoriesResponse.success || categories.isEmpty()) {
                        errorMessage = "دسته‌بندی‌ای برای این بازی یافت نشد"
                        isLoading = false
                        return@fold
                    }

                    val collectedByCategory = mutableMapOf<Int, List<WordDTO>>()
                    for (category in categories) {
                        contentRepository.getWordsByCategory(category.id).onSuccess { resp ->
                            if (resp.success) {
                                val withImages = resp.data
                                    .distinctBy { it.id }
                                    .filter { !it.imageUrl.isNullOrBlank() }
                                if (withImages.isNotEmpty()) {
                                    collectedByCategory[category.id] = withImages
                                }
                            }
                        }
                    }

                    val eligibleMain = collectedByCategory.filterValues { it.size >= MAIN_PER_ROUND }
                    val hasOtherCategoryWord =
                        collectedByCategory.values.flatten().distinctBy { it.id }.size >= MAIN_PER_ROUND + 1

                    if (eligibleMain.isEmpty() || !hasOtherCategoryWord) {
                        errorMessage = "تصاویر کافی در دسته‌بندی‌ها برای این بازی موجود نیست"
                    } else {
                        wordsByCategory = collectedByCategory
                        categoriesById = categories.associateBy { it.id }
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
        totalRounds = rounds
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

        val eligibleMainIds = wordsByCategory
            .filterValues { it.size >= MAIN_PER_ROUND }
            .keys
            .toList()

        if (eligibleMainIds.isEmpty()) {
            isWin = true
            return
        }

        val mainCategoryId = eligibleMainIds.random()
        val mainWords = wordsByCategory[mainCategoryId].orEmpty().shuffled().take(MAIN_PER_ROUND)

        val otherWordsPool = wordsByCategory
            .filterKeys { it != mainCategoryId }
            .values
            .flatten()

        if (otherWordsPool.isEmpty()) {
            isWin = true
            return
        }

        val oddWord = otherWordsPool.random()
        val newOptions = (mainWords + oddWord).shuffled()

        options.clear()
        options.addAll(newOptions)
        oddWordId = oddWord.id
        mainCategoryNameFa = categoriesById[mainCategoryId]?.nameFa

        currentRound++
        wrongOptionId = null
        correctOptionId = null
        roundHasMistake = false
    }

    fun onOptionSelected(option: WordDTO) {
        if (correctOptionId != null) return
        val targetId = oddWordId ?: return

        if (option.id == targetId) {
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
        setupGame(lastRequestedRounds)
    }

    fun retry() {
        errorMessage = null
        load(lastRequestedRounds)
    }

    companion object {
        /** تعداد کلمات از دسته‌ی اصلی در هر دور */
        private const val MAIN_PER_ROUND = 3
    }
}
