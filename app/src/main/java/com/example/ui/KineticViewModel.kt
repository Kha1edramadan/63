package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

// ── Performance: combined state → single collectAsState() in HomeScreen ──────
data class NutritionTargets(
    val cals:   Int   = 2400,
    val pro:    Int   = 180,
    val carbs:  Int   = 250,
    val fats:   Int   = 70,
    val weight: Float = 80f
)

class KineticViewModel(application: Application) : AndroidViewModel(application) {

    private val dao        = AppDatabase.getDatabase(application).workoutDao()
    private val repository = WorkoutRepository(dao)
    private val prefs      = application.getSharedPreferences("kinetic_prefs", Context.MODE_PRIVATE)

    // ─── Workout Plans & Exercises ────────────────────────────────────────────

    val plans: StateFlow<List<WorkoutPlan>> = repository.allPlans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exercises: StateFlow<List<Exercise>> = repository.allExercises
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getExercisesForPlan(planId: String)             = repository.getExercisesForPlan(planId)
    fun getLastSessionLogsForExercise(exerciseId: String) = repository.getLastSessionLogsForExercise(exerciseId)
    fun getRecentSessionLogsForExercise(exerciseId: String) = repository.getRecentSessionLogsForExercise(exerciseId)

    // ─── Nutrition Targets (from CalorieCalculator) ───────────────────────────
    // Combined StateFlow → HomeScreen collects once instead of 4× (fewer recompositions)


    private val _targetCals  = MutableStateFlow(prefs.getInt("targetCals", 2400))
    val targetCals = _targetCals.asStateFlow()

    private val _targetPro   = MutableStateFlow(prefs.getInt("targetPro", 180))
    val targetPro = _targetPro.asStateFlow()

    private val _targetCarbs = MutableStateFlow(prefs.getInt("targetCarbs", 250))
    val targetCarbs = _targetCarbs.asStateFlow()

    private val _targetFats  = MutableStateFlow(prefs.getInt("targetFats", 70))
    val targetFats = _targetFats.asStateFlow()

    private val _targetWeight = MutableStateFlow(prefs.getFloat("targetWeight", 80f))
    val targetWeight = _targetWeight.asStateFlow()

    // Single combined target — use this in HomeScreen for better performance
    val nutritionTargets: StateFlow<NutritionTargets> = combine(
        _targetCals, _targetPro, _targetCarbs, _targetFats, _targetWeight
    ) { cals, pro, carbs, fats, w -> NutritionTargets(cals, pro, carbs, fats, w) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NutritionTargets())

    // ─── Plan Settings ────────────────────────────────────────────────────────

    private val _planGoal = MutableStateFlow(prefs.getString("plan_goal", "Bulking") ?: "Bulking")
    val planGoal = _planGoal.asStateFlow()

    private val _planDurationWeeks = MutableStateFlow(prefs.getInt("plan_weeks", 12))
    val planDurationWeeks = _planDurationWeeks.asStateFlow()

    private val _planElapsedWeeks = MutableStateFlow(prefs.getInt("plan_elapsed", 1))
    val planElapsedWeeks = _planElapsedWeeks.asStateFlow()

    private val _completedCycles = MutableStateFlow(prefs.getInt("completed_cycles", 0))
    val completedCycles = _completedCycles.asStateFlow()

    private val _currentPlanIndex = MutableStateFlow(prefs.getInt("current_plan_index", 0))
    val currentPlanIndex = _currentPlanIndex.asStateFlow()

    // ─── Subscription ─────────────────────────────────────────────────────────

    private val _subscriptionStartDate = MutableStateFlow(prefs.getLong("sub_start", System.currentTimeMillis()))
    val subscriptionStartDate = _subscriptionStartDate.asStateFlow()

    private val _subscriptionMonths = MutableStateFlow(prefs.getInt("sub_months", 1))
    val subscriptionMonths = _subscriptionMonths.asStateFlow()

    // ─── Weekly Tracking ──────────────────────────────────────────────────────

