package mohaamadreza.saemipour.no.vazheh.services

import mohaamadreza.saemipour.no.vazheh.database.tables.ShadowMatchProgress
import mohaamadreza.saemipour.no.vazheh.models.ApiResponse
import mohaamadreza.saemipour.no.vazheh.models.ShadowMatchProgressDTO
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

/** Server-authoritative three-level, three-sublevel Shadow Match progression. */
object ShadowMatchProgressService {
    const val SHADOW_LEVEL_COUNT = 3
    private const val LEGACY_LEVEL_FOUR_INDEX = 3
    private const val ADVANCE_THRESHOLD = 3

    fun getProgress(childId: Int): ShadowMatchProgressDTO = transaction {
        val row = ShadowMatchProgress.selectAll()
            .where { ShadowMatchProgress.childId eq childId }
            .singleOrNull()
        if (row == null) ShadowMatchProgressDTO(0, 0)
        else normalizeStoredProgress(
            childId,
            row[ShadowMatchProgress.levelIndex],
            row[ShadowMatchProgress.successfulGames]
        )
    }

    fun recordSuccessfulGame(childId: Int, levelIndex: Int): ApiResponse<ShadowMatchProgressDTO> {
        val requestedLevel = when (levelIndex) {
            in 0 until SHADOW_LEVEL_COUNT -> levelIndex
            LEGACY_LEVEL_FOUR_INDEX -> SHADOW_LEVEL_COUNT - 1
            else -> null
        }
        if (requestedLevel == null) {
            return ApiResponse(false, "سطح بازی سایه‌ها نامعتبر است", null)
        }
        return transaction {
            val row = ShadowMatchProgress.selectAll()
                .where { ShadowMatchProgress.childId eq childId }
                .singleOrNull()

            if (row == null) {
                if (requestedLevel != 0) {
                    return@transaction ApiResponse(false, "پیشرفت از سطح اول آغاز می‌شود", null)
                }
                ShadowMatchProgress.insert {
                    it[ShadowMatchProgress.childId] = childId
                    it[ShadowMatchProgress.levelIndex] = 0
                    it[ShadowMatchProgress.successfulGames] = 1
                }
                return@transaction ApiResponse(true, "ثبت شد", ShadowMatchProgressDTO(0, 1))
            }

            val storedProgress = normalizeStoredProgress(
                childId,
                row[ShadowMatchProgress.levelIndex],
                row[ShadowMatchProgress.successfulGames]
            )
            val currentLevel = storedProgress.levelIndex
            if (currentLevel != requestedLevel) {
                return@transaction ApiResponse(false, "سطح ثبت‌شده با سطح فعلی همخوان نیست", null)
            }

            val newCount = storedProgress.successfulGames + 1
            val (finalLevel, finalCount) = when {
                newCount >= ADVANCE_THRESHOLD && currentLevel < SHADOW_LEVEL_COUNT - 1 ->
                    currentLevel + 1 to 0
                newCount >= ADVANCE_THRESHOLD -> currentLevel to ADVANCE_THRESHOLD
                else -> currentLevel to newCount
            }
            ShadowMatchProgress.update({ ShadowMatchProgress.childId eq childId }) {
                it[ShadowMatchProgress.levelIndex] = finalLevel
                it[ShadowMatchProgress.successfulGames] = finalCount
                it[ShadowMatchProgress.updatedAt] = LocalDateTime.now()
            }
            ApiResponse(true, "ثبت شد", ShadowMatchProgressDTO(finalLevel, finalCount))
        }
    }

    /** Clamp the former Level 4 terminal row to the new completed Level 3 state in place. */
    private fun normalizeStoredProgress(childId: Int, levelIndex: Int, successfulGames: Int): ShadowMatchProgressDTO {
        val wasFormerLevelFour = levelIndex >= LEGACY_LEVEL_FOUR_INDEX
        val normalizedLevel = levelIndex.coerceIn(0, SHADOW_LEVEL_COUNT - 1)
        val normalizedGames = if (wasFormerLevelFour) {
            ADVANCE_THRESHOLD
        } else {
            successfulGames.coerceIn(0, ADVANCE_THRESHOLD)
        }
        if (normalizedLevel != levelIndex || normalizedGames != successfulGames) {
            ShadowMatchProgress.update({ ShadowMatchProgress.childId eq childId }) {
                it[ShadowMatchProgress.levelIndex] = normalizedLevel
                it[ShadowMatchProgress.successfulGames] = normalizedGames
                it[ShadowMatchProgress.updatedAt] = LocalDateTime.now()
            }
        }
        return ShadowMatchProgressDTO(normalizedLevel, normalizedGames)
    }
}
