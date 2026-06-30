package com.tripexpense.ui.auth

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.tripexpense.components.AppButton
import com.tripexpense.components.AppTextInput
import com.tripexpense.components.ButtonVariant
import com.tripexpense.components.Skeleton
import com.tripexpense.data.repository.AuthRepository
import com.tripexpense.ui.theme.*
import com.tripexpense.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    navController: NavHostController,
) {
    val authState by authViewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { AuthRepository() }

    var step by remember { mutableStateOf("phone") }
    var phone by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf("") }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        scope.launch {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.result
                val idToken = account?.idToken ?: return@launch
                repo.signInWithGoogle(idToken)
            } catch (_: Exception) { }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        Text(
            text = "Trip Expense",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimary,
        )
        Spacer(modifier = Modifier.height(32.dp))

        when (step) {
            "phone" -> {
                Text(
                    text = "Enter your phone",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary,
                )
                Text(
                    text = "We'll send you a verification code",
                    fontSize = 17.sp,
                    color = textSecondary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                )
                AppTextInput(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = "Phone number",
                    prefix = "+1",
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(24.dp))
                AppButton(
                    title = "Send Code",
                    onClick = {
                        scope.launch {
                            repo.sendOtp(context as Activity, "+1$phone")
                            verificationId = ""
                            step = "otp"
                        }
                    },
                    loading = authState.isLoading,
                    enabled = phone.isNotBlank(),
                )
            }
            "otp" -> {
                Text(
                    text = "Enter verification code",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary,
                )
                Text(
                    text = "Code sent to $phone",
                    fontSize = 17.sp,
                    color = textSecondary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                )
                AppTextInput(
                    value = otpCode,
                    onValueChange = { otpCode = it },
                    placeholder = "000000",
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(24.dp))
                AppButton(
                    title = "Verify",
                    onClick = {
                        scope.launch {
                            repo.verifyOtp(verificationId, otpCode)
                            step = "name"
                        }
                    },
                    loading = authState.isLoading,
                    enabled = otpCode.length >= 6,
                )
            }
            "name" -> {
                Text(
                    text = "What's your name?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary,
                )
                Text(
                    text = "This will be visible to your group members",
                    fontSize = 17.sp,
                    color = textSecondary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                )
                AppTextInput(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Full name",
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(24.dp))
                AppButton(
                    title = "Continue",
                    onClick = {
                        scope.launch {
                            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                            repo.createUserProfile(uid, name, "", "+1$phone")
                        }
                    },
                    loading = authState.isLoading,
                    enabled = name.isNotBlank(),
                )
            }
        }

        if (step != "name") {
            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = separator)
            Spacer(modifier = Modifier.height(16.dp))
            AppButton(
                title = "Continue with Google",
                onClick = {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(com.tripexpense.R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                    val gsc: GoogleSignInClient = GoogleSignIn.getClient(context, gso)
                    googleSignInLauncher.launch(gsc.signInIntent)
                },
                variant = ButtonVariant.SECONDARY,
            )
        }
    }
}
