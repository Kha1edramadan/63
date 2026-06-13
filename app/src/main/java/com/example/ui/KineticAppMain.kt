package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.ui.screens.*
import com.example.ui.theme.*

sealed class Screen(val route: String, val titleAr: String, val icon: ImageVector) {
    object Splash    : Screen("splash",    "Splash",   Icons.Filled.Dashboard)
    object Home      : Screen("home",      "الرئيسية", Icons.Filled.Home)
    object Workout   : Screen("workout",   "تمارين",   Icons.Filled.FitnessCenter)
    object Plan      : Screen("plan",      "الخطة",    Icons.Filled.EventNote)
    object Progress  : Screen("progress",  "التقدم",   Icons.Filled.Insights)
    object Nutrition : Screen("nutrition", "تغذية",    Icons.Filled.Restaurant)
}

private val bottomNavItems = listOf(
    Screen.Home, Screen.Workout, Screen.Plan, Screen.Progress, Screen.Nutrition
)

@Composable
fun KineticAppMain() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDest      = navBackStackEntry?.destination
    val hideBottomBar    = currentDest?.route == Screen.Splash.route

    Scaffold(
        containerColor = Surface,
        bottomBar = {
            if (!hideBottomBar) {
                KineticNavBar(
                    items       = bottomNavItems,
                    currentDest = currentDest,
                    onNavigate  = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Splash.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            }
            composable(Screen.Home.route) {
                HomeScreen(onStartWorkout = { planId -> navController.navigate("workout_session/$planId") })
            }
            composable(Screen.Workout.route) {
                WorkoutScreen(onPlanClicked = { planId ->
                    if (planId == "cardio") navController.navigate("cardio_screen")
                    else                    navController.navigate("workout_session/$planId")
                })
            }
            composable("cardio_screen") { CardioScreen() }
            composable("workout_session/{planId}") { back ->
                val planId = back.arguments?.getString("planId") ?: return@composable
                WorkoutSessionScreen(
                    planId   = planId,
                    onFinish = { sessionId, planName ->
                        navController.navigate("workout_complete/$sessionId/${planName.replace("/", "-")}") {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }
            composable("workout_complete/{sessionId}/{planName}") { back ->
                val sessionId = back.arguments?.getString("sessionId")?.toLongOrNull() ?: 0L
                val planName  = back.arguments?.getString("planName") ?: ""
                WorkoutCompletionScreen(
                    sessionId = sessionId,
                    planName  = planName,
                    onDone    = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } }
                )
            }
            composable(Screen.Plan.route)      { PlanScreen() }
            composable(Screen.Progress.route)  {
                ProgressScreen(
                    onNavigateToMeasurements = { navController.navigate("body_measurements") }
                )
            }
            composable("body_measurements")    { BodyMeasurementsScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.Nutrition.route) {
                NutritionScreen(
                    onNutrientsClick   = { navController.navigate("micronutrients") },
                    onCalculatorClick  = { navController.navigate("calorie_calculator") },
                    onSupplementsClick = { navController.navigate("supplements") }
                )
            }
            composable("micronutrients")       { NutrientSourcesScreen { navController.popBackStack() } }
            composable("calorie_calculator")   { CalorieCalculatorScreen(onBack = { navController.popBackStack() }) }
            composable("supplements")          { SupplementsScreen { navController.popBackStack() } }
        }
    }
}

// ── Nav Bar ────────────────────────────────────────────────────────────────────
@Composable
private fun KineticNavBar(
    items:       List<Screen>,
    currentDest: androidx.navigation.NavDestination?,
    onNavigate:  (Screen) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0E0E0E))
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            items.forEach { screen ->
                val selected = currentDest?.hierarchy?.any { it.route == screen.route } == true
                NavItem(
                    screen   = screen,
                    selected = selected,
                    onClick  = { onNavigate(screen) }
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    screen:   Screen,
    selected: Boolean,
    onClick:  () -> Unit
) {
    val iconColor by animateColorAsState(
        targetValue   = if (selected) Primary else Color(0xFF4A4A4A),
        animationSpec = tween(200),
        label         = "iconColor"
    )

    Column(
        modifier = Modifier
            .clickable(
                indication        = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick           = onClick
            )
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector        = screen.icon,
            contentDescription = screen.titleAr,
            tint               = iconColor,
            modifier           = Modifier.size(22.dp)
        )
        if (selected) {
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(Primary)
            )
        }
    }
}
