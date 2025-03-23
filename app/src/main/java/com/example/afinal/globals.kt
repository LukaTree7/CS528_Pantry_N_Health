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

    fun toggleDarkMode() {
        isDarkMode = !isDarkMode
    }

    fun updateFontSize(newSize: Float) {
        fontSize = newSize
    }
}