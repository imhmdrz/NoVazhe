package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mohaamadreza.saemipour.no.vazheh.data.ContentRepository
import mohaamadreza.saemipour.no.vazheh.data.ColorSortingProgressDTO
import mohaamadreza.saemipour.no.vazheh.data.ProgressRepository
import mohaamadreza.saemipour.no.vazheh.data.WordDTO

enum class GameColor(
    val nameFa: String,
    val nameEn: String,
    val color: Color
) {
    RED("قرمز", "Red", Color(0xFFE53935)),
    BLUE("آبی", "Blue", Color(0xFF1E88E5)),
    GREEN("سبز", "Green", Color(0xFF43A047)),
    YELLOW("زرد", "Yellow", Color(0xFFFFEB3B)),
    ORANGE("نارنجی", "Orange", Color(0xFFFF9800)),
    PURPLE("بنفش", "Purple", Color(0xFF8E24AA)),
    PINK("صورتی", "Pink", Color(0xFFEC407A)),
    WHITE("سفید", "White", Color.White),
    BLACK("سیاه", "Black", Color(0xFF212121)),
    BROWN("قهوه‌ای", "Brown", Color(0xFF795548));

    companion object {
        /**
         * تطبیق نام انگلیسی کلمه با enum (case-insensitive، با حذف فاصله)
         */
        fun fromEnglishName(name: String?): GameColor? {
            if (name.isNullOrBlank()) return null
            val normalized = name.trim().lowercase()
            return entries.firstOrNull { it.nameEn.lowercase() == normalized }
        }
    }
}

val COLOR_SORTING_LEVEL_LADDER = listOf(
    listOf(GameColor.RED, GameColor.BLUE, GameColor.YELLOW),
    listOf(GameColor.RED, GameColor.BLUE, GameColor.YELLOW, GameColor.GREEN),
    listOf(GameColor.RED, GameColor.BLUE, GameColor.YELLOW, GameColor.GREEN, GameColor.PURPLE)
)

private val ALL_GAME_COLORS = listOf(
    GameColor.RED,
    GameColor.BLUE,
    GameColor.GREEN,
    GameColor.YELLOW,
    GameColor.PURPLE,
    GameColor.PINK,
    GameColor.BLACK,
    GameColor.BROWN
)

data class ColorItem(
    val id: Int,
    val color: GameColor,
    val emoji: String,
    var isSorted: Boolean = false
)

data class ColorBasket(
    val color: GameColor,
    // SnapshotStateList تا با اضافه‌شدن بادکنک، UI (بادکنک‌های آویزان به باکس) به‌روز شود
    val items: SnapshotStateList<ColorItem> = mutableStateListOf()
)

