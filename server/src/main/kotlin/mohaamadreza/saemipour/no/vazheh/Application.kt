package mohaamadreza.saemipour.no.vazheh

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import mohaamadreza.saemipour.no.vazheh.database.DatabaseConfig
import mohaamadreza.saemipour.no.vazheh.models.MessageResponse
import mohaamadreza.saemipour.no.vazheh.routes.authRoutes
import mohaamadreza.saemipour.no.vazheh.routes.childRoutes
import mohaamadreza.saemipour.no.vazheh.routes.contentRoutes
import mohaamadreza.saemipour.no.vazheh.routes.progressRoutes
import mohaamadreza.saemipour.no.vazheh.routes.quizRoutes
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

fun Application.configureRouting() {
    routing {
        // Health check endpoint
        get("/") {
            call.respondText("🚀 نواژه - سرور گفتار درمانی در حال اجراست! مستندات API: /swagger")
        }
        
        get("/health") {
            call.respond(
                HttpStatusCode.OK,
                MessageResponse(true, "سرور در حال اجراست")
            )
        }

            // OpenAPI JSON endpoint
        openAPI(path = "/openapi", swaggerFile = "openapi/documentation.yaml")

        // Swagger UI
        swaggerUI(path = "/swagger", swaggerFile = "openapi/documentation.yaml") {
            version = "5.17.14"
        }
        
        // API routes
        authRoutes()        // /api/auth/*
        childRoutes()       // /api/children/*
        contentRoutes()     // /api/categories/*, /api/words/*, /api/custom-words/*
        quizRoutes()        // /api/quiz/*
        progressRoutes()    // /api/progress/*
    }
}
