package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.WorkoutSession
import com.example.ui.KineticViewModel
import com.example.ui.SittingReminderWorker
import com.example.ui.QuotesWorker
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    onStartWorkout: (String) -> Unit = {},
    viewModel: KineticViewModel = viewModel()
) {
    val plans            by viewModel.plans.collectAsState()
    val currentPlanIndex by viewModel.currentPlanIndex.collectAsState()
    val activePlan       = plans.getOrNull(currentPlanIndex % plans.size.coerceAtLeast(1))

    // Combined: 4 flows → 1 collectAsState() → fewer recompositions
    val nutritionTargets by viewModel.nutritionTargets.collectAsState()
    val targetCals  = nutritionTargets.cals
    val targetPro   = nutritionTargets.pro
    val targetCarbs = nutritionTargets.carbs
    val targetFats  = nutritionTargets.fats

    val todayLogs     by viewModel.todayNutritionLogs.collectAsState()
    val consumedCals  = todayLogs.sumOf { it.calories }
    val consumedPro   = todayLogs.sumOf { it.protein.toDouble() }.toInt()
    val consumedCarbs = todayLogs.sumOf { it.carbs.toDouble() }.toInt()
    val consumedFats  = todayLogs.sumOf { it.fat.toDouble() }.toInt()

    val workoutsThisWeek by viewModel.workoutsThisWeek.collectAsState()
    val elapsedWeeks     by viewModel.planElapsedWeeks.collectAsState()
    val planGoal         by viewModel.planGoal.collectAsState()
    val totalWorkouts    by viewModel.totalWorkouts.collectAsState()

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C0C))
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(28.dp))
            HomeHeader(elapsedWeeks, planGoal, context)
        }
        item { SittingReminderToggleCard(context) }
        item { QuotesToggleCard(context) }
        item { CurrentPhaseCard(planGoal, elapsedWeeks, workoutsThisWeek) }
        item {
            TodaysWorkoutCard(
                planName      = activePlan?.name ?: "—",
                exerciseCount = 7,
                onStart       = { activePlan?.let { onStartWorkout(it.id) } }
            )
        }
        item { EnergyHomeCard(consumed = consumedCals, target = targetCals) }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MacroHomeCard("بروتين", consumedPro,   targetPro,   "g", MacroProtein, Modifier.weight(1f))
                MacroHomeCard("كارب",   consumedCarbs, targetCarbs, "g", MacroCarbs,   Modifier.weight(1f))
                MacroHomeCard("دهون",   consumedFats,  targetFats,  "g", MacroFats,    Modifier.weight(1f))
            }
        }
        item { WeeklyCalendarCard(recentSessions = viewModel.recentSessions.collectAsState().value) }
        item { SubscriptionReminderCard(viewModel) }
        item { AppGuideCard() }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// ── Header ─────────────────────────────────────────────────────────────────────
@Composable
private fun HomeHeader(week: Int, goal: String, context: Context) {
    Column {
        Text(
            text  = "الأسبوع $week",
            style = MaterialTheme.typography.headlineMedium,
            color = OnSurface,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text  = goal,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant
        )
    }
}

// ── Sitting Reminder Toggle ────────────────────────────────────────────────────
@Composable
private fun SittingReminderToggleCard(context: Context) {
    var isActive by remember {
        mutableStateOf(SittingReminderWorker.isScheduled(context))
    }
    val bgColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFF0A2A1A) else Color(0xFF161818),
        animationSpec = tween(400), label = "bg"
    )
    val accentColor = if (isActive) Color(0xFF34D399) else OnSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(
                1.dp,
                if (isActive) Color(0xFF34D399).copy(0.35f) else Color.White.copy(0.06f),
                RoundedCornerShape(16.dp)
            )
            .clickable {
                isActive = !isActive
                if (isActive) SittingReminderWorker.schedule(context)
                else          SittingReminderWorker.cancel(context)
            }
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(if (isActive) Icons.Default.DirectionsWalk else Icons.Default.Chair, contentDescription = null, tint = if (isActive) Primary else OnSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text(
                            text = if (isActive) "تذكير الحركة شغّال" else "تذكير الحركة",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isActive) "كل 30 دقيقة" else "إشعار كل 30 دقيقة",
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor.copy(0.7f)
                        )
                    }
                }
                // Toggle switch
                Box(
                    modifier = Modifier
                        .width(46.dp)
                        .height(26.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(if (isActive) Color(0xFF34D399).copy(0.9f) else Color(0xFF2A2C2C)),
                    contentAlignment = if (isActive) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .padding(3.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
        }
    }
}

