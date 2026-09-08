package mohaamadreza.saemipour.no.vazheh.services

import mohaamadreza.saemipour.no.vazheh.database.tables.ColorSortingProgress
import mohaamadreza.saemipour.no.vazheh.models.ApiResponse
import mohaamadreza.saemipour.no.vazheh.models.ColorSortingProgressDTO
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

/**
 * Color Sorting Progress Service - سرویس پیشرفت بازی رنگ‌ها
 * One row per child. The child advances from 3 colors to 4 and then 5 after exactly
 * [ADVANCE_THRESHOLD] successful boards at the current level. The final level caps the
 * success counter at 3.
 */
object ColorSortingProgressService {

    const val COLOR_LEVEL_COUNT = 3

    private const val ADVANCE_THRESHOLD = 3

    fun getProgress(childId: Int): ColorSortingProgressDTO {
        return transaction {
            val row = ColorSortingProgress.selectAll()
                .where { ColorSortingProgress.childId eq childId }
                .singleOrNull()

            if (row == null) {
                ColorSortingProgressDTO(0, 0)
            } else {
                ColorSortingProgressDTO(
                    row[ColorSortingProgress.levelIndex],
                    row[ColorSortingProgress.successfulGames]
                )
            }
        }
    }

    fun recordSuccessfulGame(childId: Int, levelIndex: Int): ApiResponse<ColorSortingProgressDTO> {
        if (levelIndex !in 0 until COLOR_LEVEL_COUNT) {
            return ApiResponse(false, "سطح بازی رنگ‌ها نامعتبر است", null)
        }

        return transaction {
            val row = ColorSortingProgress.selectAll()
                .where { ColorSortingProgress.childId eq childId }
                .singleOrNull()

            if (row == null) {
                if (levelIndex != 0) {
                    return@transaction ApiResponse(false, "پیشرفت از سطح اول آغاز می‌شود", null)
                }
                ColorSortingProgress.insert {
                    it[ColorSortingProgress.childId] = childId
                    it[ColorSortingProgress.levelIndex] = 0
                    it[ColorSortingProgress.successfulGames] = 1
                }
                return@transaction ApiResponse(true, "ثبت شد", ColorSortingProgressDTO(0, 1))
            }

            val currentLevel = row[ColorSortingProgress.levelIndex]
            if (currentLevel != levelIndex) {
                return@transaction ApiResponse(false, "سطح ثبت‌شده با سطح فعلی همخوان نیست", null)
            }

            val now = LocalDateTime.now()
            val newCount = row[ColorSortingProgress.successfulGames] + 1
            val (finalLevel, finalCount) = when {
                newCount >= ADVANCE_THRESHOLD && currentLevel < COLOR_LEVEL_COUNT - 1 ->
                    (currentLevel + 1 to 0)
                newCount >= ADVANCE_THRESHOLD ->
                    (currentLevel to ADVANCE_THRESHOLD)
                else ->
                    (currentLevel to newCount)
            }

            ColorSortingProgress.update({ ColorSortingProgress.childId eq childId }) {
                it[ColorSortingProgress.levelIndex] = finalLevel
                it[ColorSortingProgress.successfulGames] = finalCount
                it[ColorSortingProgress.updatedAt] = now
            }

            ApiResponse(true, "ثبت شد", ColorSortingProgressDTO(finalLevel, finalCount))
        }
    }
}
