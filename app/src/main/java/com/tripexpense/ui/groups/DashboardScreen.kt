package com.tripexpense.ui.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import com.tripexpense.components.BalanceCard
import com.tripexpense.components.EmptyState
import com.tripexpense.components.GroupCard
import com.tripexpense.components.Skeleton
import com.tripexpense.data.model.GroupWithMeta
import com.tripexpense.navigation.Screen
import com.tripexpense.ui.theme.*
import com.tripexpense.viewmodel.AuthViewModel
import com.tripexpense.viewmodel.GroupViewModel
import java.net.URLEncoder

@Composable
fun DashboardScreen(
    navController: NavHostController,
    groupViewModel: GroupViewModel,
    authViewModel: AuthViewModel,
) {
    val listState by groupViewModel.listState.collectAsState()
    val authState by authViewModel.state.collectAsState()
    val userId = authState.user?.uid ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            groupViewModel.subscribeToGroups(userId)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.CreateGroup.route) },
                containerColor = tint,
                contentColor = bg,
                shape = CircleShape,
                modifier = Modifier.size(56.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Group",
                    modifier = Modifier.size(28.dp),
                )
            }
        },
    ) { innerPadding ->
        if (listState.isLoading) {
            LoadingDashboard(modifier = Modifier.padding(innerPadding))
        } else if (listState.groups.isEmpty()) {
            EmptyState(
                title = "No groups yet",
                subtitle = "Create your first group to start splitting expenses",
                actionLabel = "Create Group",
                onAction = { navController.navigate(Screen.CreateGroup.route) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(
                        text = "Groups",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                    )
                }
                item {
                    BalanceCard(
                        netBalance = listState.netBalance,
                        groupCount = listState.groups.size,
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Groups",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = textSecondary,
                    )
                }
                items(listState.groups, key = { it.group.groupId }) { group ->
                    GroupCard(
                        group = group,
                        onClick = {
                            val encodedName = URLEncoder.encode(group.group.name, "UTF-8")
                            navController.navigate(
                                Screen.GroupDetail.createRoute(group.group.groupId, encodedName)
                            )
                        },
                    )
                }
                item { Spacer(modifier = Modifier.height(88.dp)) }
            }
        }
    }
}

@Composable
private fun LoadingDashboard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Skeleton(height = 40.dp, borderRadius = 8.dp, modifier = Modifier.fillMaxWidth(0.4f))
        Skeleton(height = 100.dp, borderRadius = 16.dp)
        Skeleton(height = 80.dp, borderRadius = 12.dp)
        Skeleton(height = 80.dp, borderRadius = 12.dp)
        Skeleton(height = 80.dp, borderRadius = 12.dp)
    }
}