// ── Quotes Toggle ──────────────────────────────────────────────────────────────
@Composable
private fun QuotesToggleCard(context: Context) {
    var isActive by remember {
        mutableStateOf(QuotesWorker.isScheduled(context))
    }
    val bgColor by animateColorAsState(
        targetValue   = if (isActive) Color(0xFF0A1A2A) else Color(0xFF141414),
        animationSpec = tween(400), label = "qbg"
    )
    val accentColor = if (isActive) Primary else OnSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(
                1.dp,
                if (isActive) Primary.copy(0.30f) else Color(0xFF202020),
                RoundedCornerShape(16.dp)
            )
            .clickable {
                isActive = !isActive
                if (isActive) QuotesWorker.schedule(context)
                else          QuotesWorker.cancel(context)
            }
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text       = "اقتباسات يومية",
                        style      = MaterialTheme.typography.bodyMedium,
                        color      = OnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text  = if (isActive) "كل ساعتين" else "إشعار كل ساعتين",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor.copy(0.7f)
                    )
                }
                Box(
                    modifier = Modifier
                        .width(46.dp)
                        .height(26.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(if (isActive) Primary.copy(0.9f) else Color(0xFF2A2A2A)),
                    contentAlignment = if (isActive) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .padding(3.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
        }
    }
}

// ── Current Phase ─────────────────────────────────────────────────────────────
@Composable
private fun CurrentPhaseCard(goal: String, elapsedWeeks: Int, weeklyWorkouts: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF141414))
            .border(1.dp, Color(0xFF202020), RoundedCornerShape(16.dp))
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text  = goal,
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = "4 تمارين = أسبوع",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(4) { i ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (i < weeklyWorkouts) Primary else Color(0xFF2A2A2A)
                                    )
                            )
                        }
                        Text(
                            text  = "  $weeklyWorkouts/4",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text       = "$elapsedWeeks",
                        style      = MaterialTheme.typography.displaySmall,
                        color      = Primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text  = "أسبوع",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── Today's Workout ───────────────────────────────────────────────────────────
@Composable
fun TodaysWorkoutCard(planName: String, exerciseCount: Int, onStart: () -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF161616))
            .border(1.dp, Color(0xFF242424), RoundedCornerShape(16.dp))
            .clickable { onStart() }
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text  = "تمرين اليوم",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text       = planName,
                        style      = MaterialTheme.typography.titleLarge,
                        color      = OnSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = "$exerciseCount تمارين",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint     = Color.Black,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

// ── Energy Card ───────────────────────────────────────────────────────────────
@Composable
private fun EnergyHomeCard(consumed: Int, target: Int) {
    val progress  = (consumed.toFloat() / target.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
    val remaining = target - consumed
    val animProg by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(900, easing = EaseOutCubic),
        label         = "calProg"
    )
    val overBudget = remaining < 0

    GlassCard(modifier = Modifier.fillMaxWidth(), accentGlow = true) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text  = "السعرات اليوم",
                        color = OnSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text       = consumed.toString(),
                        style      = MaterialTheme.typography.displaySmall,
                        color      = if (overBudget) Color(0xFFFF6B6B) else Primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text  = "من $target kcal",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(0.07f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animProg)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (overBudget) Color(0xFFFF6B6B) else Primary)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text  = if (!overBudget) "متبقي $remaining kcal" else "زيادة ${-remaining} kcal",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (!overBudget) Primary.copy(0.8f) else Color(0xFFFF6B6B)
                    )
                }
                // Circular progress
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(76.dp)) {
                    CircularProgressIndicator(
                        progress    = { 1f },
                        modifier    = Modifier.size(76.dp),
                        color       = Color.White.copy(0.05f),
                        strokeWidth = 6.dp
                    )
                    CircularProgressIndicator(
                        progress    = { animProg },
                        modifier    = Modifier.size(76.dp),
                        color       = if (!overBudget) Primary else Color(0xFFFF6B6B),
                        strokeWidth = 6.dp
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text  = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = OnSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ── Macro Card ────────────────────────────────────────────────────────────────
@Composable
private fun MacroHomeCard(
    label:    String,
    consumed: Int,
    target:   Int,
    unit:     String,
    color:    Color,
    modifier: Modifier = Modifier
) {
    val progress = (consumed.toFloat() / target.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
    val animProg by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(800, easing = EaseOutCubic),
        label         = "macroProg"
    )

    Column(
        modifier            = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF141414))
            .border(1.dp, Color(0xFF202020), RoundedCornerShape(14.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(52.dp)) {
            CircularProgressIndicator(
                progress    = { 1f },
                modifier    = Modifier.size(52.dp),
                color       = Color.White.copy(0.06f),
                strokeWidth = 4.dp
            )
            CircularProgressIndicator(
                progress    = { animProg },
                modifier    = Modifier.size(52.dp),
                color       = color,
                strokeWidth = 4.dp
            )
            Text(
                text       = "$consumed",
                style      = MaterialTheme.typography.labelSmall,
                color      = OnSurface,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            label,
            style      = MaterialTheme.typography.labelSmall,
            color      = OnSurfaceVariant,
            textAlign  = TextAlign.Center
        )
        Text(
            "/$target$unit",
            style     = MaterialTheme.typography.bodySmall,
            color     = Color(0xFF3A3A3A),
            textAlign = TextAlign.Center
        )
    }
}

// ── Subscription Card ─────────────────────────────────────────────────────────
@Composable
fun SubscriptionReminderCard(viewModel: KineticViewModel) {
    val daysLeft = viewModel.getSubscriptionDaysLeft()
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = "اشتراك الجيم",
                    color = OnSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text       = "$daysLeft يوم متبقي",
                    style      = MaterialTheme.typography.headlineMedium,
                    color      = if (daysLeft <= 7L) MaterialTheme.colorScheme.error else OnSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text  = "عدّل من صفحة الخطة",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (daysLeft <= 7L) MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                        else SurfaceContainerHigh
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = if (daysLeft <= 7L) Icons.Default.Warning else Icons.Default.DateRange,
                    contentDescription = null,
                    tint               = if (daysLeft <= 7L) MaterialTheme.colorScheme.error else Primary
                )
            }
        }
    }
}

