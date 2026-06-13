package com.example.ui.screens

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Exercise
import com.example.data.SetLog
import com.example.ui.KineticViewModel
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─── Main Screen ──────────────────────────────────────────────────────────────

@Composable
fun WorkoutSessionScreen(
    planId: String,
    onFinish: (sessionId: Long, planName: String) -> Unit = { _, _ -> },
    viewModel: KineticViewModel = viewModel()
) {
    val exercises by viewModel.getExercisesForPlan(planId).collectAsState(initial = emptyList())
    val plans     by viewModel.plans.collectAsState()
    val planName  = plans.find { it.id == planId }?.name ?: planId
    val sessionId = remember { System.currentTimeMillis() }

    // Session-level note
    var sessionNote by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C0C))
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(Modifier.height(28.dp))
            Column {
                Text(
                    planName,
                    style      = MaterialTheme.typography.headlineMedium,
                    color      = OnSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${exercises.size} تمارين",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }
        }

        items(exercises, key = { it.id }) { ex ->
            ExerciseLoggingCard(exercise = ex, sessionId = sessionId, viewModel = viewModel)
        }

        // ── Session Notes ──────────────────────────────────────────────────
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Edit, null, tint = Primary, modifier = Modifier.size(14.dp))
                            Text("ملاحظات الجلسة", style = MaterialTheme.typography.labelSmall, color = Primary)
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = sessionNote, onValueChange = { sessionNote = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("ملاحظاتك...", color = OnSurfaceVariant.copy(.5f)) },
                            minLines = 2, maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary, unfocusedBorderColor = Outline,
                                focusedTextColor = OnSurface, unfocusedTextColor = OnSurface, cursorColor = Primary
                            )
                        )
                    }
                }
            }
        }

        // ── Finish Button ──────────────────────────────────────────────────
        item {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    if (sessionNote.isNotBlank()) viewModel.saveWorkoutNote(sessionId, sessionNote)
                    viewModel.finishWorkout(planId, planName, sessionId)
                    onFinish(sessionId, planName)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.Black),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("إنهاء التمرين", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(96.dp))
        }
    }
}

// ─── Exercise Card ────────────────────────────────────────────────────────────

