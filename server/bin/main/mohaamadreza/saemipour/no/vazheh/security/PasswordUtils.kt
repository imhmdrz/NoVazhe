package mohaamadreza.saemipour.no.vazheh.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object PasswordUtils {
    
    private const val SALT_LENGTH = 16
    private const val ITERATIONS = 10000
    
    /**
     * Hash a password with a random salt
     */
    fun hashPassword(password: String): String {
        val salt = generateSalt()
        val hash = pbkdf2Hash(password, salt)
        return "${Base64.getEncoder().encodeToString(salt)}:${Base64.getEncoder().encodeToString(hash)}"
    }
    
    /**
     * Verify a password against a stored hash
     */
    fun verifyPassword(password: String, storedHash: String): Boolean {
        return try {
            val parts = storedHash.split(":")
            if (parts.size != 2) return false
            
            val salt = Base64.getDecoder().decode(parts[0])
            val expectedHash = Base64.getDecoder().decode(parts[1])
            val actualHash = pbkdf2Hash(password, salt)
            
            MessageDigest.isEqual(expectedHash, actualHash)
        } catch (e: Exception) {
            false
        }
    }
    
    private fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        return salt
    }
    
    private fun pbkdf2Hash(password: String, salt: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        var hash = password.toByteArray() + salt
        
        repeat(ITERATIONS) {
            hash = digest.digest(hash)
        }
        
        return hash
    }
}





