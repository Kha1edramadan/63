package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import kotlinx.coroutines.delay

enum class KneeImpact(val label: String, val color: Long) {
    LOW("منخفض ✓", 0xFF4CAF50L),
    MEDIUM("متوسط ⚠", 0xFFFF9800L),
    HIGH("مرتفع ⚠️", 0xFFF44336L)
}

data class CardioOption(
    val name:         String,
    val nameAr:       String,
    val zone:         String,
    val zoneAr:       String,
    val duration:     String,
    val estCals:      String,
    val tip:          String,
    // ─── New detailed fields ───────────────────────────────────────
    val description:  String,
    val kneeImpact:   KneeImpact,
    val safety:       String,
    val bestFor:      String,
    val weeklyTarget: String,
    val vsOthers:     String
)

private val cardioOptions = listOf(
    CardioOption(
        "Treadmill Incline Walk", "مشي تحميل (Incline Walk)",
        "Zone 2", "منطقة 2",
        "40–45 دق", "~300 kcal",
        "ميل 6-8% • سرعة 5-6 كم/ساعة • تنفس أنفي طوال الوقت",
        description  = "المشي الصاعد هو الأذكى في Zone 2. الميل يرفع الجهد القلبي بدون الحاجة للجري، مما يعني حرق دهون أعلى مع صدمات أقل بكثير على المفاصل. يحرك العضلات الخلفية للساق وعضلات الجلوس بكفاءة عالية.",
        kneeImpact   = KneeImpact.LOW,
        safety       = "الأأمن على الإطلاق، لا قفز، لا صدمات متكررة. تجنب الميل فوق 15% لوقت طويل لتفادي ضغط أوتار العرقوب. لو ظهرك بيتأذى ارفع الميل وخفف السرعة.",
        bestFor      = "حرق الدهون الذكي • تحسين صحة القلب • ريكفري فعّال بعد يوم تمرين شاق",
        weeklyTarget = "3-5 مرات، 40-45 دقيقة، مثالي في أيام الراحة",
        vsOthers     = "أبطأ من الستايرماستر في الكلوري/دقيقة لكن يمكن الاستمرار فيه أطول بكثير. أفضل من الدراجة لتفعيل عضلات أكثر."
    ),
    CardioOption(
        "Stationary Bike", "دراجة ثابتة",
        "Zone 2", "منطقة 2",
        "35–40 دق", "~280 kcal",
        "ثبّت معدل ضربات على 130-145 bpm • مقاومة متوسطة • لا ترفع الكتف",
        description  = "الدراجة تعزل الأرجل بالكامل بدون أي تحميل على المفاصل. الجسم ثابت والحمل يقع على الأرجل فقط. ممتازة لـ Zone 2 الطويل وللاستخدام بعد تمرين أرجل ثقيل.",
        kneeImpact   = KneeImpact.LOW,
        safety       = "الأأمن للركبة من بين كل الخيارات. اضبط ارتفاع المقعد بشكل صحيح، ركبتك يجب أن تظل منحنية قليلاً في أسفل الدواسة، وإلا ستحس بألم. مناسبة لمن لديهم تاريخ إصابات في الركبة.",
        bestFor      = "ريكفري أيام أرجل • Zone 2 بدون إجهاد عضلي إضافي • من لديهم مشاكل ركبة",
        weeklyTarget = "2-4 مرات، 35-40 دقيقة",
        vsOthers     = "أقل كلورياً من المشي الصاعد لأن الجسم معتمد على الكرسي. لكن الأأمن للركبة والأراحة للظهر."
    ),
    CardioOption(
        "Elliptical", "الإليبتيكال",
        "Zone 2", "منطقة 2",
        "35–40 دق", "~250 kcal",
        "مناسب لما بعد السكوات لتقليل الحمل على الركبتين",
        description  = "الإليبتيكال يحاكي حركة الجري بالكامل لكن بدون صدمات، الحركة دائرية سلسة. يشغّل الجزء العلوي والسفلي من الجسم في نفس الوقت عبر الذراعين والأرجل.",
        kneeImpact   = KneeImpact.LOW,
        safety       = "آمن جداً على الركبة — الحركة الدائرية تلغي الضربات تماماً، الأفضل كبديل للجري لمن يعانون من التهاب الركبة أو مشاكل حوضية. تأكد من ثبات وضعيتك ولا تميل للأمام كثيراً.",
        bestFor      = "بديل جري آمن • ريكفري بعد تمرين أرجل • Zone 2 مريح",
        weeklyTarget = "2-3 مرات، 35-40 دقيقة",
        vsOthers     = "أقل كلورياً من المشي الصاعد والستايرماستر. لكن الأسلم للمفاصل وأكثر راحة للظهر من الجري."
    ),
    CardioOption(
        "Stairmaster", "ستايرماستر",
        "Zone 3 / HIIT", "شدة عالية",
        "20–25 دق", "~380 kcal",
        "مستوى 5-8 • يرفع الأيض بشكل كبير • لا تعتمد على الدعامات الجانبية أبداً",
        description  = "الستايرماستر هو من أشرس أدوات الجيم. يجمع بين تحدي القلب والأوعية وتشغيل عضلات الجلوس والفخذ والساق معاً. 20 دقيقة منه تعادل 40 دقيقة من الكارديو العادي.",
        kneeImpact   = KneeImpact.MEDIUM,
        safety       = "⚠️ لا تستخدمه مباشرة بعد تمرين أرجل ثقيل، الركبة تحتاج ريكفري. الضغط على الركبة أعلى من الدراجة والإليبتيكال. الاعتماد على الدعامات الجانبية يُفقدك 40% من الفائدة ويشوه وضعيتك.",
        bestFor      = "حرق سعرات مرتفع • بناء صلابة الخلفية • رفع الأيض الأساسي",
        weeklyTarget = "1-2 مرة فقط أسبوعياً، 20-25 دقيقة، لا تبالغ فيه",
        vsOthers     = "الأعلى كلورياً بالدقيقة بين كل الخيارات، لكن الأعلى تعباً. استخدمه بحكمة وبعيد عن أيام تمرين الأرجل."
    ),
    CardioOption(
        "Rowing Machine", "جهاز الروينج",
        "Full-Body", "كامل الجسم",
        "20–30 دق", "~300 kcal",
        "تسلسل الحركة: الأرجل أولاً ← الظهر ثانياً ← الذراعين أخيراً",
        description  = "الروينج هو الوحيد الذي يشغّل 86% من عضلات الجسم في حركة واحدة، ظهر، أرجل، صدر، بطن، ذراعين. كارديو وتقوية في نفس الوقت. مثالي جداً لمن يجلس أمام الكمبيوتر لأنه يفتح الظهر والصدر.",
        kneeImpact   = KneeImpact.LOW,
        safety       = "آمن على الركبة لكن يضغط على الظهر لو التقنية خاطئة. القاعدة الذهبية: الأرجل أولاً (80% من القوة) → ميل الظهر للخلف → الذراعين أخيراً. لا تحني ظهرك أبداً — ظهرك يبقى مستقيم طوال الوقت.",
        bestFor      = "Full-body conditioning • تقوية الظهر • تعويض القعدة الطويلة • HIIT فعّال",
        weeklyTarget = "2-3 مرات، 20-30 دقيقة، أو HIIT قصير 15 دقيقة",
        vsOthers     = "الأشمل للجسم كله. مناسب جداً للفريلانسر لأنه يعمل على عضلات الظهر التي تتأذى من الجلوس."
    )
)

