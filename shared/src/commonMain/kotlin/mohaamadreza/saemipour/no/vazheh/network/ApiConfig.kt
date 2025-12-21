package mohaamadreza.saemipour.no.vazheh.network

/** API Configuration Change the BASE_URL to your server address */
object ApiConfig {
    // For Android Emulator use: http://10.0.2.2:8080
    // For iOS Simulator use: http://localhost:8080
    // For physical device use your server's IP address
    const val BASE_URL = "http://10.0.2.2:8080"

    const val AUTH_LOGIN = "/api/auth/login"
    const val AUTH_REGISTER = "/api/auth/register"
    const val AUTH_ME = "/api/auth/me"
}
