package com.avinashpatil.app.dishacomputer.screens.profile

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.ui.draw.drawBehind
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.avinashpatil.app.dishacomputer.R
import com.avinashpatil.app.dishacomputer.components.CenteredErrorDisplay
import com.avinashpatil.app.dishacomputer.navigation.Screen
import com.avinashpatil.app.dishacomputer.viewmodel.LogoutState
import com.avinashpatil.app.dishacomputer.viewmodel.ModernProfileUiState
import com.avinashpatil.app.dishacomputer.viewmodel.ModernProfileViewModel
import com.avinashpatil.app.dishacomputer.api.models.UserDetailResponse
import com.avinashpatil.app.dishacomputer.ui.theme.*

@Composable
fun ModernProfileScreen(
    navController: NavController,
    viewModel: ModernProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val logoutState by viewModel.logoutState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(logoutState) {
        if (logoutState is LogoutState.Success) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0)
            }
            viewModel.resetLogoutState()
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout Confirmation") },
            text = { Text("Once you logout then you can not sign in again without \"DISHA Computer Education\" Admin's permission. \nSo we request you to, don't logout from this device.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout()
                }) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val user = (uiState as? ModernProfileUiState.Success)?.user
    
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.onImageSelected(uri, context)
            }
        }
    )

    if (uiState is ModernProfileUiState.Error) {
        CenteredErrorDisplay(
            message = (uiState as ModernProfileUiState.Error).message,
            onRetry = { viewModel.refreshProfile() }
        )
    } else {
        ModernProfileContent(
            user = user,
            onBackClick = { navController.popBackStack() },
            onLogoutClick = { showLogoutDialog = true },
            onViewDetailsClick = { navController.navigate(Screen.StudentDetails.route) },
            onCameraClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    }
}

@Composable
fun ModernProfileContent(
    user: UserDetailResponse?,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onViewDetailsClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    val userName = user?.userName ?: "Loading..."
    val userEmail = user?.email ?: "Loading..."
    val localProfileImagePath = user?.localProfileImagePath

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Background
        MeshGradientBackground()

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header
            Header(
                onBackClick = onBackClick,
                onLogoutClick = onLogoutClick
            )

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                ProfileSection(
                    userName = userName,
                    userEmail = userEmail,
                    localProfileImagePath = localProfileImagePath,
                    onViewDetailsClick = onViewDetailsClick,
                    onCameraClick = onCameraClick
                )
                Spacer(modifier = Modifier.height(64.dp))
                AboutUsSection()
                // Spacer for Bottom Navigation and safe area
                Spacer(modifier = Modifier.height(100.dp)) 
            }
        }
    }
}

@Composable
private fun MeshGradientBackground() {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val alphaMultiplier = if (isDark) 0.15f else 1f

        // Top Left - Orange
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(MeshOrange.copy(alpha = 0.25f * alphaMultiplier), Color.Transparent),
                center = Offset(0f, 0f),
                radius = width * 0.8f
            )
        )

        // Top Right - Gold
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(MeshGold.copy(alpha = 0.25f * alphaMultiplier), Color.Transparent),
                center = Offset(width, 0f),
                radius = width * 0.8f
            )
        )

        // Bottom Right - Red
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(MeshRed.copy(alpha = 0.2f * alphaMultiplier), Color.Transparent),
                center = Offset(width, height),
                radius = width * 0.8f
            )
        )

        // Bottom Left - Gold
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(MeshGold.copy(alpha = 0.25f * alphaMultiplier), Color.Transparent),
                center = Offset(0f, height),
                radius = width * 0.8f
            )
        )
    }
}

