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
import mohaamadreza.saemipour.no.vazheh.models.CompleteFaceGameRequest
import mohaamadreza.saemipour.no.vazheh.models.FaceGameProgressDTO
import mohaamadreza.saemipour.no.vazheh.models.StartFaceGameRunRequest
import mohaamadreza.saemipour.no.vazheh.models.FinalizeFaceGameRunRequest
import mohaamadreza.saemipour.no.vazheh.services.ChildService
import mohaamadreza.saemipour.no.vazheh.services.FaceGameProgressService

/** Authenticated, child-owned Face / Body progression endpoints. */
fun Route.faceGameRoutes() {
    route("/api/face-game") {
        authenticate("auth-jwt") {
            get("/progress/{childId}") {
                try {
                    val parentId = call.principal<JWTPrincipal>()!!.payload.getClaim("parentId").asInt()
                    val childId = call.parameters["childId"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse<FaceGameProgressDTO>(false, "شناسه نامعتبر", null))
                    if (!ChildService.verifyChildOwnership(parentId, childId)) {
                        return@get call.respond(HttpStatusCode.Forbidden, ApiResponse<FaceGameProgressDTO>(false, "دسترسی غیرمجاز", null))
                    }
                    call.respond(HttpStatusCode.OK, ApiResponse(true, "موفق", FaceGameProgressService.getProgress(childId)))
                } catch (_: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse<FaceGameProgressDTO>(false, "خطای داخلی سرور", null))
                }
            }
            post("/progress/{childId}/complete") {
                try {
                    val parentId = call.principal<JWTPrincipal>()!!.payload.getClaim("parentId").asInt()
                    val childId = call.parameters["childId"]?.toIntOrNull()
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<FaceGameProgressDTO>(false, "شناسه نامعتبر", null))
                    if (!ChildService.verifyChildOwnership(parentId, childId)) {
                        return@post call.respond(HttpStatusCode.Forbidden, ApiResponse<FaceGameProgressDTO>(false, "دسترسی غیرمجاز", null))
                    }
                    val request = call.receive<CompleteFaceGameRequest>()
                    val response = FaceGameProgressService.completeLevel(
                        childId, request.levelIndex, request.characterId
                    )
                    call.respond(if (response.success) HttpStatusCode.OK else HttpStatusCode.BadRequest, response)
                } catch (_: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse<FaceGameProgressDTO>(false, "خطای داخلی سرور", null))
                }
            }
            post("/progress/{childId}/run") {
                try {
                    val parentId = call.principal<JWTPrincipal>()!!.payload.getClaim("parentId").asInt()
                    val childId = call.parameters["childId"]?.toIntOrNull()
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<FaceGameProgressDTO>(false, "شناسه نامعتبر", null))
                    if (!ChildService.verifyChildOwnership(parentId, childId)) {
                        return@post call.respond(HttpStatusCode.Forbidden, ApiResponse<FaceGameProgressDTO>(false, "دسترسی غیرمجاز", null))
                    }
                    val request = call.receive<StartFaceGameRunRequest>()
                    val response = FaceGameProgressService.startRun(childId, request.characterId)
                    call.respond(if (response.success) HttpStatusCode.OK else HttpStatusCode.BadRequest, response)
                } catch (_: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse<FaceGameProgressDTO>(false, "خطای داخلی سرور", null))
                }
            }
            post("/progress/{childId}/finalize") {
                try {
                    val parentId = call.principal<JWTPrincipal>()!!.payload.getClaim("parentId").asInt()
                    val childId = call.parameters["childId"]?.toIntOrNull()
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<FaceGameProgressDTO>(false, "شناسه نامعتبر", null))
                    if (!ChildService.verifyChildOwnership(parentId, childId)) {
                        return@post call.respond(HttpStatusCode.Forbidden, ApiResponse<FaceGameProgressDTO>(false, "دسترسی غیرمجاز", null))
                    }
                    val request = call.receive<FinalizeFaceGameRunRequest>()
                    val response = FaceGameProgressService.finalizeRun(
                        childId, request.completedCharacterId, request.nextCharacterId
                    )
                    call.respond(if (response.success) HttpStatusCode.OK else HttpStatusCode.BadRequest, response)
                } catch (_: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse<FaceGameProgressDTO>(false, "خطای داخلی سرور", null))
                }
            }
        }
    }
}
