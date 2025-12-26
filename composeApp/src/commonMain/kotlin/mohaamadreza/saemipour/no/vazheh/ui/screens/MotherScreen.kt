package mohaamadreza.saemipour.no.vazheh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import mohaamadreza.saemipour.no.vazheh.data.ChildDTO
import mohaamadreza.saemipour.no.vazheh.data.Gender
import mohaamadreza.saemipour.no.vazheh.ui.components.AddChildDialog
import mohaamadreza.saemipour.no.vazheh.ui.components.ChildContent
import mohaamadreza.saemipour.no.vazheh.ui.theme.CoralRed
import mohaamadreza.saemipour.no.vazheh.ui.theme.DarkText
import mohaamadreza.saemipour.no.vazheh.ui.theme.MutedText
import mohaamadreza.saemipour.no.vazheh.ui.theme.SoftGray
import mohaamadreza.saemipour.no.vazheh.ui.theme.TealLight
import mohaamadreza.saemipour.no.vazheh.ui.theme.TealPurple
import mohaamadreza.saemipour.no.vazheh.ui.theme.cardBackground
import mohaamadreza.saemipour.no.vazheh.ui.theme.iconTint
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.MotherTab
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.MotherViewModel
import novazheh.composeapp.generated.resources.Res
import novazheh.composeapp.generated.resources.face_man_profile
import novazheh.composeapp.generated.resources.face_woman_profile
import org.jetbrains.compose.resources.painterResource

@Composable
fun MotherScreen(
    navController: NavController,
    viewModel: MotherViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            bottomBar = {
                MotherBottomNavigation(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = viewModel::onTabSelected
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(SoftGray)
            ) {
                when (uiState.selectedTab) {
                    MotherTab.DASHBOARD -> DashboardContent(
                        children = uiState.children,
                        onChildClick = { child ->
                            viewModel.selectChild(child)
                            // TODO: Navigate to child detail screen
                        },
                        onAddChildClick = {
                            viewModel.showAddChildDialog()
                        }
                    )
                    MotherTab.PROFILE -> ProfileContent(
                        username = uiState.username,
                        displayName = uiState.displayName,
                        onLogout = {
                            viewModel.logout()
                            navController.navigate("auth") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }

        if (uiState.showAddChildDialog) {
            AddChildDialog(
                isLoading = uiState.isCreatingChild,
                onDismiss = { viewModel.hideAddChildDialog() },
                onAddChild = { name, gender ->
                    viewModel.createChild(name = name, age = 5, gender = gender)
                }
            )
        }
    }
}

@Composable
private fun MotherBottomNavigation(
    selectedTab: MotherTab,
    onTabSelected: (MotherTab) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        NavigationBarItem(
            selected = selectedTab == MotherTab.DASHBOARD,
            onClick = { onTabSelected(MotherTab.DASHBOARD) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == MotherTab.DASHBOARD) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "داشبورد",
                    modifier = Modifier.size(28.dp)
                )
            },
            label = {
                Text(
                    text = "داشبورد",
                    fontWeight = if (selectedTab == MotherTab.DASHBOARD) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TealPurple,
                selectedTextColor = TealPurple,
                unselectedIconColor = MutedText,
                unselectedTextColor = MutedText,
                indicatorColor = TealPurple.copy(alpha = 0.08f)
            )
        )

        NavigationBarItem(
            selected = selectedTab == MotherTab.PROFILE,
            onClick = { onTabSelected(MotherTab.PROFILE) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == MotherTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "پروفایل",
                    modifier = Modifier.size(28.dp)
                )
            },
            label = {
                Text(
                    text = "پروفایل",
                    fontWeight = if (selectedTab == MotherTab.PROFILE) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TealPurple,
                selectedTextColor = TealPurple,
                unselectedIconColor = MutedText,
                unselectedTextColor = MutedText,
                indicatorColor = TealPurple.copy(alpha = 0.08f)
            )
        )
    }
}

@Composable
private fun ProfileContent(
    username: String,
    displayName: String,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with gradient background
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(TealPurple, TealLight)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Avatar placeholder
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "سلام، ${displayName.ifEmpty { "کاربر" }}!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "خوش آمدید به داشبورد",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // User Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "اطلاعات کاربری",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                // Username row
                UserInfoRow(
                    label = "نام کاربری",
                    value = username.ifEmpty { "تنظیم نشده" }
                )

                // Display name row
                UserInfoRow(
                    label = "نام نمایشی",
                    value = displayName.ifEmpty { "تنظیم نشده" }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Logout Button
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CoralRed
            )
        ) {
            Text(
                text = "خروج از حساب کاربری",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun UserInfoRow(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MutedText
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = DarkText
        )
    }
}

@Composable
private fun DashboardContent(
    children: List<ChildDTO>,
    onChildClick: (ChildDTO) -> Unit,
    onAddChildClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn (
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Spacer(Modifier.size(32.dp))
                Row {
                    Text(
                        "فرزندان شما",
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        color = DarkText
                    )

                    Text(
                        "+  افزودن فرزند",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TealPurple,
                        modifier = Modifier.clickable(onClick = onAddChildClick)
                    )
                }
                Spacer(Modifier.size(24.dp))
            }

            items(children.size) { index ->
                ChildContent(
                    child = children[index],
                    onClick = { onChildClick(children[index]) }
                )
            }
        }
    }
}