@Composable
fun ExerciseLoggingCard(exercise: Exercise, sessionId: Long, viewModel: KineticViewModel) {
    val scope = rememberCoroutineScope()
    var expanded     by remember { mutableStateOf(false) }
    var showCues     by remember { mutableStateOf(false) }
    var totalSets    by remember { mutableIntStateOf(exercise.sets) }
    val setWeights   = remember { mutableStateListOf(*Array(exercise.sets) { "" }) }
    val setReps      = remember { mutableStateListOf(*Array(exercise.sets) { "" }) }
    val setCompleted = remember { mutableStateListOf(*Array(exercise.sets) { false }) }
    val lastLogs by viewModel.getLastSessionLogsForExercise(exercise.id).collectAsState(initial = emptyList())

    val context = LocalContext.current

    // ── Sound + Vibration + Notification helper ───────────────────────────────
    fun fireAlertSignal() {
        // Vibrate: long-short-long pattern
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 400, 150, 200, 150, 600), -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 400, 150, 200, 150, 600), -1)
        }
        // Sound via STREAM_ALARM — bypasses silent mode and DND
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 90)
            toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1400)
        } catch (_: Exception) {}
    }

    fun ensureRestChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            val ch = android.app.NotificationChannel(
                "igym_rest_timer", "IGYM تايمر الراحة",
                android.app.NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "عداد وقت الراحة بين المجاميع"
                enableVibration(false)
                setSound(null, null)
            }
            nm.createNotificationChannel(ch)
            val chAlert = android.app.NotificationChannel(
                "igym_rest_end", "IGYM انتهت الراحة",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعار نهاية وقت الراحة"
                enableVibration(true)
                setBypassDnd(true)
                vibrationPattern = longArrayOf(0, 400, 150, 400)
            }
            nm.createNotificationChannel(chAlert)
        }
    }

    fun postRestCountdownNotification(secsLeft: Int, totalRestSecs: Int = 180) {
        try {
            ensureRestChannel()
            val mins = secsLeft / 60
            val secs = secsLeft % 60
            val timeStr = "%d:%02d".format(mins, secs)
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            val notif = androidx.core.app.NotificationCompat.Builder(context, "igym_rest_timer")
                .setSmallIcon(android.R.drawable.ic_media_pause)
                .setContentTitle("راحة")
                .setContentText("المجموعة التالية بعد $timeStr")
                .setSubText("IGYM")
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setSilent(true)
                .setProgress(totalRestSecs, totalRestSecs - secsLeft, false)
                .build()
            nm.notify(8000, notif)
        } catch (_: Exception) {}
    }

    fun cancelRestCountdownNotification() {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            nm.cancel(8000)
        } catch (_: Exception) {}
    }

    fun fireRestEndNotification() {
        try {
            ensureRestChannel()
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            nm.cancel(8000)  // cancel the countdown
            val notif = androidx.core.app.NotificationCompat.Builder(context, "igym_rest_end")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle("IGYM — ابدأ المجموعة")
                .setContentText("وقت الراحة انتهى")
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MAX)
                .setCategory(androidx.core.app.NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 400, 150, 400))
                .build()
            nm.notify(8001, notif)
        } catch (_: Exception) {}
    }

    // ── Rest Timer ─────────────────────────────────────────────────────────
    var showRestTimer  by remember { mutableStateOf(false) }
    var restSecsLeft   by remember { mutableIntStateOf(180) }
    val restPresets    = listOf(60 to "1 د", 90 to "1:30", 120 to "2 د", 180 to "3 د", 300 to "5 د")
    var restPresetIdx  by remember { mutableIntStateOf(3) }  // default 3 min

    LaunchedEffect(showRestTimer) {
        if (showRestTimer) {
            // Post initial countdown notification in status bar
            postRestCountdownNotification(restSecsLeft, restPresets[restPresetIdx].first)
            while (restSecsLeft > 0) {
                delay(1000)
                restSecsLeft--
                // Update notification every 5 seconds (or every second in final 10)
                if (restSecsLeft % 5 == 0 || restSecsLeft <= 10) {
                    postRestCountdownNotification(restSecsLeft, restPresets[restPresetIdx].first)
                }
            }
            // Rest over — alert + clear countdown + show "go!" notification
            fireAlertSignal()
            fireRestEndNotification()
            showRestTimer = false
        } else {
            // Dismissed / skipped — cancel the ongoing countdown notification
            cancelRestCountdownNotification()
        }
    }

    // ── Sitting Reminder Timer (30 min after set logged) ──────────────────────
    var sittingTimerActive   by remember { mutableStateOf(false) }
    var sittingSecsLeft      by remember { mutableIntStateOf(1800) }   // 30 min
    var showSittingReminder  by remember { mutableStateOf(false) }

    LaunchedEffect(sittingTimerActive) {
        if (sittingTimerActive) {
            sittingSecsLeft = 1800
            showSittingReminder = false
            while (sittingSecsLeft > 0) { delay(1000); sittingSecsLeft-- }
            sittingTimerActive = false
            showSittingReminder = true
            // Fire alarm-level signal even if DND is on
            fireAlertSignal()
        }
    }
    var pendingSet   by remember { mutableIntStateOf(0) }
    var rpeValue     by remember { mutableIntStateOf(7) }
    var showRpe      by remember { mutableStateOf(false) }

    // ── PR Banner ──────────────────────────────────────────────────────────
    var prMessage    by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(prMessage) {
        if (prMessage != null) { delay(4000); prMessage = null }
    }

    // Sync sets lists on change
    LaunchedEffect(totalSets) {
        while (setWeights.size < totalSets)   setWeights.add("")
        while (setReps.size < totalSets)      setReps.add("")
        while (setCompleted.size < totalSets) setCompleted.add(false)
        while (setWeights.size > totalSets)   setWeights.removeLast()
        while (setReps.size > totalSets)      setReps.removeLast()
        while (setCompleted.size > totalSets) setCompleted.removeLast()
    }

    // ── Rest Timer Overlay ─────────────────────────────────────────────────
    if (showRestTimer) {
        AlertDialog(
            onDismissRequest = { showRestTimer = false; cancelRestCountdownNotification() },
            containerColor   = SurfaceContainerHigh,
            title = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text("وقت الراحة", color = OnSurface, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()) {
                    // Big countdown
                    val mins = restSecsLeft / 60
                    val secs = restSecsLeft % 60
                    Text(
                        "%d:%02d".format(mins, secs),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                        color = if (restSecsLeft <= 10) Error else Primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    // Preset buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        restPresets.forEachIndexed { idx, (secs, label) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (idx == restPresetIdx) Primary else SurfaceContainerHighest)
                                    .clickable { restPresetIdx = idx; restSecsLeft = secs }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(label, style = MaterialTheme.typography.labelSmall,
                                    color = if (idx == restPresetIdx) Color.Black else OnSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRestTimer = false; cancelRestCountdownNotification() }) {
                    Text("تخطي الراحة", color = OnSurfaceVariant)
                }
            }
        )
    }

    // ── RPE Dialog ─────────────────────────────────────────────────────────
    if (showRpe) {
        AlertDialog(
            onDismissRequest = { showRpe = false },
            containerColor   = SurfaceContainerHigh,
            title = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text("الجهد — Set $pendingSet", color = OnSurface)
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()) {
                    val rpeLabel = when (rpeValue) {
                        in 1..3  -> "خفيف"
                        in 4..5  -> "معقول"
                        in 6..7  -> "صعب"
                        in 8..9  -> "صعب جداً"
                        else     -> "الحد الأقصى"
                    }
                    Text(rpeValue.toString(), style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
                        color = Primary, fontWeight = FontWeight.Bold)
                    Text(rpeLabel, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Slider(
                        value = rpeValue.toFloat(), onValueChange = { rpeValue = it.toInt() },
                        valueRange = 1f..10f, steps = 8,
                        colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("1 خفيف", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        Text("10 أقصى", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.logRpe(sessionId, exercise.id, pendingSet, rpeValue)
                    showRpe = false
                    // Start rest timer after RPE
                    restSecsLeft = restPresets[restPresetIdx].first
                    showRestTimer = true
                    // Start sitting reminder (30 min)
                    sittingTimerActive = true
                }) { Text("حفظ", color = Primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRpe = false
                    restSecsLeft = restPresets[restPresetIdx].first
                    showRestTimer = true
                    // Start sitting reminder (30 min)
                    sittingTimerActive = true
                }) { Text("تخطي", color = OnSurfaceVariant) }
            }
        )
    }

    // ── Sitting Reminder Dialog ────────────────────────────────────────────
    if (showSittingReminder) {
        AlertDialog(
            onDismissRequest = { showSittingReminder = false },
            containerColor = SurfaceContainerHigh,
            title = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text("قوم اتحرك", color = OnSurface, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text(
                        "امشي خطوات أو اتمدد دقيقتين.",
                        color = OnSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showSittingReminder = false
                    sittingTimerActive = true
                }) { Text("شغّل تاني", color = Primary) }
            },
            dismissButton = {
                TextButton(onClick = { showSittingReminder = false }) {
                    Text("إلغاء", color = OnSurfaceVariant)
                }
            }
        )
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            // ── Sitting Reminder Badge ──────────────────────────────────────
            AnimatedVisibility(visible = sittingTimerActive,
                enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                val sMin = sittingSecsLeft / 60
                val sSec = sittingSecsLeft % 60
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(Color(0xFF2196F3).copy(.10f))
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("حركة: %d:%02d".format(sMin, sSec),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2196F3))
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2196F3).copy(.15f))
                            .clickable { sittingTimerActive = false; showSittingReminder = false }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("إيقاف", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2196F3))
                    }
                }
            }
            // ── PR Banner ───────────────────────────────────────────────────
            AnimatedVisibility(visible = prMessage != null,
                enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                prMessage?.let { msg ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            .background(Primary.copy(.15f))
                            .border(1.dp, Primary.copy(.4f), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.EmojiEvents, null, tint = Primary, modifier = Modifier.size(20.dp))
                        Column {
                            Text("رقم قياسي", style = MaterialTheme.typography.labelSmall,
                                color = Primary, fontWeight = FontWeight.Bold)
                            Text(msg, style = MaterialTheme.typography.bodySmall, color = Primary.copy(.8f))
                        }
                    }
                }
            }

            // ── Exercise Header ──────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
                .padding(if (prMessage != null) PaddingValues(top = 12.dp) else PaddingValues(0.dp)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(exercise.name, style = MaterialTheme.typography.headlineMedium,
                            color = OnSurface, fontWeight = FontWeight.Bold)
                    }
                    Text(exercise.muscleGroup, style = MaterialTheme.typography.bodySmall, color = Primary)
                }
                Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    null, tint = OnSurfaceVariant, modifier = Modifier.size(24.dp))
            }

            // ── Chips ────────────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ExerciseChip("${exercise.sets} Sets")
                ExerciseChip(exercise.repsTarget + " Reps")
                ExerciseChip("RIR ${exercise.rir}")
                ExerciseChip(exercise.restTime)
            }

            // ── Expandable details ───────────────────────────────────────────
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = Color.White.copy(.08f))
                    Spacer(Modifier.height(12.dp))

                    // Cues
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Text(exercise.coachingCue1, style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                        if (showCues) {
                            Spacer(Modifier.height(8.dp))
                            Text(exercise.coachingCue2, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(color = Color.White.copy(.06f))
                            Spacer(Modifier.height(8.dp))
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                Text("البدائل:", style = MaterialTheme.typography.labelSmall, color = Primary)
                                Spacer(Modifier.height(4.dp))
                                Text(exercise.alternative, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { showCues = !showCues },
                        contentPadding = PaddingValues(0.dp)) {
                        Text(if (showCues) "إخفاء التفاصيل ↑" else "إرشادات + بدائل ↓",
                            style = MaterialTheme.typography.labelSmall, color = Primary)
                    }
                    HorizontalDivider(color = Color.White.copy(.08f))
                    Spacer(Modifier.height(12.dp))
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Sets Header ──────────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("SET", style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                Text("PREV", style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("KG", style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant, modifier = Modifier.width(64.dp), textAlign = TextAlign.Center)
                Text("REPS", style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant, modifier = Modifier.width(64.dp), textAlign = TextAlign.Center)
                Spacer(Modifier.width(40.dp))
            }
            Spacer(Modifier.height(8.dp))

            // ── Set Rows ─────────────────────────────────────────────────────
            for (i in 0 until totalSets) {
                SetRow(
                    setNumber = i + 1,
                    prevLog   = lastLogs.getOrNull(i),
                    weight    = setWeights.getOrElse(i) { "" },
                    reps      = setReps.getOrElse(i) { "" },
                    completed = setCompleted.getOrElse(i) { false },
                    onWeightChange = { if (i < setWeights.size) setWeights[i] = it },
                    onRepsChange   = { if (i < setReps.size)    setReps[i]    = it },
                    onComplete = { done ->
                        if (i < setCompleted.size) setCompleted[i] = done
                        if (done) {
                            val w = setWeights.getOrElse(i) { "" }.toFloatOrNull() ?: 0f
                            val r = setReps.getOrElse(i) { "" }.toIntOrNull() ?: 0
                            viewModel.logSet(exercise.id, sessionId, i + 1, w, r)
                            // PR check
                            if (w > 0f && r > 0) {
                                scope.launch {
                                    val isNewPR = viewModel.checkAndSavePR(
                                        exercise.id, exercise.name, exercise.muscleGroup, w, r)
                                    if (isNewPR) {
                                        prMessage = "${exercise.name} — ${w}kg × ${r} reps"
                                    }
                                }
                            }
                            // Show RPE then rest timer
                            pendingSet = i + 1
                            showRpe = true
                        }
                    }
                )
                if (i < totalSets - 1) Spacer(Modifier.height(8.dp))
            }

            // ── Add / Remove Set ─────────────────────────────────────────────
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val canRemove = totalSets > 1
                Row(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                        .background(if (canRemove) Error.copy(.08f) else SurfaceContainerHigh.copy(.4f))
                        .then(if (canRemove) Modifier.clickable { totalSets-- } else Modifier)
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Remove, null,
                        tint = if (canRemove) Error.copy(.75f) else OnSurfaceVariant.copy(.25f),
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("REMOVE SET", style = MaterialTheme.typography.labelSmall,
                        color = if (canRemove) Error.copy(.75f) else OnSurfaceVariant.copy(.25f))
                }
                Row(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                        .background(Primary.copy(.08f)).clickable { totalSets++ }.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, null, tint = Primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("ADD SET", style = MaterialTheme.typography.labelSmall, color = Primary)
                }
            }
        }
    }
}

