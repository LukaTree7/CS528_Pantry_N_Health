import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppState = staticCompositionLocalOf<AppState> {
    error("No AppState provided!")
}

class AppState {
    var isDarkMode by mutableStateOf(false)
        private set

    var fontSize by mutableStateOf(16f)
        private set

    var stepCount by mutableStateOf(0)

    var username by mutableStateOf("")
        private set

    fun toggleDarkMode() {
        isDarkMode = !isDarkMode
    }

    fun updateFontSize(newSize: Float) {
        fontSize = newSize
    }

    fun updateUsername(newUsername: String) {
        username = newUsername
    }
}