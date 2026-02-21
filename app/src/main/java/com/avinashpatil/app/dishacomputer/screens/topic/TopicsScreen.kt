package com.avinashpatil.app.dishacomputer.screens.topic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.graphics.luminance
import com.avinashpatil.app.dishacomputer.components.CenteredErrorDisplay
import com.avinashpatil.app.dishacomputer.navigation.Screen
import com.avinashpatil.app.dishacomputer.ui.theme.*
import com.avinashpatil.app.dishacomputer.viewmodel.TopicsUiState
import com.avinashpatil.app.dishacomputer.viewmodel.TopicsViewModel

@Composable
fun TopicsScreen(
    navController: NavController,
    viewModel: TopicsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    TopicsScreenContent(
        navController = navController,
        uiState = uiState,
        searchQuery = searchQuery,
        onQueryChange = viewModel::onSearchQueryChanged,
        onRetry = viewModel::refreshTopics
    )
}

@Composable
fun TopicsScreenContent(
    navController: NavController,
    uiState: TopicsUiState,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onRetry: () -> Unit
) {
    // Predefined styles to cycle through for dynamic data
    val gradients = listOf(
        listOf(GradientOrangeStart, GradientOrangeEnd), // orange-400 to yellow-300
        listOf(GradientBlueStart, GradientBlueEnd), // blue-500 to cyan-300
        listOf(GradientGreenStart, GradientGreenEnd), // emerald-500 to lime-300
        listOf(GradientPurpleStart, GradientPurpleEnd), // purple-500 to pink-400
        listOf(GradientRoseStart, GradientRoseEnd), // rose-500 to orange-300
        listOf(GradientIndigoStart, GradientIndigoEnd)  // indigo-500 to blue-400
    )
    val icons = listOf(
        Icons.Default.Calculate,
        Icons.Default.Category,
        Icons.Default.Biotech,
        Icons.Default.Bolt,
        Icons.AutoMirrored.Filled.MenuBook,
        Icons.Default.HistoryEdu
    )

        Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopicsTopBar(navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = onQueryChange
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Content
            when (val state = uiState) {
                is TopicsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is TopicsUiState.Error -> {
                    CenteredErrorDisplay(
                        message = state.message,
                        onRetry = onRetry
                    )
                }
                is TopicsUiState.Success -> {
                    val topics = state.topics
                    if (topics.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No topics found matching '$searchQuery'" else "No topics available",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                bottom = 24.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                            )
                        ) {
                            itemsIndexed(topics) { index, topic ->
                                val styleIndex = index % gradients.size
                                val topicUiItem = TopicUiItem(
                                    title = topic.topicName ?: "Unknown Topic",
                                    icon = icons[styleIndex],
                                    gradientColors = gradients[styleIndex]
                                )
                                TopicItem(topicUiItem) {
                                    topic.topicId?.let { topicId ->
                                        navController.navigate("${Screen.PracticeQuiz.route}?topicId=$topicId&limit=25&source=TOPIC")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun TopicsTopBar(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        // Back Button
        Surface(
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.CenterStart)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape
                ),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface
        ) {
            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Title
        Text(
            text = "Topic Wise Quiz",
            modifier = Modifier.align(Alignment.Center),
            style = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun SearchBar(
    query: String = "",
    onQueryChange: (String) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .height(56.dp)
            .shadow(
                elevation = 2.dp,
                shape = CircleShape
            )
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = "Search your favorite topic...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TopicItem(topic: TopicUiItem, onClick: () -> Unit = {}) {
    val shadowColor = topic.gradientColors.first().copy(alpha = 0.2f)
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Box(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 10.dp)
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gradient Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape
                    )
                    .background(
                        brush = Brush.linearGradient(
                            colors = topic.gradientColors
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = topic.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title
            Text(
                text = topic.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            // Chevron
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

data class TopicUiItem(
    val title: String,
    val icon: ImageVector,
    val gradientColors: List<Color>
)

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun TopicsScreenLightPreview() {
    val dummyTopics = listOf(
        com.avinashpatil.app.dishacomputer.api.models.TopicDto("1", "Mathematics", "1"),
        com.avinashpatil.app.dishacomputer.api.models.TopicDto("2", "Science", "1"),
        com.avinashpatil.app.dishacomputer.api.models.TopicDto("3", "History", "1")
    )
    com.avinashpatil.app.dishacomputer.ui.theme.DISHAComputerTheme {
        TopicsScreenContent(
            navController = rememberNavController(),
            uiState = TopicsUiState.Success(dummyTopics),
            searchQuery = "",
            onQueryChange = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
fun TopicsScreenDarkPreview() {
    val dummyTopics = listOf(
        com.avinashpatil.app.dishacomputer.api.models.TopicDto("1", "Mathematics", "1"),
        com.avinashpatil.app.dishacomputer.api.models.TopicDto("2", "Science", "1")
    )
    com.avinashpatil.app.dishacomputer.ui.theme.DISHAComputerTheme {
        TopicsScreenContent(
            navController = rememberNavController(),
            uiState = TopicsUiState.Success(dummyTopics),
            searchQuery = "",
            onQueryChange = {},
            onRetry = {}
        )
    }
}

