package mohaamadreza.saemipour.no.vazheh.services

import mohaamadreza.saemipour.no.vazheh.database.tables.FaceGameProgress
import mohaamadreza.saemipour.no.vazheh.models.ApiResponse
import mohaamadreza.saemipour.no.vazheh.models.FaceGameProgressDTO
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

/** Server-authoritative Face / Body character-run progression. */
object FaceGameProgressService {
    const val LAST_LEVEL = 2
    private val eligibleCharacterIds = setOf(
        "character_03", "character_04", "character_05", "character_06", "character_07"
    )

    fun isEligibleCharacter(characterId: String): Boolean = characterId in eligibleCharacterIds

    fun getProgress(childId: Int): FaceGameProgressDTO = transaction {
        val row = FaceGameProgress.selectAll()
            .where { FaceGameProgress.childId eq childId }
            .singleOrNull()
        if (row == null) FaceGameProgressDTO(0, 0, null)
        else row.toProgress()
    }

    /** Creates a new run or assigns a character to a legacy unfinished run without losing its level. */
    fun startRun(childId: Int, characterId: String): ApiResponse<FaceGameProgressDTO> {
        if (!isEligibleCharacter(characterId)) {
            return ApiResponse(false, "شخصیت برای همه مرحله‌های بازی موجود نیست", null)
        }
        return transaction {
            val row = FaceGameProgress.selectAll()
                .where { FaceGameProgress.childId eq childId }
                .singleOrNull()
            if (row == null) {
                FaceGameProgress.insert {
                    it[FaceGameProgress.childId] = childId
                    it[FaceGameProgress.levelIndex] = 0
                    it[FaceGameProgress.successfulGames] = 0
                    it[FaceGameProgress.activeCharacterId] = characterId
                    it[FaceGameProgress.updatedAt] = LocalDateTime.now()
                }
                return@transaction ApiResponse(true, "شروع شد", FaceGameProgressDTO(0, 0, characterId))
            }

            val existing = row.toProgress()
            val storedCharacterId = existing.activeCharacterId
            if (storedCharacterId != null && isEligibleCharacter(storedCharacterId)) {
                // A duplicate/stale start request must never replace an already active run.
                return@transaction ApiResponse(true, "پیشرفت فعلی بازی", existing)
            }

            // Legacy terminal rows used to mean a replayable Level 3. Treat those as a
            // completed old run and start the new lifecycle at Level 1. Other legacy rows
            // retain their current level while acquiring a stable character.
            val isLegacyTerminal = existing.levelIndex == LAST_LEVEL && existing.successfulGames == 1
            val level = if (isLegacyTerminal) 0 else existing.levelIndex
            val successes = if (isLegacyTerminal) 0 else existing.successfulGames
            FaceGameProgress.update({ FaceGameProgress.childId eq childId }) {
                it[FaceGameProgress.levelIndex] = level
                it[FaceGameProgress.successfulGames] = successes
                it[FaceGameProgress.activeCharacterId] = characterId
                it[FaceGameProgress.updatedAt] = LocalDateTime.now()
            }
            ApiResponse(true, "شروع شد", FaceGameProgressDTO(level, successes, characterId))
        }
    }

