package mohaamadreza.saemipour.no.vazheh.routes

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mohaamadreza.saemipour.no.vazheh.models.*
import mohaamadreza.saemipour.no.vazheh.services.ChildService
import mohaamadreza.saemipour.no.vazheh.services.ContentService

/**
 * Content Routes - مسیرهای محتوا
 * دسته‌بندی‌ها، کلمات و کلمات سفارشی
 */
fun Route.contentRoutes() {
    route("/api") {
        
        // ==================== Categories - دسته‌بندی‌ها ====================
        
        /**
         * GET /api/categories
         * Get all categories
         * دریافت همه دسته‌بندی‌ها
         */
        get("/categories") {
            try {
                val response = ContentService.getAllCategories()
                call.respond(HttpStatusCode.OK, response)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ListResponse<CategoryDTO>(false, emptyList(), 0))
            }
        }
        
        /**
         * POST /api/categories
         * Create a new category (authenticated parents only)
         * ایجاد دسته‌بندی جدید توسط مادر
         *
         * Categories are shared globally (no per-parent ownership) but creation
         * requires authentication so anonymous traffic can't pollute the catalog.
         */
        authenticate("auth-jwt") {
            post("/categories") {
                try {
                    val request = call.receive<CreateCategoryRequest>()

                    if (request.nameFa.isBlank()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse<CategoryDTO>(false, "نام فارسی دسته‌بندی الزامی است", null)
                        )
                        return@post
                    }

                    val response = ContentService.createCategory(request)
                    val status = if (response.success) HttpStatusCode.Created else HttpStatusCode.BadRequest
                    call.respond(status, response)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<CategoryDTO>(false, "خطای داخلی سرور", null)
                    )
                }
            }
        }

        /**
         * GET /api/categories/{id}
         * Get category by ID
         * دریافت دسته‌بندی با شناسه
         */
        get("/categories/{id}") {
            try {
                val categoryId = call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse<CategoryDTO>(false, "شناسه نامعتبر", null))
                
                val response = ContentService.getCategoryById(categoryId)
                val status = if (response.success) HttpStatusCode.OK else HttpStatusCode.NotFound
                call.respond(status, response)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ApiResponse<CategoryDTO>(false, "خطای داخلی سرور", null))
            }
        }
        
        // ==================== Words - کلمات ====================
        
        /**
         * GET /api/categories/{id}/words
         * Get words by category
         * دریافت کلمات یک دسته‌بندی
         */
        get("/categories/{id}/words") {
            try {
                val categoryId = call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ListResponse<WordDTO>(false, emptyList(), 0))
                
                val response = ContentService.getWordsByCategory(categoryId)
                call.respond(HttpStatusCode.OK, response)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ListResponse<WordDTO>(false, emptyList(), 0))
            }
        }
        
        /**
         * GET /api/categories/{id}/words?childId={childId}
         * Get words by category with child progress
         * دریافت کلمات یک دسته‌بندی با پیشرفت فرزند
         */
        authenticate("auth-jwt") {
            get("/categories/{categoryId}/words/progress") {
                try {
                    val categoryId = call.parameters["categoryId"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ListResponse<WordWithProgressDTO>(false, emptyList(), 0))
                    
                    val childId = call.request.queryParameters["childId"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ListResponse<WordWithProgressDTO>(false, emptyList(), 0))
                    
                    // Verify parent owns child
                    val principal = call.principal<JWTPrincipal>()!!
                    val parentId = principal.payload.getClaim("parentId").asInt()
                    if (!ChildService.verifyChildOwnership(parentId, childId)) {
                        call.respond(HttpStatusCode.Forbidden, ListResponse<WordWithProgressDTO>(false, emptyList(), 0))
                        return@get
                    }
                    
                    val response = ContentService.getWordsByCategoryWithProgress(categoryId, childId)
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ListResponse<WordWithProgressDTO>(false, emptyList(), 0))
                }
            }
        }
        
        /**
         * GET /api/words/{id}
         * Get word by ID
         * دریافت کلمه با شناسه
         */
        get("/words/{id}") {
            try {
                val wordId = call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse<WordDTO>(false, "شناسه نامعتبر", null))
                
                val response = ContentService.getWordById(wordId)
                val status = if (response.success) HttpStatusCode.OK else HttpStatusCode.NotFound
                call.respond(status, response)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ApiResponse<WordDTO>(false, "خطای داخلی سرور", null))
            }
        }
        
        // ==================== Custom Words - کلمات سفارشی ====================
        
        authenticate("auth-jwt") {
            route("/custom-words") {
                
                /**
                 * GET /api/custom-words
                 * Get custom words created by parent
                 * دریافت کلمات سفارشی مادر
                 */
                get {
                    try {
                        val principal = call.principal<JWTPrincipal>()!!
                        val parentId = principal.payload.getClaim("parentId").asInt()
                        
                        val response = ContentService.getCustomWords(parentId)
                        call.respond(HttpStatusCode.OK, response)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, ListResponse<CustomWordDTO>(false, emptyList(), 0))
                    }
                }
                
                /**
                 * POST /api/custom-words
                 * Create custom word
                 * ایجاد کلمه سفارشی
                 */
                post {
                    try {
                        val principal = call.principal<JWTPrincipal>()!!
                        val parentId = principal.payload.getClaim("parentId").asInt()
                        
                        val request = call.receive<CreateCustomWordRequest>()
                        
                        if (request.wordFa.isBlank()) {
                            call.respond(HttpStatusCode.BadRequest, ApiResponse<CustomWordDTO>(false, "کلمه فارسی الزامی است", null))
                            return@post
                        }
                        if (request.audioUrl.isBlank()) {
                            call.respond(HttpStatusCode.BadRequest, ApiResponse<CustomWordDTO>(false, "صوت الزامی است", null))
                            return@post
                        }
                        
                        val response = ContentService.createCustomWord(parentId, request)
                        val status = if (response.success) HttpStatusCode.Created else HttpStatusCode.BadRequest
                        call.respond(status, response)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, ApiResponse<CustomWordDTO>(false, "خطای داخلی سرور", null))
                    }
                }
                
                /**
                 * DELETE /api/custom-words/{id}
                 * Delete custom word
                 * حذف کلمه سفارشی
                 */
                delete("/{id}") {
                    try {
                        val principal = call.principal<JWTPrincipal>()!!
                        val parentId = principal.payload.getClaim("parentId").asInt()
                        
                        val wordId = call.parameters["id"]?.toIntOrNull()
                            ?: return@delete call.respond(HttpStatusCode.BadRequest, MessageResponse(false, "شناسه نامعتبر"))
                        
                        val response = ContentService.deleteCustomWord(parentId, wordId)
                        val status = if (response.success) HttpStatusCode.OK else HttpStatusCode.NotFound
                        call.respond(status, response)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, MessageResponse(false, "خطای داخلی سرور"))
                    }
                }
            }
        }
    }
}
