package mohaamadreza.saemipour.no.vazheh.routes

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mohaamadreza.saemipour.no.vazheh.models.*
import mohaamadreza.saemipour.no.vazheh.services.ChildService

/**
 * Child Routes - مسیرهای مدیریت فرزندان
 * مادر می‌تواند ۱ یا ۲ فرزند اضافه کند
 */
fun Route.childRoutes() {
    route("/api/children") {
        
        // All child routes require authentication
        authenticate("auth-jwt") {
            
            /**
             * GET /api/children
             * Get all children of parent
             * دریافت لیست فرزندان
             */
            get {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val parentId = principal.payload.getClaim("parentId").asInt()
                    
                    val response = ChildService.getChildren(parentId)
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ListResponse<ChildDTO>(false, emptyList(), 0))
                }
            }
            
            /**
             * GET /api/children/{id}
             * Get child by ID
             * دریافت فرزند با شناسه
             */
            get("/{id}") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val parentId = principal.payload.getClaim("parentId").asInt()
                    
                    val childId = call.parameters["id"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse<ChildDTO>(false, "شناسه نامعتبر", null))
                    
                    val response = ChildService.getChildById(parentId, childId)
                    val status = if (response.success) HttpStatusCode.OK else HttpStatusCode.NotFound
                    call.respond(status, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse<ChildDTO>(false, "خطا: ${e.message}", null))
                }
            }
            
            /**
             * POST /api/children
             * Create a new child (max 2)
             * ایجاد فرزند جدید (حداکثر ۲ فرزند)
             */
            post {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val parentId = principal.payload.getClaim("parentId").asInt()
                    
                    val request = call.receive<CreateChildRequest>()
                    
                    // Validate
                    if (request.name.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse<ChildDTO>(false, "نام فرزند الزامی است", null))
                        return@post
                    }
                    if (request.age < 1 || request.age > 18) {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse<ChildDTO>(false, "سن باید بین ۱ تا ۱۸ سال باشد", null))
                        return@post
                    }
                    
                    val response = ChildService.createChild(parentId, request)
                    val status = if (response.success) HttpStatusCode.Created else HttpStatusCode.BadRequest
                    call.respond(status, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse<ChildDTO>(false, "خطا: ${e.message}", null))
                }
            }
            
            /**
             * PUT /api/children/{id}
             * Update child
             * ویرایش فرزند
             */
            put("/{id}") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val parentId = principal.payload.getClaim("parentId").asInt()
                    
                    val childId = call.parameters["id"]?.toIntOrNull()
                        ?: return@put call.respond(HttpStatusCode.BadRequest, ApiResponse<ChildDTO>(false, "شناسه نامعتبر", null))
                    
                    val request = call.receive<UpdateChildRequest>()
                    
                    val response = ChildService.updateChild(parentId, childId, request)
                    val status = if (response.success) HttpStatusCode.OK else HttpStatusCode.NotFound
                    call.respond(status, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse<ChildDTO>(false, "خطا: ${e.message}", null))
                }
            }
            
            /**
             * DELETE /api/children/{id}
             * Delete child
             * حذف فرزند
             */
            delete("/{id}") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val parentId = principal.payload.getClaim("parentId").asInt()
                    
                    val childId = call.parameters["id"]?.toIntOrNull()
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, MessageResponse(false, "شناسه نامعتبر"))
                    
                    val response = ChildService.deleteChild(parentId, childId)
                    val status = if (response.success) HttpStatusCode.OK else HttpStatusCode.NotFound
                    call.respond(status, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, MessageResponse(false, "خطا: ${e.message}"))
                }
            }
        }
    }
}