    fun completeLevel(
        childId: Int,
        requestedLevel: Int,
        expectedCharacterId: String? = null
    ): ApiResponse<FaceGameProgressDTO> {
        if (requestedLevel !in 0..LAST_LEVEL) {
            return ApiResponse(false, "سطح بازی چهره نامعتبر است", null)
        }
        return transaction {
            val row = FaceGameProgress.selectAll()
                .where { FaceGameProgress.childId eq childId }
                .singleOrNull()
                ?: return@transaction ApiResponse(false, "بازی هنوز شروع نشده است", null)

            val current = row.toProgress()
            if (current.activeCharacterId !in eligibleCharacterIds) {
                return@transaction ApiResponse(false, "شخصیت فعال بازی معتبر نیست", null)
            }
            if (expectedCharacterId != null && expectedCharacterId != current.activeCharacterId) {
                return@transaction ApiResponse(false, "شخصیت ثبت‌شده با شخصیت فعال همخوان نیست", current)
            }
            // A previous completion may have reached the server while its response was lost.
            if (requestedLevel < current.levelIndex) {
                return@transaction ApiResponse(true, "پیشرفت قبلاً ثبت شده است", current)
            }
            if (requestedLevel != current.levelIndex) {
                return@transaction ApiResponse(false, "سطح ثبت‌شده با سطح فعلی همخوان نیست", null)
            }
            if (requestedLevel == LAST_LEVEL && current.successfulGames == 1) {
                return@transaction ApiResponse(true, "بازی قبلاً کامل شده است", current)
            }

            val updated = if (requestedLevel == LAST_LEVEL) {
                FaceGameProgressDTO(LAST_LEVEL, 1, current.activeCharacterId)
            } else {
                FaceGameProgressDTO(requestedLevel + 1, 0, current.activeCharacterId)
            }
            FaceGameProgress.update({ FaceGameProgress.childId eq childId }) {
                it[FaceGameProgress.levelIndex] = updated.levelIndex
                it[FaceGameProgress.successfulGames] = updated.successfulGames
                it[FaceGameProgress.updatedAt] = LocalDateTime.now()
            }
            ApiResponse(true, "ثبت شد", updated)
        }
    }

    /** Resets only the expected completed run; a retry after a lost response is harmless. */
    fun finalizeRun(
        childId: Int,
        completedCharacterId: String,
        nextCharacterId: String
    ): ApiResponse<FaceGameProgressDTO> {
        if (!isEligibleCharacter(completedCharacterId) || !isEligibleCharacter(nextCharacterId)) {
            return ApiResponse(false, "شخصیت برای همه مرحله‌های بازی موجود نیست", null)
        }
        if (eligibleCharacterIds.size > 1 && completedCharacterId == nextCharacterId) {
            return ApiResponse(false, "شخصیت جدید باید با شخصیت قبلی متفاوت باشد", null)
        }
        return transaction {
            val row = FaceGameProgress.selectAll()
                .where { FaceGameProgress.childId eq childId }
                .singleOrNull()
                ?: return@transaction ApiResponse(false, "پیشرفت بازی پیدا نشد", null)
            val current = row.toProgress()

            // Idempotent response when the exact requested next run was already committed.
            if (current.levelIndex == 0 && current.successfulGames == 0 &&
                current.activeCharacterId == nextCharacterId
            ) {
                return@transaction ApiResponse(true, "بازی بعدی قبلاً شروع شده است", current)
            }
            if (current.levelIndex != LAST_LEVEL || current.successfulGames != 1 ||
                current.activeCharacterId != completedCharacterId
            ) {
                return@transaction ApiResponse(false, "بازی کامل‌شده با وضعیت فعلی همخوان نیست", current)
            }

            FaceGameProgress.update({ FaceGameProgress.childId eq childId }) {
                it[FaceGameProgress.levelIndex] = 0
                it[FaceGameProgress.successfulGames] = 0
                it[FaceGameProgress.activeCharacterId] = nextCharacterId
                it[FaceGameProgress.updatedAt] = LocalDateTime.now()
            }
            ApiResponse(true, "بازی جدید شروع شد", FaceGameProgressDTO(0, 0, nextCharacterId))
        }
    }

    private fun org.jetbrains.exposed.sql.ResultRow.toProgress() = FaceGameProgressDTO(
        levelIndex = this[FaceGameProgress.levelIndex].coerceIn(0, LAST_LEVEL),
        successfulGames = this[FaceGameProgress.successfulGames].coerceIn(0, 1),
        activeCharacterId = this[FaceGameProgress.activeCharacterId]
            ?.takeIf(::isEligibleCharacter)
    )
}
