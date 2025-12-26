package mohaamadreza.saemipour.no.vazheh.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import mohaamadreza.saemipour.no.vazheh.ui.theme.DarkText
import mohaamadreza.saemipour.no.vazheh.ui.theme.MutedText
import mohaamadreza.saemipour.no.vazheh.ui.theme.TealPurple

@Composable
fun EmptyChildrenContent(
    onAddChildClick: () -> Unit,
    canAddChild: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "👶",
                style = MaterialTheme.typography.displayMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "هنوز فرزندی اضافه نکرده‌اید",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = DarkText
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "با افزودن فرزند، می‌توانید پیشرفت یادگیری آن‌ها را پیگیری کنید",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedText,
                textAlign = TextAlign.Center
            )
            
            if (canAddChild) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onAddChildClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TealPurple
                    )
                ) {
                    Text(
                        text = "+ افزودن فرزند",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
