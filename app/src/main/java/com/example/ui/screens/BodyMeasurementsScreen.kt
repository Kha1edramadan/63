package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.BodyMeasurement
import com.example.ui.KineticViewModel
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BodyMeasurementsScreen(
    onBack: () -> Unit = {},
    viewModel: KineticViewModel = viewModel()
) {
    val measurements by viewModel.recentBodyMeasurements.collectAsState()

    var chest by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var hips  by remember { mutableStateOf("") }
    var arm   by remember { mutableStateOf("") }
    var thigh by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize().background(Color(0xFF0C0C0C)).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {

        item {
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, null, tint = OnSurface)
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("قياسات الجسم", style = MaterialTheme.typography.headlineMedium,
                        color = OnSurface, fontWeight = FontWeight.Bold)
                    Text("تتبع تغيرات جسمك بالأرقام", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                }
            }
        }

        // ── Input Form ─────────────────────────────────────────────────────
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Add, null, tint = Primary, modifier = Modifier.size(14.dp))
                            Text("تسجيل قياسات جديدة", style = MaterialTheme.typography.labelSmall, color = Primary)
                        }
                        Spacer(Modifier.height(14.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MeasurementField("صدر", chest, { chest = it; saved = false }, Modifier.weight(1f))
                            MeasurementField("خصر", waist, { waist = it; saved = false }, Modifier.weight(1f))
                            MeasurementField("أرداف", hips,  { hips  = it; saved = false }, Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MeasurementField("ذراع", arm, { arm = it; saved = false }, Modifier.weight(1f))
                            MeasurementField("فخذ", thigh, { thigh = it; saved = false }, Modifier.weight(1f))
                            Spacer(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = {
                                viewModel.saveBodyMeasurement(
                                    chest.toFloatOrNull() ?: 0f, waist.toFloatOrNull() ?: 0f,
                                    hips.toFloatOrNull()  ?: 0f, arm.toFloatOrNull()   ?: 0f,
                                    thigh.toFloatOrNull() ?: 0f
                                )
                                chest = ""; waist = ""; hips = ""; arm = ""; thigh = ""; saved = true
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("حفظ القياسات", fontWeight = FontWeight.Bold)
                        }
                        if (saved) {
                            Spacer(Modifier.height(8.dp))
                            Text("تم الحفظ", style = MaterialTheme.typography.labelSmall, color = Primary)
                        }
                    }
                }
            }
        }

        // ── History ────────────────────────────────────────────────────────
        if (measurements.isNotEmpty()) {
            item {
                Text("السجل", style = MaterialTheme.typography.headlineMedium,
                    color = OnSurface, fontWeight = FontWeight.Bold)
            }

            val prev = measurements.drop(1)
            items(measurements, key = { it.id }) { m ->
                val older = prev.firstOrNull { it.loggedAt < m.loggedAt }
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Column {
                            Text(SimpleDateFormat("d MMM yyyy", Locale("ar")).format(Date(m.loggedAt)),
                                style = MaterialTheme.typography.labelSmall, color = Primary)
                            Spacer(Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MeasBadge("صدر",  m.chestCm, older?.chestCm, Modifier.weight(1f))
                                MeasBadge("خصر",  m.waistCm, older?.waistCm, Modifier.weight(1f))
                                MeasBadge("أرداف", m.hipsCm,  older?.hipsCm,  Modifier.weight(1f))
                                MeasBadge("ذراع", m.armCm,   older?.armCm,   Modifier.weight(1f))
                                MeasBadge("فخذ",  m.thighCm, older?.thighCm, Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(40.dp)) }
    }
}

@Composable
private fun MeasurementField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        modifier = modifier, label = { Text("$label (سم)") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary, unfocusedBorderColor = Outline,
            focusedTextColor = OnSurface, unfocusedTextColor = OnSurface, cursorColor = Primary,
            focusedLabelColor = Primary, unfocusedLabelColor = OnSurfaceVariant
        )
    )
}

@Composable
private fun MeasBadge(label: String, value: Float, prev: Float?, modifier: Modifier) {
    val diff = if (prev != null && prev > 0f) value - prev else null
    Column(modifier = modifier.clip(RoundedCornerShape(8.dp)).background(SurfaceContainerHigh).padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
        if (value > 0f) {
            Text("${value.toInt()}", style = MaterialTheme.typography.bodyLarge,
                color = OnSurface, fontWeight = FontWeight.Bold)
            if (diff != null && diff != 0f) {
                Text("${if (diff > 0) "+" else ""}${"%.1f".format(diff)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (diff < 0) Color(0xFF6BCB77) else Color(0xFFFF6B6B))
            }
        } else {
            Text("—", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
        }
    }
}
