package com.avinashpatil.app.dishacomputer.screens.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.avinashpatil.app.dishacomputer.data.ranking.RankingItem
import com.avinashpatil.app.dishacomputer.ui.theme.*
import com.avinashpatil.app.dishacomputer.viewmodel.RankingUiState
import com.avinashpatil.app.dishacomputer.viewmodel.RankingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(
    navController: NavController,
    viewModel: RankingViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    // Rows come from GET api/leaderboard (RankingViewModel -> ExamRepository).
    val rankings = when (val state = uiState) {
        is RankingUiState.Success -> state.users.map { user ->
            RankingItem(
                rank = user.rank,
                imageUrl = "",
                name = user.fullName,
                score = user.score
            )
        }
        else -> emptyList()
    }

    val gradients = listOf(
        // Soft Lavender to Light Pink
        Brush.horizontalGradient(colors = listOf(Purple200, Pink200)),
        // Pale Blue to Light Cyan
        Brush.horizontalGradient(colors = listOf(Sky200, Cyan200)),
        // Mint to Pale Green
        Brush.horizontalGradient(colors = listOf(Emerald100, Sky100)),
        // Light Rose to Peach
        Brush.horizontalGradient(colors = listOf(Red100, Rose100)),
        // Pale Yellow to Light Orange
        Brush.horizontalGradient(colors = listOf(Amber100, Rose100)),
        // Light Sky Blue to Pale Indigo
        Brush.horizontalGradient(colors = listOf(Blue100, Indigo100)),
        // Pale Lime to Light Emerald
        Brush.horizontalGradient(colors = listOf(Lime100, Emerald100)),
        // Light Pink to Pale Purple
        Brush.horizontalGradient(colors = listOf(Pink100, Purple200)),
        // Pale Indigo to Light Blue
        Brush.horizontalGradient(colors = listOf(Indigo100, Blue100)),
        // Light Coral to Pale Rose
        Brush.horizontalGradient(colors = listOf(Rose100, Red100))
    )

    val filteredRankings = rankings.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Sky200)
                .shadow(
                    8.dp,
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "LEADERBOARD",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    placeholder = { Text("Search by Student Name") },
                    trailingIcon = { Icon(Icons.Default.Search, null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = RankOrange,
                        unfocusedBorderColor = RankGold,
                        cursorColor = RankOrange
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RankGold, RoundedCornerShape(5.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Rank", color = White, fontWeight = FontWeight.Bold)
                    Text("Profile", color = White, fontWeight = FontWeight.Bold)
                    Text("Name", color = White, fontWeight = FontWeight.Bold)
                    Text("Score", color = White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (val state = uiState) {
                    is RankingUiState.Loading -> Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = RankOrange)
                    }

                    is RankingUiState.Error -> Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.loadLeaderboard() },
                            colors = ButtonDefaults.buttonColors(containerColor = RankOrange)
                        ) {
                            Text("Retry")
                        }
                    }

                    is RankingUiState.Success -> {
                        if (filteredRankings.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isBlank()) {
                                        "No rankings available yet."
                                    } else {
                                        "No student matches \"$searchQuery\"."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn {
                                items(filteredRankings) { ranking ->
                                    RankingListItem(
                                        ranking = ranking,
                                        gradient = gradients[
                                            (ranking.rank - 1).coerceAtLeast(0) %
                                                gradients.size
                                        ]
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RankingListItem(ranking: RankingItem, gradient: Brush) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(gradient, RoundedCornerShape(30.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = String.format("%02d", ranking.rank),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (ranking.imageUrl.isBlank()) {
            // The backend leaderboard has no avatar URL, so show the student's initial.
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ranking.name.trim().take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            AsyncImage(
                model = ranking.imageUrl,
                contentDescription = "Profile image of ${ranking.name}",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Text(
            text = ranking.name,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "${ranking.score}",
            style = MaterialTheme.typography.titleMedium,
            color = RankOrange,
            fontWeight = FontWeight.Bold
        )
    }
}
