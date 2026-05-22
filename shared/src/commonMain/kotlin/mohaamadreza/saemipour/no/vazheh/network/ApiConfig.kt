package mohaamadreza.saemipour.no.vazheh.network

/**
 * Platform-specific base URL
 * - Android Emulator: http://10.0.2.2:8080
 * - iOS Simulator: http://localhost:8080
 * - JVM/Desktop: http://localhost:8080
 */
expect fun getBaseUrl(): String

/** API Configuration */
object ApiConfig {
    val BASE_URL: String get() = getBaseUrl()

    // Auth endpoints
    const val AUTH_LOGIN = "/api/auth/login"
    const val AUTH_REGISTER = "/api/auth/register"
    const val AUTH_ME = "/api/auth/me"
    const val AUTH_TEST = "/api/auth/test-without-login"

    // Content endpoints
    const val CATEGORIES = "/api/categories"
    const val WORDS = "/api/words"
    const val CUSTOM_WORDS = "/api/custom-words"

    // Children endpoints
    const val CHILDREN = "/api/children"

    // Quiz endpoints
    const val QUIZ_QUESTION = "/api/quiz/question"
    const val QUIZ_QUESTIONS = "/api/quiz/questions"
    const val QUIZ_SUBMIT = "/api/quiz/submit"
}
