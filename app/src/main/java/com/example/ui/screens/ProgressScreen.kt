package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.KineticViewModel
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

@Composable
fun ProgressScreen(
    onNavigateToMeasurements: () -> Unit = {},
    viewModel: KineticViewModel = viewModel()
) {
    val exercises        by viewModel.exercises.collectAsState()
    val elapsedWeeks     by viewModel.planElapsedWeeks.collectAsState()
    val goal             by viewModel.planGoal.collectAsState()
    val targetWeight     by viewModel.targetWeight.collectAsState()
    val workoutsThisWeek by viewModel.workoutsThisWeek.collectAsState()
    val totalWorkouts    by viewModel.totalWorkouts.collectAsState()

    // Deduplicate exercises by name so the selector shows each exercise once
    val uniqueExercises  = remember(exercises) { exercises.distinctBy { it.name } }

    var selectedExerciseId by remember { mutableStateOf<String?>(null) }
    var dropdownExpanded   by remember { mutableStateOf(false) }

    // Weight logging dialog
    var showWeightDialog  by remember { mutableStateOf(false) }
    var weightInput       by remember { mutableStateOf("") }
    val bodyWeightLogs    by viewModel.recentBodyWeightLogs.collectAsState()
    val prs              by viewModel.allPersonalRecords.collectAsState()

    if (showWeightDialog) {
        AlertDialog(
            onDismissRequest = { showWeightDialog = false },
            containerColor   = SurfaceContainerHigh,
            title = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text("سجّل وزن الجسم", color = OnSurface)
                }
            },
            text = {
                OutlinedTextField(
                    value         = weightInput,
                    onValueChange = { weightInput = it },
                    label         = { Text("الوزن (كجم)", color = OnSurfaceVariant) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Primary,
                        unfocusedBorderColor = Outline,
                        focusedTextColor     = OnSurface,
                        unfocusedTextColor   = OnSurface
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    weightInput.toFloatOrNull()?.let { viewModel.logBodyWeight(it) }
                    showWeightDialog = false
                    weightInput = ""
                }) {
                    Text("حفظ", color = Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWeightDialog = false; weightInput = "" }) {
                    Text("إلغاء", color = OnSurfaceVariant)
                }
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
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "التقدم",
                            style      = MaterialTheme.typography.headlineMedium,
                            color      = OnSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "الوزن والقوة والانتظام",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .background(com.example.ui.theme.Primary.copy(alpha = 0.12f))
                            .clickable { showWeightDialog = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null,
                                tint = com.example.ui.theme.Primary, modifier = Modifier.size(16.dp))
                            Text("سجّل وزن", style = MaterialTheme.typography.labelSmall,
                                color = com.example.ui.theme.Primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ── Consistency Card ──────────────────────────────────────────────
        item {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, null,
                        tint = com.example.ui.theme.Primary, modifier = Modifier.size(14.dp))
                    Text("الانتظام", style = MaterialTheme.typography.labelSmall,
                        color = com.example.ui.theme.Primary, fontWeight = FontWeight.Bold)
                    Text("تمارين / أسابيع",
                        style = MaterialTheme.typography.labelSmall, color = com.example.ui.theme.OnSurfaceVariant)
                }
            }
            ConsistencyCard(
                totalWorkouts    = totalWorkouts,
                weeklyWorkouts   = workoutsThisWeek,
                elapsedWeeks     = elapsedWeeks
            )
        }

        // ── Body Weight Card ──────────────────────────────────────────────
        item {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.MonitorWeight, null,
                        tint = com.example.ui.theme.Primary, modifier = Modifier.size(14.dp))
                    Text("وزن الجسم", style = MaterialTheme.typography.labelSmall,
                        color = com.example.ui.theme.Primary, fontWeight = FontWeight.Bold)
                    Text("لا توجد قراءات بعد",
                        style = MaterialTheme.typography.labelSmall, color = com.example.ui.theme.OnSurfaceVariant)
                }
            }
            BodyWeightCard(
                targetWeight   = targetWeight,
                goal           = goal,
                recentLogs     = bodyWeightLogs.map { it.weightKg },
                onLogWeight    = { showWeightDialog = true }
            )
        }

        // ── Exercise Progress Tracker ─────────────────────────────────────
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.TrendingUp, null,
                                tint = com.example.ui.theme.Primary, modifier = Modifier.size(16.dp))
                            Text(
                                "تتبع التمرين",
                                style = MaterialTheme.typography.headlineMedium,
                                color = OnSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "اختر تمريناً لعرض منحنى تطور قوتك خلال الجلسات الأخيرة",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Exercise picker
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val selectedEx = uniqueExercises.find { it.id == selectedExerciseId }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceContainerHigh)
                                .clickable { dropdownExpanded = true }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(
                                text  = selectedEx?.name ?: "اختر تمريناً ▼",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selectedEx != null) Primary else OnSurfaceVariant
                            )
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = OnSurfaceVariant)
                        }

                        DropdownMenu(
                            expanded          = dropdownExpanded,
                            onDismissRequest  = { dropdownExpanded = false },
                            modifier          = Modifier.background(SurfaceContainerHigh)
                        ) {
                            uniqueExercises.forEach { ex ->
                                DropdownMenuItem(
                                    text    = {
                                        Column {
                                            Text(ex.name, color = OnSurface)
                                            Text(ex.muscleGroup, color = OnSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                                        }
                                    },
                                    onClick = {
                                        selectedExerciseId = ex.id
                                        dropdownExpanded   = false
                                    }
                                )
                            }
                        }
                    }

                    // Exercise log display
                    if (selectedExerciseId != null) {
                        Spacer(modifier = Modifier.height(20.dp))
                        ExerciseProgressContent(
                            exerciseId   = selectedExerciseId!!,
                            elapsedWeeks = elapsedWeeks,
                            goal         = goal,
                            viewModel    = viewModel
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        item {
            if (prs.isNotEmpty()) {
                Column {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.EmojiEvents, null, tint = Primary, modifier = Modifier.size(16.dp))
                            Text("أرقامك القياسية", style = MaterialTheme.typography.headlineMedium,
                                color = OnSurface, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        prs.forEach { pr ->
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                    Row(modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text(pr.exerciseName, style = MaterialTheme.typography.bodyLarge,
                                                color = OnSurface, fontWeight = FontWeight.Bold)
                                            Text(pr.exerciseNameAr, style = MaterialTheme.typography.labelSmall,
                                                color = OnSurfaceVariant)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("${pr.weightKg}kg × ${pr.reps}",
                                                style = MaterialTheme.typography.bodyLarge, color = Primary, fontWeight = FontWeight.Bold)
                                            Text("1RM ≈ ${"%.1f".format(pr.estimatedOneRepMax)}kg",
                                                style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
private fun ExerciseProgressContent(
    exerciseId: String,
    elapsedWeeks: Int,
    goal: String,
    viewModel: KineticViewModel
) {
    val recentLogs by viewModel.getRecentSessionLogsForExercise(exerciseId)
        .collectAsState(initial = emptyList())

    // Group logs by sessionId, compute max weight per session
    val sessionMaxWeights = remember(recentLogs) {
        recentLogs
            .groupBy { it.sessionId }
            .entries
            .sortedBy { it.key }   // ascending by time
            .map { (_, logs) -> logs.maxOf { it.weight } }
            .takeLast(6)           // last 6 sessions
    }

    val totalSessions = recentLogs.distinctBy { it.sessionId }.size

    if (totalSessions == 0) {
        // No data yet
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerHigh)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "لا توجد بيانات بعد",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurface,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "سجّل جلسات التمرين وستظهر هنا إحصاءات تطورك الفعلية.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        return
    }

    // Progression recommendation
    val lastMax = sessionMaxWeights.lastOrNull() ?: 0f
    val prevMax = sessionMaxWeights.getOrNull(sessionMaxWeights.size - 2) ?: 0f
    val diffKg  = lastMax - prevMax

    val recText = when {
        diffKg > 0  -> "ممتاز! تحسنت بـ +${diffKg}كجم. استمر في الزيادة التدريجية."
        diffKg < 0  -> "انخفض الأداء قليلاً، ركز على الجودة وتحسن الانتعاش."
        else        -> "الأداء ثابت، جرّب زيادة 2.5 كجم في المرة القادمة."
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column {
            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProgressStatItem("$totalSessions", "جلسة", Primary)
                ProgressStatItem("${lastMax.let { if (it == it.toLong().toFloat()) it.toInt().toString() else it.toString() }}كجم", "أعلى وزن", OnSurface)
                ProgressStatItem("${recentLogs.size}", "مجموع سيتات", OnSurface)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Chart
            if (sessionMaxWeights.isNotEmpty()) {
                val maxW = sessionMaxWeights.maxOrNull()!!.coerceAtLeast(1f)
                Text(
                    "أقصى وزن لكل جلسة (كجم)",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Bottom
                ) {
                    sessionMaxWeights.forEachIndexed { idx, w ->
                        val isLast = idx == sessionMaxWeights.lastIndex
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f).padding(horizontal = 3.dp)
                        ) {
                            if (isLast) {
                                Text(
                                    "${w.toInt()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .height(((w / maxW) * 80).dp.coerceAtLeast(4.dp))
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(if (isLast) Primary else Color.White.copy(alpha = 0.15f))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recommendation box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Primary.copy(alpha = 0.08f))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        recText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurface
                    )
                }
            }
        }
    }
}

@Composable
fun ConsistencyCard(
    totalWorkouts: Int,
    weeklyWorkouts: Int,
    elapsedWeeks: Int
) {
    val expectedSessions = elapsedWeeks * 4
    val score = if (expectedSessions == 0) 100f
    else (totalWorkouts.toFloat() / expectedSessions.toFloat()).coerceIn(0f, 1f) * 100f

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                "CONSISTENCY SCORE",
                color = OnSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(150.dp)) {
                CircularProgressIndicator(
                    progress    = { 1f },
                    modifier    = Modifier.size(150.dp),
                    color       = Color.White.copy(alpha = 0.05f),
                    strokeWidth = 12.dp
                )
                CircularProgressIndicator(
                    progress    = { score / 100f },
                    modifier    = Modifier.size(150.dp),
                    color       = when {
                        score >= 80f -> Primary
                        score >= 50f -> Color(0xFFFFB347)
                        else         -> MaterialTheme.colorScheme.error
                    },
                    strokeWidth = 12.dp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${score.toInt()}%",
                        style = MaterialTheme.typography.displayLarge,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "OVERALL",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ProgressStatItem("$totalWorkouts", "SESSIONS", OnSurface)
                ProgressStatItem("$elapsedWeeks", "WEEKS", OnSurface)
                ProgressStatItem("$weeklyWorkouts", "THIS WEEK", Primary)
            }
        }
    }
}

@Composable
private fun BodyWeightCard(
    targetWeight: Float,
    goal: String,
    recentLogs: List<Float>,
    onLogWeight: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text("BODY WEIGHT", color = OnSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        val displayWeight = recentLogs.lastOrNull() ?: targetWeight
                        Text(
                            String.format("%.1f", displayWeight),
                            style = MaterialTheme.typography.displayLarge,
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(" kg", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    Text(
                        when {
                            goal.contains("cut", true) || goal.contains("Cutting") -> "هدف: خفض الوزن تدريجياً"
                            goal.contains("bulk", true) || goal.contains("Bulking") -> "هدف: بناء الكتلة العضلية"
                            else -> "هدف: المحافظة على الوزن"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Primary
                    )
                }
                IconButton(onClick = onLogWeight) {
                    Icon(Icons.Default.Add, contentDescription = "Log Weight", tint = Primary)
                }
            }

            if (recentLogs.size >= 2) {
                Spacer(modifier = Modifier.height(16.dp))
                val maxW = recentLogs.maxOrNull()!!.coerceAtLeast(1f)
                val minW = recentLogs.minOrNull()!!
                val range = (maxW - minW).coerceAtLeast(1f)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Bottom
                ) {
                    recentLogs.takeLast(8).forEachIndexed { idx, w ->
                        val h = ((w - minW) / range * 60f + 20f)
                        val isLast = idx == recentLogs.takeLast(8).lastIndex
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .weight(1f)
                                .height(h.dp)
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                .background(if (isLast) Primary else Color.White.copy(alpha = 0.12f))
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "اضغط + لتسجيل وزن جسمك وتتبع منحنى تطورك",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ProgressStatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
    }
}
