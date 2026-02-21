package com.avinashpatil.app.dishacomputer.screens.bill

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.avinashpatil.app.dishacomputer.api.models.UserDetailResponse
import com.avinashpatil.app.dishacomputer.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.avinashpatil.app.dishacomputer.ui.theme.*

// Define gradient colors
private val cardGradient = Brush.verticalGradient(
    colors = listOf(
        BillCardGradientStart,
        BillCardGradientEnd
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllInOneBillScreen(
    navController: NavHostController,
    studentId: String, // Added parameter to fetch specific student
    viewModel: BillViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val finalPrice = uiState.studentData?.finalPrice?.toDoubleOrNull() ?: 0.0
    val paidAmount = uiState.studentData?.paidAmount?.toDoubleOrNull() ?: 0.0
    val remainingPrice = finalPrice - paidAmount

    LaunchedEffect(studentId) {
        viewModel.loadStudentData(studentId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BillBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Top App Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Sky200)
                    .shadow(8.dp, shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Black
                        )
                    }
                    Text(
                        text = "STUDENT DETAILS",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Black
                    )
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Black
                        )
                    }
                }
            }

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                    }
                }
                uiState.studentData != null -> {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        InfoCard(
                            title = "Student Information",
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            ReadOnlyInfoItem(
                                label = "Student Name",
                                value = uiState.studentData?.userName ?: ""
                            )
                            ReadOnlyInfoItem(
                                label = "Email",
                                value = uiState.studentData?.email ?: ""
                            )
                        }

                        InfoCard(
                            title = "Selected Course",
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            ReadOnlyInfoItem(
                                label = "Courses",
                                value = uiState.studentData?.courseName ?: ""
                            )
                        }

                        InfoCard(
                            title = "Selected Batch",
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            ReadOnlyInfoItem(
                                label = "Batches",
                                value = uiState.studentData?.batchName ?: ""
                            )
                        }

                        InfoCard(title = "Payment Details") {
                            ReadOnlyPriceItem(
                                label = "Total Price",
                                value = "₹${"%.2f".format(finalPrice)}"
                            )

                            ReadOnlyPriceItem(
                                label = "Given Price",
                                value = "₹${"%.2f".format(paidAmount)}"
                            )

                            PriceInfoItem(
                                label = "Remaining Price",
                                value = "₹${"%.2f".format(remainingPrice)}",
                                isHighlighted = remainingPrice > 0
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = cardGradient,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
private fun ReadOnlyInfoItem(
    label: String,
    value: String
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
        Text(
            text = value,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ReadOnlyPriceItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PriceInfoItem(
    label: String,
    value: String,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlighted) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

@HiltViewModel
class BillViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(BillUiState())
    val uiState: StateFlow<BillUiState> = _uiState

    fun loadStudentData(studentId: String) {
        viewModelScope.launch {
            _uiState.value = BillUiState(isLoading = true)
            val result = userRepository.getUserById(studentId)
            result.onSuccess { data ->
                _uiState.value = BillUiState(studentData = data)
            }.onFailure { e ->
                _uiState.value = BillUiState(error = e.message)
            }
        }
    }
}

data class BillUiState(
    val studentData: UserDetailResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)