class ColorSortingViewModel(
    private val contentRepository: ContentRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    var items = mutableStateListOf<ColorItem>()
        private set

    var baskets = mutableStateListOf<ColorBasket>()
        private set

    var isWin by mutableStateOf(false)
        private set

    var isLevelUpWin by mutableStateOf(false)
        private set

    var sortedCount by mutableStateOf(0)
        private set

    var totalItems by mutableStateOf(0)
        private set

    var levelIndex by mutableStateOf(0)
        private set

    var successfulGames by mutableStateOf(0)
        private set

    var celebrationStarsOverride by mutableStateOf<Int?>(null)
        private set

    /** Dimensions for the current advancing-level celebration only. */
    var levelUpFromLevelIndex by mutableStateOf<Int?>(null)
        private set

    var levelUpToLevelIndex by mutableStateOf<Int?>(null)
        private set

    var isCurrentlyDragging by mutableStateOf(false)
        private set

    var lastSortedItem by mutableStateOf<ColorItem?>(null)
        private set

    var showWrongFeedback by mutableStateOf(false)
        private set

    /**
     * صدای رنگی که الان باید پخش شود (پس از رها کردن صحیح)
     */
    var currentSoundUrl by mutableStateOf<String?>(null)
        private set

    /**
     * map از GameColor به audioUrl گرفته شده از API
     */
    private var colorAudioMap: Map<GameColor, String> = emptyMap()
    private var activeColors: List<GameColor> = emptyList()
    private var activeColorsLevelIndex: Int? = null
    private var previousBasketOrder: List<GameColor>? = null

    private var childId: Int? = null
    private var colorCategoryId: Int? = null
    private var completionInFlight: Job? = null
    private var boardGeneration = 0

    fun loadGame(childId: Int? = null) {
        boardGeneration++
        this.childId = childId
        activeColors = emptyList()
        activeColorsLevelIndex = null
        previousBasketOrder = null
        isLevelUpWin = false
        celebrationStarsOverride = null
        levelUpFromLevelIndex = null
        levelUpToLevelIndex = null

        viewModelScope.launch {
            ensureColorWordsLoaded()
            if (childId != null) {
                fetchProgression(childId)
            } else {
                useFallbackLevel()
            }
            setupGame()
        }
    }

    /**
     * گرفتن دسته رنگ‌ها از API و سپس راه‌اندازی بازی
     */
    private suspend fun ensureColorWordsLoaded() {
        if (colorCategoryId != null || colorAudioMap.isNotEmpty()) return

        contentRepository.getAllCategories().fold(
            onSuccess = { categoriesResponse ->
                if (categoriesResponse.success) {
                    val colorCategory = categoriesResponse.data.firstOrNull { cat ->
                        cat.nameEn.equals("Colors", ignoreCase = true) ||
                            cat.nameEn.equals("Color", ignoreCase = true) ||
                            cat.nameFa.contains("رنگ")
                    }
                    if (colorCategory != null) {
                        colorCategoryId = colorCategory.id
                        loadWordsForCategory(colorCategory.id)
                    }
                }
            },
            onFailure = { /* game still works without color-word audio */ }
        )
    }

    private suspend fun loadWordsForCategory(categoryId: Int) {
        contentRepository.getWordsByCategory(categoryId).fold(
            onSuccess = { wordsResponse ->
                if (wordsResponse.success) {
                    buildColorAudioMap(wordsResponse.data)
                }
            },
            onFailure = { /* game still works without color-word audio */ }
        )
    }

    /**
     * ساخت map از GameColor به audioUrl با تطبیق wordEn
     */
    private fun buildColorAudioMap(words: List<WordDTO>) {
        val map = mutableMapOf<GameColor, String>()
        words.forEach { word ->
            val gameColor = GameColor.fromEnglishName(word.wordEn)
            val url = word.audioUrl
            if (gameColor != null && !url.isNullOrBlank()) {
                map[gameColor] = url
            }
        }
        colorAudioMap = map
    }

    private val colorEmojis = mapOf(
        GameColor.RED to listOf("🎈"),
        GameColor.BLUE to listOf("🎈"),
        GameColor.GREEN to listOf("🎈"),
        GameColor.YELLOW to listOf("🎈"),
        GameColor.ORANGE to listOf("🎈"),
        GameColor.PURPLE to listOf("🎈"),
        GameColor.PINK to listOf("🎈"),
        GameColor.WHITE to listOf("🎈"),
        GameColor.BLACK to listOf("🎈"),
        GameColor.BROWN to listOf("🎈"),
    )

    private suspend fun fetchProgression(childId: Int) {
        progressRepository.getColorSortingProgress(childId).fold(
            onSuccess = { response ->
                val progress = response.data
                if (response.success && progress != null) {
                    levelIndex = progress.levelIndex.coerceIn(0, COLOR_SORTING_LEVEL_LADDER.lastIndex)
                    successfulGames = progress.successfulGames.coerceIn(0, 3)
                } else {
                    useFallbackLevel()
                }
            },
            onFailure = { useFallbackLevel() }
        )
    }

    private fun useFallbackLevel() {
        levelIndex = 0
        successfulGames = 0
        isLevelUpWin = false
        celebrationStarsOverride = null
        levelUpFromLevelIndex = null
        levelUpToLevelIndex = null
    }

    fun setupGame(itemsPerColor: Int = 1) {
        boardGeneration++
        items.clear()
        baskets.clear()
        isWin = false
        isLevelUpWin = false
        sortedCount = 0
        lastSortedItem = null
        showWrongFeedback = false
        currentSoundUrl = null
        celebrationStarsOverride = null
        levelUpFromLevelIndex = null
        levelUpToLevelIndex = null

        val currentLevelIndex = levelIndex.coerceIn(0, COLOR_SORTING_LEVEL_LADDER.lastIndex)
        val colorCount = COLOR_SORTING_LEVEL_LADDER[currentLevelIndex].size
        if (activeColorsLevelIndex != currentLevelIndex || activeColors.isEmpty()) {
            activeColors = ALL_GAME_COLORS.shuffled().take(colorCount)
            activeColorsLevelIndex = currentLevelIndex
            previousBasketOrder = null
        }

        val basketColors = shuffledBasketOrder(activeColors)
        val itemColors = activeColors.shuffled()

        basketColors.forEach { color ->
            baskets.add(ColorBasket(color = color))
        }

        var itemId = 0
        itemColors.forEach { color ->
            val emojis = colorEmojis[color] ?: listOf("🎈")
            repeat(itemsPerColor) {
                items.add(ColorItem(id = itemId++, color = color, emoji = emojis.first()))
            }
        }

        items.shuffle()
        totalItems = items.size
    }

    /** Shuffle basket positions for a stage without changing the level's active colors. */
    private fun shuffledBasketOrder(colors: List<GameColor>): List<GameColor> {
        var shuffled = colors.shuffled()
        val previous = previousBasketOrder
        if (previous != null && colors.size > 1 && shuffled == previous) {
            shuffled = previous.drop(1) + previous.first()
        }
        previousBasketOrder = shuffled
        return shuffled
    }

    fun startDragging() {
        isCurrentlyDragging = true
    }

    fun stopDragging() {
        isCurrentlyDragging = false
    }

    fun sortItemToBasket(item: ColorItem, basket: ColorBasket): Boolean {
        if (item.color == basket.color) {
            val itemIndex = items.indexOfFirst { it.id == item.id }
            if (itemIndex != -1) {
                items[itemIndex] = items[itemIndex].copy(isSorted = true)
                basket.items.add(item)
                sortedCount++
                lastSortedItem = item

                // پخش صدای رنگ
                colorAudioMap[basket.color]?.let { url ->
                    currentSoundUrl = url
                }

                if (sortedCount == totalItems) {
                    val generation = boardGeneration
                    val previous = completionInFlight
                    completionInFlight = viewModelScope.launch {
                        previous?.join()
                        completeSuccessfulGame(generation)
                    }
                }
                return true
            }
        } else {
            showWrongFeedback = true
        }
        return false
    }

    fun clearCurrentSound() {
        currentSoundUrl = null
    }

    fun clearWrongFeedback() {
        showWrongFeedback = false
    }

    fun clearLastSortedItem() {
        lastSortedItem = null
    }

    private suspend fun completeSuccessfulGame(generation: Int) {
        val currentChildId = childId
        if (currentChildId == null) {
            if (generation == boardGeneration) {
                isLevelUpWin = false
                isWin = true
            }
            return
        }

        submitCompletion(currentChildId, generation, levelIndex, successfulGames, isResubmission = false)
    }

    private suspend fun submitCompletion(
        childId: Int,
        generation: Int,
        completedLevel: Int,
        gamesBefore: Int,
        isResubmission: Boolean
    ) {
        progressRepository
            .recordColorSortingGameComplete(
                childId = childId,
                levelIndex = completedLevel,
                successfulGamesBefore = gamesBefore
            )
            .fold(
                onSuccess = { response ->
                    val progress = response.data
                    if (response.success && progress != null) {
                        applyProgressResponse(generation, completedLevel, progress)
                    } else if (!isResubmission) {
                        val fresh = progressRepository.getColorSortingProgress(childId).getOrNull()
                            ?.takeIf { it.success }?.data
                        if (fresh != null) {
                            levelIndex = fresh.levelIndex.coerceIn(0, COLOR_SORTING_LEVEL_LADDER.lastIndex)
                            successfulGames = fresh.successfulGames.coerceIn(0, 3)
                            submitCompletion(childId, generation, levelIndex, successfulGames, isResubmission = true)
                        }
                    }
                },
                onFailure = { /* pending retry is owned by ProgressRepository */ }
            )
    }

    private fun applyProgressResponse(
        generation: Int,
        completedLevel: Int,
        progress: ColorSortingProgressDTO
    ) {
        val returnedLevel = progress.levelIndex.coerceIn(0, COLOR_SORTING_LEVEL_LADDER.lastIndex)
        val returnedSuccessfulGames = progress.successfulGames.coerceIn(0, 3)
        val didLevelUp = returnedLevel > completedLevel
        levelIndex = returnedLevel
        successfulGames = returnedSuccessfulGames
        celebrationStarsOverride = if (didLevelUp) 3 else null

        if (generation == boardGeneration) {
            levelUpFromLevelIndex = if (didLevelUp) completedLevel else null
            levelUpToLevelIndex = if (didLevelUp) returnedLevel else null
            isLevelUpWin = didLevelUp
            isWin = true
        }
    }

    fun resetGame() {
        boardGeneration++
        isWin = false
        isLevelUpWin = false
        celebrationStarsOverride = null
        levelUpFromLevelIndex = null
        levelUpToLevelIndex = null
        setupGame()
    }
}
