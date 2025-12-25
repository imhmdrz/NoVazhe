package mohaamadreza.saemipour.no.vazheh.services

import mohaamadreza.saemipour.no.vazheh.database.tables.Children
import mohaamadreza.saemipour.no.vazheh.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Child Service - سرویس مدیریت فرزندان
 * مادر می‌تواند ۱ یا ۲ فرزند اضافه کند
 */
object ChildService {
    
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private const val MAX_CHILDREN = 2 // حداکثر ۲ فرزند
    
    /**
     * Get all children of a parent
     * دریافت لیست فرزندان مادر
     */
    fun getChildren(parentId: Int): ListResponse<ChildDTO> {
        return transaction {
            val children = Children.selectAll()
                .where { (Children.parentId eq parentId) and (Children.isActive eq true) }
                .orderBy(Children.createdAt)
                .map { row ->
                    ChildDTO(
                        id = row[Children.id].value,
                        parentId = row[Children.parentId].value,
                        name = row[Children.name],
                        age = row[Children.age],
                        avatarUrl = row[Children.avatarUrl],
                        isActive = row[Children.isActive],
                        createdAt = row[Children.createdAt].format(dateFormatter)
                    )
                }
            
            ListResponse(true, children, children.size)
        }
    }
    
    /**
     * Get child by ID (verify parent ownership)
     * دریافت فرزند با شناسه
     */
    fun getChildById(parentId: Int, childId: Int): ApiResponse<ChildDTO> {
        return transaction {
            val row = Children.selectAll()
                .where { (Children.id eq childId) and (Children.parentId eq parentId) and (Children.isActive eq true) }
                .singleOrNull()
                ?: return@transaction ApiResponse(false, "فرزند یافت نشد", null)
            
            val child = ChildDTO(
                id = row[Children.id].value,
                parentId = row[Children.parentId].value,
                name = row[Children.name],
                age = row[Children.age],
                avatarUrl = row[Children.avatarUrl],
                isActive = row[Children.isActive],
                createdAt = row[Children.createdAt].format(dateFormatter)
            )
            
            ApiResponse(true, "موفق", child)
        }
    }
    
    /**
     * Create a new child
     * ایجاد فرزند جدید (حداکثر ۲ فرزند)
     */
    fun createChild(parentId: Int, request: CreateChildRequest): ApiResponse<ChildDTO> {
        return transaction {
            // Check max children limit
            val currentCount = Children.selectAll()
                .where { (Children.parentId eq parentId) and (Children.isActive eq true) }
                .count()
            
            if (currentCount >= MAX_CHILDREN) {
                return@transaction ApiResponse(false, "شما حداکثر $MAX_CHILDREN فرزند می‌توانید اضافه کنید", null)
            }
            
            val now = LocalDateTime.now()
            val childId = Children.insertAndGetId {
                it[Children.parentId] = parentId
                it[name] = request.name
                it[age] = request.age
                it[avatarUrl] = request.avatarUrl
                it[createdAt] = now
                it[updatedAt] = now
            }
            
            val child = ChildDTO(
                id = childId.value,
                parentId = parentId,
                name = request.name,
                age = request.age,
                avatarUrl = request.avatarUrl,
                isActive = true,
                createdAt = now.format(dateFormatter)
            )
            
            ApiResponse(true, "فرزند با موفقیت اضافه شد", child)
        }
    }
    
    /**
     * Update child
     * ویرایش فرزند
     */
    fun updateChild(parentId: Int, childId: Int, request: UpdateChildRequest): ApiResponse<ChildDTO> {
        return transaction {
            // Verify ownership
            val existingChild = Children.selectAll()
                .where { (Children.id eq childId) and (Children.parentId eq parentId) and (Children.isActive eq true) }
                .singleOrNull()
                ?: return@transaction ApiResponse(false, "فرزند یافت نشد", null)
            
            val now = LocalDateTime.now()
            Children.update({ Children.id eq childId }) {
                request.name?.let { name -> it[Children.name] = name }
                request.age?.let { age -> it[Children.age] = age }
                request.avatarUrl?.let { url -> it[avatarUrl] = url }
                it[updatedAt] = now
            }
            
            val child = ChildDTO(
                id = childId,
                parentId = parentId,
                name = request.name ?: existingChild[Children.name],
                age = request.age ?: existingChild[Children.age],
                avatarUrl = request.avatarUrl ?: existingChild[Children.avatarUrl],
                isActive = true,
                createdAt = existingChild[Children.createdAt].format(dateFormatter)
            )
            
            ApiResponse(true, "فرزند با موفقیت ویرایش شد", child)
        }
    }
    
    /**
     * Delete child (soft delete)
     * حذف فرزند
     */
    fun deleteChild(parentId: Int, childId: Int): MessageResponse {
        return transaction {
            val deleted = Children.update({ (Children.id eq childId) and (Children.parentId eq parentId) }) {
                it[isActive] = false
                it[updatedAt] = LocalDateTime.now()
            }
            
            if (deleted > 0) {
                MessageResponse(true, "فرزند با موفقیت حذف شد")
            } else {
                MessageResponse(false, "فرزند یافت نشد")
            }
        }
    }
    
    /**
     * Verify child belongs to parent
     */
    fun verifyChildOwnership(parentId: Int, childId: Int): Boolean {
        return transaction {
            Children.selectAll()
                .where { (Children.id eq childId) and (Children.parentId eq parentId) and (Children.isActive eq true) }
                .singleOrNull() != null
        }
    }
}