// ── Weekly Calendar Card ───────────────────────────────────────────────────────

@Composable
fun WeeklyCalendarCard(recentSessions: List<com.example.data.WorkoutSession>) {
    val cal       = java.util.Calendar.getInstance()
    val today     = cal.get(java.util.Calendar.DAY_OF_WEEK)
    val dayLabels = listOf("الأحد","الاثنين","الثلاثاء","الأربعاء","الخميس","الجمعة","السبت")

    // Build set of day-of-week indices that had sessions this week
    val weekStart = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.DAY_OF_WEEK, firstDayOfWeek)
        set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis
    val workedDays = recentSessions
        .filter { it.completedAt >= weekStart }
        .map {
            val c = java.util.Calendar.getInstance()
            c.timeInMillis = it.completedAt
            c.get(java.util.Calendar.DAY_OF_WEEK)
        }.toSet()

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.DateRange, null, tint = Primary, modifier = Modifier.size(14.dp))
                    Text("هذا الأسبوع", style = MaterialTheme.typography.labelSmall, color = Primary)
                    Spacer(Modifier.weight(1f))
                    Text("${workedDays.size}/7 أيام هذا الأسبوع", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                for (dayIdx in 1..7) {
                    val isToday   = dayIdx == today
                    val didTrain  = dayIdx in workedDays
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)) {
                        Text(dayLabels[dayIdx - 1].take(3),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) Primary else OnSurfaceVariant.copy(.6f),
                            maxLines = 1)
                        Spacer(Modifier.height(5.dp))
                        Box(
                            modifier = Modifier.size(30.dp).clip(androidx.compose.foundation.shape.CircleShape)
                                .background(when { didTrain -> Primary; isToday -> Primary.copy(.2f); else -> SurfaceContainerHigh })
                                .then(if (isToday && !didTrain) Modifier.border(1.5.dp, Primary, androidx.compose.foundation.shape.CircleShape) else Modifier),
                            contentAlignment = Alignment.Center
                        ) {
                            if (didTrain) Icon(Icons.Default.Check, null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

// ── App Guide Card ────────────────────────────────────────────────────────────

@Composable
fun AppGuideCard() {
    var expanded by remember { mutableStateOf(false) }

    val sections = listOf(
        Triple("الرئيسية",  "الرئيسية",  "السعرات والماكرو اليومي، تمرين اليوم، تقدم الأسبوع."),
        Triple("التمارين", "التمارين", "اختر التمرين وابدأ. اضغط على اسم التمرين للنصائح. عدّل الوزن والعدات في كل مجموعة."),
        Triple("الخطة",    "الخطة",    "اضبط هدفك ومدة الخطة وتاريخ اشتراك الجيم."),
        Triple("التقدم",   "التقدم",   "سجّل وزن جسمك. شوف منحنى قوتك في أي تمرين. تتبع مقاسات جسمك."),
        Triple("التغذية",  "التغذية",  "ابحث عن أي أكل وسجّله. اضبط سعراتك من الحاسبة. احفظ وجباتك المفضلة."),
        Triple("المكملات", "المكملات", "هل يستاهل؟ الجرعة الصح. التوقيت المناسب."),
        Triple("الكارديو", "الكارديو", "اختار النوع وابدأ التايمر.")
    )

    GlassCard(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                                .background(Primary.copy(0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                        }
                        Column {
                            Text("دليل التطبيق السريع",
                                style = MaterialTheme.typography.bodyLarge,
                                color = OnSurface,
                                fontWeight = FontWeight.Bold)
                            Text("اضغط لمعرفة كل قسم",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant)
                        }
                    }
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp)
                    )
                }

                if (expanded) {
                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(0.07f))
                    Spacer(Modifier.height(12.dp))
                    sections.forEachIndexed { i, (emoji, title, desc) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceContainerHigh),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, style = MaterialTheme.typography.bodyMedium)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(title,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Primary,
                                    fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(2.dp))
                                Text(desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant)
                            }
                        }
                        if (i < sections.lastIndex) {
                            HorizontalDivider(
                                color = Color.White.copy(0.04f),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
