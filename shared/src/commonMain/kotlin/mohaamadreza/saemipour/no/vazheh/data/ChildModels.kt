package mohaamadreza.saemipour.no.vazheh.data

import kotlinx.serialization.Serializable

// ==================== Child DTOs - فرزندان ====================

@Serializable
data class ChildDTO(
    val id: Int,
    val parentId: Int,
    val name: String,
    val age: Int,
    val avatarUrl: String?,
    val isActive: Boolean,
    val createdAt: String
)

@Serializable
data class CreateChildRequest(
    val name: String,
    val age: Int,
    val avatarUrl: String? = null
)

@Serializable
data class UpdateChildRequest(
    val name: String? = null,
    val age: Int? = null,
    val avatarUrl: String? = null
)

