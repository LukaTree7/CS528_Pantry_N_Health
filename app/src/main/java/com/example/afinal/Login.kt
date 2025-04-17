package com.example.afinal

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.afinal.ui.theme.FinalTheme

@Composable
fun LoginRegisterScreen(navController: NavController) {
    // Get application context
    val context = LocalContext.current

    // Initialize ViewModel with factory
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(context.applicationContext as Application)
    )

    // Observe login and registration results
    val loginState by authViewModel.loginResult.collectAsStateWithLifecycle()
    val registrationState by authViewModel.registrationResult.collectAsStateWithLifecycle()

    // Navigate when successfully logged in
    LaunchedEffect(loginState) {
        if (loginState) {
            navController.navigate(Screen.Exercise.route) {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    FinalTheme {
        BackgroundWithImage(
            navController = navController,
            authViewModel = authViewModel
        )
    }
}

@Composable
fun BackgroundWithImage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val backgroundImage = painterResource(id = R.drawable.bkg)

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = backgroundImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        FrostedGlassContainer(
            navController = navController,
            authViewModel = authViewModel
        )
    }
}

@Composable
fun FrostedGlassContainer(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {

        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
        }

        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .width(350.dp)
                .height(700.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.3f),
            shadowElevation = 0.dp,
            border = null
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .graphicsLayer(alpha = 0.75f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                LoginRegisterForm(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginRegisterForm(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }

    Column {
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent
            ),
            visualTransformation = VisualTransformation.None
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent
            ),
            visualTransformation = PasswordVisualTransformation()
        )

        if (!isLoginMode) {
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent
                ),
                visualTransformation = PasswordVisualTransformation()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (username.isBlank() || password.isBlank()) {
                    errorMessage = "Username and password cannot be empty"
                    return@Button
                }

                if (isLoginMode) {
                    val isValid = dbHelper.validateUser(username, password)
                    if (isValid) {
                        navController.navigate(Screen.Exercise.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    } else {
                        errorMessage = "Invalid username or password"
                    }
                } else {
                    if (dbHelper.isUsernameExists(username)) {
                        errorMessage = "Username already exists"
                    } else {
                        if (password != confirmPassword) {
                            errorMessage = "Passwords do not match"
                            return@Button
                        }

                        val success = dbHelper.addUser(username, password)
                        if (success) {
                            isLoginMode = true
                            errorMessage = null
                        } else {
                            errorMessage = "Registration failed"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                if (isLoginMode) "Login" else "Register",
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                isLoginMode = !isLoginMode
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                if (isLoginMode) "Switch to Register" else "Switch to Login",
                color = Color.Black
            )
        }
    }
}

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "food_database"
        private const val DATABASE_VERSION = 2
        private const val TABLE_ACCOUNTS = "accounts"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PWD = "pwd"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE IF NOT EXISTS $TABLE_ACCOUNTS (
                $COLUMN_USERNAME TEXT PRIMARY KEY,
                $COLUMN_PWD TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ACCOUNTS")
        onCreate(db)
    }

    fun validateUser(username: String, password: String): Boolean {
        val db = readableDatabase
        val query = """
            SELECT * FROM $TABLE_ACCOUNTS 
            WHERE $COLUMN_USERNAME = ? AND $COLUMN_PWD = ?
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(username, password))
        val isValid = cursor.count > 0
        cursor.close()
        return isValid
    }

    fun isUsernameExists(username: String): Boolean {
        val db = readableDatabase
        val query = """
            SELECT * FROM $TABLE_ACCOUNTS 
            WHERE $COLUMN_USERNAME = ?
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(username))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun addUser(username: String, password: String, email: String? = null): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PWD, password)
        }
        val result = db.insert(TABLE_ACCOUNTS, null, values)
        return result != -1L
    }
}