package mohaamadreza.saemipour.no.vazheh.services

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import mohaamadreza.saemipour.no.vazheh.database.migrations.FaceGameActiveCharacterMigration
import mohaamadreza.saemipour.no.vazheh.database.tables.Children
import mohaamadreza.saemipour.no.vazheh.database.tables.FaceGameProgress
import mohaamadreza.saemipour.no.vazheh.database.tables.Parents
import mohaamadreza.saemipour.no.vazheh.models.FaceGameProgressDTO
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class FaceGameProgressServiceTest {
    private data class TestChildren(val parentId: Int, val first: Int, val second: Int)

    private fun createDatabase(): TestChildren {
        val database = Database.connect(
            url = "jdbc:h2:mem:face_progress_${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver"
        )
        val children = transaction(database) {
            SchemaUtils.create(Parents, Children, FaceGameProgress)
            val parentId = Parents.insertAndGetId {
                it[username] = "face-progress-${UUID.randomUUID()}"
                it[passwordHash] = "test-hash"
                it[displayName] = "Test parent"
            }
            fun child(name: String): Int = Children.insertAndGetId {
                it[Children.parentId] = parentId
                it[Children.name] = name
                it[age] = 6
            }.value
            TestChildren(parentId.value, child("A"), child("B"))
        }
        FaceGameActiveCharacterMigration.migrate()
        return children
    }

    @Test
    fun fullRunKeepsCharacterAndFinalizationIsIdempotentAndChildScoped() {
        val children = createDatabase()
        val a = "character_05"
        val b = "character_04"
        val next = "character_06"

        assertEquals(FaceGameProgressDTO(0, 0, null), FaceGameProgressService.getProgress(children.first))
        assertEquals(FaceGameProgressDTO(0, 0, a), FaceGameProgressService.startRun(children.first, a).data)
        assertEquals(FaceGameProgressDTO(0, 0, b), FaceGameProgressService.startRun(children.second, b).data)

        assertEquals(FaceGameProgressDTO(1, 0, a), FaceGameProgressService.completeLevel(children.first, 0).data)
        assertEquals(FaceGameProgressDTO(1, 0, a), FaceGameProgressService.getProgress(children.first))
        assertEquals(FaceGameProgressDTO(2, 0, a), FaceGameProgressService.completeLevel(children.first, 1).data)
        assertEquals(FaceGameProgressDTO(2, 0, a), FaceGameProgressService.getProgress(children.first))
        assertEquals(FaceGameProgressDTO(2, 1, a), FaceGameProgressService.completeLevel(children.first, 2).data)
        assertEquals(FaceGameProgressDTO(2, 1, a), FaceGameProgressService.getProgress(children.first))

        val finalized = FaceGameProgressDTO(0, 0, next)
        assertEquals(finalized, FaceGameProgressService.finalizeRun(children.first, a, next).data)
        assertEquals(finalized, FaceGameProgressService.finalizeRun(children.first, a, next).data, "lost-response retry")
        assertEquals(finalized, FaceGameProgressService.getProgress(children.first))
        assertEquals(FaceGameProgressDTO(0, 0, b), FaceGameProgressService.getProgress(children.second))
        assertFalse(FaceGameProgressService.startRun(children.first, "face_01").success)
        assertFalse(FaceGameProgressService.completeLevel(children.first, 0, "character_03").success)
    }

    @Test
    fun legacyRowsKeepUnfinishedLevelAndTerminalRowsStartFreshRun() {
        val children = createDatabase()
        val cases = listOf(
            Triple(children.first, 0, FaceGameProgressDTO(0, 0, "character_03")),
            Triple(children.second, 1, FaceGameProgressDTO(1, 0, "character_04"))
        )
        cases.forEach { (childId, oldLevel, expected) ->
            transaction {
                FaceGameProgress.insert {
                    it[FaceGameProgress.childId] = childId
                    it[FaceGameProgress.levelIndex] = oldLevel
                    it[FaceGameProgress.successfulGames] = 0
                    it[FaceGameProgress.activeCharacterId] = null
                }
            }
            assertEquals(expected, FaceGameProgressService.startRun(childId, expected.activeCharacterId!!).data)
            assertEquals(expected, FaceGameProgressService.getProgress(childId))
        }

        val ordinaryL3Child = transaction {
            Children.insertAndGetId {
                it[Children.parentId] = children.parentId
                it[name] = "ordinary L3"
                it[age] = 6
            }.value
        }
        transaction {
            FaceGameProgress.insert {
                it[FaceGameProgress.childId] = ordinaryL3Child
                it[FaceGameProgress.levelIndex] = 2
                it[FaceGameProgress.successfulGames] = 0
                it[FaceGameProgress.activeCharacterId] = null
            }
        }
        assertEquals(
            FaceGameProgressDTO(2, 0, "character_07"),
            FaceGameProgressService.startRun(ordinaryL3Child, "character_07").data
        )

        val terminalChild = transaction {
            Children.insertAndGetId {
                it[Children.parentId] = children.parentId
                it[name] = "legacy terminal"
                it[age] = 6
            }.value
        }
        transaction {
            FaceGameProgress.insert {
                it[FaceGameProgress.childId] = terminalChild
                it[FaceGameProgress.levelIndex] = 2
                it[FaceGameProgress.successfulGames] = 1
                it[FaceGameProgress.activeCharacterId] = null
            }
        }
        assertEquals(
            FaceGameProgressDTO(0, 0, "character_03"),
            FaceGameProgressService.startRun(terminalChild, "character_03").data
        )
    }

    @Test
    fun migrationAddsNullableColumnToExistingTableAndCanBeRerun() {
        val database = Database.connect(
            url = "jdbc:h2:mem:face_migration_${UUID.randomUUID()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
            driver = "org.h2.Driver"
        )
        transaction(database) {
            SchemaUtils.create(Parents, Children)
            exec("CREATE TABLE face_game_progress (id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, child_id INT NOT NULL UNIQUE, level_index INT NOT NULL DEFAULT 0, successful_games INT NOT NULL DEFAULT 0, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)")
            exec("INSERT INTO face_game_progress (child_id, level_index, successful_games) VALUES (51, 1, 0)")
        }
        FaceGameActiveCharacterMigration.migrate()
        FaceGameActiveCharacterMigration.migrate()
        val result = transaction(database) {
            val columns = exec("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'FACE_GAME_PROGRESS'") { rs ->
                buildSet { while (rs.next()) add(rs.getString(1).lowercase()) }
            }
            val oldRow = exec("SELECT child_id, level_index, successful_games, active_character_id FROM face_game_progress") { rs ->
                if (rs.next()) listOf(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getString(4)) else emptyList()
            }
            columns to oldRow
        }
        assertNotNull(result)
        assertTrue("active_character_id" in result.first.orEmpty())
        assertEquals(listOf(51, 1, 0, null), result.second, "migration preserves legacy progress")
    }
}
