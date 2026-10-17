package mohaamadreza.saemipour.no.vazheh.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import mohaamadreza.saemipour.no.vazheh.models.ApiResponse
import mohaamadreza.saemipour.no.vazheh.models.CompleteShadowMatchGameRequest
import mohaamadreza.saemipour.no.vazheh.models.ShadowMatchProgressDTO
import mohaamadreza.saemipour.no.vazheh.services.ChildService
import mohaamadreza.saemipour.no.vazheh.services.ShadowMatchProgressService

/** Authenticated, child-owned Shadow Match progress endpoints. */
fun Route.shadowMatchRoutes() {
    route("/api/shadow-match") {
        authenticate("auth-jwt") {
            get("/progress/{childId}") {
                try {
                    val parentId = call.principal<JWTPrincipal>()!!.payload.getClaim("parentId").asInt()
                    val childId = call.parameters["childId"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse<ShadowMatchProgressDTO>(false, "شناسه نامعتبر", null))
                    if (!ChildService.verifyChildOwnership(parentId, childId)) {
                        return@get call.respond(HttpStatusCode.Forbidden, ApiResponse<ShadowMatchProgressDTO>(false, "دسترسی غیرمجاز", null))
                    }
                    call.respond(HttpStatusCode.OK, ApiResponse(true, "موفق", ShadowMatchProgressService.getProgress(childId)))
                } catch (_: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse<ShadowMatchProgressDTO>(false, "خطای داخلی سرور", null))
                }
            }
            post("/progress/{childId}/complete") {
                try {
                    val parentId = call.principal<JWTPrincipal>()!!.payload.getClaim("parentId").asInt()
                    val childId = call.parameters["childId"]?.toIntOrNull()
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<ShadowMatchProgressDTO>(false, "شناسه نامعتبر", null))
                    if (!ChildService.verifyChildOwnership(parentId, childId)) {
                        return@post call.respond(HttpStatusCode.Forbidden, ApiResponse<ShadowMatchProgressDTO>(false, "دسترسی غیرمجاز", null))
                    }
                    val request = call.receive<CompleteShadowMatchGameRequest>()
                    val response = ShadowMatchProgressService.recordSuccessfulGame(childId, request.levelIndex)
                    call.respond(if (response.success) HttpStatusCode.OK else HttpStatusCode.BadRequest, response)
                } catch (_: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse<ShadowMatchProgressDTO>(false, "خطای داخلی سرور", null))
                }
            }
        }
    }
}
