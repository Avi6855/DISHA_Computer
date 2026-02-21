package com.avinashpatil.app.dishacomputer.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.luminance
import androidx.hilt.navigation.compose.hiltViewModel
import com.avinashpatil.app.dishacomputer.viewmodel.HomeViewModel
import com.avinashpatil.app.dishacomputer.viewmodel.UiState
import com.avinashpatil.app.dishacomputer.api.models.UserDetailResponse
import com.avinashpatil.app.dishacomputer.api.models.Course
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.avinashpatil.app.dishacomputer.components.CenteredErrorDisplay
import com.avinashpatil.app.dishacomputer.ui.theme.*

@Composable
fun EnhancedHomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val userDetailsState by viewModel.userDetailsState.collectAsState()
    val allCoursesState by viewModel.allCoursesState.collectAsState()
    val filteredCourses by viewModel.filteredCoursesState.collectAsState()
    val searchQuery by viewModel.searchQueryState.collectAsState()

    EnhancedHomeScreenContent(
        userDetailsState = userDetailsState,
        allCoursesState = allCoursesState,
        filteredCourses = filteredCourses,
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::setSearchQuery,
        onRefresh = viewModel::refreshData,
        navController = navController
    )
}

@Composable
fun EnhancedHomeScreenContent(
    userDetailsState: UiState<UserDetailResponse>,
    allCoursesState: UiState<List<Course>>,
    filteredCourses: List<Course>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onRefresh: () -> Unit,
    navController: NavController
) {
    val firstName = remember(userDetailsState) {
        (userDetailsState as? UiState.Success)?.data?.userName?.split(" ")?.firstOrNull() ?: "Learner"
    }

    // Background Container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Mesh Gradient Background
        MeshGradientBackground()

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 40.dp) // Padding for bottom indicator
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp)
            ) {
                Text(
                    text = "Hello, $firstName!",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Welcome to DISHA Computer Education",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange
                )
            }

            // My Enrolled Courses Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, start = 24.dp, end = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Enrolled Courses",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    /*
                    Text(
                        text = "View All",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { /* Handle click */ }
                    )

                     */
                }

                when (val state = userDetailsState) {
                    is UiState.Success -> {
                        val user = state.data
                        if (!user.courseName.isNullOrEmpty()) {
                            EnrolledCourseCard(
                                courseName = user.courseName,
                                description = user.courseDescription ?: "",
                                batchName = user.batchName ?: user.batchNo ?: "",
                                timing = user.timing ?: ""
                            )
                        } else {
                            // Show placeholder or nothing if no course enrolled
                            Text("No active enrollment found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    is UiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    is UiState.Error -> {
                        Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                            CenteredErrorDisplay(
                                message = state.message,
                                onRetry = onRefresh
                            )
                        }
                    }
                }
            }

            // All Courses Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 24.dp, end = 24.dp)
            ) {
                Text(
                    text = "All Courses",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                when (val state = allCoursesState) {
                    is UiState.Loading -> {
                         CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    is UiState.Error -> {
                        Box(modifier = Modifier.height(300.dp).fillMaxWidth()) {
                            CenteredErrorDisplay(
                                message = state.message,
                                onRetry = onRefresh
                            )
                        }
                    }
                    is UiState.Success -> {
                        filteredCourses.forEach { course ->
                            CourseItem(
                                name = course.courseName,
                                description = course.courseDescription ?: "",
                                icon = Icons.Rounded.Computer, // You might want to map icons based on course name
                                color = MaterialTheme.colorScheme.primary // You might want to map colors based on course name
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        if (filteredCourses.isEmpty()) {
                            Text("No courses found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MeshGradientBackground() {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Canvas(modifier = Modifier.fillMaxSize()) {
        if (isDark) {
            // Dark mode gradients
            // radial-gradient(at 0% 0%, rgba(99, 102, 241, 0.15) 0, transparent 50%)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(PrimaryIndigo.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(0f, 0f),
                    radius = screenWidthPx * 0.8f
                ),
                center = Offset(0f, 0f),
                radius = screenWidthPx * 0.8f
            )
            // radial-gradient(at 100% 100%, rgba(245, 158, 11, 0.1) 0, transparent 50%)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(WarningAmber.copy(alpha = 0.1f), Color.Transparent),
                    center = Offset(screenWidthPx, screenHeightPx),
                    radius = screenWidthPx * 0.8f
                ),
                center = Offset(screenWidthPx, screenHeightPx),
                radius = screenWidthPx * 0.8f
            )
        } else {
            // Light mode gradients (Subtle to match project style)
            // Using similar subtle mesh as other screens for consistency
            val indigoColor = PrimaryIndigo.copy(alpha = 0.08f)
            val roseColor = AccentRose.copy(alpha = 0.08f)
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(indigoColor, Color.Transparent),
                    center = Offset(screenWidthPx * 0.1f, screenHeightPx * 0.2f),
                    radius = screenWidthPx * 0.8f
                ),
                center = Offset(screenWidthPx * 0.1f, screenHeightPx * 0.2f),
                radius = screenWidthPx * 0.8f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(roseColor, Color.Transparent),
                    center = Offset(screenWidthPx * 0.9f, screenHeightPx * 0.8f),
                    radius = screenWidthPx * 0.8f
                ),
                center = Offset(screenWidthPx * 0.9f, screenHeightPx * 0.8f),
                radius = screenWidthPx * 0.8f
            )
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    // Shadow simulation
    Box(
        modifier = Modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (query.isEmpty()) {
                            Text(
                                text = "Search courses...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }
    }
}

@Composable
fun EnrolledCourseCard(
    courseName: String,
    description: String,
    batchName: String,
    timing: String
) {
    // bg-gradient-to-br from-emerald-400 to-teal-600
    val gradientColors = listOf(Emerald400, Teal600) // emerald-400, teal-600

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(24.dp) // rounded-3xl
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY) // Diagonal
                )
            )
    ) {
        // Decorative blur circle: -right-8 -top-8 w-32 h-32 bg-white/10 blur-2xl
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 32.dp, y = (-32).dp)
                .size(128.dp)
                .blur(40.dp) // blur-2xl
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        )

        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Top Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Icon Box
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.School,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50)) // rounded-full
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "IN PROGRESS",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Title & Subtitle
            Text(
                text = courseName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = description,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Emerald50.copy(alpha = 0.8f), // emerald-50/80
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Info Grid
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Batch Info
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp)) // rounded-xl
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "BATCH",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = batchName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Time Info
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "TIME",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = timing,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CourseItem(
    name: String,
    description: String,
    icon: ImageVector,
    color: Color
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    // Glass Card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            //.shadow(
                //elevation = 2.dp,
                //shape = RoundedCornerShape(16.dp)
            //)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .clickable { }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(56.dp) // w-14 h-14
                    .clip(RoundedCornerShape(12.dp)) // rounded-xl
                    .background(color.copy(alpha = if (isDark) 0.3f else 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
/*
            // Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

 */
        }
    }
}

