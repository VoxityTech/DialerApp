// src/commonMain/kotlin/voxity/org/dialer/ui/theme/Theme.kt
package voxity.org.dialer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Color palette inspired by shadcn/ui
private val Gray50 = Color(0xFFFAFAFA)
private val Gray100 = Color(0xFFF4F4F5)
private val Gray200 = Color(0xFFE4E4E7)
private val Gray300 = Color(0xFFD4D4D8)
private val Gray400 = Color(0xFFA1A1AA)
private val Gray500 = Color(0xFF71717A)
private val Gray600 = Color(0xFF52525B)
private val Gray700 = Color(0xFF3F3F46)
private val Gray800 = Color(0xFF27272A)
private val Gray900 = Color(0xFF18181B)
private val Gray950 = Color(0xFF09090B)

private val White = Color(0xFFFFFFFF)
private val Black = Color(0xFF000000)

// Accent colors
private val Blue600 = Color(0xFF2563EB)
private val Blue700 = Color(0xFF1D4ED8)
private val Green600 = Color(0xFF16A34A)
private val Green700 = Color(0xFF15803D)
private val Red600 = Color(0xFFDC2626)
private val Red700 = Color(0xFFB91C1C)

// Light theme colors
private val LightColorScheme = lightColorScheme(
    primary = Gray900,
    onPrimary = White,
    primaryContainer = Gray100,
    onPrimaryContainer = Gray900,

    secondary = Gray600,
    onSecondary = White,
    secondaryContainer = Gray100,
    onSecondaryContainer = Gray800,

    tertiary = Blue600,
    onTertiary = White,
    tertiaryContainer = Gray50,
    onTertiaryContainer = Blue700,

    error = Red600,
    onError = White,
    errorContainer = Gray50,
    onErrorContainer = Red700,

    background = White,
    onBackground = Gray900,

    surface = White,
    onSurface = Gray900,
    surfaceVariant = Gray50,
    onSurfaceVariant = Gray600,

    outline = Gray300,
    outlineVariant = Gray200,

    scrim = Black,
    inverseSurface = Gray900,
    inverseOnSurface = Gray50,
    inversePrimary = Gray100,

    surfaceDim = Gray50,
    surfaceBright = White,
    surfaceContainerLowest = White,
    surfaceContainerLow = Gray50,
    surfaceContainer = Gray100,
    surfaceContainerHigh = Gray100,
    surfaceContainerHighest = Gray200
)

// Dark theme colors
private val DarkColorScheme = darkColorScheme(
    primary = White,
    onPrimary = Gray900,
    primaryContainer = Gray800,
    onPrimaryContainer = Gray100,

    secondary = Gray400,
    onSecondary = Gray900,
    secondaryContainer = Gray800,
    onSecondaryContainer = Gray200,

    tertiary = Blue600,
    onTertiary = White,
    tertiaryContainer = Gray950,
    onTertiaryContainer = Blue600,

    error = Red600,
    onError = White,
    errorContainer = Gray950,
    onErrorContainer = Red600,

    background = Gray950,
    onBackground = Gray50,

    surface = Gray950,
    onSurface = Gray50,
    surfaceVariant = Gray900,
    onSurfaceVariant = Gray400,

    outline = Gray700,
    outlineVariant = Gray800,

    scrim = Black,
    inverseSurface = Gray50,
    inverseOnSurface = Gray900,
    inversePrimary = Gray900,

    surfaceDim = Gray950,
    surfaceBright = Gray800,
    surfaceContainerLowest = Gray950,
    surfaceContainerLow = Gray900,
    surfaceContainer = Gray900,
    surfaceContainerHigh = Gray800,
    surfaceContainerHighest = Gray700
)

// Custom colors for call-specific UI
object CallColors {
    val callGreen = Green600
    val callGreenPressed = Green700
    val callRed = Red600
    val callRedPressed = Red700
    val callBlue = Blue600
    val callBluePressed = Blue700
}

@Composable
fun DialerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DialerTypography,
        content = content
    )
}

// Custom typography following shadcn/ui principles
val DialerTypography = Typography(
    displayLarge = Typography().displayLarge.copy(
        color = Color.Unspecified
    ),
    displayMedium = Typography().displayMedium.copy(
        color = Color.Unspecified
    ),
    displaySmall = Typography().displaySmall.copy(
        color = Color.Unspecified
    ),
    headlineLarge = Typography().headlineLarge.copy(
        color = Color.Unspecified
    ),
    headlineMedium = Typography().headlineMedium.copy(
        color = Color.Unspecified
    ),
    headlineSmall = Typography().headlineSmall.copy(
        color = Color.Unspecified
    ),
    titleLarge = Typography().titleLarge.copy(
        color = Color.Unspecified
    ),
    titleMedium = Typography().titleMedium.copy(
        color = Color.Unspecified
    ),
    titleSmall = Typography().titleSmall.copy(
        color = Color.Unspecified
    ),
    bodyLarge = Typography().bodyLarge.copy(
        color = Color.Unspecified
    ),
    bodyMedium = Typography().bodyMedium.copy(
        color = Color.Unspecified
    ),
    bodySmall = Typography().bodySmall.copy(
        color = Color.Unspecified
    ),
    labelLarge = Typography().labelLarge.copy(
        color = Color.Unspecified
    ),
    labelMedium = Typography().labelMedium.copy(
        color = Color.Unspecified
    ),
    labelSmall = Typography().labelSmall.copy(
        color = Color.Unspecified
    )
)

// Extension functions for easier access to custom colors
@Composable
fun callButtonColors(
    containerColor: Color = CallColors.callGreen,
    contentColor: Color = Color.White
): ButtonColors = ButtonDefaults.buttonColors(
    containerColor = containerColor,
    contentColor = contentColor
)

@Composable
fun endCallButtonColors(): ButtonColors = ButtonDefaults.buttonColors(
    containerColor = CallColors.callRed,
    contentColor = Color.White
)

@Composable
fun actionButtonColors(): ButtonColors = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.surfaceVariant,
    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
)