    /** Epoch ms of Monday 00:00 of the current calendar week. */
    val currentWeekStart: Long
        get() {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

    /** Number of workouts completed this calendar week (live from DB). */
    val workoutsThisWeek: StateFlow<Int> = repository.getSessionCountSince(currentWeekStart)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** Total workout sessions ever completed. */
    val totalWorkouts: StateFlow<Int> = repository.getTotalSessionCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** Recent sessions for display on progress/home. */
    val recentSessions: StateFlow<List<WorkoutSession>> = repository.recentSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ─── Daily Nutrition Tracking ─────────────────────────────────────────────

    /** Midnight epoch (local) for today — the key for today's nutrition logs. */
    val todayKey: Long
        get() {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

    /** Today's food logs, live from Room. */
    val todayNutritionLogs: StateFlow<List<NutritionLog>> =
        repository.getNutritionLogsForDay(todayKey)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ─── Body Weight Logs ─────────────────────────────────────────────────────

    val recentBodyWeightLogs: StateFlow<List<BodyWeightLog>> = repository.getRecentBodyWeightLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ─── Init ─────────────────────────────────────────────────────────────────

    init {
        viewModelScope.launch {
            repository.populateInitialData()
        }
    }

    // ─── Actions: Workout ─────────────────────────────────────────────────────

    fun logSet(exerciseId: String, sessionId: Long, setNumber: Int, weight: Float, reps: Int) {
        viewModelScope.launch {
            repository.logSet(
                SetLog(
                    exerciseId    = exerciseId,
                    sessionId     = sessionId,
                    setNumber     = setNumber,
                    weight        = weight,
                    repsCompleted = reps
                )
            )
        }
    }

    /**
     * Called when the user finishes a workout session.
     * - Records the session in Room.
     * - Advances [currentPlanIndex] to the next plan.
     * - Advances [planElapsedWeeks] when a full 4-plan cycle is completed.
     */
    fun finishWorkout(planId: String, planName: String, sessionId: Long) {
        viewModelScope.launch {
            repository.recordWorkoutSession(
                WorkoutSession(
                    sessionId   = sessionId,
                    planId      = planId,
                    planName    = planName,
                    completedAt = System.currentTimeMillis()
                )
            )
        }

        val totalPlans = plans.value.size.coerceAtLeast(1)
        var nextIndex  = (_currentPlanIndex.value + 1) % totalPlans

        // Advance week when we've cycled through all 4 plans
        if (nextIndex == 0) {
            val nextWeek = _planElapsedWeeks.value + 1
            _planElapsedWeeks.value = nextWeek
            prefs.edit().putInt("plan_elapsed", nextWeek).apply()
        }

        _currentPlanIndex.value = nextIndex
        prefs.edit().putInt("current_plan_index", nextIndex).apply()
    }

    // ─── Actions: Nutrition ───────────────────────────────────────────────────

    /**
     * Logs a food item for today. The [grams] parameter scales macros proportionally
     * from the food's per-100g values.
     */
    fun logFood(
        foodId: String,
        foodName: String,
        foodNameAr: String,
        grams: Float,
        calsPerHundred: Int,
        proteinPerHundred: Float,
        carbsPerHundred: Float,
        fatPerHundred: Float
    ) {
        val scale   = grams / 100f
        viewModelScope.launch {
            repository.logNutrition(
                NutritionLog(
                    dayKey     = todayKey,
                    foodId     = foodId,
                    foodName   = foodName,
                    foodNameAr = foodNameAr,
                    grams      = grams,
                    calories   = (calsPerHundred * scale).toInt(),
                    protein    = proteinPerHundred * scale,
                    carbs      = carbsPerHundred * scale,
                    fat        = fatPerHundred * scale
                )
            )
        }
    }

    fun removeNutritionLog(id: Int) {
        viewModelScope.launch { repository.removeNutritionLog(id) }
    }

    // ─── Actions: Settings ────────────────────────────────────────────────────

    fun updateNutritionTargets(cals: Int, pro: Int, carbs: Int, fats: Int, goal: String, weight: Float) {
        prefs.edit()
            .putInt("targetCals",    cals)
            .putInt("targetPro",     pro)
            .putInt("targetCarbs",   carbs)
            .putInt("targetFats",    fats)
            .putString("plan_goal",  goal)
            .putFloat("targetWeight", weight)
            .apply()
        _targetCals.value   = cals
        _targetPro.value    = pro
        _targetCarbs.value  = carbs
        _targetFats.value   = fats
        _planGoal.value     = goal
        _targetWeight.value = weight
    }

    fun updateSubscription(startDateMs: Long, months: Int) {
        prefs.edit()
            .putLong("sub_start",    startDateMs)
            .putInt("sub_months",    months)
            .apply()
        _subscriptionStartDate.value = startDateMs
        _subscriptionMonths.value    = months
    }

    fun updatePlanSettings(goal: String, durationWeeks: Int) {
        prefs.edit()
            .putString("plan_goal",  goal)
            .putInt("plan_weeks",    durationWeeks)
            .apply()
        _planGoal.value          = goal
        _planDurationWeeks.value = durationWeeks
    }

    fun resetPlanProgress() {
        val nextCycles = _completedCycles.value + 1
        prefs.edit()
            .putInt("plan_elapsed",      1)
            .putInt("completed_cycles",  nextCycles)
            .putInt("current_plan_index", 0)
            .apply()
        _planElapsedWeeks.value  = 1
        _completedCycles.value   = nextCycles
        _currentPlanIndex.value  = 0
    }

    fun logBodyWeight(weightKg: Float) {
        viewModelScope.launch { repository.logBodyWeight(BodyWeightLog(weightKg = weightKg)) }
    }

    // ─── Personal Records ─────────────────────────────────────────────────────

    val allPersonalRecords: StateFlow<List<PersonalRecord>> = repository.getAllPersonalRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Returns true if a new PR was set. */
    suspend fun checkAndSavePR(exerciseId: String, name: String, nameAr: String, weight: Float, reps: Int): Boolean =
        repository.checkAndSavePR(exerciseId, name, nameAr, weight, reps)

    // ─── Water Tracking ───────────────────────────────────────────────────────

    private val todayMidnight: Long get() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0);      cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    val todayWaterMl: StateFlow<Int> = repository.getTodayWaterMl(todayMidnight)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun addWater(amountMl: Int) {
        viewModelScope.launch { repository.addWater(todayMidnight, amountMl) }
    }

    fun resetTodayWater() {
        viewModelScope.launch { repository.resetTodayWater(todayMidnight) }
    }

    // ─── Body Measurements ────────────────────────────────────────────────────

    val recentBodyMeasurements: StateFlow<List<BodyMeasurement>> = repository.getRecentBodyMeasurements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveBodyMeasurement(chest: Float, waist: Float, hips: Float, arm: Float, thigh: Float) {
        viewModelScope.launch {
            repository.insertBodyMeasurement(BodyMeasurement(
                chestCm = chest, waistCm = waist, hipsCm = hips,
                armCm = arm, thighCm = thigh
            ))
        }
    }

    // ─── Workout Notes ────────────────────────────────────────────────────────

    fun saveWorkoutNote(sessionId: Long, note: String) {
        if (note.isBlank()) return
        viewModelScope.launch { repository.saveWorkoutNote(sessionId, note) }
    }

    // ─── RPE ──────────────────────────────────────────────────────────────────

    fun logRpe(sessionId: Long, exerciseId: String, setNumber: Int, rpe: Int) {
        viewModelScope.launch { repository.logRpe(sessionId, exerciseId, setNumber, rpe) }
    }

    // ─── Custom Meals ─────────────────────────────────────────────────────────

    val customMeals: StateFlow<List<CustomMeal>> = repository.getAllCustomMeals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveCustomMeal(nameAr: String, ingredients: String, cals: Int, pro: Float, carbs: Float, fat: Float) {
        viewModelScope.launch {
            repository.insertCustomMeal(CustomMeal(
                nameAr = nameAr, ingredientsJson = ingredients,
                totalCalories = cals, totalProtein = pro,
                totalCarbs = carbs, totalFat = fat
            ))
        }
    }

    fun deleteCustomMeal(id: Int) {
        viewModelScope.launch { repository.deleteCustomMeal(id) }
    }

    fun logCustomMealAsCalories(meal: CustomMeal) {
        viewModelScope.launch {
            repository.logNutrition(
                NutritionLog(
                    dayKey      = todayKey,
                    foodId      = "meal_${meal.id}",
                    foodName    = meal.nameAr,
                    foodNameAr  = meal.nameAr,
                    grams       = 100f,
                    calories    = meal.totalCalories,
                    protein     = meal.totalProtein,
                    carbs       = meal.totalCarbs,
                    fat         = meal.totalFat
                )
            )
        }
    }

    // ─── Computed Helpers ─────────────────────────────────────────────────────

    fun getSubscriptionDaysLeft(): Long {
        val endCal = Calendar.getInstance().apply {
            timeInMillis = _subscriptionStartDate.value
            add(Calendar.MONTH, _subscriptionMonths.value)
        }
        val diff = endCal.timeInMillis - System.currentTimeMillis()
        return if (diff > 0) diff / (1000L * 60 * 60 * 24) else 0L
    }

    /** True if the plan duration has elapsed (time to start a new cycle). */
    fun isPlanComplete(): Boolean =
        _planElapsedWeeks.value > _planDurationWeeks.value
}
