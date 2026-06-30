package com.tripexpense.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tripexpense.data.model.User
import com.tripexpense.data.repository.AuthRepository
import com.tripexpense.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val authInitialized: Boolean = false,
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepo = AuthRepository()
    private val firestoreRepo = FirestoreRepository()

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private var profileSub: kotlinx.coroutines.Job? = null

    init {
        listenToAuth()
    }

    private fun listenToAuth() {
        viewModelScope.launch {
            authRepo.authStateFlow().collect { firebaseUser ->
                if (firebaseUser != null) {
                    val uid = firebaseUser.uid
                    profileSub?.cancel()
                    profileSub = viewModelScope.launch {
                        firestoreRepo.userProfileFlow(uid).collect { profile ->
                            _state.value = if (profile != null) {
                                AuthState(user = profile, isLoading = false, authInitialized = true)
                            } else {
                                AuthState(
                                    user = User(
                                        uid = uid,
                                        name = firebaseUser.displayName ?: "",
                                        email = firebaseUser.email ?: "",
                                        phone = firebaseUser.phoneNumber ?: "",
                                    ),
                                    isLoading = false,
                                    authInitialized = true,
                                )
                            }
                        }
                    }
                } else {
                    profileSub?.cancel()
                    _state.value = AuthState(user = null, isLoading = false, authInitialized = true)
                }
            }
        }
    }

    fun sendOtp(phone: String, onCodeSent: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                authRepo.sendOtp(getApplication(), phone)
                _state.value = _state.value.copy(isLoading = false)
                onCodeSent()
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                onError(e.message ?: "Failed to send code")
            }
        }
    }

    fun verifyOtp(verificationId: String, code: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                authRepo.verifyOtp(verificationId, code)
                _state.value = _state.value.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                onError(e.message ?: "Invalid code")
            }
        }
    }

    fun createProfile(uid: String, name: String, phone: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                authRepo.createUserProfile(uid, name, "", phone)
                _state.value = _state.value.copy(isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun signOut() {
        authRepo.signOut()
    }
}
