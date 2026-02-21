package com.avinashpatil.app.dishacomputer.screens.bill

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.avinashpatil.app.dishacomputer.components.CenteredErrorDisplay
import com.avinashpatil.app.dishacomputer.viewmodel.StudentDetailsViewModel
import com.avinashpatil.app.dishacomputer.viewmodel.StudentDetailsUiState
import com.avinashpatil.app.dishacomputer.ui.theme.*

@Composable
fun StudentDetailsScreen(
    onBackClick: () -> Unit = {},
    viewModel: StudentDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    StudentDetailsScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onReload = { viewModel.loadUserDetails() }
    )
}

@Composable
fun StudentDetailsScreenContent(
    uiState: StudentDetailsUiState,
    onBackClick: () -> Unit,
    onReload: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Header(
                onBackClick = onBackClick,
                onReload = onReload
            )

            when (val state = uiState) {
                is StudentDetailsUiState.Loading -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is StudentDetailsUiState.Error -> {
                    CenteredErrorDisplay(
                        message = state.message,
                        onRetry = onReload
                    )
                }
                is StudentDetailsUiState.Success -> {
                    val user = state.user
                    val finalPrice = user.finalPrice?.toDoubleOrNull() ?: 0.0
                    val paidAmount = user.paidAmount?.toDoubleOrNull() ?: 0.0

                    // Content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Student Information Card
                        GradientCard(
                            colors = listOf(PrimaryIndigo, Purple600),
                            shadowColor = if (isDark) Color.Transparent else Indigo200,
                            icon = Icons.Rounded.Person,
                            title = "Student Information"
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                InfoItem(label = "Student Name", value = user.userName ?: "N/A", isLarge = true)
                                InfoItem(label = "Email Address", value = user.email ?: "N/A")
                            }
                        }

                        // Selected Course Card
                        GradientCard(
                            colors = listOf(Emerald400, Teal600),
                            shadowColor = if (isDark) Color.Transparent else Emerald100,
                            icon = Icons.Rounded.School,
                            title = "Selected Course"
                        ) {
                            InfoItem(label = "Active Courses", value = user.courseName ?: "N/A", isLarge = true)
                        }

                        // Selected Batch Card
                        GradientCard(
                            colors = listOf(GradientOrangeStart, AccentRose),
                            shadowColor = if (isDark) Color.Transparent else Rose100,
                            icon = Icons.Rounded.Groups,
                            title = "Selected Batch"
                        ) {
                            InfoItem(
                                label = "Current Batch ID", 
                                value = user.batchNo ?: "N/A", 
                                isLarge = true, 
                                letterSpacing = 0.05.em
                            )
                        }

                        // Payment Details Card
                        PaymentDetailsCard(
                            finalPrice = finalPrice,
                            paidAmount = paidAmount
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(
    onBackClick: () -> Unit,
    onReload: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
    val contentColor = MaterialTheme.colorScheme.onBackground

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.dp) // h-16
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = androidx.compose.ui.graphics.RectangleShape
            )
            .drawBottomBorder(borderColor)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .absoluteOffset(x = 0.dp)
        )
    }
    
    // Better Header Implementation
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(backgroundColor)
            .drawBottomBorder(borderColor)
            .padding(horizontal = 16.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(40.dp)
                .background(Color.Transparent, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = contentColor
            )
        }

        Text(
            text = "STUDENT DETAILS",
            color = contentColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.025).em,
            modifier = Modifier.align(Alignment.Center)
        )

        IconButton(
            onClick = onReload,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(40.dp)
                .background(Color.Transparent, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Reload",
                tint = contentColor
            )
        }
    }
}

@Composable
private fun GradientCard(
    colors: List<Color>,
    shadowColor: Color,
    icon: ImageVector,
    title: String,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp) // rounded-3xl approx
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(colors)) // br gradient
            .padding(24.dp) // p-6
    ) {
        // Content Wrapper
        Column(modifier = Modifier.fillMaxWidth()) {
            // Card Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 16.dp) // mb-4
            ) {
                // Icon Box
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp)) // rounded-xl
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(8.dp) // p-2
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp, // text-lg
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Main Content
            Box(modifier = Modifier.fillMaxWidth()) {
                 content()
            }
        }
    }
}

@Composable
private fun InfoItem(
    label: String, 
    value: String, 
    isLarge: Boolean = false,
    letterSpacing: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified
) {
    Column {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp, // text-sm
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = if (isLarge) 20.sp else 16.sp, // text-xl or base
            fontWeight = if (isLarge) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = letterSpacing
        )
    }
}
/*
@Composable
private fun DecorationCircle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.1f), CircleShape)
    )
}

@Composable
private fun Modifier.blur(radius: androidx.compose.ui.unit.Dp): Modifier {
    // Simple alpha simulation since real blur is expensive/API dependent
    return this.background(Color.White.copy(alpha = 0.1f), CircleShape) 
}


 */
