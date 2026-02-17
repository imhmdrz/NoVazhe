package mohaamadreza.saemipour.no.vazheh.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Singleton to manage authentication state across the app
 * مدیریت وضعیت احراز هویت در سراسر برنامه
 * 
 * When server returns 401 (unauthorized), this manager notifies
 * all observers to logout and redirect to login screen.
 */
object AuthStateManager {
    
    private val _authEvents = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val authEvents = _authEvents.asSharedFlow()
    
    /**
     * Call this when a 401 unauthorized error is received
     * فراخوانی هنگام دریافت خطای 401
     */
    fun notifyUnauthorized() {
        _authEvents.tryEmit(AuthEvent.Unauthorized)
    }
    
    /**
     * Call this when user successfully logs in
     * فراخوانی هنگام ورود موفق کاربر
     */
    fun notifyLoggedIn() {
        _authEvents.tryEmit(AuthEvent.LoggedIn)
    }
    
    /**
     * Call this when user logs out manually
     * فراخوانی هنگام خروج دستی کاربر
     */
    fun notifyLoggedOut() {
        _authEvents.tryEmit(AuthEvent.LoggedOut)
    }
}

sealed class AuthEvent {
    /** User received 401 - token is invalid or expired */
    data object Unauthorized : AuthEvent()
    
    /** User successfully logged in */
    data object LoggedIn : AuthEvent()
    
    /** User manually logged out */
    data object LoggedOut : AuthEvent()
}
