package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.EgyptianFoodDatabase
import com.example.data.FoodItem

import com.example.data.NutritionLog
import com.example.ui.KineticViewModel
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Save

// ─── Nutrition Screen ─────────────────────────────────────────────────────────

@Composable
fun NutritionScreen(
    onSupplementsClick: () -> Unit = {},
    onCalculatorClick:  () -> Unit = {},
    onNutrientsClick:   () -> Unit = {},
    viewModel: KineticViewModel = viewModel()
) {
    // Targets from ViewModel (set by CalorieCalculator)
    val targetCals  by viewModel.targetCals.collectAsState()
    val targetPro   by viewModel.targetPro.collectAsState()
    val targetCarbs by viewModel.targetCarbs.collectAsState()
    val targetFats  by viewModel.targetFats.collectAsState()

    // Today's logs from Room
    val todayLogs   by viewModel.todayNutritionLogs.collectAsState()
    val totalCals   = todayLogs.sumOf { it.calories }
    val totalPro    = todayLogs.sumOf { it.protein.toDouble() }.toFloat()
    val totalCarbs  = todayLogs.sumOf { it.carbs.toDouble() }.toFloat()
    val totalFats   = todayLogs.sumOf { it.fat.toDouble() }.toFloat()

    // Food search
    var searchQuery      by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var pendingFood   by remember { mutableStateOf<FoodItem?>(null) }
    // Category filter map، Arabic display label → DB category key or ID prefix
    val categoryFilters = remember {
        listOf(
            "كل الأكلات"  to "",
            "بيض وشكشوكة" to "Egg",
            "بتنجان"       to "BT",
            "مصرية"        to "EG",
            "وجبات منزلية" to "CM",
            "لحوم"         to "Protein",
            "خضار"         to "Veg",
            "كارب"         to "Carb",
            "ألبان"        to "Dairy"
        )
    }

    val filteredFoods = remember(searchQuery, selectedCategory) {
        EgyptianFoodDatabase.foods.filter { food ->
            val matchesSearch = searchQuery.isBlank() ||
                food.name.contains(searchQuery, ignoreCase = true) ||
                food.nameAr.contains(searchQuery)
            val matchesCategory = when {
                selectedCategory.isBlank() -> true
                selectedCategory == "BT"   -> food.id.startsWith("bt")
                selectedCategory == "EG"   -> food.id.startsWith("eg")
                selectedCategory == "CM"   -> food.id.startsWith("cm")
                else                       -> food.category.equals(selectedCategory, ignoreCase = true)
            }
            matchesSearch && matchesCategory
        }.let { results ->
            if (searchQuery.isBlank() && selectedCategory.isBlank()) emptyList()
            else results.take(30)
        }
    }


    // Add food quantity dialog
    if (showAddDialog && pendingFood != null) {
        FoodQuantityDialog(
            food       = pendingFood!!,
            onConfirm  = { grams ->
                val f = pendingFood!!
                viewModel.logFood(
                    foodId             = f.id,
                    foodName           = f.name,
                    foodNameAr         = f.nameAr,
                    grams              = grams,
                    calsPerHundred     = f.calories,
                    proteinPerHundred  = f.protein,
                    carbsPerHundred    = f.carbs,
                    fatPerHundred      = f.fat
                )
                showAddDialog = false
                pendingFood   = null
                searchQuery   = ""
            },
            onDismiss  = { showAddDialog = false; pendingFood = null }
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
                Text(
                    "التغذية",
                    style = MaterialTheme.typography.displayLarge,
                    color = OnSurface,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onCalculatorClick) {
                    Icon(Icons.Default.Calculate, contentDescription = "Calculator", tint = Primary)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Energy balance card
        item {
            EnergyBalanceCard(
                consumed = totalCals,
                target   = targetCals
            )
        }

        // Macro rings
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MacroRingCard("بروتين",  totalPro.toInt(),   targetPro,   "g", Modifier.weight(1f))
                MacroRingCard("كارب",    totalCarbs.toInt(), targetCarbs, "g", Modifier.weight(1f))
                MacroRingCard("دهون",    totalFats.toInt(),  targetFats,  "g", Modifier.weight(1f))
            }
        }

        // Food search
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Text(
                            "ابحث وأضف طعامك",
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value         = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier      = Modifier.fillMaxWidth(),
                            placeholder   = { Text("شكشوكة، بتنجان، أرز...  أو اكتب بالإنجليزي", color = OnSurfaceVariant.copy(alpha = 0.6f)) },
                            leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null, tint = OnSurfaceVariant) },
                            trailingIcon  = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = OnSurfaceVariant)
                                    }
                                }
                            },
                            singleLine    = true,
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = Primary,
                                unfocusedBorderColor = Outline,
                                focusedTextColor     = OnSurface,
                                unfocusedTextColor   = OnSurface,
                                cursorColor          = Primary
                            )
                        )

                    }

                    // ── Category Chips ────────────────────────────────────────
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoryFilters.forEach { (label, key) ->
                            val isActive = selectedCategory == key
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isActive) Primary else SurfaceContainerHigh)
                                    .border(
                                        width = 0.5.dp,
                                        color = if (isActive) Primary else Outline.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        selectedCategory = if (isActive) "" else key
                                    }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isActive) OnPrimary else OnSurfaceVariant
                                )
                            }
                        }
                    }

                    if (filteredFoods.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                        Spacer(modifier = Modifier.height(8.dp))
                        filteredFoods.forEachIndexed { idx, food ->
                            FoodSearchResultRow(food = food) {
                                pendingFood   = food
                                showAddDialog = true
                            }
                            if (idx < filteredFoods.lastIndex) {
                                HorizontalDivider(
                                    color    = Color.White.copy(alpha = 0.05f),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    } else if (searchQuery.isBlank() && selectedCategory.isBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "اختر تصنيف أو ابحث عن أكلة بالاسم ↑",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        // Today's food log
        item {
            TodayFoodLogCard(
                logs     = todayLogs,
                onRemove = { viewModel.removeNutritionLog(it.id) }
            )
        }

        // ── Meal Builder ──────────────────────────────────────────────────
        item { MealBuilderSection(viewModel = viewModel) }

        // Quick nav: Supplements / Nutrients
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NutritionNavCard(
                    title    = "المكملات",
                    subtitle = "دليل علمي",
                    icon     = Icons.Default.Science,
                    onClick  = onSupplementsClick,
                    modifier = Modifier.weight(1f)
                )
                NutritionNavCard(
                    title    = "مصادر الغذاء",
                    subtitle = "الفيتامينات والمعادن",
                    icon     = Icons.Default.Eco,
                    onClick  = onNutrientsClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

// ─── Food Quantity Dialog ─────────────────────────────────────────────────────

@Composable
private fun FoodQuantityDialog(
    food:      FoodItem,
    onConfirm: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var gramsText by remember { mutableStateOf("100") }
    val grams     = gramsText.toFloatOrNull() ?: 100f
    val scale     = grams / 100f

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = SurfaceContainerHigh,
        title = {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Column {
                    Text(food.nameAr.ifBlank { food.name }, color = OnSurface, fontWeight = FontWeight.Bold)
                    Text(food.name, color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        text = {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value         = gramsText,
                        onValueChange = { gramsText = it },
                        label         = { Text("الكمية (جرام)", color = OnSurfaceVariant) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine    = true,
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Primary,
                            unfocusedBorderColor = Outline,
                            focusedTextColor     = OnSurface,
                            unfocusedTextColor   = OnSurface
                        ),
                        modifier      = Modifier.fillMaxWidth()
                    )
                    // Live preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Primary.copy(alpha = 0.08f))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            MiniMacro("${(food.calories * scale).toInt()}", "kcal")
                            MiniMacro("${(food.protein * scale).toInt()}g", "بروتين")
                            MiniMacro("${(food.carbs * scale).toInt()}g", "كارب")
                            MiniMacro("${(food.fat * scale).toInt()}g", "دهون")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(grams) },
                colors  = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.Black),
                enabled = grams > 0f
            ) {
                Text("أضف", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = OnSurfaceVariant)
            }
        }
    )
}

@Composable
private fun MiniMacro(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = OnSurface, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
    }
}

// ─── Energy Balance ───────────────────────────────────────────────────────────

@Composable
fun EnergyBalanceCard(consumed: Int, target: Int) {
    val progress  = (consumed.toFloat() / target.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
    val remaining = target - consumed

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "ENERGY BALANCE",
                    color = OnSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "$consumed",
                        style      = MaterialTheme.typography.displayLarge,
                        color      = OnSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        " / $target kcal",
                        style    = MaterialTheme.typography.bodyLarge,
                        color    = OnSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Text(
                    if (remaining >= 0) "متبقي $remaining kcal"
                    else "تجاوزت الهدف بـ ${-remaining} kcal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (remaining >= 0) Primary else MaterialTheme.colorScheme.error
                )
            }
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
                CircularProgressIndicator(progress = { 1f }, modifier = Modifier.size(72.dp),
                    color = Color.White.copy(alpha = 0.05f), strokeWidth = 7.dp)
                CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(72.dp),
                    color = if (remaining >= 0) Primary else MaterialTheme.colorScheme.error, strokeWidth = 7.dp)
                Text(
                    "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurface, fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─── Macro Ring Card ──────────────────────────────────────────────────────────

@Composable
fun MacroRingCard(
    title: String, current: Int, target: Int,
    unit: String, modifier: Modifier
) {
    val progress = (current.toFloat() / target.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
    GlassCard(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.fillMaxWidth()
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
                CircularProgressIndicator(progress = { 1f }, modifier = Modifier.size(64.dp),
                    color = Color.White.copy(alpha = 0.05f), strokeWidth = 5.dp)
                CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(64.dp),
                    color = Primary, strokeWidth = 5.dp)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$current", style = MaterialTheme.typography.titleSmall, color = OnSurface, fontWeight = FontWeight.Bold)
                    Text("/$target", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = OnSurface, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(
                "${(target - current).coerceAtLeast(0)}$unit متبقي",
                color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// ─── Food Search Result Row ───────────────────────────────────────────────────

@Composable
private fun FoodSearchResultRow(food: FoodItem, onAdd: () -> Unit) {
    val categoryLabel = when {
        food.id.startsWith("bt")                   -> "🍆 بتنجان"
        food.id.startsWith("eg")                   -> "🇪🇬 مصرية"
        food.id.startsWith("cm")                   -> "🍽️ وجبة"
        food.id.startsWith("fr")                   -> "🍎 فواكه"
        food.id.startsWith("sn")                   -> "🍟 سناك"
        food.id.startsWith("bv")                   -> "☕ مشروبات"
        food.id.startsWith("pf")                   -> "رياضة"
        food.category == "Egg"                     -> "🥚 بيض"
        food.category == "Protein"                 -> "🥩 بروتين"
        food.category == "Dairy"                   -> "🥛 ألبان"
        food.category == "Carb"                    -> "🌾 كارب"
        food.category == "Veg"                     -> "🥦 خضار"
        food.category == "Mixed"                   -> "🍛 مشكل"
        food.category == "Supplement"              -> "🧪 مكمل"
        else                                        -> ""
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAdd() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    food.nameAr.ifBlank { food.name },
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnSurface
                )
                if (categoryLabel.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SurfaceContainerHigh)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            categoryLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = OnSurfaceVariant
                        )
                    }
                }
            }
            Text(
                "${food.calories} kcal | بروتين ${food.protein.toInt()}g | كارب ${food.carbs.toInt()}g | دهون ${food.fat.toInt()}g",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = Primary, modifier = Modifier.size(18.dp))
        }
    }
}

// ─── Today Food Log ───────────────────────────────────────────────────────────

@Composable
private fun TodayFoodLogCard(
    logs:     List<NutritionLog>,
    onRemove: (NutritionLog) -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column {
                Text(
                    "سجل اليوم",
                    style = MaterialTheme.typography.headlineMedium,
                    color = OnSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (logs.isEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "لم تسجل أي وجبة بعد. ابحث وأضف طعامك من الأعلى.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth()
                    )
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                    logs.forEachIndexed { idx, log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            // Name & macros
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    log.foodNameAr.ifBlank { log.foodName },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = OnSurface,
                                    maxLines = 1
                                )
                                Text(
                                    "${log.grams.toInt()}ج | ${log.calories} kcal  P:${log.protein.toInt()}  C:${log.carbs.toInt()}  F:${log.fat.toInt()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { onRemove(log) }, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint   = OnSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        if (idx < logs.lastIndex) {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                        }
                    }
                }
            }
        }
    }
}