@Preview
@Composable
fun EnhancedHomeScreenPreview() {
    val mockUser = UserDetailResponse(
        userName = "Avinash Patil",
        courseName = "Full Stack Development",
        courseDescription = "Comprehensive course covering frontend and backend technologies.",
        batchName = "Batch A1",
        timing = "10:00 AM - 12:00 PM"
    )

    val mockCourses = listOf(
        Course(courseId = "1", courseName = "Android Development", courseDescription = "Learn modern Android with Kotlin & Compose", courseDuration = "3 months", coursePrice = 5000.0, batches = emptyList()),
        Course(courseId = "2", courseName = "Web Development", courseDescription = "Full stack web development", courseDuration = "4 months", coursePrice = 6000.0, batches = emptyList())
    )

    com.avinashpatil.app.dishacomputer.ui.theme.DISHAComputerTheme(darkTheme = false) {
        EnhancedHomeScreenContent(
            userDetailsState = UiState.Success(mockUser),
            allCoursesState = UiState.Success(mockCourses),
            filteredCourses = mockCourses,
            searchQuery = "",
            onSearchQueryChange = {},
            onRefresh = {},
            navController = rememberNavController()
        )
    }
}

@Preview
@Composable
fun EnhancedHomeScreenDarkPreview() {
    val mockUser = UserDetailResponse(
        userName = "Avinash Patil",
        courseName = "Full Stack Development",
        courseDescription = "Comprehensive course covering frontend and backend technologies.",
        batchName = "Batch A1",
        timing = "10:00 AM - 12:00 PM"
    )

    val mockCourses = listOf(
        Course(courseId = "1", courseName = "Android Development", courseDescription = "Learn modern Android with Kotlin & Compose", courseDuration = "3 months", coursePrice = 5000.0, batches = emptyList()),
        Course(courseId = "2", courseName = "Web Development", courseDescription = "Full stack web development", courseDuration = "4 months", coursePrice = 6000.0, batches = emptyList())
    )

    com.avinashpatil.app.dishacomputer.ui.theme.DISHAComputerTheme(darkTheme = true) {
        EnhancedHomeScreenContent(
            userDetailsState = UiState.Success(mockUser),
            allCoursesState = UiState.Success(mockCourses),
            filteredCourses = mockCourses,
            searchQuery = "",
            onSearchQueryChange = {},
            onRefresh = {},
            navController = rememberNavController()
        )
    }
}
