package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mohaamadreza.saemipour.no.vazheh.data.ContentRepository
import mohaamadreza.saemipour.no.vazheh.data.ProgressRepository
import mohaamadreza.saemipour.no.vazheh.data.WordDTO

data class ShadowLevelConfig(val targetCount: Int, val optionCount: Int)

val SHADOW_LEVEL_LADDER = listOf(
    ShadowLevelConfig(1, 4), ShadowLevelConfig(2, 4),
    ShadowLevelConfig(3, 6)
)

private const val SUBLEVELS_PER_LEVEL = 3
private const val NUMBERS_CATEGORY_NAME = "Numbers"

/** A board stays intact until every target is matched; wrong attempts only show temporary feedback. */
class ShadowMatchViewModel(
    private val contentRepository: ContentRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {
    var targets = mutableStateListOf<WordDTO>(); private set
    var options = mutableStateListOf<WordDTO>(); private set
    var matchedTargetIds by mutableStateOf<Set<Int>>(emptySet()); private set

    var levelIndex by mutableStateOf(0); private set
    var successfulGames by mutableStateOf(0); private set
    private var replaySuccessfulGames by mutableStateOf<Int?>(null)
    val currentLevel get() = levelIndex + 1
    val displayedSuccessfulGames get() = replaySuccessfulGames ?: successfulGames
    val currentSublevel get() = displayedSuccessfulGames.coerceIn(0, SUBLEVELS_PER_LEVEL - 1) + 1
    val stars get() = celebrationStarsOverride ?: displayedSuccessfulGames
    val totalSublevels get() = SUBLEVELS_PER_LEVEL
    val levelConfig get() = SHADOW_LEVEL_LADDER[levelIndex]
    val numberTargetIds: Set<Int>
        get() = targets
            .filter { it.categoryId in numberCategoryIds }
            .mapTo(mutableSetOf()) { it.id }

    var isLoading by mutableStateOf(false); private set
    var errorMessage by mutableStateOf<String?>(null); private set
    var isWin by mutableStateOf(false); private set
    var isLevelUpWin by mutableStateOf(false); private set
    var isAllLevelsCompletedWin by mutableStateOf(false); private set
    var celebrationStarsOverride by mutableStateOf<Int?>(null); private set
    var lastCorrectWord by mutableStateOf<WordDTO?>(null); private set
    var showWrongFeedback by mutableStateOf(false); private set
    var wrongOptionId by mutableStateOf<Int?>(null); private set
    var correctOptionId by mutableStateOf<Int?>(null); private set
    var isInputLocked by mutableStateOf(false); private set

    private var allWords = emptyList<WordDTO>()
    private var numberCategoryIds = emptySet<Int>()
    private var selectedChildId: Int? = null
    private var lastMode: LoadMode = LoadMode.None
    private var completionInFlight = false
    private var gameGeneration = 0

    fun loadWords(categoryId: Int, childId: Int?) {
        lastMode = LoadMode.Single(categoryId); selectedChildId = childId
        isLoading = true; errorMessage = null
        viewModelScope.launch {
            val category = contentRepository.getCategoryById(categoryId).getOrNull()
            val categoryData = category?.data
            if (category?.success != true || categoryData == null) {
                fail("خطا در شناسایی دسته‌بندی بازی"); return@launch
            }
            numberCategoryIds = if (categoryData.nameEn.equals(NUMBERS_CATEGORY_NAME, ignoreCase = true)) {
                setOf(categoryData.id)
            } else {
                emptySet()
            }
            contentRepository.getWordsByCategory(categoryId).fold(
                onSuccess = { if (it.success) initialize(it.data) else fail("تصاویر کافی برای این بازی موجود نیست") },
                onFailure = { fail("خطا در بارگذاری محتوا") }
            )
        }
    }

    fun loadCombinedWords(childId: Int?) {
        lastMode = LoadMode.Combined; selectedChildId = childId
        isLoading = true; errorMessage = null
        viewModelScope.launch {
            contentRepository.getAllCategories().fold(
                onSuccess = { categoriesResponse ->
                    if (!categoriesResponse.success || categoriesResponse.data.isEmpty()) {
                        fail("دسته‌بندی‌ای برای بازی ترکیبی یافت نشد"); return@fold
                    }
                    numberCategoryIds = categoriesResponse.data
                        .filter { it.nameEn.equals(NUMBERS_CATEGORY_NAME, ignoreCase = true) }
                        .mapTo(mutableSetOf()) { it.id }
                    val collected = mutableListOf<WordDTO>()
                    categoriesResponse.data.forEach { category ->
                        contentRepository.getWordsByCategory(category.id).onSuccess { response ->
                            if (response.success) collected += response.data
                        }
                    }
                    initialize(collected)
                },
                onFailure = { fail("خطا در بارگذاری محتوا") }
            )
        }
    }

    private fun initialize(words: List<WordDTO>) {
        val usable = words.distinctBy { it.id }.filter { !it.imageUrl.isNullOrBlank() }
        if (usable.size < SHADOW_LEVEL_LADDER.maxOf { it.optionCount }) {
            fail("برای همه سطح‌های بازی سایه‌ها حداقل شش تصویر متفاوت لازم است"); return
        }
        allWords = usable
        val childId = selectedChildId
        if (childId == null) {
            applyLoadedProgress(0, 0); beginBoard(); return
        }
        viewModelScope.launch {
            progressRepository.getShadowMatchProgress(childId).fold(
                onSuccess = { response ->
                    response.data?.takeIf { response.success }?.let {
                        applyLoadedProgress(it.levelIndex, it.successfulGames); beginBoard()
                    } ?: fail("خطا در بارگذاری پیشرفت بازی")
                },
                onFailure = { fail("خطا در بارگذاری پیشرفت بازی") }
            )
        }
    }

    fun onOptionSelected(option: WordDTO) {
        if (
            isInputLocked || isWin || option.id in matchedTargetIds ||
            matchedTargetIds.size == targets.size
        ) return
        if (option.id in targets.map { it.id }) {
            matchedTargetIds = matchedTargetIds + option.id
            correctOptionId = option.id
            lastCorrectWord = option
        } else {
            isInputLocked = true
            wrongOptionId = option.id
            showWrongFeedback = true
        }
    }

    fun clearWrongFeedback() {
        showWrongFeedback = false; wrongOptionId = null; isInputLocked = false
    }

    /** Called by the screen only after the correct jingle and pronunciation complete. */
    fun finishCorrectFeedback() {
        lastCorrectWord = null; correctOptionId = null
        if (matchedTargetIds.size == targets.size) recordCompletedSublevel() else isInputLocked = false
    }

    private fun recordCompletedSublevel() {
        if (completionInFlight) return
        completionInFlight = true
        val completedLevel = levelIndex
        val gamesBefore = successfulGames
        val generation = gameGeneration
        val childId = selectedChildId
        if (childId == null) {
            val completedCount = gamesBefore + 1
            val (nextLevel, nextCount) = when {
                completedCount >= SUBLEVELS_PER_LEVEL && completedLevel < SHADOW_LEVEL_LADDER.lastIndex ->
                    completedLevel + 1 to 0
                completedCount >= SUBLEVELS_PER_LEVEL -> completedLevel to SUBLEVELS_PER_LEVEL
                else -> completedLevel to completedCount
            }
            applyCompletedProgress(completedLevel, gamesBefore, nextLevel, nextCount)
            completionInFlight = false
            return
        }
        viewModelScope.launch {
            progressRepository.recordShadowMatchGameComplete(childId, completedLevel, gamesBefore).fold(
                onSuccess = { response ->
                    val progress = response.data
                    if (response.success && progress != null && generation == gameGeneration) {
                        applyCompletedProgress(
                            completedLevel,
                            gamesBefore,
                            progress.levelIndex,
                            progress.successfulGames
                        )
                    } else if (generation == gameGeneration) {
                        showRecoverableWin(generation)
                    }
                },
                onFailure = { showRecoverableWin(generation) }
            )
            completionInFlight = false
        }
    }

    private fun applyCompletedProgress(
        completedLevel: Int,
        gamesBefore: Int,
        serverLevel: Int,
        serverGames: Int
    ) {
        val returnedLevel = serverLevel.coerceIn(0, SHADOW_LEVEL_LADDER.lastIndex)
        val returnedGames = serverGames.coerceIn(0, SUBLEVELS_PER_LEVEL)
        val completedAllLevelsNow =
            completedLevel == SHADOW_LEVEL_LADDER.lastIndex &&
                gamesBefore < SUBLEVELS_PER_LEVEL &&
                returnedLevel == SHADOW_LEVEL_LADDER.lastIndex &&
                returnedGames == SUBLEVELS_PER_LEVEL
        val completedMaxLevelReplay =
            completedLevel == SHADOW_LEVEL_LADDER.lastIndex &&
                gamesBefore >= SUBLEVELS_PER_LEVEL &&
                returnedLevel == SHADOW_LEVEL_LADDER.lastIndex &&
                returnedGames == SUBLEVELS_PER_LEVEL
        isLevelUpWin = returnedLevel > completedLevel
        isAllLevelsCompletedWin = completedAllLevelsNow
        celebrationStarsOverride = if (isLevelUpWin) SUBLEVELS_PER_LEVEL else null
        if (completedMaxLevelReplay) {
            replaySuccessfulGames =
                ((replaySuccessfulGames ?: 0) + 1).coerceAtMost(SUBLEVELS_PER_LEVEL)
        } else if (returnedGames < SUBLEVELS_PER_LEVEL) {
            replaySuccessfulGames = null
        }
        applyProgress(serverLevel, serverGames)
        isWin = true
    }

    private fun applyLoadedProgress(serverLevel: Int, serverGames: Int) {
        applyProgress(serverLevel, serverGames)
        replaySuccessfulGames = if (
            levelIndex == SHADOW_LEVEL_LADDER.lastIndex &&
            successfulGames == SUBLEVELS_PER_LEVEL
        ) {
            0
        } else {
            null
        }
    }

    private fun applyProgress(serverLevel: Int, serverGames: Int) {
        levelIndex = serverLevel.coerceIn(0, SHADOW_LEVEL_LADDER.lastIndex)
        successfulGames = if (serverLevel > SHADOW_LEVEL_LADDER.lastIndex) {
            SUBLEVELS_PER_LEVEL
        } else {
            serverGames.coerceIn(0, SUBLEVELS_PER_LEVEL)
        }
    }

    /** Starts the next board while retaining authoritative level/sublevel progress. */
    fun resetGame() {
        if (isAllLevelsCompletedWin || (replaySuccessfulGames ?: 0) >= SUBLEVELS_PER_LEVEL) {
            replaySuccessfulGames = 0
        }
        isWin = false; isLevelUpWin = false; isAllLevelsCompletedWin = false
        celebrationStarsOverride = null; errorMessage = null
        beginBoard()
    }

    private fun showRecoverableWin(generation: Int) {
        if (generation != gameGeneration) return
        isLevelUpWin = false
        isAllLevelsCompletedWin = false
        errorMessage = null
        isInputLocked = false
        isWin = true
    }

    private fun beginBoard() {
        val config = levelConfig
        if (allWords.size < config.optionCount) { fail("تصاویر کافی برای این سطح موجود نیست"); return }
        gameGeneration++
        val selectedTargets = selectTargets(config.targetCount)
        val targetIds = selectedTargets.map { it.id }.toSet()
        val distractors = allWords.filterNot { it.id in targetIds }.shuffled()
            .take(config.optionCount - config.targetCount)
        if (selectedTargets.size != config.targetCount || distractors.size != config.optionCount - config.targetCount) {
            fail("تصاویر متفاوت کافی برای این سطح موجود نیست"); return
        }
        targets.clear(); targets.addAll(selectedTargets)
        options.clear(); options.addAll((selectedTargets + distractors).shuffled())
        matchedTargetIds = emptySet(); lastCorrectWord = null; showWrongFeedback = false
        wrongOptionId = null; correctOptionId = null; isInputLocked = false; isLoading = false
    }

    /** Preserve normal random selection while accepting at most one Numbers-category target. */
    private fun selectTargets(targetCount: Int): List<WordDTO> {
        val selected = ArrayList<WordDTO>(targetCount)
        var hasNumberTarget = false
        for (word in allWords.shuffled()) {
            val isNumber = word.categoryId in numberCategoryIds
            if (isNumber && hasNumberTarget) continue
            selected += word
            if (isNumber) hasNumberTarget = true
            if (selected.size == targetCount) break
        }
        return selected
    }

    fun retry() {
        errorMessage = null
        when (val mode = lastMode) {
            is LoadMode.Single -> loadWords(mode.categoryId, selectedChildId)
            LoadMode.Combined -> loadCombinedWords(selectedChildId)
            LoadMode.None -> Unit
        }
    }

    private fun fail(message: String) { errorMessage = message; isLoading = false }

    private sealed class LoadMode {
        data object None : LoadMode()
        data class Single(val categoryId: Int) : LoadMode()
        data object Combined : LoadMode()
    }
}