// ─── Set Row ──────────────────────────────────────────────────────────────────

@Composable
fun SetRow(setNumber: Int, prevLog: SetLog?, weight: String, reps: String,
           completed: Boolean, onWeightChange: (String) -> Unit,
           onRepsChange: (String) -> Unit, onComplete: (Boolean) -> Unit) {
    val prevText = if (prevLog != null && (prevLog.weight > 0f || prevLog.repsCompleted > 0)) {
        "${prevLog.weight.let { if (it == it.toLong().toFloat()) it.toInt().toString() else it.toString() }}×${prevLog.repsCompleted}"
    } else "—"
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .background(if (completed) Primary.copy(.10f) else SurfaceContainerHigh)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(setNumber.toString(), color = if (completed) Primary else OnSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold,
            modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
        Text(prevText, color = OnSurfaceVariant.copy(.7f),
            style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        WorkoutTextField(value = weight, onChange = onWeightChange, hint = "kg",
            modifier = Modifier.width(64.dp), enabled = !completed)
        WorkoutTextField(value = reps, onChange = onRepsChange, hint = "reps",
            modifier = Modifier.width(64.dp), enabled = !completed)
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                .border(2.dp, if (completed) Primary else Outline, RoundedCornerShape(10.dp))
                .background(if (completed) Primary else Color.Transparent)
                .clickable { onComplete(!completed) },
            contentAlignment = Alignment.Center
        ) {
            if (completed) Icon(Icons.Default.Check, "Done", tint = Color.Black, modifier = Modifier.size(18.dp))
        }
    }
}

// ─── Supporting Composables ───────────────────────────────────────────────────

@Composable
private fun ExerciseChip(text: String) {
    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(SurfaceContainerHigh).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
    }
}

@Composable
fun WorkoutTextField(value: String, onChange: (String) -> Unit, hint: String,
                     modifier: Modifier = Modifier, enabled: Boolean = true) {
    BasicTextField(
        value = value, onValueChange = { if (enabled) onChange(it) }, enabled = enabled,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = if (enabled) OnSurface else OnSurfaceVariant, textAlign = TextAlign.Center),
        cursorBrush = SolidColor(Primary),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.height(40.dp).clip(RoundedCornerShape(8.dp))
            .background(if (enabled) Color.White.copy(.05f) else Color.Transparent)
            .border(1.dp, if (enabled) Outline else Outline.copy(.3f), RoundedCornerShape(8.dp)),
        decorationBox = { inner ->
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (value.isEmpty()) Text(hint, color = OnSurfaceVariant.copy(.4f), style = MaterialTheme.typography.bodyMedium)
                inner()
            }
        }
    )
}
