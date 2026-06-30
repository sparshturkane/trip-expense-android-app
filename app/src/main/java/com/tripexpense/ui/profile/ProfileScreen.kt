package com.tripexpense.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tripexpense.components.AppButton
import com.tripexpense.components.AppTextInput
import com.tripexpense.components.Avatar
import com.tripexpense.components.ButtonVariant
import com.tripexpense.data.repository.FirestoreRepository
import com.tripexpense.ui.theme.*
import com.tripexpense.viewmodel.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
) {
    val authState by authViewModel.state.collectAsState()
    val user = authState.user
    val repo = remember { FirestoreRepository() }
    val scope = rememberCoroutineScope()

    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(user?.name ?: "") }
    var isSaving by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutDialog = false
                    authViewModel.signOut()
                }) {
                    Text("Sign Out", color = red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Avatar(
            name = user?.name ?: "",
            photoURL = user?.photoURL,
            size = 80.dp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = user?.name ?: "",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimary,
        )
        Text(
            text = user?.phone ?: user?.email ?: "",
            fontSize = 15.sp,
            color = textSecondary,
        )
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = bgSecondary),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileRow(
                    label = "Name",
                    value = if (isEditing) "" else user?.name ?: "",
                    isEditing = isEditing,
                    editContent = {
                        AppTextInput(
                            value = editedName,
                            onValueChange = { editedName = it },
                            placeholder = "Full name",
                            modifier = Modifier.fillMaxWidth(),
                        )
                    },
                    onEdit = { isEditing = true; editedName = user?.name ?: "" },
                    onSave = {
                        isSaving = true
                        scope.launch {
                            try {
                                user?.uid?.let { uid ->
                                    repo.updateUserName(uid, editedName)
                                }
                                isEditing = false
                            } catch (_: Exception) {}
                            isSaving = false
                        }
                    },
                    onCancel = { isEditing = false },
                )
                HorizontalDivider(color = separator, modifier = Modifier.padding(vertical = 12.dp))
                ProfileInfoRow("Email", user?.email ?: "")
                HorizontalDivider(color = separator, modifier = Modifier.padding(vertical = 12.dp))
                ProfileInfoRow("Phone", user?.phone ?: "")
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        AppButton(
            title = "Sign Out",
            onClick = { showSignOutDialog = true },
            variant = ButtonVariant.DANGER,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Version 0.0.1",
            fontSize = 13.sp,
            color = textTertiary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ProfileRow(
    label: String,
    value: String,
    isEditing: Boolean,
    editContent: @Composable () -> Unit,
    onEdit: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Column {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = textSecondary,
        )
        if (isEditing) {
            Spacer(modifier = Modifier.height(8.dp))
            editContent()
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onSave) { Text("Save", color = blue) }
            TextButton(onClick = onCancel) { Text("Cancel", color = red) }
        } else {
            Text(
                text = value,
                fontSize = 17.sp,
                color = textPrimary,
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = textSecondary,
        )
        Text(
            text = value.ifEmpty { "Not set" },
            fontSize = 17.sp,
            color = if (value.isEmpty()) textTertiary else textPrimary,
        )
    }
}
