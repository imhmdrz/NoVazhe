package mohaamadreza.saemipour.no.vazheh.routes

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mohaamadreza.saemipour.no.vazheh.models.*
import mohaamadreza.saemipour.no.vazheh.services.ChildService
import mohaamadreza.saemipour.no.vazheh.services.ProgressService

/**
 * Progress Routes - مسیرهای پیشرفت
 * میزان پیشرفت فرزند
 */
fun Route.progressRoutes() {
    route("/api/progress") {
        
        authenticate("auth-jwt") {
            
            /**
             * GET /api/progress/child/{childId}
             * Get child progress stats
             * دریافت آمار پیشرفت فرزند
             */
            get("/child/{childId}") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val parentId = principal.payload.getClaim("parentId").asInt()
                    
                    val childId = call.parameters["childId"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse<ChildProgressStatsDTO>(false, "شناسه نامعتبر", null))
                    
                    val response = ProgressService.getChildProgressStats(parentId, childId)
                    val status = if (response.success) HttpStatusCode.OK else HttpStatusCode.NotFound
                    call.respond(status, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse<ChildProgressStatsDTO>(false, "خطا: ${e.message}", null))
                }
            }
            
            /**
             * GET /api/progress/child/{childId}/recent
             * Get recent quiz attempts for child
             * دریافت آزمون‌های اخیر فرزند
             */
            get("/child/{childId}/recent") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val parentId = principal.payload.getClaim("parentId").asInt()
                    
                    val childId = call.parameters["childId"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ListResponse<RecentQuizAttemptDTO>(false, emptyList(), 0))
                    
                    val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                    
                    val response = ProgressService.getRecentAttempts(parentId, childId, limit.coerceIn(1, 100))
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ListResponse<RecentQuizAttemptDTO>(false, emptyList(), 0))
                }
            }
            
            /**
             * GET /api/progress/child/{childId}/word/{wordId}
             * Get child progress for specific word
             * دریافت پیشرفت فرزند برای کلمه خاص
             */
            get("/child/{childId}/word/{wordId}") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val parentId = principal.payload.getClaim("parentId").asInt()
                    
                    val childId = call.parameters["childId"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse<WordProgressDTO>(false, "شناسه فرزند نامعتبر", null))
                    
                    val wordId = call.parameters["wordId"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse<WordProgressDTO>(false, "شناسه کلمه نامعتبر", null))
                    
                    // Verify child ownership
                    if (!ChildService.verifyChildOwnership(parentId, childId)) {
                        call.respond(HttpStatusCode.Forbidden, ApiResponse<WordProgressDTO>(false, "دسترسی غیرمجاز", null))
                        return@get
                    }
                    
                    val response = ProgressService.getWordProgress(childId, wordId)
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse<WordProgressDTO>(false, "خطا: ${e.message}", null))
                }
            }
        }
    }
}
