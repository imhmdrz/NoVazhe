package mohaamadreza.saemipour.no.vazheh.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.http.HttpStatusCode
import mohaamadreza.saemipour.no.vazheh.data.AuthStateManager

/** Factory for creating platform-specific HttpClient */
expect fun createHttpClient(): HttpClient

/**
 * Common HTTP client configuration with 401 handling
 * پیکربندی مشترک کلاینت HTTP با مدیریت خطای 401
 */
fun HttpClientConfig<*>.configureAuthHandling() {
    HttpResponseValidator {
        validateResponse { response ->
            if (response.status == HttpStatusCode.Unauthorized) {
                // Invalid login credentials are a form error, not an expired session.
                if (response.call.request.url.encodedPath != ApiConfig.AUTH_LOGIN) {
                    // Notify the app about unauthorized access
                    // اطلاع‌رسانی به برنامه درباره عدم دسترسی
                    AuthStateManager.notifyUnauthorized()
                }
            }
        }
    }
    
    // Don't throw on non-2xx responses, let the repositories handle them
    expectSuccess = false
}
