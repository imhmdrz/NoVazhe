package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mohaamadreza.saemipour.no.vazheh.data.ContentRepository
import mohaamadreza.saemipour.no.vazheh.data.WordDTO

data class MemoryCard(
    val id: Int,
    val word: WordDTO,
    val pairId: Int,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)

class MemoryGameViewModel(
    private val contentRepository: ContentRepository
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

    private var firstFlippedCard: MemoryCard? = null
    private var isProcessing by mutableStateOf(false)
    
    var currentMatchedWord by mutableStateOf<WordDTO?>(null)
        private set

    fun loadWords(categoryId: Int, pairCount: Int = 6) {
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
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
    }

    fun onCardClick(card: MemoryCard) {
        if (isProcessing || card.isFlipped || card.isMatched) return

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
                        delay(500)
                        isWin = true
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

    fun clearMatchedWord() {
        currentMatchedWord = null
    }

    fun resetGame() {
        val currentWords = cards.map { it.word }.distinctBy { it.id }
        if (currentWords.isNotEmpty()) {
            setupGame(currentWords)
        }
    }

    fun retry() {
        errorMessage = null
    }
}
