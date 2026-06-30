package com.tripexpense.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tripexpense.data.model.Expense
import com.tripexpense.data.model.Group
import com.tripexpense.data.model.GroupWithMeta
import com.tripexpense.data.model.SplitStrategy
import com.tripexpense.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    fun userProfileFlow(uid: String): Flow<User?> = callbackFlow {
        val listener = db.collection("users").document(uid)
            .addSnapshotListener { snapshot, _ ->
                val user = snapshot?.toObject(User::class.java)
                trySend(user)
            }
        awaitClose { listener.remove() }
    }

    fun groupsFlow(userId: String): Flow<List<Group>> = callbackFlow {
        val listener = db.collection("groups")
            .whereArrayContains("members", userId)
            .addSnapshotListener { snapshot, _ ->
                val groups = snapshot?.documents?.mapNotNull {
                    it.toObject(Group::class.java)?.copy(groupId = it.id)
                } ?: emptyList()
                trySend(groups)
            }
        awaitClose { listener.remove() }
    }

    fun groupFlow(groupId: String): Flow<Group?> = callbackFlow {
        val listener = db.collection("groups").document(groupId)
            .addSnapshotListener { snapshot, _ ->
                val group = snapshot?.toObject(Group::class.java)?.copy(groupId = snapshot.id)
                trySend(group)
            }
        awaitClose { listener.remove() }
    }

    fun expensesFlow(groupId: String): Flow<List<Expense>> = callbackFlow {
        val listener = db.collection("groups").document(groupId)
            .collection("expenses")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val expenses = snapshot?.documents?.mapNotNull {
                    it.toObject(Expense::class.java)?.copy(expenseId = it.id)
                } ?: emptyList()
                trySend(expenses)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createGroup(name: String, memberIds: List<String>, createdBy: String): String {
        val deduped = (memberIds + createdBy).distinct()
        val docRef = db.collection("groups").document()
        val group = Group(
            groupId = docRef.id,
            name = name,
            members = deduped,
            balances = emptyMap(),
            createdAt = Timestamp.now(),
            createdBy = createdBy,
        )
        docRef.set(group).await()
        return docRef.id
    }

    suspend fun createExpense(
        groupId: String,
        description: String,
        amount: Long,
        paidBy: String,
        splitStrategy: SplitStrategy,
        owedBy: Map<String, Long>,
    ) {
        val docRef = db.collection("groups").document(groupId)
            .collection("expenses").document()
        val expense = Expense(
            expenseId = docRef.id,
            groupId = groupId,
            description = description,
            amount = amount,
            paidBy = paidBy,
            splitStrategy = splitStrategy,
            owedBy = owedBy,
            timestamp = Timestamp.now(),
            updatedAt = Timestamp.now(),
        )
        docRef.set(expense).await()
    }

    suspend fun updateExpense(
        groupId: String,
        expenseId: String,
        description: String,
        amount: Long,
    ) {
        db.collection("groups").document(groupId)
            .collection("expenses").document(expenseId)
            .update(
                mapOf(
                    "description" to description,
                    "amount" to amount,
                    "updatedAt" to Timestamp.now(),
                )
            ).await()
    }

    suspend fun deleteExpense(groupId: String, expenseId: String) {
        db.collection("groups").document(groupId)
            .collection("expenses").document(expenseId)
            .delete().await()
    }

    suspend fun updateUserName(uid: String, name: String) {
        db.collection("users").document(uid).update("name", name).await()
    }

    suspend fun searchUsers(query: String): List<User> {
        if (query.isBlank()) return emptyList()
        return db.collection("users")
            .orderBy("name")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(User::class.java) }
    }

    fun computeUserBalance(group: Group, userId: String): Long {
        var balance = 0L
        group.balances.forEach { (key, value) ->
            val parts = key.split("_")
            if (parts.size == 2) {
                when (userId) {
                    parts[0] -> balance -= value
                    parts[1] -> balance += value
                }
            }
        }
        return balance
    }

    fun computeNetBalance(groups: List<GroupWithMeta>): Long =
        groups.sumOf { it.userBalance }

    fun parseBalances(balances: Map<String, Long>): List<com.tripexpense.data.model.BalanceEntry> {
        val entries = mutableListOf<com.tripexpense.data.model.BalanceEntry>()
        balances.forEach { (key, amount) ->
            if (amount > 0) {
                val parts = key.split("_")
                if (parts.size == 2) {
                    entries.add(
                        com.tripexpense.data.model.BalanceEntry(
                            userA = parts[0],
                            userB = parts[1],
                            amount = amount,
                        )
                    )
                }
            }
        }
        return entries
    }
}
