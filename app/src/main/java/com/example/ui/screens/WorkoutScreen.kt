package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.WorkoutPlan
import com.example.ui.KineticViewModel
import com.example.ui.theme.*

@Composable
fun WorkoutScreen(
    viewModel:     KineticViewModel = viewModel(),
    onPlanClicked: (String) -> Unit
) {
    val plans            by viewModel.plans.collectAsState()
    val currentPlanIndex by viewModel.currentPlanIndex.collectAsState()
    val exercises        by viewModel.exercises.collectAsState()
    val workoutsThisWeek by viewModel.workoutsThisWeek.collectAsState()

    val countsByPlan = remember(exercises) {
        exercises.groupBy { it.planId }.mapValues { it.value.size }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C0C))
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(28.dp))
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Column {
                    Text(
                        "التمارين",
                        style      = MaterialTheme.typography.headlineMedium,
                        color      = OnSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "اختر البرنامج",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }
        }

        // Week progress summary chip
        item {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                WeekProgressBanner(workoutsThisWeek)
            }
        }

        items(plans, key = { it.id }) { plan ->
            val planIndex = plans.indexOf(plan)
            val isNext = planIndex == (currentPlanIndex % plans.size.coerceAtLeast(1))
            val exCount = countsByPlan[plan.id] ?: 0
            PlanCategoryCard(
                plan    = plan,
                isNext  = isNext,
                exCount = exCount,
                onClick = { onPlanClicked(plan.id) }
            )
        }

        item {
            CardioCategoryCard(onClick = { onPlanClicked("cardio") })
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun WeekProgressBanner(workoutsThisWeek: Int) {
    val done = workoutsThisWeek.coerceIn(0, 4)
    val allDone = done >= 4

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (allDone) Color(0xFF0A2A1A)
                else Color(0xFF161818)
            )
            .border(
                1.dp,
                if (allDone) Color(0xFF34D399).copy(0.35f) else Color.White.copy(0.07f),
                RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (allDone) "الأسبوع مكتمل" else "الأسبوع الحالي",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (allDone) Color(0xFF4ADE80) else OnSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (allDone) "4/4 تمارين" else "${4 - done} متبقي",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(4) { i ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (i < done) {
                                    if (allDone) Color(0xFF34D399) else Primary
                                } else Color.White.copy(0.1f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun PlanCategoryCard(
    plan:    WorkoutPlan,
    isNext:  Boolean,
    exCount: Int,
    onClick: () -> Unit
) {
    val cardBg by animateColorAsState(
        targetValue   = if (isNext) Color(0xFF1A1C0C) else Color(0xFF161818),
        animationSpec = tween(300),
        label         = "cardBg"
    )
    val borderColor by animateColorAsState(
        targetValue   = if (isNext) Primary.copy(0.25f) else Color.White.copy(0.07f),
        animationSpec = tween(300),
        label         = "borderColor"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Plan icon box — clean, no emoji
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isNext) Primary.copy(0.12f) else Color(0xFF1E1E1E)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = if (
                                plan.name.contains("Upper", ignoreCase = true) ||
                                plan.name.contains("أعلى",  ignoreCase = true)
                            ) Icons.Default.FitnessCenter else Icons.Default.DirectionsRun,
                            contentDescription = null,
                            tint               = if (isNext) Primary else OnSurfaceVariant,
                            modifier           = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                plan.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = OnSurface,
                                fontWeight = FontWeight.Bold
                            )
                            if (isNext) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Primary.copy(0.18f))
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        "التالي",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "$exCount تمرين",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                }

                // Chevron
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isNext) Primary.copy(0.15f) else SurfaceContainerHigh
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = null,
                        tint = if (isNext) Primary else OnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CardioCategoryCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF161818))
            .border(1.dp, Color.White.copy(0.07f), RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E1E1E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.DirectionsRun,
                            contentDescription = null,
                            tint     = OnSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            "كارديو",
                            style = MaterialTheme.typography.headlineMedium,
                            color = OnSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Zone 2 & HIIT & تايمر",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ChevronLeft, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ── extension ─────────────────────────────────────────────────────────────────