@Composable
private fun PaymentDetailsCard(
    finalPrice: Double,
    paidAmount: Double
) {
    val remainingBalance = finalPrice - paidAmount
    val cardBg = MaterialTheme.colorScheme.surface
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val contentColor = MaterialTheme.colorScheme.onSurface
    val secondaryContentColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp, // shadow-sm
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(24.dp) // p-6
    ) {
        Column {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 24.dp) // mb-6
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp)) // rounded-xl
                        .background(PrimaryIndigo.copy(alpha = 0.1f)) // bg-primary/10
                        .padding(8.dp) // p-2
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountBalanceWallet,
                        contentDescription = null,
                        tint = PrimaryIndigo, // text-primary
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Text(
                    text = "Payment Details",
                    color = contentColor,
                    fontSize = 18.sp, // text-lg
                    fontWeight = FontWeight.Bold
                )
            }

            // Details List
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { // space-y-4 (16dp usually, but looks tighter in UI)
                // Total Price
                PaymentRow(
                    label = "Total Price",
                    value = "₹${String.format("%.2f", finalPrice)}",
                    valueColor = contentColor
                )
                
                Divider(color = borderColor, thickness = 1.dp)

                // Given Price
                PaymentRow(
                    label = "Given Price",
                    value = "₹${String.format("%.2f", paidAmount)}",
                    valueColor = AccentEmerald
                )

                Divider(color = borderColor, thickness = 1.dp)

                // Remaining Balance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Remaining Balance",
                        color = secondaryContentColor,
                        fontSize = 16.sp, // font-medium
                        fontWeight = FontWeight.Medium
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${String.format("%.2f", remainingBalance)}",
                            color = if (remainingBalance > 0 || remainingBalance < 0) AccentRose else contentColor, // Using Rose500 for non-zero as per original design for negative
                            fontSize = 18.sp, // text-lg
                            fontWeight = FontWeight.Bold
                        )
                        if (remainingBalance < 0) {
                            Text(
                                text = "CREDIT BALANCE",
                                color = Rose400,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.05.em // wider
                            )
                        } else if (remainingBalance > 0) {
                            Text(
                                text = "DUE BALANCE",
                                color = Rose400,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.05.em // wider
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentRow(
    label: String, 
    value: String, 
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}



// Helper for bottom border
private fun Modifier.drawBottomBorder(color: Color, strokeWidth: androidx.compose.ui.unit.Dp = 1.dp): Modifier = this.drawBehind {
    val strokeWidthPx = strokeWidth.toPx()
    drawLine(
        color = color,
        start = Offset(0f, size.height),
        end = Offset(size.width, size.height),
        strokeWidth = strokeWidthPx
    )
}

// Extension to scale text spacing
private val Double.em: androidx.compose.ui.unit.TextUnit
    get() = androidx.compose.ui.unit.TextUnit(this.toFloat(), androidx.compose.ui.unit.TextUnitType.Em)

private val Float.em: androidx.compose.ui.unit.TextUnit
    get() = androidx.compose.ui.unit.TextUnit(this, androidx.compose.ui.unit.TextUnitType.Em)

@Preview(showBackground = true)
@Composable
fun StudentDetailsScreenPreview() {
    val dummyUser = com.avinashpatil.app.dishacomputer.api.models.UserDetailResponse(
        userId = "1",
        userName = "John Doe",
        email = "john.doe@example.com",
        courseName = "Computer Science",
        batchNo = "BATCH-001",
        finalPrice = "1000.00",
        paidAmount = "500.00"
    )
    DISHAComputerTheme(darkTheme = false) {
        StudentDetailsScreenContent(
            uiState = StudentDetailsUiState.Success(dummyUser),
            onBackClick = {},
            onReload = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StudentDetailsScreenDarkPreview() {
    val dummyUser = com.avinashpatil.app.dishacomputer.api.models.UserDetailResponse(
        userId = "1",
        userName = "John Doe",
        email = "john.doe@example.com",
        courseName = "Computer Science",
        batchNo = "BATCH-001",
        finalPrice = "1000.00",
        paidAmount = "500.00"
    )
    DISHAComputerTheme(darkTheme = true) {
        StudentDetailsScreenContent(
            uiState = StudentDetailsUiState.Success(dummyUser),
            onBackClick = {},
            onReload = {}
        )
    }
}
