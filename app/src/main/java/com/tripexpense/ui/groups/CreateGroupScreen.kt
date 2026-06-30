package com.tripexpense.ui.groups

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.tripexpense.components.AppButton
import com.tripexpense.components.AppTextInput
import com.tripexpense.data.repository.FirestoreRepository
import com.tripexpense.ui.theme.*
import com.tripexpense.viewmodel.AuthViewModel
import com.tripexpense.viewmodel.GroupViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun CreateGroupScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel? = null,
    groupViewModel: GroupViewModel = viewModel(),
) {
    var groupName by remember { mutableStateOf("") }
    var selectedMembers by remember { mutableStateOf<List<com.tripexpense.data.model.User>>(emptyList()) }
    var isCreating by remember { mutableStateOf(false) }
    val repo = remember { FirestoreRepository() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "New Group",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = textPrimary,
        )
        Spacer(modifier = Modifier.height(24.dp))
        AppTextInput(
            value = groupName,
            onValueChange = { groupName = it },
            placeholder = "Group Name",
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "${selectedMembers.size + 1} members total (including you)",
            fontSize = 13.sp,
            color = textSecondary,
        )
        Spacer(modifier = Modifier.height(24.dp))
        AppButton(
            title = "Create",
            onClick = {
                isCreating = true
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@AppButton
                val memberIds = selectedMembers.map { it.uid }
                scope.launch {
                    try {
                        repo.createGroup(groupName, memberIds, uid)
                        navController.popBackStack()
                    } catch (e: Exception) {
                        isCreating = false
                    }
                }
            },
            loading = isCreating,
            enabled = groupName.isNotBlank(),
        )
    }
}
