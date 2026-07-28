package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════════════════
//  🎨 BAOU Finance — Dark Finance Premium Design System
//  Redesign Senior UX/UI — Version 2.0
// ══════════════════════════════════════════════════════════

// ── Fonds principaux (Dark Finance Night) ────────────────
val DeepBackground       = Color(0xFF080E1C) // Fond global nuit financière
val SurfaceCard          = Color(0xFF101828) // Surface carte principale
val SurfaceCard2         = Color(0xFF182035) // Surface carte secondaire
val SurfaceElevated      = Color(0xFF1C2940) // Surface élevée / hover

// ── Brand Colors (identiques Web Admin) ──────────────────
val OrangeBrand          = Color(0xFFFF8200) // Orange BAOU Officiel
val OrangeDeep           = Color(0xFFE56500) // Orange sombre (gradient end)
val OrangeBrandLight     = Color(0x1AFF8200) // Tint orange 10%
val OrangeGlow           = Color(0x33FF8200) // Halo / shadow orange

// ── Marine (Sidebar Web Admin) ────────────────────────────
val DeepNavy             = Color(0xFF0B1C30) // Dark Navy Admin harmonisé
val DeepNavySurface      = Color(0xFF132A45) // Variation carte sombre

// ── Couleurs Financières — Statuts ───────────────────────
val GainGreen            = Color(0xFF22D3A4) // Hausse / Validé — cyan-vert moderne
val GainGreenBg          = Color(0x1A22D3A4) // Fond vert 10%
val LossRed              = Color(0xFFF25A5A) // Baisse / Rejeté
val LossRedBg            = Color(0x1AF25A5A) // Fond rouge 10%
val PendingAmber         = Color(0xFFFBBF24) // En attente / Ambre
val PendingAmberBg       = Color(0x1AFBBF24) // Fond ambre 10%
val GoldPremium          = Color(0xFFF59E0B) // Or Premium / Compte Vérifié

// ── Aliases backward compat ───────────────────────────────
val ForestGreen          = GainGreen
val ForestGreenLight     = GainGreenBg
val GoldPremiumLight     = Color(0x33F59E0B)
val RedLoss              = LossRed
val RedLossLight         = LossRedBg
val PendingOrange        = PendingAmber
val PendingOrangeBg      = PendingAmberBg

// ── Texte ─────────────────────────────────────────────────
val TextPrimary          = Color(0xFFF1F5F9) // Blanc doux (pas pur blanc)
val TextSecondary        = Color(0xFF94A3B8) // Gris bleuté
val TextMuted            = Color(0xFF64748B) // Gris muted
val DarkOnBackground     = Color(0xFFF1F5F9) // Texte sur fond sombre

// ── Bordures & Séparateurs ────────────────────────────────
val BorderSubtle         = Color(0x14FFFFFF) // Bordure blanche 8%
val BorderMedium         = Color(0x1FFFFFFF) // Bordure blanche 12%
val GrayBorder           = Color(0x1AFFFFFF) // Alias backward compat

// ── Surfaces Light (mode clair — compatibilité) ───────────
val LightBackground      = Color(0xFFF4F6FA)
val ScreenBackground     = Color(0xFF080E1C) // Remplacé par Dark
val LightSurface         = Color(0xFF101828)
val CardSurface          = Color(0xFF101828)
val SurfaceVariant       = Color(0xFF1C2940)
