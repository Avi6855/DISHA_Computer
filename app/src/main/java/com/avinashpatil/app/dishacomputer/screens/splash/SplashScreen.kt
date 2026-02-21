package com.avinashpatil.app.dishacomputer.screens.splash

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.avinashpatil.app.dishacomputer.R
import com.avinashpatil.app.dishacomputer.navigation.Screen
import com.avinashpatil.app.dishacomputer.screens.login.StartDestination
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val loginViewModel: com.avinashpatil.app.dishacomputer.screens.login.LoginViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    var visible by remember { mutableStateOf(false) }
    var startAnimation by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = tween(800),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        visible = true
        startAnimation = true
        delay(1000)
        try {
            val destination = loginViewModel.resolveStartDestination()
            val targetRoute = when (destination) {
                is StartDestination.Login -> Screen.Login.route
                is StartDestination.Restriction -> Screen.Restriction.route
                is StartDestination.Home -> Screen.Home.route
            }
            navController.navigate(targetRoute) {
                popUpTo(Screen.Splash.route) { inclusive = true }
                launchSingleTop = true
            }
        } catch (e: Exception) {
            try {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                    launchSingleTop = true
                }
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(800))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.splash_screen),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "DISHA COMPUTER EDUCATION HALADI",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    //fontWeight = FontWeight.SemiBold,
                    fontWeight = FontWeight.Bold,
                    //textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(if (startAnimation) 1f else 0f)
                )

                Spacer(modifier = Modifier.height(8.dp))
/*
                Text(
                    text = "Design & Developed By \n Avinash Rajendra Patil \n Senior Android Developer",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    //fontWeight = FontWeight.SemiBold,
                    fontWeight = FontWeight.Bold,
                    //textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(if (startAnimation) 1f else 0f)
                )

 */
                /*
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    /*
                    colors = CardDefaults.cardColors(
                        containerColor = Amber100
                    )

                     */
                ) {

                 */
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Design & Developed By: \nAvinash Rajendra Patil",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = "Senior Android Developer",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    }
