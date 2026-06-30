package com.tripexpense.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tripexpense.ui.auth.LoginScreen
import com.tripexpense.ui.auth.SplashScreen
import com.tripexpense.ui.expenses.AddExpenseScreen
import com.tripexpense.ui.expenses.EditExpenseScreen
import com.tripexpense.ui.groups.CreateGroupScreen
import com.tripexpense.ui.groups.DashboardScreen
import com.tripexpense.ui.groups.GroupDetailScreen
import com.tripexpense.ui.profile.ProfileScreen
import com.tripexpense.ui.theme.bg
import com.tripexpense.ui.theme.blue
import com.tripexpense.ui.theme.textSecondary
import com.tripexpense.ui.theme.textTertiary
import com.tripexpense.viewmodel.AuthViewModel
import com.tripexpense.viewmodel.ExpenseViewModel
import com.tripexpense.viewmodel.GroupViewModel
import java.net.URLDecoder
import java.net.URLEncoder

data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem("Groups", Screen.Dashboard.route, Icons.Filled.Group, Icons.Outlined.Group),
    BottomNavItem("Profile", Screen.Profile.route, Icons.Filled.Person, Icons.Outlined.Person),
)

@Composable
fun NavGraph(
    authViewModel: AuthViewModel = viewModel(),
) {
    val authState by authViewModel.state.collectAsState()
    val navController = rememberNavController()

    if (!authState.authInitialized || authState.isLoading) {
        SplashScreen(authViewModel = authViewModel, navController = navController)
        return
    }

    if (authState.user == null) {
        AuthNavHost(navController = navController, authViewModel = authViewModel)
    } else {
        MainNavHost(navController = navController, authViewModel = authViewModel)
    }
}

@Composable
private fun AuthNavHost(navController: NavHostController, authViewModel: AuthViewModel) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(authViewModel = authViewModel, navController = navController)
        }
        composable(
            route = Screen.Login.route,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
        ) {
            LoginScreen(authViewModel = authViewModel, navController = navController)
        }
    }
}

@Composable
private fun MainNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    groupViewModel: GroupViewModel = viewModel(),
    expenseViewModel: ExpenseViewModel = viewModel(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(Screen.Dashboard.route, Screen.Profile.route)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = bg,
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(Screen.Dashboard.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                )
                            },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    navController = navController,
                    groupViewModel = groupViewModel,
                    authViewModel = authViewModel,
                )
            }
            composable(
                route = Screen.CreateGroup.route,
            ) {
                CreateGroupScreen(
                    navController = navController,
                )
            }
            composable(
                route = Screen.GroupDetail.route,
                arguments = listOf(
                    navArgument("groupId") { type = NavType.StringType },
                    navArgument("groupName") { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                val groupName = URLDecoder.decode(
                    backStackEntry.arguments?.getString("groupName") ?: "",
                    "UTF-8"
                )
                GroupDetailScreen(
                    groupId = groupId,
                    groupName = groupName,
                    navController = navController,
                    groupViewModel = groupViewModel,
                    expenseViewModel = expenseViewModel,
                    authViewModel = authViewModel,
                )
            }
            composable(
                route = Screen.AddExpense.route,
                arguments = listOf(
                    navArgument("groupId") { type = NavType.StringType },
                    navArgument("members") { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                val members = backStackEntry.arguments?.getString("members")?.split(",")
                    ?: emptyList()
                AddExpenseScreen(
                    groupId = groupId,
                    members = members,
                    navController = navController,
                    expenseViewModel = expenseViewModel,
                    authViewModel = authViewModel,
                )
            }
            composable(
                route = Screen.EditExpense.route,
                arguments = listOf(
                    navArgument("groupId") { type = NavType.StringType },
                    navArgument("expenseJson") { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                val expenseId = URLDecoder.decode(
                    backStackEntry.arguments?.getString("expenseJson") ?: "",
                    "UTF-8"
                )
                EditExpenseScreen(
                    groupId = groupId,
                    expenseId = expenseId,
                    navController = navController,
                    expenseViewModel = expenseViewModel,
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                )
            }
        }
    }
}
