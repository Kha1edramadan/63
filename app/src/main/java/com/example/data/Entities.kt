package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_plans")
data class WorkoutPlan(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val orderIndex: Int
)

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey val id: String,
    val planId: String,
    val sequenceNumber: Int,
    val name: String,
    val muscleGroup: String,
    val sets: Int,
    val repsTarget: String,
    val rir: String,
    val restTime: String,
    val focusArea: String,
    val coachingCue1: String,
    val coachingCue2: String,
    val alternative: String
)

@Entity(tableName = "set_logs")
data class SetLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val exerciseId: String,
    val sessionId: Long,
    val setNumber: Int,
    val weight: Float,
    val repsCompleted: Int,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Records a completed workout session.
 */
@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey val sessionId: Long = System.currentTimeMillis(),
    val planId: String,
    val planName: String,
    val completedAt: Long = System.currentTimeMillis()
)

/**
 * Daily nutrition log entry. Each row = one food item logged for a specific day.
 * dayKey = start-of-day epoch millis (midnight, local time) for easy daily grouping.
 */
@Entity(tableName = "nutrition_logs")
data class NutritionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dayKey: Long,           // midnight epoch of the log day
    val foodId: String,
    val foodName: String,
    val foodNameAr: String,
    val grams: Float,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val loggedAt: Long = System.currentTimeMillis()
)

/**
 * Body weight snapshot, one per measurement.
 */
@Entity(tableName = "body_weight_logs")
data class BodyWeightLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val weightKg: Float,
    val loggedAt: Long = System.currentTimeMillis()
)

/** Personal record per exercise — one row per exerciseId (upserted on every new PR). */
@Entity(tableName = "personal_records")
data class PersonalRecord(
    @PrimaryKey val exerciseId: String,
    val exerciseName: String,
    val exerciseNameAr: String,
    val weightKg: Float,
    val reps: Int,
    val estimatedOneRepMax: Float,   // Epley: w*(1+r/30)
    val achievedAt: Long = System.currentTimeMillis()
)

/** Daily water intake log. */
@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dayKey: Long,       // midnight epoch millis
    val amountMl: Int,
    val loggedAt: Long = System.currentTimeMillis()
)

/** Body measurements snapshot. */
@Entity(tableName = "body_measurements")
data class BodyMeasurement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chestCm: Float = 0f,
    val waistCm: Float = 0f,
    val hipsCm: Float = 0f,
    val armCm: Float = 0f,
    val thighCm: Float = 0f,
    val loggedAt: Long = System.currentTimeMillis()
)

/** Free-text note attached to a workout session. */
@Entity(tableName = "workout_notes")
data class WorkoutNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Long,
    val note: String,
    val loggedAt: Long = System.currentTimeMillis()
)

/** RPE (1-10) logged per set. */
@Entity(tableName = "rpe_logs")
data class RpeLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Long,
    val exerciseId: String,
    val setNumber: Int,
    val rpeValue: Int,          // 1-10
    val loggedAt: Long = System.currentTimeMillis()
)

/** User-saved custom meal (name + serialised ingredient list + pre-computed macros). */
@Entity(tableName = "custom_meals")
data class CustomMeal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nameAr: String,
    val ingredientsJson: String,    // JSON: [{"foodId":"egg1","grams":150},...]
    val totalCalories: Int,
    val totalProtein: Float,
    val totalCarbs: Float,
    val totalFat: Float,
    val createdAt: Long = System.currentTimeMillis()
)
