package mohaamadreza.saemipour.no.vazheh.services

import mohaamadreza.saemipour.no.vazheh.database.tables.Parents
import mohaamadreza.saemipour.no.vazheh.models.AuthResponse
import mohaamadreza.saemipour.no.vazheh.models.LoginRequest
import mohaamadreza.saemipour.no.vazheh.models.ParentDTO
import mohaamadreza.saemipour.no.vazheh.models.RegisterRequest
import mohaamadreza.saemipour.no.vazheh.security.JwtConfig
import mohaamadreza.saemipour.no.vazheh.security.PasswordUtils
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** Authentication Service - سرویس احراز هویت ثبت نام و ورود والدین */
object AuthService {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    /** Register a new parent (mother) ثبت نام مادر جدید */
    fun register(request: RegisterRequest): AuthResponse {
        return transaction {
            // Check if username already exists
            val existingParent =
                    Parents.selectAll()
                            .where { Parents.username eq request.username }
                            .singleOrNull()
            if (existingParent != null) {
                return@transaction AuthResponse(false, "این نام کاربری قبلاً ثبت شده است")
            }

            // Create parent
            val parentId =
                    Parents.insertAndGetId {
                        it[username] = request.username
                        it[passwordHash] = PasswordUtils.hashPassword(request.password)
                        it[displayName] = request.displayName
                        it[createdAt] = LocalDateTime.now()
                        it[updatedAt] = LocalDateTime.now()
                    }

            val token = JwtConfig.makeToken(parentId.value)
            val parent = getParentById(parentId.value)

            AuthResponse(true, "ثبت نام با موفقیت انجام شد", token, parent)
        }
    }

    /** Login parent ورود مادر */
    fun login(request: LoginRequest): AuthResponse {
        return transaction {
            val parentRow =
                    Parents.selectAll()
                            .where { Parents.username eq request.username }
                            .singleOrNull()
                            ?: return@transaction AuthResponse(
                                    false,
                                    "نام کاربری یا رمز عبور اشتباه است"
                            )

            if (!PasswordUtils.verifyPassword(request.password, parentRow[Parents.passwordHash])) {
                return@transaction AuthResponse(false, "نام کاربری یا رمز عبور اشتباه است")
            }

            if (!parentRow[Parents.isActive]) {
                return@transaction AuthResponse(false, "حساب کاربری غیرفعال است")
            }

            val parentId = parentRow[Parents.id].value
            val token = JwtConfig.makeToken(parentId)
            val parent = getParentById(parentId)

            AuthResponse(true, "ورود با موفقیت انجام شد", token, parent)
        }
    }

    /** Test mode without login - allows app testing without authentication تست بدون ورود */
    fun testWithoutLogin(): AuthResponse {
        return transaction {
            // Create or get test user
            val testUsername = "test_user"
            val existingTestUser = Parents.selectAll()
                    .where { Parents.username eq testUsername }
                    .singleOrNull()

            val parentId = existingTestUser?.let { it[Parents.id].value }
                    ?: run {
                        // Create test user if it doesn't exist
                        Parents.insertAndGetId {
                            it[username] = testUsername
                            it[passwordHash] = PasswordUtils.hashPassword("test123")
                            it[displayName] = "Test User"
                            it[createdAt] = LocalDateTime.now()
                            it[updatedAt] = LocalDateTime.now()
                        }.value
                    }

            val token = JwtConfig.makeToken(parentId)
            val parent = getParentById(parentId)

            AuthResponse(true, "تست بدون ورود فعال شد", token, parent)
        }
    }

    /** Get parent by ID */
    fun getParentById(parentId: Int): ParentDTO? {
        return transaction {
            Parents.selectAll().where { Parents.id eq parentId }.singleOrNull()?.let { row ->
                ParentDTO(
                        id = row[Parents.id].value,
                        username = row[Parents.username],
                        displayName = row[Parents.displayName],
                        isActive = row[Parents.isActive],
                        createdAt = row[Parents.createdAt].format(dateFormatter)
                )
            }
        }
    }
}






