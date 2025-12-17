package mohaamadreza.saemipour.no.vazheh

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import mohaamadreza.saemipour.no.vazheh.database.DatabaseConfig
import mohaamadreza.saemipour.no.vazheh.models.MessageResponse
import mohaamadreza.saemipour.no.vazheh.routes.*
import mohaamadreza.saemipour.no.vazheh.security.JwtConfig
import org.slf4j.event.Level

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // Initialize database
    DatabaseConfig.init()
    
    // Install plugins
    configureSerialization()
    configureCORS()
    configureAuthentication()
    configureStatusPages()
    configureCallLogging()
    
    // Configure routes
    configureRouting()
}

/**
 * Configure JSON serialization
 */
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }
}

/**
 * Configure CORS for cross-origin requests
 */
fun Application.configureCORS() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        
        allowCredentials = true
        
        // Allow all hosts in development
        anyHost()
    }
}

/**
 * Configure JWT Authentication
 */
fun Application.configureAuthentication() {
    val (issuer, audience, verifier) = JwtConfig.getConfigForKtor()
    
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "Novazheh Server"
            verifier(verifier)
            
            validate { credential ->
                if (credential.payload.getClaim("parentId").asInt() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    MessageResponse(false, "توکن نامعتبر یا منقضی شده است")
                )
            }
        }
    }
}

/**
 * Configure error handling
 */
fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                MessageResponse(false, "خطای سرور: ${cause.localizedMessage}")
            )
        }
        
        status(HttpStatusCode.NotFound) { call, _ ->
            call.respond(
                HttpStatusCode.NotFound,
                MessageResponse(false, "مسیر یافت نشد")
            )
        }
        
        status(HttpStatusCode.Unauthorized) { call, _ ->
            call.respond(
                HttpStatusCode.Unauthorized,
                MessageResponse(false, "احراز هویت الزامی است")
            )
        }
    }
}

/**
 * Configure request logging
 */
fun Application.configureCallLogging() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/api") }
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val path = call.request.path()
            "[$httpMethod] $path - $status"
        }
    }
}

/**
 * Configure all routes
 */
fun Application.configureRouting() {
    routing {
        // Health check endpoint
        get("/") {
            call.respondText("🚀 نواژه - سرور گفتار درمانی در حال اجراست!")
        }
        
        get("/health") {
            call.respond(
                HttpStatusCode.OK,
                MessageResponse(true, "سرور در حال اجراست")
            )
        }
        
        // API routes
        authRoutes()        // /api/auth/*
        childRoutes()       // /api/children/*
        contentRoutes()     // /api/categories/*, /api/words/*, /api/custom-words/*
        quizRoutes()        // /api/quiz/*
        progressRoutes()    // /api/progress/*
    }
}
