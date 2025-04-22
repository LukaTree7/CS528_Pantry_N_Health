package com.example.afinal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.FoodDatabase
import com.example.afinal.data.Login
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val database = FoodDatabase.getDatabase(application)
    private val repository: LoginRepository = LoginRepository(database.loginDao())

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentAccount = MutableStateFlow<Login?>(null)
    val currentAccount: StateFlow<Login?> = _currentAccount.asStateFlow()

    init {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repository.getLoggedInAccount()?.let { account ->
                    _currentAccount.value = account
                    _authState.value = AuthState.Authenticated
                } ?: run {
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Failed to load session: ${e.message}")
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val success = repository.login(username, password)
                if (success) {
                    _currentAccount.value = repository.getLoggedInAccount()
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error("Invalid credentials")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Login failed: ${e.message}")
            }
        }
    }

    fun register(
        username: String,
        password: String,
        height: Int? = null,
        weight: Int? = null,
        age: Int? = null
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val success = repository.registerUser(username, password, height, weight, age)
                if (success) {
                    login(username, password) // Auto-login after registration
                } else {
                    _authState.value = AuthState.Error("Registration failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Registration error: ${e.message}")
            }
        }
    }

    fun updateProfile(
        height: Int? = null,
        weight: Int? = null,
        age: Int? = null
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                _currentAccount.value?.username?.let { username ->
                    val success = repository.updateProfile(username, height, weight, age)
                    if (success) {
                        _currentAccount.value = repository.getLoggedInAccount()
                        _authState.value = AuthState.Authenticated
                    } else {
                        _authState.value = AuthState.Error("Update failed")
                    }
                } ?: run {
                    _authState.value = AuthState.Error("No active session")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Update error: ${e.message}")
            }
        }
    }

    fun updatePassword(newPassword: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                _currentAccount.value?.username?.let { username ->
                    val success = repository.updatePassword(username, newPassword)
                    if (success) {
                        _currentAccount.value = repository.getLoggedInAccount()
                        _authState.value = AuthState.Authenticated
                    } else {
                        _authState.value = AuthState.Error("Password update failed")
                    }
                } ?: run {
                    _authState.value = AuthState.Error("No active session")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Password error: ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repository.logout()
                _currentAccount.value = null
                _authState.value = AuthState.Unauthenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Logout error: ${e.message}")
            }
        }
    }

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object Authenticated : AuthState()
        object Unauthenticated : AuthState()
        data class Error(val message: String) : AuthState()
    }
}

class AuthViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}