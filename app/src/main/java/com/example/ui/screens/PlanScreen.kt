package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.KineticViewModel
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun PlanScreen(viewModel: KineticViewModel = viewModel()) {
    val plans            by viewModel.plans.collectAsState()
    val currentGoal      by viewModel.planGoal.collectAsState()
    val planDuration     by viewModel.planDurationWeeks.collectAsState()
    val elapsedWeeks     by viewModel.planElapsedWeeks.collectAsState()
    val completedCycles  by viewModel.completedCycles.collectAsState()
    val currentPlanIndex by viewModel.currentPlanIndex.collectAsState()
    val recentSessions      by viewModel.recentSessions.collectAsState()
    val subscriptionMonths  by viewModel.subscriptionMonths.collectAsState()
    val subscriptionStart   by viewModel.subscriptionStartDate.collectAsState()

    var showSettingsDialog  by remember { mutableStateOf(false) }
    var showResetConfirm    by remember { mutableStateOf(false) }

    // Settings dialog local state
    var selectedGoal     by remember { mutableStateOf(currentGoal) }
    var durationText     by remember { mutableStateOf(planDuration.toString()) }
    var subMonthsText    by remember(subscriptionMonths) { mutableStateOf(subscriptionMonths.toString()) }
    var subStartMs       by remember(subscriptionStart) { mutableStateOf(subscriptionStart) }

    // Sync dialog state when dialog opens
    LaunchedEffect(showSettingsDialog) {
        if (showSettingsDialog) {
            selectedGoal  = currentGoal
            durationText  = planDuration.toString()
        }
    }

    // ── Settings Dialog ────────────────────────────────────────────────
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            containerColor   = SurfaceContainerHigh,
            title = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text("إعدادات الخطة", color = OnSurface, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Goal
                        Text("الهدف الحالي", style = MaterialTheme.typography.labelSmall, color = Primary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Bulking" to "تضخيم", "Cutting" to "تنشيف", "Maintain" to "ثبات").forEach { (key, label) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedGoal == key) Primary else SurfaceContainerHigh.copy(alpha = 0.5f))
                                        .clickable { selectedGoal = key },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        label,
                                        color = if (selectedGoal == key) Color.Black else OnSurface,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (selectedGoal == key) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        // Duration
                        OutlinedTextField(
                            value         = durationText,
                            onValueChange = { durationText = it },
                            label         = { Text("مدة الخطة (أسابيع)", color = OnSurfaceVariant) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine    = true,
                            modifier      = Modifier.fillMaxWidth(),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = Primary,
                                unfocusedBorderColor = Outline,
                                focusedTextColor     = OnSurface,
                                unfocusedTextColor   = OnSurface
                            )
                        )

                        // Subscription
                        Text("الاشتراك", style = MaterialTheme.typography.labelSmall, color = Primary)
                        OutlinedTextField(
                            value         = subMonthsText,
                            onValueChange = { subMonthsText = it },
                            label         = { Text("عدد شهور الاشتراك", color = OnSurfaceVariant) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine    = true,
                            modifier      = Modifier.fillMaxWidth(),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = Primary,
                                unfocusedBorderColor = Outline,
                                focusedTextColor     = OnSurface,
                                unfocusedTextColor   = OnSurface
                            )
                        )
                        Text(
                            "تاريخ بداية الاشتراك: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(subStartMs))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("اليوم" to 0L, "منذ أسبوع" to -7L, "منذ شهر" to -30L).forEach { (label, dayOffset) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceContainerHigh)
                                        .clickable {
                                            val cal = Calendar.getInstance()
                                            cal.add(Calendar.DAY_OF_YEAR, dayOffset.toInt())
                                            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
                                            subStartMs = cal.timeInMillis
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurface)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updatePlanSettings(
                            goal          = selectedGoal,
                            durationWeeks = durationText.toIntOrNull() ?: planDuration
                        )
                        viewModel.updateSubscription(
                            startDateMs = subStartMs,
                            months      = subMonthsText.toIntOrNull() ?: 1
                        )
                        showSettingsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.Black)
                ) { Text("حفظ", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("إلغاء", color = OnSurfaceVariant)
                }
            }
        )
    }

    // ── Reset Confirm Dialog (with goal picker) ────────────────────────
    var resetSelectedGoal by remember { mutableStateOf(currentGoal) }
    if (showResetConfirm) {
        LaunchedEffect(showResetConfirm) { resetSelectedGoal = currentGoal }
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            containerColor   = SurfaceContainerHigh,
            title            = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text("ابدأ دورة جديدة", color = OnSurface, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            "سيُعاد الأسبوع إلى 1 وتبدأ دورة جديدة. سجلات التمارين لن تُحذف.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                        Text("اختر هدف الدورة الجديدة:", style = MaterialTheme.typography.labelSmall, color = Primary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Bulking" to "تضخيم", "Cutting" to "تنشيف", "Maintain" to "ثبات").forEach { (key, label) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (resetSelectedGoal == key) Primary else SurfaceContainerHigh)
                                        .clickable { resetSelectedGoal = key },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        label,
                                        color = if (resetSelectedGoal == key) Color.Black else OnSurface,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (resetSelectedGoal == key) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updatePlanSettings(goal = resetSelectedGoal, durationWeeks = planDuration)
                        viewModel.resetPlanProgress()
                        showResetConfirm = false
                    },
                    colors  = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.Black)
                ) { Text("ابدأ من جديد", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("إلغاء", color = OnSurfaceVariant) }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C0C))
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("الخطة", style = MaterialTheme.typography.headlineMedium, color = OnSurface, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { showResetConfirm = true }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = OnSurfaceVariant)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Primary.copy(alpha = 0.12f))
                            .clickable { showSettingsDialog = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "Settings", tint = Primary, modifier = Modifier.size(16.dp))
                            Text("ضبط الخطة", style = MaterialTheme.typography.labelSmall, color = Primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // ── Plan overview ─────────────────────────────────────────────
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column {
                        Text("الخطة الحالية", style = MaterialTheme.typography.labelSmall, color = Primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            PlanStatItem("الهدف",    currentGoal)
                            PlanStatItem("الأسبوع",  "$elapsedWeeks / $planDuration")
                            PlanStatItem("الدورات",  "$completedCycles مكتملة")
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Progress bar
                        val progress = (elapsedWeeks.toFloat() / planDuration.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress    = { progress },
                            modifier    = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color       = Primary,
                            trackColor  = SurfaceContainerHigh
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "${(progress * 100).toInt()}% من الخطة مكتمل",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )

                        if (viewModel.isPlanComplete()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Primary.copy(alpha = 0.1f))
                                    .border(1.dp, Primary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "انتهت الخطة! اضغط إعادة التعيين لبدء دورة جديدة.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Workout rotation ──────────────────────────────────────────
        item {
            Text("دورة التمارين الأسبوعية", style = MaterialTheme.typography.headlineMedium, color = OnSurface, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "5 خطط تتناوب، الخامسة مخصصة لتعويض القعدة الطويلة",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )
        }

        items(plans, key = { it.id }) { plan ->
            val dayIndex = plans.indexOf(plan)
            val isCurrent = dayIndex == currentPlanIndex % plans.size.coerceAtLeast(1)
            PlanDayCard(
                planName    = plan.name,
                dayLabel    = "اليوم ${dayIndex + 1}",
                isCurrent   = isCurrent,
                planCategory = plan.category
            )
        }

        // ── Recent Sessions ───────────────────────────────────────────
        if (recentSessions.isNotEmpty()) {
            item {
                Text("آخر الجلسات", style = MaterialTheme.typography.headlineMedium, color = OnSurface, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(recentSessions.take(5), key = { it.sessionId }) { session ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(session.planName, style = MaterialTheme.typography.bodyLarge, color = OnSurface, fontWeight = FontWeight.Bold)
                                Text(
                                    SimpleDateFormat("EEE، dd MMM yyyy  HH:mm", Locale("ar")).format(Date(session.completedAt)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun PlanStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = OnSurface, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
    }
}

@Composable
private fun PlanDayCard(
    planName:     String,
    dayLabel:     String,
    isCurrent:    Boolean,
    planCategory: String
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val isPosture = planName.contains("POSTURE", ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(when {
                            isCurrent  -> Primary
                            isPosture  -> Color(0xFF1A2A1A)
                            else       -> SurfaceContainerHigh
                        }),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isPosture) Icons.Default.SelfImprovement else Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = when {
                            isCurrent -> Color.Black
                            isPosture -> Color(0xFF6FCF6F)
                            else      -> OnSurfaceVariant
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(planName, style = MaterialTheme.typography.bodyLarge, color = OnSurface, fontWeight = FontWeight.Bold)
                    }
                    Text(dayLabel, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                }
            }
            if (isCurrent) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Primary.copy(alpha = 0.15f))
                        .border(1.dp, Primary.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("التالي", style = MaterialTheme.typography.labelSmall, color = Primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
