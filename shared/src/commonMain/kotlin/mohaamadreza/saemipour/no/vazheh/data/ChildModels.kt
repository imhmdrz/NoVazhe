package mohaamadreza.saemipour.no.vazheh.data

import kotlinx.serialization.Serializable

// ==================== Child DTOs - فرزندان ====================

/**
 * Gender enum for children
 * جنسیت فرزند - پسر یا دختر
 */
@Serializable
enum class Gender {
    BOY,   // پسر
    GIRL   // دختر
}

@Serializable
data class ChildDTO(
    val id: Int,
    val parentId: Int,
    val name: String,
    val age: Int,
    val gender: Gender,
    val avatarUrl: String?,
    val isActive: Boolean,
    val createdAt: String
)

@Serializable
data class CreateChildRequest(
    val name: String,
    val age: Int,
    val gender: Gender,
    val avatarUrl: String? = null
)

@Serializable
data class UpdateChildRequest(
    val name: String? = null,
    val age: Int? = null,
    val gender: Gender? = null,
    val avatarUrl: String? = null
)

