package mohaamadreza.saemipour.no.vazheh.data

import kotlinx.serialization.Serializable

// ==================== Authentication DTOs ====================

@Serializable data class LoginRequest(val username: String, val password: String)

@Serializable
data class RegisterRequest(val username: String, val password: String, val displayName: String)

/**
 * Verify the account password of the already logged-in parent - تأیید رمز عبور حساب
 * The parent is identified by the JWT, so only the password travels in the body.
 */
@Serializable data class VerifyPasswordRequest(val password: String)

@Serializable
data class AuthResponse(
        val success: Boolean,
        val message: String,
        val token: String? = null,
        val parent: ParentDTO? = null
)

@Serializable
data class ParentDTO(
        val id: Int,
        val username: String,
        val displayName: String,
        val isActive: Boolean,
        val createdAt: String
)

// ==================== Generic Response DTOs ====================

@Serializable
data class ApiResponse<T>(val success: Boolean, val message: String, val data: T? = null)
