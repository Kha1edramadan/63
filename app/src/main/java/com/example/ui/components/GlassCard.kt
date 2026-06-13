package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ── GlassCard — minimal, consistent ──────────────────────────────────────────
@Composable
fun GlassCard(
    modifier:  Modifier  = Modifier,
    padding:   Dp        = 16.dp,
    accentGlow: Boolean  = false,   // kept for API compat — ignored
    content:   @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF141414))
            .border(1.dp, Color(0xFF202020), RoundedCornerShape(16.dp))
            .padding(padding),
        content = content
    )
}

// ── PrimaryGlowCard — kept for compat, now same as GlassCard ─────────────────
@Composable
fun PrimaryGlowCard(
    modifier: Modifier = Modifier,
    padding:  Dp       = 16.dp,
    content:  @Composable BoxScope.() -> Unit
) = GlassCard(modifier = modifier, padding = padding, content = content)
