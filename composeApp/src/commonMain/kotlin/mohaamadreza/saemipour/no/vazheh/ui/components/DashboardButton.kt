package mohaamadreza.saemipour.no.vazheh.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * بازگشت مستقیم به داشبورد (صفحه‌ی mother): اگر در پشته باشد به آن pop می‌کنیم،
 * وگرنه به‌عنوان مقصد تازه با پاک‌کردن پشته باز می‌شود.
 */
fun NavController.backToDashboard() {
    val popped = popBackStack("mother", false)
    if (!popped) {
        navigate("mother") { popUpTo(0) { inclusive = true } }
    }
}

/**
 * دکمه‌ی «داشبورد» که در بالای صفحات به‌جای دکمه‌ی بازگشت می‌نشیند: همان لوگوی
 * خانه‌ی تبِ داشبورد در نوار پایین، داخل دایره‌ی کم‌رنگ. کارکردش همان بازگشت است
 * (بازگشت به داشبورد).
 */
@Composable
fun DashboardButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.background(tint.copy(alpha = 0.12f), CircleShape)
    ) {
        Icon(
            imageVector = Icons.Filled.Home,
            contentDescription = "بازگشت به داشبورد",
            tint = tint,
            modifier = Modifier.size(26.dp)
        )
    }
}
