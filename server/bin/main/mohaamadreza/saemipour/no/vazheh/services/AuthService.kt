package mohaamadreza.saemipour.no.vazheh.services

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import mohaamadreza.saemipour.no.vazheh.database.tables.Parents
import mohaamadreza.saemipour.no.vazheh.models.*
import mohaamadreza.saemipour.no.vazheh.security.JwtConfig
import mohaamadreza.saemipour.no.vazheh.security.PasswordUtils
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

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





