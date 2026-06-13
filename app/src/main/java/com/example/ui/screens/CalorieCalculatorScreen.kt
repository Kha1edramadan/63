package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
fun CalorieCalculatorScreen(
    onBack: () -> Unit = {},
    viewModel: KineticViewModel = viewModel()
) {
    var weight   by remember { mutableStateOf("") }
    var height   by remember { mutableStateOf("") }
    var age      by remember { mutableStateOf("") }
    var gender   by remember { mutableStateOf("M") }
    var activity by remember { mutableStateOf("MOD") }
    var goal     by remember { mutableStateOf("BULK") }

    var resultCals    by remember { mutableIntStateOf(0) }
    var resultProtein by remember { mutableIntStateOf(0) }
    var resultCarbs   by remember { mutableIntStateOf(0) }
    var resultFats    by remember { mutableIntStateOf(0) }
    var resultBmr     by remember { mutableIntStateOf(0) }
    var resultTdee    by remember { mutableIntStateOf(0) }
    var applied       by remember { mutableStateOf(false) }

    fun calculate() {
        val w = weight.toDoubleOrNull() ?: return
        val h = height.toDoubleOrNull() ?: return
        val a = age.toIntOrNull()       ?: return
        if (w <= 0 || h <= 0 || a <= 0) return

        // Mifflin-St Jeor BMR equation
        val bmr = 10.0 * w + 6.25 * h - 5.0 * a + (if (gender == "M") 5.0 else -161.0)

        val activityMult = when (activity) {
            "SEDENTARY" -> 1.2
            "LOW"       -> 1.375
            "MOD"       -> 1.55
            "HIGH"      -> 1.725
            "VERY_HIGH" -> 1.9
            else        -> 1.55
        }
        val tdee = bmr * activityMult

        val targetCals = when (goal) {
            "CUT"      -> tdee - 400.0
            "BULK"     -> tdee + 300.0
            else       -> tdee
        }

        // ISSN Position Stand: 1.6–2.2 g/kg for athletes; use 2.0 g/kg
        val protein = (w * 2.0).toInt()
        // Min fat: 0.8 g/kg for hormonal health
        val fats    = (w * 0.8).toInt()
        val carbCals = targetCals - (protein * 4.0) - (fats * 9.0)
        val carbs    = (carbCals / 4.0).coerceAtLeast(50.0).toInt()

        resultBmr     = bmr.toInt()
        resultTdee    = tdee.toInt()
        resultCals    = targetCals.toInt()
        resultProtein = protein
        resultFats    = fats
        resultCarbs   = carbs
        applied = false
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C0C))
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(28.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = OnSurface)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("حاسبة السعرات", style = MaterialTheme.typography.headlineMedium, color = OnSurface, fontWeight = FontWeight.Bold)
                    Text("Mifflin-St Jeor • ISSN Guidelines", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                }
            }
        }

        // Body data
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("بيانات الجسم", style = MaterialTheme.typography.labelSmall, color = Primary)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CalcInput("الوزن (كجم)", weight, { weight = it }, Modifier.weight(1f))
                            CalcInput("الطول (سم)",  height, { height = it }, Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CalcInput("العمر (سنة)", age, { age = it }, Modifier.weight(1f))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("الجنس", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SelectChip("ذكر",  selected = gender == "M", modifier = Modifier.weight(1f)) { gender = "M" }
                                    SelectChip("أنثى", selected = gender == "F", modifier = Modifier.weight(1f)) { gender = "F" }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Activity level
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("مستوى النشاط", style = MaterialTheme.typography.labelSmall, color = Primary)
                        val levels = listOf(
                            "SEDENTARY" to "قليل الحركة (مكتب أو جلوس)",
                            "LOW"       to "خفيف (1–3 أيام/أسبوع)",
                            "MOD"       to "متوسط (3–5 أيام/أسبوع)",
                            "HIGH"      to "عالي (6–7 أيام/أسبوع)",
                            "VERY_HIGH" to "شديد جداً (رياضي محترف)"
                        )
                        levels.forEach { (key, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (activity == key) Primary.copy(alpha = 0.12f) else Color.Transparent)
                                    .border(
                                        width  = 1.dp,
                                        color  = if (activity == key) Primary.copy(alpha = 0.5f) else Outline.copy(alpha = 0.3f),
                                        shape  = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { activity = key }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text(label, style = MaterialTheme.typography.bodyMedium, color = if (activity == key) Primary else OnSurface)
                                if (activity == key) Icon(Icons.Default.Check, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Goal
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("الهدف", style = MaterialTheme.typography.labelSmall, color = Primary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SelectChip("تنشيف\nCUT",      goal == "CUT",      Modifier.weight(1f)) { goal = "CUT" }
                            SelectChip("ثبات\nMAINTAIN", goal == "MAINTAIN", Modifier.weight(1f)) { goal = "MAINTAIN" }
                            SelectChip("تضخيم\nBULK",    goal == "BULK",     Modifier.weight(1f)) { goal = "BULK" }
                        }
                        Text(
                            when (goal) {
                                "CUT"  -> "عجز 400 kcal ← خسارة ~0.4 كجم/أسبوع"
                                "BULK" -> "فائض 300 kcal ← تضخيم نظيف"
                                else   -> "السعرات = TDEE بالكامل"
                            },
                            style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant
                        )
                    }
                }
            }
        }

        // Calculate button
        item {
            Button(
                onClick  = { calculate() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.Black),
                shape    = RoundedCornerShape(14.dp),
                enabled  = weight.isNotBlank() && height.isNotBlank() && age.isNotBlank()
            ) {
                Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("احسب الآن  •  CALCULATE", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }

        // Results
        if (resultCals > 0) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("النتيجة", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "%,d kcal".format(resultCals),
                                style = MaterialTheme.typography.displayLarge,
                                color = Primary, fontWeight = FontWeight.Bold
                            )
                            Text("BMR: $resultBmr | TDEE: $resultTdee", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                            Spacer(modifier = Modifier.height(20.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                MacroResultItem("بروتين", "${resultProtein}g", "2.0 جم/كجم")
                                MacroResultItem("كارب",   "${resultCarbs}g",   "متبقي")
                                MacroResultItem("دهون",   "${resultFats}g",    "0.8 جم/كجم")
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceContainerHigh).padding(12.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Science, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                                    Text(
                                        "البروتين 2.0 جم/كجم وفق ISSN Position Stand. الدهون 0.8 جم/كجم للحفاظ على الهرمونات والصحة.",
                                        style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    viewModel.updateNutritionTargets(
                                        cals   = resultCals,
                                        pro    = resultProtein,
                                        carbs  = resultCarbs,
                                        fats   = resultFats,
                                        goal   = goal,
                                        weight = weight.toFloatOrNull() ?: 80f
                                    )
                                    applied = true
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (applied) SurfaceContainerHigh else Primary,
                                    contentColor   = if (applied) Primary else Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    if (applied) Icons.Default.Check else Icons.Default.Sync,
                                    contentDescription = null, modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (applied) "✓ تم التطبيق على كل الصفحات" else "طبّق على كل التطبيق",
                                    style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}

@Composable
fun CalcInput(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = Primary,
                unfocusedBorderColor = Outline,
                focusedTextColor     = OnSurface,
                unfocusedTextColor   = OnSurface,
                cursorColor          = Primary
            )
        )
    }
}

@Composable
fun SelectChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.height(52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Primary else SurfaceContainerHigh)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            label, color = if (selected) Color.Black else OnSurface,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MacroResultItem(label: String, qty: String, note: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(qty,   style = MaterialTheme.typography.headlineMedium, color = OnSurface, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall,     color = Primary)
        Text(note,  style = MaterialTheme.typography.bodySmall,      color = OnSurfaceVariant)
    }
}