@Composable
private fun Header(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val glassBg = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
    val glassBorder = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val contentColor = MaterialTheme.colorScheme.onSurface
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(glassBg)
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                drawLine(
                    color = glassBorder,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = strokeWidth
                )
            }
    ) {
        // Status Bar Spacer
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Title
            Text(
                text = "STUDENT PROFILE",
                color = contentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.2.em
            )

            // Logout Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    .clickable { onLogoutClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileSection(
    userName: String,
    userEmail: String,
    localProfileImagePath: String?,
    onViewDetailsClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    val contentColor = MaterialTheme.colorScheme.onBackground
    val secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = MaterialTheme.colorScheme.surface

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Avatar Container
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(160.dp)
        ) {
            // Glow
            Box(
                modifier = Modifier
                    .size(144.dp)
                    .scale(1.25f)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            )
            
            // Gradient Border & Avatar
            Box(
                modifier = Modifier
                    .size(144.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(MeshOrange, MeshGold, MeshRed)
                        )
                    )
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                // White Border
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(cardColor)
                        .border(2.dp, cardColor.copy(alpha = 0.6f), CircleShape)
                ) {
                    if (localProfileImagePath != null) {
                        AsyncImage(
                            model = File(localProfileImagePath),
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Camera Button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-12).dp, y = (-12).dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(cardColor)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .shadow(8.dp, CircleShape)
                    .clickable { onCameraClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Edit",
                    tint = AccentOrange,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Name & Email
        Text(
            text = userName,
            color = contentColor,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.025).em
        )
        Text(
            text = userEmail,
            color = secondaryColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // View Course Details Button
        Button(
            onClick = { onViewDetailsClick() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .widthIn(max = 280.dp)
                .shadow(
                    elevation = 15.dp,
                    shape = CircleShape
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp),
            shape = CircleShape
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(MeshOrange, AccentOrange, MeshRed),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "VIEW COURSE DETAILS",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.05.em
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutUsSection() {
    val context = LocalContext.current
    val contentColor = MaterialTheme.colorScheme.onSurface
    val cardBg = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val logoBg = MaterialTheme.colorScheme.surface

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "About Us",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.025).em,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(40.dp))
                .background(cardBg)
                .border(1.dp, borderColor, RoundedCornerShape(40.dp))
                .padding(32.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Logo
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(logoBg.copy(alpha = 0.4f))
                        .border(1.dp, logoBg.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "DISHA COMPUTER EDUCATION HALADI",
                    color = contentColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Contact Info
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Email
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .clickable {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:dishahaladi@gmail.com")
                                }
                                context.startActivity(intent)
                            }
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "OFFICIAL SUPPORT",
                            color = Slate500,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.1.em,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "dishahaladi@gmail.com",
                            color = AccentOrange,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Contacts
                    ContactRow(
                        name = "Sunil Mithari Sir",
                        number = "+919421175700",
                        iconTint = AccentOrange,
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:+919421175700")
                            }
                            context.startActivity(intent)
                        }
                    )
                    ContactRow(
                        name = "Nikhil Mithari Sir",
                        number = "+917798121040",
                        iconTint = WarningAmber,
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:+917798121040")
                            }
                            context.startActivity(intent)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Social Icons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SocialIcon(
                        iconResId = R.drawable.linkedin,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/company/disha-computer-education-haladi/"))
                            context.startActivity(intent)
                        }
                    )
                    SocialIcon(
                        iconResId = R.drawable.instagram,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/dishacomputerhaladi/"))
                            context.startActivity(intent)
                        }
                    )
                    SocialIcon(
                        iconResId = R.drawable.whatsapp,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/919881525700"))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))

        // Developer Credit
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(32.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "DESIGN & DEVELOPED BY",
                    color = AccentOrange.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.25.em
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Avinash Rajendra Patil",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.025).em
                )
                Text(
                    text = "Senior Android Developer",
                    color = Slate500,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ContactRow(
    name: String,
    number: String,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = name,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = number,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Icon(
            imageVector = Icons.Default.Call,
            contentDescription = "Call",
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun SocialIcon(
    iconResId: Int,
    onClick: () -> Unit
) {
    Image(
        painter = painterResource(id = iconResId),
        contentDescription = null,
        modifier = Modifier
            .size(56.dp)
            .shadow(10.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentScale = ContentScale.Crop
    )
}

// Extension to scale text spacing
val Float.em: androidx.compose.ui.unit.TextUnit
    get() = androidx.compose.ui.unit.TextUnit(this, androidx.compose.ui.unit.TextUnitType.Em)

@Preview(name = "Light Mode")
@Composable
fun PreviewModernProfileScreen() {
    val dummyUser = UserDetailResponse(
        userId = "1",
        fullName = "John Doe",
        userName = "johndoe",
        email = "john@example.com",
        courseName = "Computer Science",
        localProfileImagePath = null,
        deviceName = "device123",
        enrollmentDateTime = "2023-01-01"
    )
    
    ModernProfileContent(
        user = dummyUser,
        onBackClick = {},
        onLogoutClick = {},
        onViewDetailsClick = {},
        onCameraClick = {}
    )
}

@Preview(name = "Dark Mode")
@Composable
fun PreviewModernProfileScreenDark() {
    val dummyUser = UserDetailResponse(
        userId = "1",
        fullName = "John Doe",
        userName = "johndoe",
        email = "john@example.com",
        courseName = "Computer Science",
        localProfileImagePath = null,
        deviceName = "device123",
        enrollmentDateTime = "2023-01-01"
    )
    
    // In preview, we can't easily force dark mode via system check 
    // unless we wrap it in a Theme that forces it.
    // For now, we just show the content. 
    // If we want to test dark mode preview, we should wrap it in DISHAComputerTheme(darkTheme = true)
    
    DISHAComputerTheme(darkTheme = true) {
        ModernProfileContent(
            user = dummyUser,
            onBackClick = {},
            onLogoutClick = {},
            onViewDetailsClick = {},
            onCameraClick = {}
        )
    }
}