@Composable
fun CardioScreen() {
    var activeTimerIndex by remember { mutableStateOf<Int?>(null) }
    var timerSeconds     by remember { mutableIntStateOf(0) }
    var timerRunning     by remember { mutableStateOf(false) }
    var expandedIndex    by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(timerRunning, activeTimerIndex) {
        if (timerRunning && activeTimerIndex != null) {
            while (timerRunning) {
                delay(1000)
                timerSeconds++
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C0C))
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(28.dp))
            Text("كارديو", style = MaterialTheme.typography.headlineMedium, color = OnSurface, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("اضغط على أي أداة لفهم الفرق والأمان والهدف المناسب", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
        }

        // ── Knee Safety Guide ────────────────────────────────────────────────
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Zone 2  الكارديو الذكي", style = MaterialTheme.typography.labelMedium, color = Primary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "60-70% من أقصى معدل ضربات القلب. يحسّن صحة القلب وحرق الدهون بكفاءة عالية. الهدف الأسبوعي: 150 دقيقة كحد أدنى.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OnSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("أقصى معدل تقريبي = 220 – عمرك", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.07f))
                        Spacer(modifier = Modifier.height(12.dp))
                        // Knee impact legend
                        Text("الأمان على الركبة:", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KneeLegendChip("منخفض ✓", Color(0xFF4CAF50))
                            KneeLegendChip("متوسط ⚠", Color(0xFFFF9800))
                            KneeLegendChip("مرتفع ⚠️", Color(0xFFF44336))
                        }
                    }
                }
            }
        }

        cardioOptions.forEachIndexed { idx, option ->
            item {
                val isActive   = activeTimerIndex == idx
                val isExpanded = expandedIndex == idx
                CardioCard(
                    option     = option,
                    isActive   = isActive,
                    isExpanded = isExpanded,
                    timerSecs  = if (isActive) timerSeconds else 0,
                    running    = isActive && timerRunning,
                    onToggle   = {
                        if (isActive) {
                            timerRunning = !timerRunning
                        } else {
                            activeTimerIndex = idx
                            timerSeconds     = 0
                            timerRunning     = true
                        }
                    },
                    onStop = {
                        timerRunning     = false
                        activeTimerIndex = null
                        timerSeconds     = 0
                    },
                    onExpandToggle = {
                        expandedIndex = if (isExpanded) null else idx
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun KneeLegendChip(label: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun CardioCard(
    option:         CardioOption,
    isActive:       Boolean,
    isExpanded:     Boolean,
    timerSecs:      Int,
    running:        Boolean,
    onToggle:       () -> Unit,
    onStop:         () -> Unit,
    onExpandToggle: () -> Unit
) {
    val kneeColor = Color(option.kneeImpact.color)

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column {
                // ── Header Row ───────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(option.nameAr, style = MaterialTheme.typography.headlineMedium, color = OnSurface, fontWeight = FontWeight.Bold)
                        Text(option.name,   style = MaterialTheme.typography.bodySmall,     color = OnSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        // Knee impact badge
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(kneeColor.copy(alpha = 0.12f))
                                .padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(kneeColor))
                            Text(
                                "تأثير على الركبة: ${option.kneeImpact.label}",
                                style = MaterialTheme.typography.labelSmall,
                                color = kneeColor
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Expand/collapse details button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isExpanded) Primary.copy(alpha = 0.15f) else SurfaceContainerHigh)
                                .clickable { onExpandToggle() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (isExpanded) Primary else OnSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        // Play/Pause timer button — clearly labelled
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (running) Primary
                                    else if (isActive) Primary.copy(alpha = 0.4f)
                                    else SurfaceContainerHigh
                                )
                                .clickable { onToggle() }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    if (running) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = if (running) Color.Black
                                           else if (isActive) Primary else OnSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = when {
                                        running  -> "إيقاف"
                                        isActive -> "استكمال"
                                        else     -> "ابدأ التايمر"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (running) Color.Black
                                            else if (isActive) Primary else OnSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CardioStat(option.zoneAr, option.zone)
                    CardioStat("المدة", option.duration)
                    CardioStat("السعرات", option.estCals)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceContainerHigh)
                        .padding(10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment     = Alignment.Top
                    ) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                        Text(option.tip, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                }

                // ── Expandable Detail Section ─────────────────────────────────
                AnimatedVisibility(
                    visible = isExpanded,
                    enter   = expandVertically(),
                    exit    = shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Description
                        DetailRow(Icons.Default.Info, "ما هو؟", option.description)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Safety
                        DetailRow(Icons.Default.Shield, "الأمان والإصابات", option.safety)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Best for
                        DetailRow(Icons.Default.Star, "الأفضل لـ", option.bestFor)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Weekly target
                        DetailRow(Icons.Default.DateRange, "الجرعة الأسبوعية", option.weeklyTarget)
                        Spacer(modifier = Modifier.height(10.dp))

                        // vs Others
                        DetailRow(Icons.Default.CompareArrows, "مقارنة بالباقي", option.vsOthers)
                    }
                }

                // ── Active Timer ──────────────────────────────────────────────
                if (isActive) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("المؤقت", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                            val m = timerSecs / 60
                            val s = timerSecs % 60
                            Text(
                                "%02d:%02d".format(m, s),
                                style = MaterialTheme.typography.headlineLarge,
                                color = Primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        TextButton(onClick = onStop) {
                            Icon(Icons.Default.Stop, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إنهاء", color = OnSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = Primary.copy(alpha = 0.7f), modifier = Modifier.size(15.dp).padding(top = 2.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Primary.copy(alpha = 0.85f), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(3.dp))
            Text(value, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
        }
    }
}

@Composable
private fun CardioStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = OnSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        Text(value, color = Primary,          style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
