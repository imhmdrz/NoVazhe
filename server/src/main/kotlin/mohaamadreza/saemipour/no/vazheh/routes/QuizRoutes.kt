package mohaamadreza.saemipour.no.vazheh.routes

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mohaamadreza.saemipour.no.vazheh.models.*
import mohaamadreza.saemipour.no.vazheh.services.ChildService
import mohaamadreza.saemipour.no.vazheh.services.QuizService

/**
 * Quiz Routes - مسیرهای آزمون
 * صوت کلمه پخش می‌شود و فرزند کلمه درست را انتخاب می‌کند
 */
fun Route.quizRoutes() {
    route("/api/quiz") {

        // Quiz questions are public so guests (not logged in) can play. When a parent
        // is authenticated and passes a childId we prioritise that child's not-yet-learned
        // words; guests just get generic questions.
        authenticate("auth-jwt", optional = true) {

            /**
             * GET /api/quiz/question?categoryId={id}&childId={id}
             * Generate a quiz question
             * تولید یک سوال آزمون
             */
            get("/question") {
                try {
                    val parentId = call.principal<JWTPrincipal>()?.payload?.getClaim("parentId")?.asInt()

                    val categoryId = call.request.queryParameters["categoryId"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse<QuizQuestionDTO>(false, "شناسه دسته‌بندی الزامی است", null))

                    val childId = call.request.queryParameters["childId"]?.toIntOrNull()

                    // Only honour childId for the owning parent; ignore it for guests.
                    val effectiveChildId = if (parentId != null && childId != null) {
                        if (!ChildService.verifyChildOwnership(parentId, childId)) {
                            call.respond(HttpStatusCode.Forbidden, ApiResponse<QuizQuestionDTO>(false, "دسترسی غیرمجاز", null))
                            return@get
                        }
                        childId
                    } else null

                    val response = QuizService.generateQuizQuestion(categoryId, effectiveChildId)
                    val status = if (response.success) HttpStatusCode.OK else HttpStatusCode.BadRequest
                    call.respond(status, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse<QuizQuestionDTO>(false, "خطای داخلی سرور", null))
                }
            }

            /**
             * GET /api/quiz/questions?categoryId={id}&childId={id}&count={n}
             * Generate multiple quiz questions
             * تولید چند سوال آزمون
             */
            get("/questions") {
                try {
                    val parentId = call.principal<JWTPrincipal>()?.payload?.getClaim("parentId")?.asInt()

                    val categoryId = call.request.queryParameters["categoryId"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ListResponse<QuizQuestionDTO>(false, emptyList(), 0))

                    val childId = call.request.queryParameters["childId"]?.toIntOrNull()
                    val count = call.request.queryParameters["count"]?.toIntOrNull() ?: 5

                    // Only honour childId for the owning parent; ignore it for guests.
                    val effectiveChildId = if (parentId != null && childId != null) {
                        if (!ChildService.verifyChildOwnership(parentId, childId)) {
                            call.respond(HttpStatusCode.Forbidden, ListResponse<QuizQuestionDTO>(false, emptyList(), 0))
                            return@get
                        }
                        childId
                    } else null

                    val response = QuizService.generateQuizQuestions(categoryId, effectiveChildId, count.coerceIn(1, 20))
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ListResponse<QuizQuestionDTO>(false, emptyList(), 0))
                }
            }
        }

        // Recording progress requires a real child, so it stays authenticated.
        authenticate("auth-jwt") {

            /**
             * POST /api/quiz/submit
             * Submit quiz answer
             * ثبت پاسخ آزمون
             */
            post("/submit") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val parentId = principal.payload.getClaim("parentId").asInt()
                    
                    val request = call.receive<SubmitQuizRequest>()
                    
                    // Verify child ownership
                    if (!ChildService.verifyChildOwnership(parentId, request.childId)) {
                        call.respond(HttpStatusCode.Forbidden, ApiResponse<QuizResultDTO>(false, "دسترسی غیرمجاز", null))
                        return@post
                    }
                    
                    val response = QuizService.submitQuizAnswer(request)
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse<QuizResultDTO>(false, "خطای داخلی سرور", null))
                }
            }
        }
    }
}





