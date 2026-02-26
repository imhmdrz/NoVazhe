package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel

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
    PURPLE("بنفش", "Purple", Color(0xFF8E24AA))
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

class ColorSortingViewModel : ViewModel() {

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

    private val colorEmojis = mapOf(
        GameColor.RED to listOf("🍎", "🍒", "🍓", "❤️", "🌹"),
        GameColor.BLUE to listOf("🫐", "💙", "🦋", "💎", "🌊"),
        GameColor.GREEN to listOf("🥒", "🥦", "🍀", "🐸", "🌲"),
        GameColor.YELLOW to listOf("🍋", "🌟", "⭐", "🌻", "🍌"),
        GameColor.ORANGE to listOf("🍊", "🥕", "🧡", "🏀", "🎃"),
        GameColor.PURPLE to listOf("🍇", "🍆", "💜", "🔮", "👾")
    )

    init {
        setupGame()
    }

    fun setupGame(colorCount: Int = 4, itemsPerColor: Int = 2) {
        items.clear()
        baskets.clear()
        isWin = false
        sortedCount = 0
        lastSortedItem = null
        showWrongFeedback = false

        val selectedColors = GameColor.entries.shuffled().take(colorCount)
        
        selectedColors.forEach { color ->
            baskets.add(ColorBasket(color = color))
        }

        var itemId = 0
        selectedColors.forEach { color ->
            val emojis = colorEmojis[color]?.shuffled()?.take(itemsPerColor) ?: listOf("⬤")
            emojis.forEach { emoji ->
                items.add(ColorItem(id = itemId++, color = color, emoji = emoji))
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
