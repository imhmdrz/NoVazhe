package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mohaamadreza.saemipour.no.vazheh.data.ContentRepository
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
    BLACK("سیاه", "Black", Color(0xFF212121)),
    WHITE("سفید", "White", Color(0xFFFAFAFA)),
    BROWN("قهوه‌ای", "Brown", Color(0xFF6D4C41));

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

data class ColorItem(
    val id: Int,
    val color: GameColor,
    val emoji: String,
    var isSorted: Boolean = false
)

data class ColorBasket(
    val color: GameColor,
    val items: MutableList<ColorItem> = mutableListOf()
)

class ColorSortingViewModel(
    private val contentRepository: ContentRepository
) : ViewModel() {

    var items = mutableStateListOf<ColorItem>()
        private set

    var baskets = mutableStateListOf<ColorBasket>()
        private set

    var isWin by mutableStateOf(false)
        private set

    var sortedCount by mutableStateOf(0)
        private set

    var totalItems by mutableStateOf(0)
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

    init {
        loadColorWordsAndSetup()
    }

    /**
     * گرفتن دسته رنگ‌ها از API و سپس راه‌اندازی بازی
     */
    private fun loadColorWordsAndSetup() {
        viewModelScope.launch {
            contentRepository.getAllCategories().fold(
                onSuccess = { categoriesResponse ->
                    if (categoriesResponse.success) {
                        val colorCategory = categoriesResponse.data.firstOrNull { cat ->
                            cat.nameEn.equals("Colors", ignoreCase = true) ||
                                cat.nameEn.equals("Color", ignoreCase = true) ||
                                cat.nameFa.contains("رنگ")
                        }
                        if (colorCategory != null) {
                            loadWordsForCategory(colorCategory.id)
                        } else {
                            // اگر دسته رنگ‌ها پیدا نشد، بازی بدون صدا راه‌اندازی شود
                            setupGame()
                        }
                    } else {
                        setupGame()
                    }
                },
                onFailure = {
                    setupGame()
                }
            )
        }
    }

    private fun loadWordsForCategory(categoryId: Int) {
        viewModelScope.launch {
            contentRepository.getWordsByCategory(categoryId).fold(
                onSuccess = { wordsResponse ->
                    if (wordsResponse.success) {
                        buildColorAudioMap(wordsResponse.data)
                    }
                    setupGame()
                },
                onFailure = {
                    setupGame()
                }
            )
        }
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
        GameColor.BLACK to listOf("🎈"),
        GameColor.WHITE to listOf("🎈"),
        GameColor.BROWN to listOf("🎈")
    )

    fun setupGame(colorCount: Int = 4, itemsPerColor: Int = 2) {
        items.clear()
        baskets.clear()
        isWin = false
        sortedCount = 0
        lastSortedItem = null
        showWrongFeedback = false
        currentSoundUrl = null

        // اولویت با رنگ‌هایی که audio دارند
        val colorsWithAudio = GameColor.entries.filter { colorAudioMap.containsKey(it) }
        val pool = if (colorsWithAudio.size >= colorCount) colorsWithAudio else GameColor.entries
        val selectedColors = pool.shuffled().take(colorCount)

        selectedColors.forEach { color ->
            baskets.add(ColorBasket(color = color))
        }

        var itemId = 0
        selectedColors.forEach { color ->
            val emojis = colorEmojis[color] ?: listOf("🎈")
            repeat(itemsPerColor) {
                items.add(ColorItem(id = itemId++, color = color, emoji = emojis.first()))
            }
        }

        items.shuffle()
        totalItems = items.size
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
                    isWin = true
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

    fun resetGame() {
        setupGame()
    }
}
