package com.example.afinal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.FoodDatabase
import com.example.afinal.data.Login
import com.example.afinal.data.LoginRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LoginRepository

    private val _loginResult = MutableStateFlow(false)
    val loginResult: StateFlow<Boolean> = _loginResult

    private val _registrationResult = MutableStateFlow(false)
    val registrationResult: StateFlow<Boolean> = _registrationResult

    private val _currentUser = MutableStateFlow<Login?>(null)
    val currentUser: StateFlow<Login?> = _currentUser

    init {
        val userDao = FoodDatabase.getDatabase(application).loginDao()
        repository = LoginRepository(userDao)
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            val result = repository.loginUser(username, password)
            _loginResult.value = result

            if (result) {
                // If login successful, get user details
                _currentUser.value = repository.getUserByUsername(username)
            }
        }
    }

    fun register(username: String, password: String, email: String?) {
        viewModelScope.launch {
            val result = repository.registerUser(username, password, email)
            _registrationResult.value = result

            if (result) {
                // If registration successful, get user details
                _currentUser.value = repository.getUserByUsername(username)
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _loginResult.value = false
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