package mohaamadreza.saemipour.no.vazheh.routes

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mohaamadreza.saemipour.no.vazheh.models.ApiResponse
import mohaamadreza.saemipour.no.vazheh.models.ColorSortingProgressDTO
import mohaamadreza.saemipour.no.vazheh.models.CompleteColorSortingGameRequest
import mohaamadreza.saemipour.no.vazheh.services.ChildService
import mohaamadreza.saemipour.no.vazheh.services.ColorSortingProgressService

/**
 * Color Sorting Routes - مسیرهای پیشرفت بازی رنگ‌ها
 * هر فرزند پیشرفت مستقل خود را در سطوح ۳، ۴ و ۵ رنگ دارد.
 */
fun Route.colorSortingRoutes() {
    route("/api/color-sorting") {
        authenticate("auth-jwt") {
            get("/progress/{childId}") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val parentId = principal.payload.getClaim("parentId").asInt()

                    val childId = call.parameters["childId"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse<ColorSortingProgressDTO>(false, "شناسه نامعتبر", null))

                    if (!ChildService.verifyChildOwnership(parentId, childId)) {
                        call.respond(HttpStatusCode.Forbidden, ApiResponse<ColorSortingProgressDTO>(false, "دسترسی غیرمجاز", null))
                        return@get
                    }

                    val progress = ColorSortingProgressService.getProgress(childId)
                    call.respond(HttpStatusCode.OK, ApiResponse(true, "موفق", progress))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse<ColorSortingProgressDTO>(false, "خطا: ${e.message}", null))
                }
            }

            post("/progress/{childId}/complete") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val parentId = principal.payload.getClaim("parentId").asInt()

                    val childId = call.parameters["childId"]?.toIntOrNull()
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<ColorSortingProgressDTO>(false, "شناسه نامعتبر", null))

                    if (!ChildService.verifyChildOwnership(parentId, childId)) {
                        call.respond(HttpStatusCode.Forbidden, ApiResponse<ColorSortingProgressDTO>(false, "دسترسی غیرمجاز", null))
                        return@post
                    }

                    val request = call.receive<CompleteColorSortingGameRequest>()
                    val response = ColorSortingProgressService.recordSuccessfulGame(childId, request.levelIndex)
                    val status = if (response.success) HttpStatusCode.OK else HttpStatusCode.BadRequest
                    call.respond(status, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse<ColorSortingProgressDTO>(false, "خطا: ${e.message}", null))
                }
            }
        }
    }
}
