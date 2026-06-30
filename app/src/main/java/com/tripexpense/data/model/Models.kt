package com.tripexpense.data.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val photoURL: String? = null,
)

data class Group(
    val groupId: String = "",
    val name: String = "",
    val members: List<String> = emptyList(),
    val balances: Map<String, Long> = emptyMap(),
    val createdAt: Timestamp? = null,
    val createdBy: String = "",
)

data class GroupWithMeta(
    val group: Group,
    val userBalance: Long = 0,
    val memberCount: Int = 0,
)

data class Expense(
    val expenseId: String = "",
    val groupId: String = "",
    val description: String = "",
    val amount: Long = 0,
    val paidBy: String = "",
    val splitStrategy: SplitStrategy = SplitStrategy.EQUAL,
    val owedBy: Map<String, Long> = emptyMap(),
    val timestamp: Timestamp? = null,
    val updatedAt: Timestamp? = null,
)

enum class SplitStrategy {
    EQUAL, EXACT;

    companion object {
        fun fromString(value: String): SplitStrategy = when (value) {
            "EXACT" -> EXACT
            else -> EQUAL
        }
    }
}

data class BalanceEntry(
    val userA: String,
    val userB: String,
    val amount: Long,
)
