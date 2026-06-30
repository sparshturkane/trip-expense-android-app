package com.tripexpense.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Dashboard : Screen("dashboard")
    data object CreateGroup : Screen("create_group")
    data object GroupDetail : Screen("group_detail/{groupId}/{groupName}") {
        fun createRoute(groupId: String, groupName: String): String =
            "group_detail/$groupId/$groupName"
    }
    data object AddExpense : Screen("add_expense/{groupId}/{members}") {
        fun createRoute(groupId: String, members: List<String>): String =
            "add_expense/$groupId/${members.joinToString(",")}"
    }
    data object EditExpense : Screen("edit_expense/{groupId}/{expenseJson}") {
        fun createRoute(groupId: String, expenseId: String): String =
            "edit_expense/$groupId/$expenseId"
    }
    data object Profile : Screen("profile")
}
