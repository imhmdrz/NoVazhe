package mohaamadreza.saemipour.no.vazheh.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import mohaamadreza.saemipour.no.vazheh.data.Gender
import mohaamadreza.saemipour.no.vazheh.ui.theme.CoralRed
import mohaamadreza.saemipour.no.vazheh.ui.theme.DarkText
import mohaamadreza.saemipour.no.vazheh.ui.theme.MutedText
import mohaamadreza.saemipour.no.vazheh.ui.theme.SoftGray
import mohaamadreza.saemipour.no.vazheh.ui.theme.TealPurple
import novazheh.composeapp.generated.resources.Res
import novazheh.composeapp.generated.resources.face_man_profile
import novazheh.composeapp.generated.resources.face_woman_profile

@Composable
fun AddChildDialog(
    isLoading: Boolean,
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onAddChild: (name: String, age: Int, gender: Gender) -> Unit,
    onClearError: () -> Unit = {}
) {
    var childName by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf(Gender.BOY) }
    var selectedAge by remember { mutableStateOf(3) }
    var showAgePopup by remember { mutableStateOf(false) }

    LaunchedEffect(childName, selectedGender, selectedAge) {
        if (errorMessage != null) {
            onClearError()
        }
    }

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isLoading,
            dismissOnClickOutside = !isLoading,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "جنسیت فرزند شما",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Gender selection buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Girl button
                    GenderButton(
                        emoji = Res.drawable.face_woman_profile,
                        label = "دختر",
                        isSelected = selectedGender == Gender.GIRL,
                        onClick = { selectedGender = Gender.GIRL },
                        modifier = Modifier.weight(1f)
                    )

                    // Boy button
                    GenderButton(
                        emoji = Res.drawable.face_man_profile,
                        label = "پسر",
                        isSelected = selectedGender == Gender.BOY,
                        onClick = { selectedGender = Gender.BOY },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Name input title
                Text(
                    text = "نام فرزند شما",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Name text field
                OutlinedTextField(
                    value = childName,
                    onValueChange = { childName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = if (selectedGender == Gender.GIRL) "مهسا" else "ماهان",
                            color = MutedText
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPurple,
                        unfocusedBorderColor = SoftGray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        errorBorderColor = CoralRed
                    ),
                    singleLine = true,
                    enabled = !isLoading,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Age selection title
                Text(
                    text = "سن فرزند شما",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Age selection row
                Box(modifier = Modifier.fillMaxWidth()) {
                    AgeSelector(
                        selectedAge = selectedAge,
                        onClick = { showAgePopup = true },
                        enabled = !isLoading
                    )

                    if (showAgePopup) {
                        Popup(
                            alignment = Alignment.TopCenter,
                            onDismissRequest = { showAgePopup = false },
                            properties = PopupProperties(focusable = true)
                        ) {
                            AgeSelectionPopup(
                                selectedAge = selectedAge,
                                onAgeSelected = { age ->
                                    selectedAge = age
                                    showAgePopup = false
                                },
                                onDismiss = { showAgePopup = false }
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    ErrorMessageCard(
                        message = errorMessage,
                        onDismiss = onClearError
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Add button
                Button(
                    onClick = {
                        if (childName.isNotBlank()) {
                            onAddChild(childName, selectedAge, selectedGender)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TealPurple
                    ),
                    enabled = childName.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "+ افزودن فرزند",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
