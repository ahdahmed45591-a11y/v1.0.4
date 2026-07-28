package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════════════════
//  🎨 BAOU Finance — Dark Finance Premium Theme v2.0
//  Dark Mode par défaut (style Binance / Robinhood)
// ══════════════════════════════════════════════════════════

private val DarkFinanceColorScheme = darkColorScheme(
    // Brand
    primary          = OrangeBrand,
    onPrimary        = Color.White,
    primaryContainer = OrangeBrandLight,
    onPrimaryContainer = OrangeBrand,

    // Secondary — Gain / Vert
    secondary        = GainGreen,
    onSecondary      = Color(0xFF003828),
    secondaryContainer = GainGreenBg,
    onSecondaryContainer = GainGreen,

    // Tertiary — Gold Premium
    tertiary         = GoldPremium,
    onTertiary       = Color(0xFF1A1000),
    tertiaryContainer = GoldPremiumLight,

    // Backgrounds — Nuit Financière
    background       = DeepBackground,
    onBackground     = TextPrimary,

    // Surfaces — Cartes & Composants
    surface          = SurfaceCard,
    onSurface        = TextPrimary,
    surfaceVariant   = SurfaceCard2,
    onSurfaceVariant = TextSecondary,

    // Outline / Borders
    outline          = BorderSubtle,
    outlineVariant   = BorderMedium,

    // Error
    error            = LossRed,
    onError          = Color.White,
    errorContainer   = LossRedBg,

    // Inverse (pour snackbar / toast)
    inverseSurface   = TextPrimary,
    inverseOnSurface = DeepBackground,
    inversePrimary   = OrangeDeep,

    // Scrim
    scrim            = Color(0xCC080E1C),
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    // Dark Finance Premium — toujours sombre
    MaterialTheme(
        colorScheme = DarkFinanceColorScheme,
        typography  = Typography,
        content     = content
    )
}
