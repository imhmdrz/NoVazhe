package mohaamadreza.saemipour.no.vazheh.database.migrations

import org.jetbrains.exposed.sql.transactions.transaction

/** Adds the active-run character without dropping existing per-child progress. */
object FaceGameActiveCharacterMigration {
    fun migrate() {
        transaction {
            exec("ALTER TABLE face_game_progress ADD COLUMN IF NOT EXISTS active_character_id VARCHAR(64)")
        }
    }
}
