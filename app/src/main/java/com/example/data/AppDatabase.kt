package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        WorkoutPlan::class, Exercise::class, SetLog::class,
        WorkoutSession::class, NutritionLog::class, BodyWeightLog::class,
        PersonalRecord::class, WaterLog::class, BodyMeasurement::class,
        WorkoutNote::class, RpeLog::class, CustomMeal::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS workout_sessions (sessionId INTEGER NOT NULL PRIMARY KEY, planId TEXT NOT NULL, planName TEXT NOT NULL, completedAt INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS nutrition_logs (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, dayKey INTEGER NOT NULL, foodId TEXT NOT NULL, foodName TEXT NOT NULL, foodNameAr TEXT NOT NULL, grams REAL NOT NULL, calories INTEGER NOT NULL, protein REAL NOT NULL, carbs REAL NOT NULL, fat REAL NOT NULL, loggedAt INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS body_weight_logs (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, weightKg REAL NOT NULL, loggedAt INTEGER NOT NULL)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS personal_records (exerciseId TEXT NOT NULL PRIMARY KEY, exerciseName TEXT NOT NULL, exerciseNameAr TEXT NOT NULL, weightKg REAL NOT NULL, reps INTEGER NOT NULL, estimatedOneRepMax REAL NOT NULL, achievedAt INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS water_logs (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, dayKey INTEGER NOT NULL, amountMl INTEGER NOT NULL, loggedAt INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS body_measurements (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, chestCm REAL NOT NULL, waistCm REAL NOT NULL, hipsCm REAL NOT NULL, armCm REAL NOT NULL, thighCm REAL NOT NULL, loggedAt INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS workout_notes (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, sessionId INTEGER NOT NULL, note TEXT NOT NULL, loggedAt INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS rpe_logs (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, sessionId INTEGER NOT NULL, exerciseId TEXT NOT NULL, setNumber INTEGER NOT NULL, rpeValue INTEGER NOT NULL, loggedAt INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS custom_meals (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, nameAr TEXT NOT NULL, ingredientsJson TEXT NOT NULL, totalCalories INTEGER NOT NULL, totalProtein REAL NOT NULL, totalCarbs REAL NOT NULL, totalFat REAL NOT NULL, createdAt INTEGER NOT NULL)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "kinetic_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
