package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ══════════════════════════════════════════════════════════
//  📐 BAOU Finance — Typographie Premium v2.0
//  Système complet de 8 styles typographiques
// ══════════════════════════════════════════════════════════

val Typography = Typography(
    // Grand affichage — Solde Portfolio (36sp, ExtraBold)
    displayLarge = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.ExtraBold,
        fontSize     = 36.sp,
        lineHeight   = 42.sp,
        letterSpacing = (-0.5).sp,
    ),

    // Affichage medium — Titres majeurs (28sp, Bold)
    displayMedium = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.Bold,
        fontSize     = 28.sp,
        lineHeight   = 34.sp,
        letterSpacing = (-0.25).sp,
    ),

    // Titre écran (22sp, Bold)
    headlineLarge = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.Bold,
        fontSize     = 22.sp,
        lineHeight   = 28.sp,
        letterSpacing = 0.sp,
    ),

    // Sous-titre section (18sp, SemiBold)
    headlineMedium = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 18.sp,
        lineHeight   = 24.sp,
        letterSpacing = 0.sp,
    ),

    // Titre petite section (16sp, SemiBold)
    headlineSmall = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 16.sp,
        lineHeight   = 22.sp,
        letterSpacing = 0.sp,
    ),

    // Titre composant (15sp, SemiBold)
    titleLarge = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 15.sp,
        lineHeight   = 20.sp,
        letterSpacing = 0.sp,
    ),

    // Titre medium (14sp, Medium)
    titleMedium = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.Medium,
        fontSize     = 14.sp,
        lineHeight   = 20.sp,
        letterSpacing = 0.1.sp,
    ),

    // Titre petit / Nom Ticker (13sp, SemiBold)
    titleSmall = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 13.sp,
        lineHeight   = 18.sp,
        letterSpacing = 0.1.sp,
    ),

    // Corps principal (14sp, Regular)
    bodyLarge = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.Normal,
        fontSize     = 14.sp,
        lineHeight   = 22.sp,
        letterSpacing = 0.25.sp,
    ),

    // Corps medium (13sp, Regular)
    bodyMedium = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.Normal,
        fontSize     = 13.sp,
        lineHeight   = 20.sp,
        letterSpacing = 0.25.sp,
    ),

    // Corps petit / Secondaire (12sp, Regular)
    bodySmall = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.Normal,
        fontSize     = 12.sp,
        lineHeight   = 17.sp,
        letterSpacing = 0.4.sp,
    ),

    // Label/Badge (11sp, Bold, espacé)
    labelLarge = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.Bold,
        fontSize     = 11.sp,
        lineHeight   = 16.sp,
        letterSpacing = 0.5.sp,
    ),

    // Label medium (10sp, Medium)
    labelMedium = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.Medium,
        fontSize     = 10.sp,
        lineHeight   = 14.sp,
        letterSpacing = 0.5.sp,
    ),

    // Micro-label (9sp, Bold, lettres espacées)
    labelSmall = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.Bold,
        fontSize     = 9.sp,
        lineHeight   = 13.sp,
        letterSpacing = 0.8.sp,
    ),
)

// ── Styles Monospace pour Montants FCFA ──────────────────
// Utilisés manuellement dans les composables avec .copy(fontFamily = MonoFamily)
val MonoFamily = FontFamily.Monospace
