package com.tripexpense.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tripexpense.navigation.Screen
import com.tripexpense.ui.theme.*
import com.tripexpense.viewmodel.AuthViewModel

@Composable
fun SplashScreen(
    authViewModel: AuthViewModel,
    navController: NavHostController,
) {
    val authState by authViewModel.state.collectAsState()

    LaunchedEffect(authState.authInitialized, authState.user) {
        if (authState.authInitialized && authState.user == null) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Trip Expense",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimary,
            letterSpacing = (-0.5).sp,
        )
        Text(
            text = "Split expenses effortlessly",
            fontSize = 17.sp,
            color = textSecondary,
            modifier = Modifier.padding(top = 8.dp),
        )
        Spacer(modifier = Modifier.height(48.dp))
        CircularProgressIndicator(
            color = tint,
            modifier = Modifier.size(36.dp),
            strokeWidth = 3.dp,
        )
    }
}
