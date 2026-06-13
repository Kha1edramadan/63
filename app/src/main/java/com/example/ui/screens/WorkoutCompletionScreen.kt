package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.KineticViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

// ── Static smart post-workout notes ──────────────────────────────────────────
private fun defaultInsight(durationMin: Int, newPRs: Int, totalWorkouts: Int): String {
    val durationNote = when {
        durationMin < 35 -> "التمرين كان خفيف في الوقت، حاول توصل لـ 45-55 دقيقة المرة الجاية."
        durationMin > 80 -> "التمرين طال أوي، الجلسة المثالية 45-70 دقيقة. حاول تقصّر الراحة."
        else             -> "${durationMin} دقيقة"
    }
    val prNote = if (newPRs > 0) " — $newPRs رقم قياسي" else ""
    val streakNote = when {
        totalWorkouts % 10 == 0 && totalWorkouts > 0 ->
            " | وصلت لـ $totalWorkouts تمرين، إنجاز حقيقي!"
        totalWorkouts < 5 ->
            " | في البداية كل جلسة بتبني الأساس، استمر."
        else ->
            " | الانتظام هو الفرق بينك وبين اللي بيتمنوا بس مش بيعملوا."
    }
    return durationNote + prNote + streakNote
}

// ─────────────────────────────────────────────────────────────────────────────

data class ConfettiPiece(
    val x: Float, val y: Float, val vx: Float, val vy: Float,
    val color: Color, val size: Float, val rotation: Float, val rotationSpeed: Float
)

@Composable
fun WorkoutCompletionScreen(
    sessionId: Long,
    planName: String,
    onDone: () -> Unit,
    viewModel: KineticViewModel = viewModel()
) {
    val allPRs        by viewModel.allPersonalRecords.collectAsState()
    val totalWorkouts by viewModel.totalWorkouts.collectAsState()

    val sessionPRs = remember(allPRs, sessionId) {
        allPRs.filter { it.achievedAt >= sessionId - 500 }
    }
    val durationMin = remember { ((System.currentTimeMillis() - sessionId) / 60000).toInt().coerceAtLeast(1) }

    // Static smart note
    val insight = remember(durationMin, sessionPRs.size, totalWorkouts) {
        defaultInsight(durationMin, sessionPRs.size, totalWorkouts)
    }

    // Confetti
    val confettiColors = listOf(Primary, Color(0xFF00C9FF), Color(0xFFFF6B6B),
        Color(0xFFFFD93D), Color(0xFF6BCB77), Color(0xFFFF922B))
    val pieces = remember {
        List(80) {
            ConfettiPiece(
                x = Random.nextFloat(), y = Random.nextDouble(-0.2, 0.0).toFloat(),
                vx = Random.nextDouble(-0.003, 0.003).toFloat(),
                vy = Random.nextDouble(0.004, 0.009).toFloat(),
                color = confettiColors.random(),
                size = Random.nextDouble(6.0, 14.0).toFloat(),
                rotation = Random.nextDouble(0.0, 360.0).toFloat(),
                rotationSpeed = Random.nextDouble(-3.0, 3.0).toFloat()
            )
        }
    }
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { repeat(200) { delay(16); tick++ } }

    Box(modifier = Modifier.fillMaxSize().background(Surface)) {

        // ── Confetti Canvas ────────────────────────────────────────────────
        if (tick < 180) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                pieces.forEach { p ->
                    val newY = p.y + p.vy * tick
                    val newX = p.x + p.vx * tick
                    if (newY < 1.2f) {
                        drawRect(
                            color = p.color.copy(alpha = (1f - newY.coerceIn(0f, 1f)) * 0.9f),
                            topLeft = Offset(newX * size.width - p.size / 2, newY * size.height - p.size / 2),
                            size = androidx.compose.ui.geometry.Size(p.size, p.size * 0.6f)
                        )
                    }
                }
            }
        }

        // ── Content ────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
            .background(Color(0xFF0C0C0C))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint     = Primary,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "تمرين مكتمل",
                style      = MaterialTheme.typography.headlineMedium,
                color      = OnSurface,
                fontWeight = FontWeight.Bold
            )
            Text(planName, style = MaterialTheme.typography.bodyMedium, color = Primary)
            Spacer(Modifier.height(24.dp))

            // ── Stats Row ─────────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CompletionStat("${durationMin}د",    "مدة",       Modifier.weight(1f))
                CompletionStat("$totalWorkouts",     "تمرين كلي", Modifier.weight(1f))
                CompletionStat("${sessionPRs.size}", "أرقام",     Modifier.weight(1f))
            }

            // ── New PRs ───────────────────────────────────────────────────
            if (sessionPRs.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF141414))
                        .border(1.dp, Primary.copy(.20f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Text(
                            "أرقام قياسية جديدة",
                            style      = MaterialTheme.typography.labelMedium,
                            color      = Primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        sessionPRs.forEach { pr ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(pr.exerciseName, style = MaterialTheme.typography.bodySmall, color = OnSurface)
                                Text(
                                    "${pr.weightKg}kg × ${pr.reps}",
                                    style      = MaterialTheme.typography.bodySmall,
                                    color      = Primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // ── Session Notes ─────────────────────────────────────────────
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF141414))
                    .border(1.dp, Color(0xFF202020), RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text(
                        "ملاحظات الجلسة",
                        style      = MaterialTheme.typography.labelMedium,
                        color      = Primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        insight,
                        style      = MaterialTheme.typography.bodyMedium,
                        color      = OnSurface
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Done Button ───────────────────────────────────────────────
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.Black),
                shape    = RoundedCornerShape(16.dp)
            ) {
                Text("رجوع للرئيسية", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CompletionStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF141414))
            .border(1.dp, Color(0xFF202020), RoundedCornerShape(14.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            style      = MaterialTheme.typography.titleLarge,
            color      = OnSurface,
            fontWeight = FontWeight.Bold
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
    }
}
