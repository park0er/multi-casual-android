package ai.multica.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.alexzhirkevich.cupertino.ExperimentalCupertinoApi
import io.github.alexzhirkevich.cupertino.theme.CupertinoTheme

object MulticaColors {
    var Accent = Color(0xFF2563EB)
        private set
    var AccentSoft = Color(0xFF1C2638)
        private set
    var Background = Color.Black
        private set
    var GroupedBackground = Color.Black
        private set
    var Surface = Color(0xFF1C1C1E)
        private set
    var SurfaceElevated = Color(0xFF242426)
        private set
    var Text = Color(0xFFF5F5F7)
        private set
    var TextPrimary = Color(0xFFF5F5F7)
        private set
    var TextSecondary = Color(0xFFA1A1AA)
        private set
    var TextTertiary = Color(0xFF71717A)
        private set
    var Muted = Color(0xFF8E8E93)
        private set
    var Border = Color(0xFF2C2C2E)
        private set
    var Success = Color(0xFF16A34A)
        private set
    var Danger = Color(0xFFFF453A)
        private set
    var Warning = Color(0xFFFF9F0A)
        private set

    fun applyDarkTheme(darkTheme: Boolean) {
        Accent = Color(0xFF2563EB)
        Success = Color(0xFF16A34A)
        Danger = if (darkTheme) Color(0xFFFF453A) else Color(0xFFDC2626)
        Warning = if (darkTheme) Color(0xFFFF9F0A) else Color(0xFFD97706)
        if (darkTheme) {
            AccentSoft = Color(0xFF1C2638)
            Background = Color.Black
            GroupedBackground = Color(0xFF0B0B0D)
            Surface = Color(0xFF1C1C1E)
            SurfaceElevated = Color(0xFF242426)
            TextPrimary = Color(0xFFF5F5F7)
            TextSecondary = Color(0xFFA1A1AA)
            TextTertiary = Color(0xFF71717A)
            Text = TextPrimary
            Muted = TextSecondary
            Border = Color(0xFF2C2C2E)
        } else {
            AccentSoft = Color(0xFFEAF2FF)
            Background = Color(0xFFF6F7F9)
            GroupedBackground = Color(0xFFF2F3F6)
            Surface = Color.White
            SurfaceElevated = Color.White
            TextPrimary = Color(0xFF111827)
            TextSecondary = Color(0xFF6B7280)
            TextTertiary = Color(0xFF9CA3AF)
            Text = TextPrimary
            Muted = TextSecondary
            Border = Color(0xFFE5E7EB)
        }
    }
}

@Immutable
data class MulticaSpacing(
    val page: Dp = 18.dp,
    val pageHorizontal: Dp = 20.dp,
    val pageTop: Dp = 28.dp,
    val sectionTop: Dp = 22.dp,
    val rowGap: Dp = 10.dp,
    val rowPaddingHorizontal: Dp = 14.dp,
    val rowPaddingVertical: Dp = 12.dp,
    val compactRowVertical: Dp = 9.dp,
    val controlHeight: Dp = 44.dp,
    val smallControlHeight: Dp = 34.dp,
    val bottomBarHorizontal: Dp = 16.dp,
    val pillHorizontal: Dp = 12.dp,
    val pillVertical: Dp = 7.dp,
)

val LocalMulticaSpacing = staticCompositionLocalOf { MulticaSpacing() }

private fun multicaLightScheme(): ColorScheme = lightColorScheme(
    primary = MulticaColors.Accent,
    onPrimary = Color.White,
    primaryContainer = MulticaColors.AccentSoft,
    onPrimaryContainer = MulticaColors.Accent,
    background = MulticaColors.Background,
    onBackground = MulticaColors.Text,
    surface = MulticaColors.Surface,
    onSurface = MulticaColors.Text,
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = MulticaColors.Muted,
    outline = MulticaColors.Border,
    error = MulticaColors.Danger,
    onError = Color.White,
)

private fun multicaDarkScheme(): ColorScheme = darkColorScheme(
    primary = MulticaColors.Accent,
    onPrimary = Color.White,
    primaryContainer = MulticaColors.AccentSoft,
    onPrimaryContainer = Color(0xFF7DB1FF),
    background = MulticaColors.Background,
    onBackground = MulticaColors.Text,
    surface = MulticaColors.Surface,
    onSurface = MulticaColors.Text,
    surfaceVariant = Color(0xFF242426),
    onSurfaceVariant = MulticaColors.Muted,
    outline = MulticaColors.Border,
    error = MulticaColors.Danger,
    onError = Color.White,
)

private val largeTitle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 34.sp,
    lineHeight = 40.sp,
)

private val pageTitle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 34.sp,
)

private val rowTitle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 17.sp,
    lineHeight = 22.sp,
)

private val bodyText = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 23.sp,
)

private val subheadline = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 20.sp,
)

private val captionText = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 13.sp,
    lineHeight = 17.sp,
)

private val eyebrowText = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 11.sp,
    lineHeight = 14.sp,
)

private val multicaTypography = Typography(
    displaySmall = largeTitle,
    headlineMedium = pageTitle,
    titleMedium = rowTitle,
    bodyLarge = bodyText,
    bodyMedium = subheadline,
    bodySmall = captionText,
    labelMedium = eyebrowText,
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)

private val multicaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
@OptIn(ExperimentalCupertinoApi::class)
fun MulticaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MulticaColors.applyDarkTheme(darkTheme)
    CupertinoTheme(
        content = {
            MaterialTheme(
                colorScheme = if (darkTheme) multicaDarkScheme() else multicaLightScheme(),
                typography = multicaTypography,
                shapes = multicaShapes,
                content = content,
            )
        },
    )
}
