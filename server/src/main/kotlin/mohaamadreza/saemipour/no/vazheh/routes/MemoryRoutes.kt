package mohaamadreza.saemipour.no.vazheh.routes

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mohaamadreza.saemipour.no.vazheh.models.*
import mohaamadreza.saemipour.no.vazheh.services.ChildService
import mohaamadreza.saemipour.no.vazheh.services.MemoryProgressService

/**
 * Memory Routes - مسیرهای پیشرفت بازی حافظه
 * هر فرزند پیشرفت مستقل خود را در نردبان ابعاد (2x2 تا 4x4) دارد
 */
fun Route.memoryRoutes() {
    route("/api/memory") {

        authenticate("auth-jwt") {

            /**
             * GET /api/memory/progress/{childId}
             * Current memory-game progression for a child
             * دریافت پیشرفت فعلی بازی حافظه فرزند
             */
            get("/progress/{childId}") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val parentId = principal.payload.getClaim("parentId").asInt()

                    val childId = call.parameters["childId"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse<MemoryProgressDTO>(false, "شناسه نامعتبر", null))

                    if (!ChildService.verifyChildOwnership(parentId, childId)) {
                        call.respond(HttpStatusCode.Forbidden, ApiResponse<MemoryProgressDTO>(false, "دسترسی غیرمجاز", null))
                        return@get
                    }

                    val progress = MemoryProgressService.getProgress(childId)
                    call.respond(HttpStatusCode.OK, ApiResponse(true, "موفق", progress))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse<MemoryProgressDTO>(false, "خطای داخلی سرور", null))
                }
            }

            /**
             * POST /api/memory/progress/{childId}/complete
             * Record one successful memory game at the reported dimension
             * ثبت یک برد موفق بازی حافظه در ابعاد گزارش‌شده
             */
            post("/progress/{childId}/complete") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val parentId = principal.payload.getClaim("parentId").asInt()

                    val childId = call.parameters["childId"]?.toIntOrNull()
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<MemoryProgressDTO>(false, "شناسه نامعتبر", null))

                    // Verify child ownership — the client's token defines the parent identity.
                    if (!ChildService.verifyChildOwnership(parentId, childId)) {
                        call.respond(HttpStatusCode.Forbidden, ApiResponse<MemoryProgressDTO>(false, "دسترسی غیرمجاز", null))
                        return@post
                    }

                    val request = call.receive<CompleteMemoryGameRequest>()
                    val response = MemoryProgressService.recordSuccessfulGame(childId, request.dimensionIndex)
                    val status = if (response.success) HttpStatusCode.OK else HttpStatusCode.BadRequest
                    call.respond(status, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse<MemoryProgressDTO>(false, "خطای داخلی سرور", null))
                }
            }
        }
    }
}
