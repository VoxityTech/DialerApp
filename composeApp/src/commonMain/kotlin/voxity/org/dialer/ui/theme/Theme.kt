// src/commonMain/kotlin/voxity/org/dialer/ui/theme/Theme.kt
package voxity.org.dialer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Grayscale definitions for a luxurious monochrome palette
private val Gray50 = Color(0xFFF8F8F8)
private val Gray100 = Color(0xFFE8E8E8)
private val Gray200 = Color(0xFFCCCCCC)
private val Gray300 = Color(0xFFAAAAAA)
private val Gray400 = Color(0xFF888888)
private val Gray600 = Color(0xFF444444)
private val Gray700 = Color(0xFF333333)
private val Gray800 = Color(0xFF222222)
private val Gray900 = Color(0xFF111111)

private val White = Color(0xFFFFFFFF)
private val Black = Color(0xFF000000)

private val Green = Color(0xFF22C55E)
private val Red = Color(0xFFEF4444)

private val LightColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    primaryContainer = Gray200,
    onPrimaryContainer = Black,
    secondary = Gray600,
    onSecondary = White,
    secondaryContainer = Gray200,
    onSecondaryContainer = Black,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray600,
    outline = Gray300,
    outlineVariant = Gray200,
    error = Black,
    onError = White,
    errorContainer = Gray200,
    onErrorContainer = Black
)

private val DarkColorScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    primaryContainer = Gray800,
    onPrimaryContainer = White,
    secondary = Gray400,
    onSecondary = Black,
    secondaryContainer = Gray800,
    onSecondaryContainer = White,
    background = Black,
    onBackground = White,
    surface = Gray900,
    onSurface = White,
    surfaceVariant = Gray800,
    onSurfaceVariant = Gray400,
    outline = Gray600,
    outlineVariant = Gray700,
    error = White,
    onError = Black,
    errorContainer = Gray700,
    onErrorContainer = White
)

object CallColors {
    val callGreen = Green
    val callRed = Red
    val dialerBackground = Gray50
}

@Composable
fun DialerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = DialerTypography,
        shapes = DialerShapes,
        content = content
    )
}

val DialerTypography = Typography(
    headlineSmall = Typography().headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = Typography().titleLarge.copy(fontWeight = FontWeight.Medium),
    titleMedium = Typography().titleMedium.copy(fontWeight = FontWeight.Medium)
)

val DialerShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
)
