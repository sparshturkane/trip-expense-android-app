package com.tripexpense.data.repository

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.tripexpense.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser

    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun fetchUserProfile(uid: String): User? {
        val doc = firestore.collection("users").document(uid).get().await()
        return if (doc.exists()) doc.toObject(User::class.java) else null
    }

    suspend fun createUserProfile(uid: String, name: String, email: String, phone: String) {
        val user = User(uid = uid, name = name, email = email, phone = phone)
        firestore.collection("users").document(uid).set(user).await()
    }

    suspend fun sendOtp(activity: Activity, phone: String): String {
        val result = suspendOtpResult()
        val options = PhoneAuthOptions.newBuilder(auth)
            .setActivity(activity)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setCallbacks(result.callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        return result.verificationId
    }

    suspend fun verifyOtp(verificationId: String, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        auth.signInWithCredential(credential).await()
    }

    suspend fun signInWithGoogle(idToken: String) {
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
    }

    fun signOut() {
        auth.signOut()
    }

    private suspend fun suspendOtpResult(): OtpResult {
        val result = OtpResult()
        result.callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                result.complete(credential)
            }

            override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                result.fail(e)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                result.setVerificationId(verificationId)
            }
        }
        return result
    }
}

private class OtpResult {
    lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var _verificationId = ""
    private var exception: com.google.firebase.FirebaseException? = null
    private var credential: PhoneAuthCredential? = null
    private var completed = false

    val verificationId: String get() {
        while (!completed && _verificationId.isEmpty() && exception == null) {
            Thread.sleep(50)
        }
        exception?.let { throw it }
        if (credential != null) {
            FirebaseAuth.getInstance().signInWithCredential(credential!!)
            return ""
        }
        return _verificationId
    }

    fun setVerificationId(id: String) { _verificationId = id; completed = true }
    fun complete(cred: PhoneAuthCredential) { credential = cred; completed = true }
    fun fail(e: com.google.firebase.FirebaseException) { exception = e; completed = true }
}
