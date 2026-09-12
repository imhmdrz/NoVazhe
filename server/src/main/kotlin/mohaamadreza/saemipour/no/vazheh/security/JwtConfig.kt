package mohaamadreza.saemipour.no.vazheh.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtConfig {
    private val secret = System.getenv("JWT_SECRET")?.trim()?.takeIf { it.isNotEmpty() }
        ?: error("JWT_SECRET must be set before starting the server")
    private val issuer = System.getenv("JWT_ISSUER") ?: "novazheh-server"
    private val audience = System.getenv("JWT_AUDIENCE") ?: "novazheh-users"
    private const val validityInMs = 30L * 24 * 60 * 60 * 1000 // 30 days
    
    private val algorithm = Algorithm.HMAC256(secret)
    
    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()
    
    /**
     * Create JWT token for parent
     * ایجاد توکن برای مادر
     */
    fun makeToken(parentId: Int): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim("parentId", parentId)
        .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
        .sign(algorithm)
    
    fun getConfigForKtor(): Triple<String, String, JWTVerifier> {
        return Triple(issuer, audience, verifier)
    }
}
