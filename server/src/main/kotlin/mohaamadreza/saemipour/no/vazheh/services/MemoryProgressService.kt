package mohaamadreza.saemipour.no.vazheh.services

import mohaamadreza.saemipour.no.vazheh.database.tables.MemoryProgress
import mohaamadreza.saemipour.no.vazheh.models.ApiResponse
import mohaamadreza.saemipour.no.vazheh.models.MemoryProgressDTO
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * Memory Progress Service - سرویس پیشرفت بازی حافظه
 * One row per child. A child advances to the next dimension after exactly
 * [ADVANCE_THRESHOLD] successful games at the current dimension and the success
 * counter then resets to 0. The server is authoritative: clients only report which
 * dimension a completed game was played on, and stale/incorrect dimensions are rejected.
 */
object MemoryProgressService {

    /** Number of dimensions in the ladder (2x2, 2x3, 3x4, 4x4) — must match the client ladder; keep them in sync. */
    const val MEMORY_DIMENSION_COUNT = 4

    private const val ADVANCE_THRESHOLD = 3 // تعداد برد‌های موفق لازم برای پیشرفت به ابعاد بعدی

    /**
     * Current progress for a child - دریافت پیشرفت فعلی بازی حافظه
     * Returns the default state {0, 0} when no row exists yet.
     */
    fun getProgress(childId: Int): MemoryProgressDTO {
        return transaction {
            val row = MemoryProgress.selectAll()
                .where { MemoryProgress.childId eq childId }
                .singleOrNull()

            if (row == null) {
                MemoryProgressDTO(0, 0)
            } else {
                MemoryProgressDTO(row[MemoryProgress.dimensionIndex], row[MemoryProgress.successfulGames])
            }
        }
    }

    /**
     * Record one successful memory game at [dimensionIndex] - ثبت یک برد موفق بازی حافظه
     *
     * Rules:
     * - Invalid dimension indexes are rejected.
     * - A child without a progress row may only start at dimension 0.
     * - Submitting for any other (stale/incorrect) dimension is rejected, so duplicate or
     *   stale submissions can never advance progression twice.
     * - After the third success the child advances to the next dimension and the counter
     *   resets to 0; on the final dimension the counter caps at 3.
     */
    fun recordSuccessfulGame(childId: Int, dimensionIndex: Int): ApiResponse<MemoryProgressDTO> {
        if (dimensionIndex !in 0 until MEMORY_DIMENSION_COUNT) {
            return ApiResponse(false, "شاخص ابعاد نامعتبر است", null)
        }

        return transaction {
            val row = MemoryProgress.selectAll()
                .where { MemoryProgress.childId eq childId }
                .singleOrNull()

            if (row == null) {
                // Fresh child — progression always starts at the first dimension.
                if (dimensionIndex != 0) {
                    return@transaction ApiResponse(false, "پیشرفت از اولین ابعاد آغاز می‌شود", null)
                }
                MemoryProgress.insert {
                    it[MemoryProgress.childId] = childId
                    it[MemoryProgress.dimensionIndex] = 0
                    it[MemoryProgress.successfulGames] = 1
                }
                return@transaction ApiResponse(true, "ثبت شد", MemoryProgressDTO(0, 1))
            }

            val currentDimension = row[MemoryProgress.dimensionIndex]
            if (currentDimension != dimensionIndex) {
                // Stale/incorrect dimension — never apply it.
                return@transaction ApiResponse(false, "ابعاد ثبت‌شده با ابعاد فعلی همخوان نیست", null)
            }

            val now = LocalDateTime.now()
            val newCount = row[MemoryProgress.successfulGames] + 1
            val (finalDimension, finalCount) = when {
                newCount >= ADVANCE_THRESHOLD && currentDimension < MEMORY_DIMENSION_COUNT - 1 ->
                    (currentDimension + 1 to 0) // advance — counter resets to 0
                newCount >= ADVANCE_THRESHOLD ->
                    (currentDimension to ADVANCE_THRESHOLD) // final dimension — capped at 3
                else ->
                    (currentDimension to newCount)
            }

            MemoryProgress.update({ MemoryProgress.childId eq childId }) {
                it[MemoryProgress.dimensionIndex] = finalDimension
                it[MemoryProgress.successfulGames] = finalCount
                it[MemoryProgress.updatedAt] = now
            }

            ApiResponse(true, "ثبت شد", MemoryProgressDTO(finalDimension, finalCount))
        }
    }
}
