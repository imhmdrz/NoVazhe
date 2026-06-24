package mohaamadreza.saemipour.no.vazheh.routes

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mohaamadreza.saemipour.no.vazheh.models.*
import mohaamadreza.saemipour.no.vazheh.services.AuthService

/** Authentication Routes - مسیرهای احراز هویت */
fun Route.authRoutes() {
    route("/api/auth") {

        /** POST /api/auth/register Register a new parent (mother) ثبت نام مادر جدید */
        post("/register") {
            try {
                val request = call.receive<RegisterRequest>()

                // Validate input
                if (request.username.isBlank()) {
                    call.respond(
                            HttpStatusCode.BadRequest,
                            AuthResponse(false, "نام کاربری نامعتبر است")
                    )
                    return@post
                }
                if (request.username.length < 3) {
                    call.respond(
                            HttpStatusCode.BadRequest,
                            AuthResponse(false, "نام کاربری باید حداقل ۳ کاراکتر باشد")
                    )
                    return@post
                }
                if (request.password.length < 6) {
                    call.respond(
                            HttpStatusCode.BadRequest,
                            AuthResponse(false, "رمز عبور باید حداقل ۶ کاراکتر باشد")
                    )
                    return@post
                }
                if (request.displayName.isBlank()) {
                    call.respond(
                            HttpStatusCode.BadRequest,
                            AuthResponse(false, "نام نمایشی الزامی است")
                    )
                    return@post
                }

                val response = AuthService.register(request)
                val status =
                        if (response.success) HttpStatusCode.Created else HttpStatusCode.BadRequest
                call.respond(status, response)
            } catch (e: Exception) {
                call.respond(
                        HttpStatusCode.InternalServerError,
                        AuthResponse(false, "خطا در ثبت نام: ${e.message}")
                )
            }
        }

        /** POST /api/auth/login Login parent ورود مادر */
        post("/login") {
            try {
                val request = call.receive<LoginRequest>()

                if (request.username.isBlank() || request.password.isBlank()) {
                    call.respond(
                            HttpStatusCode.BadRequest,
                            AuthResponse(false, "نام کاربری و رمز عبور الزامی است")
                    )
                    return@post
                }

                val response = AuthService.login(request)
                val status =
                        if (response.success) HttpStatusCode.OK else HttpStatusCode.Unauthorized
                call.respond(status, response)
            } catch (e: Exception) {
                call.respond(
                        HttpStatusCode.InternalServerError,
                        AuthResponse(false, "خطا در ورود: ${e.message}")
                )
            }
        }

        /** GET /api/auth/me Get current parent info دریافت اطلاعات مادر فعلی */
        authenticate("auth-jwt") {
            get("/me") {
                try {
                    val principal = call.principal<JWTPrincipal>()!!
                    val parentId = principal.payload.getClaim("parentId").asInt()

                    val parent = AuthService.getParentById(parentId)
                    if (parent != null) {
                        call.respond(HttpStatusCode.OK, ApiResponse(true, "موفق", parent))
                    } else {
                        call.respond(
                                HttpStatusCode.NotFound,
                                ApiResponse<ParentDTO>(false, "کاربر یافت نشد", null)
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                            HttpStatusCode.InternalServerError,
                            ApiResponse<ParentDTO>(false, "خطا: ${e.message}", null)
                    )
                }
            }
        }
    }
}
