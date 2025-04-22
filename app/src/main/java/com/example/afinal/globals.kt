import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.afinal.AuthViewModel

val LocalAppState = staticCompositionLocalOf<AppState> {
    error("No AppState provided!")
}

class AppState(authViewModel: AuthViewModel) {
    var isDarkMode by mutableStateOf(false)
        private set

    var fontSize by mutableStateOf(16f)

    var stepCount by mutableStateOf(0)

    var username by mutableStateOf("")
    var height by mutableStateOf("")
    var weight by mutableStateOf("")
    var age by mutableStateOf("")

    fun toggleDarkMode() {
        isDarkMode = !isDarkMode
    }

    fun updateFontSize(newSize: Float) {
        fontSize = newSize
    }

    fun resetUserInfo() {
        username = ""
        height = ""
        weight = ""
        age = ""
    }
}
