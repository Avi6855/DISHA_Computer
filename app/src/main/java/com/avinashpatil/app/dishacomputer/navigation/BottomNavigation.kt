package com.avinashpatil.app.dishacomputer.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Topic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.avinashpatil.app.dishacomputer.R
import java.util.Locale

@Composable
fun BottomNavigation(
    navController: NavController,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        NavigationBar(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Screen.bottomNavItems.forEach { screen ->
                val isSelected = currentRoute == screen.route
                NavigationBarItem(
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    icon = {
                        when (screen) {
                            Screen.ModernDishaAI -> Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = screen.route,
                                modifier = Modifier.size(24.dp)
                            )
                            else -> Icon(
                                imageVector = when (screen) {
                                    Screen.EnhancedHome -> Icons.Default.Home
                                    Screen.Topics -> Icons.Default.Topic
                                    Screen.ExamScreen -> Icons.Default.MenuBook
                                    Screen.Exams -> Icons.Default.MenuBook
                                    Screen.ModernProfile -> Icons.Default.Person
                                    else -> Icons.Default.Home
                                },
                                contentDescription = screen.route
                            )
                        }
                    },
                    label = {
                        Text(text = when (screen) {
                            Screen.ModernProfile -> "Profile"
                            Screen.ModernDishaAI -> "AI"
                            else -> screen.route.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(Locale.ROOT)
                                else it.toString()
                            }
                        })
                    },
                    selected = currentRoute == screen.route,
                    onClick = {
                        if (currentRoute != screen.route) {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}