// ─── Nav Cards ───────────────────────────────────────────────────────────────

@Composable
private fun NutritionNavCard(
    title:    String,
    subtitle: String,
    icon:     androidx.compose.ui.graphics.vector.ImageVector,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.clickable { onClick() }) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title,    style = MaterialTheme.typography.labelLarge, color = OnSurface,        fontWeight = FontWeight.Bold,   textAlign = TextAlign.Center)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,  color = OnSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

// ─── Meal Builder Section ─────────────────────────────────────────────────────

// Stable ingredient entry with unique key
private data class IngredientEntry(
    val key: Int,
    val food: com.example.data.FoodItem,
    var grams: Float
)

@Composable
private fun MealBuilderSection(viewModel: KineticViewModel) {
    val customMeals by viewModel.customMeals.collectAsState()
    var showBuilder by remember { mutableStateOf(false) }
    var mealName    by remember { mutableStateOf("") }
    var searchTerm  by remember { mutableStateOf("") }
    var nextKey     by remember { mutableIntStateOf(0) }

    // Stable ingredient list using unique keys (not array indices)
    val ingredients = remember { mutableStateListOf<IngredientEntry>() }

    // Weight text per ingredient key
    val weightEdits = remember { mutableStateMapOf<Int, String>() }

    // Feedback state for "added to calories"
    var lastAddedMealName by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(lastAddedMealName) {
        if (lastAddedMealName != null) {
            kotlinx.coroutines.delay(2000)
            lastAddedMealName = null
        }
    }

    val searchResults = remember(searchTerm) {
        if (searchTerm.length < 2) emptyList()
        else com.example.data.EgyptianFoodDatabase.foods.filter {
            it.name.contains(searchTerm, ignoreCase = true) || it.nameAr.contains(searchTerm)
        }.take(12)
    }

    fun clearBuilder() {
        mealName = ""
        ingredients.clear()
        weightEdits.clear()
        searchTerm = ""
        nextKey = 0
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column {
                // ── Header ──────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Restaurant, null, tint = Primary, modifier = Modifier.size(14.dp))
                        Text(
                            "وجباتي المحفوظة",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                            color = Primary
                        )
                        if (customMeals.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Primary.copy(0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${customMeals.size}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = Primary
                                )
                            }
                        }
                    }
                    IconButton(onClick = {
                        if (showBuilder) clearBuilder()
                        showBuilder = !showBuilder
                    }) {
                        Icon(
                            if (showBuilder) Icons.Default.KeyboardArrowUp else Icons.Default.Add,
                            null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // ── Saved Meals List ─────────────────────────────────────────
                if (customMeals.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    customMeals.forEachIndexed { i, meal ->
                        val isLastAdded = lastAddedMealName == meal.nameAr
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Meal info
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    meal.nameAr,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OnSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "${meal.totalCalories} kcal  ·  بروتين ${meal.totalProtein.toInt()}g  ·  كارب ${meal.totalCarbs.toInt()}g",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSurfaceVariant
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            // Add to today button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isLastAdded) Primary.copy(0.3f) else Primary.copy(0.15f)
                                    )
                                    .clickable {
                                        viewModel.logCustomMealAsCalories(meal)
                                        lastAddedMealName = meal.nameAr
                                    }
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLastAdded) {
                                    Icon(
                                        Icons.Default.Check,
                                        null,
                                        tint = Primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else {
                                    Text(
                                        "+ سعراتي",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            // Delete button
                            IconButton(
                                onClick = { viewModel.deleteCustomMeal(meal.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    null,
                                    tint = OnSurfaceVariant.copy(0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        if (i < customMeals.lastIndex) {
                            HorizontalDivider(color = androidx.compose.ui.graphics.Color.White.copy(.05f))
                        }
                    }
                }

                // Empty state
                if (customMeals.isEmpty() && !showBuilder) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "لا توجد وجبات محفوظة بعد. اضغط + لإنشاء وجبتك الأولى.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )
                }

                // ── Builder UI ───────────────────────────────────────────────
                androidx.compose.animation.AnimatedVisibility(visible = showBuilder) {
                    Column {
                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(color = androidx.compose.ui.graphics.Color.White.copy(.1f))
                        Spacer(Modifier.height(14.dp))

                        // Meal name field
                        OutlinedTextField(
                            value = mealName,
                            onValueChange = { mealName = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("اسم الوجبة (مثال: شكشوكة بالجبنة)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Outline,
                                focusedTextColor = OnSurface,
                                unfocusedTextColor = OnSurface,
                                cursorColor = Primary,
                                focusedLabelColor = Primary,
                                unfocusedLabelColor = OnSurfaceVariant
                            )
                        )
                        Spacer(Modifier.height(10.dp))

                        // Search field
                        OutlinedTextField(
                            value = searchTerm,
                            onValueChange = { searchTerm = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("ابحث عن مكوّن (اكتب 2 حروف على الأقل)", color = OnSurfaceVariant.copy(.5f)) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = OnSurfaceVariant) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Outline,
                                focusedTextColor = OnSurface,
                                unfocusedTextColor = OnSurface,
                                cursorColor = Primary
                            )
                        )

                        // Search results
                        if (searchResults.isNotEmpty()) {
                            Spacer(Modifier.height(6.dp))
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceContainerHigh)
                            ) {
                                searchResults.forEachIndexed { i, food ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val key = nextKey++
                                                ingredients.add(IngredientEntry(key, food, 100f))
                                                weightEdits[key] = "100"
                                                searchTerm = ""
                                            }
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(food.nameAr, style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                                            Text(food.name, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                        }
                                        Text(
                                            "${food.calories} kcal/100g",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Primary
                                        )
                                    }
                                    if (i < searchResults.lastIndex) {
                                        HorizontalDivider(color = androidx.compose.ui.graphics.Color.White.copy(.04f))
                                    }
                                }
                            }
                        }

                        // Ingredients list
                        if (ingredients.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("المكونات:", style = MaterialTheme.typography.labelSmall, color = Primary)
                                Text(
                                    "${ingredients.size} مكوّن",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSurfaceVariant
                                )
                            }
                            Spacer(Modifier.height(6.dp))

                            ingredients.forEach { entry ->
                                val displayCals = (entry.food.calories * entry.grams / 100f).toInt()
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            entry.food.nameAr,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = OnSurface
                                        )
                                        Text(
                                            "$displayCals kcal",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Primary
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = weightEdits.getOrDefault(entry.key, entry.grams.toInt().toString()),
                                            onValueChange = { v ->
                                                weightEdits[entry.key] = v
                                                v.toFloatOrNull()?.let { newG ->
                                                    val idx = ingredients.indexOfFirst { it.key == entry.key }
                                                    if (idx >= 0) ingredients[idx] = entry.copy(grams = newG)
                                                }
                                            },
                                            modifier = Modifier.width(72.dp),
                                            singleLine = true,
                                            suffix = {
                                                Text("g", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                            },
                                            textStyle = MaterialTheme.typography.bodySmall,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Primary,
                                                unfocusedBorderColor = OnSurfaceVariant.copy(.3f),
                                                focusedTextColor = OnSurface,
                                                unfocusedTextColor = OnSurface
                                            ),
                                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        IconButton(
                                            onClick = {
                                                val idx = ingredients.indexOfFirst { it.key == entry.key }
                                                if (idx >= 0) ingredients.removeAt(idx)
                                                weightEdits.remove(entry.key)
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                null,
                                                tint = OnSurfaceVariant,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Totals row
                            val totalCals = ingredients.sumOf { (it.food.calories * it.grams / 100f).toDouble() }.toInt()
                            val totalPro  = ingredients.sumOf { (it.food.protein * it.grams / 100f).toDouble() }.toFloat()
                            val totalCarb = ingredients.sumOf { (it.food.carbs   * it.grams / 100f).toDouble() }.toFloat()
                            val totalFat  = ingredients.sumOf { (it.food.fat     * it.grams / 100f).toDouble() }.toFloat()

                            Spacer(Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Primary.copy(.1f))
                                    .border(1.dp, Primary.copy(.25f), RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("$totalCals", style = MaterialTheme.typography.titleMedium, color = Primary, fontWeight = FontWeight.Bold)
                                    Text("kcal", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${totalPro.toInt()}g", style = MaterialTheme.typography.bodyMedium, color = OnSurface, fontWeight = FontWeight.SemiBold)
                                    Text("بروتين", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${totalCarb.toInt()}g", style = MaterialTheme.typography.bodyMedium, color = OnSurface, fontWeight = FontWeight.SemiBold)
                                    Text("كارب", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${totalFat.toInt()}g", style = MaterialTheme.typography.bodyMedium, color = OnSurface, fontWeight = FontWeight.SemiBold)
                                    Text("دهون", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Save button
                            Button(
                                onClick = {
                                    if (mealName.isNotBlank() && ingredients.isNotEmpty()) {
                                        val saveCals = ingredients.sumOf { (it.food.calories * it.grams / 100f).toDouble() }.toInt()
                                        val savePro  = ingredients.sumOf { (it.food.protein * it.grams / 100f).toDouble() }.toFloat()
                                        val saveCarb = ingredients.sumOf { (it.food.carbs * it.grams / 100f).toDouble() }.toFloat()
                                        val saveFat  = ingredients.sumOf { (it.food.fat * it.grams / 100f).toDouble() }.toFloat()
                                        viewModel.saveCustomMeal(
                                            nameAr = mealName,
                                            ingredients = "[]",
                                            cals = saveCals,
                                            pro = savePro,
                                            carbs = saveCarb,
                                            fat = saveFat
                                        )
                                        clearBuilder()
                                        showBuilder = false
                                    }
                                },
                                enabled = mealName.isNotBlank() && ingredients.isNotEmpty(),
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Primary,
                                    contentColor = androidx.compose.ui.graphics.Color.Black,
                                    disabledContainerColor = Primary.copy(0.3f),
                                    disabledContentColor = androidx.compose.ui.graphics.Color.Black.copy(0.4f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("حفظ الوجبة", fontWeight = FontWeight.Bold)
                            }

                            if (mealName.isBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "أدخل اسم الوجبة أولاً",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

