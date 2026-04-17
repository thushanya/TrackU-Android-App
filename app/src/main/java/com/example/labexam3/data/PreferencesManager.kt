package com.example.labexam3.data

import android.content.Context
import android.content.SharedPreferences
import com.example.labexam3.models.Habit
import com.example.labexam3.models.HydrationReminder
import com.example.labexam3.models.MoodEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manager class for handling all SharedPreferences operations
 * Stores habits, mood entries, and app settings
 */
class PreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val PREFS_NAME = "wellness_app_prefs"
        private const val KEY_HABITS = "habits"
        private const val KEY_MOODS = "moods"
        private const val KEY_REMINDERS = "reminders"
        
        // Default reminder times: 8AM, 10:30AM, 1PM, 4:30PM, 7PM
        val DEFAULT_REMINDERS = listOf(
            HydrationReminder(8, 0, true),
            HydrationReminder(10, 30, true),
            HydrationReminder(13, 0, true),
            HydrationReminder(16, 30, true),
            HydrationReminder(19, 0, true)
        )
    }
    
    // ==================== Habit Management ====================
    
    /**
     * Get all saved habits
     */
    fun getHabits(): MutableList<Habit> {
        val json = prefs.getString(KEY_HABITS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Habit>>() {}.type
        return gson.fromJson(json, type)
    }
    
    /**
     * Save habits list
     */
    fun saveHabits(habits: List<Habit>) {
        val json = gson.toJson(habits)
        prefs.edit().putString(KEY_HABITS, json).apply()
    }
    
    /**
     * Add a new habit
     */
    fun addHabit(habit: Habit) {
        val habits = getHabits()
        habits.add(habit)
        saveHabits(habits)
    }
    
    /**
     * Update an existing habit
     */
    fun updateHabit(updatedHabit: Habit) {
        val habits = getHabits()
        val index = habits.indexOfFirst { it.id == updatedHabit.id }
        if (index != -1) {
            habits[index] = updatedHabit
            saveHabits(habits)
        }
    }
    
    /**
     * Delete a habit by ID
     */
    fun deleteHabit(habitId: String) {
        val habits = getHabits()
        habits.removeAll { it.id == habitId }
        saveHabits(habits)
    }
    
    /**
     * Toggle habit completion for today
     */
    fun toggleHabitCompletion(habitId: String, date: String = getTodayDate()): Boolean {
        val habits = getHabits()
        val habit = habits.find { it.id == habitId } ?: return false
        
        val isCompleted = if (habit.completedDates.contains(date)) {
            habit.completedDates.remove(date)
            false
        } else {
            habit.completedDates.add(date)
            true
        }
        
        saveHabits(habits)
        return isCompleted
    }
    
    /**
     * Get today's habit completion percentage
     */
    fun getTodayCompletionPercentage(): Int {
        val habits = getHabits()
        if (habits.isEmpty()) return 0
        
        val today = getTodayDate()
        val completedCount = habits.count { it.completedDates.contains(today) }
        return (completedCount * 100) / habits.size
    }
    
    // ==================== Mood Management ====================
    
    /**
     * Get all mood entries
     */
    fun getMoodEntries(): MutableList<MoodEntry> {
        val json = prefs.getString(KEY_MOODS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<MoodEntry>>() {}.type
        return gson.fromJson(json, type)
    }
    
    /**
     * Save mood entries list
     */
    fun saveMoodEntries(moods: List<MoodEntry>) {
        val json = gson.toJson(moods)
        prefs.edit().putString(KEY_MOODS, json).apply()
    }
    
    /**
     * Add a new mood entry
     */
    fun addMoodEntry(entry: MoodEntry) {
        val moods = getMoodEntries()
        moods.add(0, entry) // Add to beginning for chronological order
        saveMoodEntries(moods)
    }
    
    /**
     * Get mood entries for the last N days
     */
    fun getMoodEntriesForLastDays(days: Int): List<MoodEntry> {
        val allMoods = getMoodEntries()
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return allMoods.filter { it.timestamp >= cutoffTime }
    }
    
    // ==================== Hydration Reminders ====================
    
    /**
     * Get hydration reminder settings
     */
    fun getHydrationReminders(): List<HydrationReminder> {
        val json = prefs.getString(KEY_REMINDERS, null)
        if (json == null) {
            // First time - return defaults
            saveHydrationReminders(DEFAULT_REMINDERS)
            return DEFAULT_REMINDERS
        }
        val type = object : TypeToken<List<HydrationReminder>>() {}.type
        return gson.fromJson(json, type)
    }
    
    /**
     * Save hydration reminder settings
     */
    fun saveHydrationReminders(reminders: List<HydrationReminder>) {
        val json = gson.toJson(reminders)
        prefs.edit().putString(KEY_REMINDERS, json).apply()
    }
    
    /**
     * Update a specific reminder's enabled state
     */
    fun updateReminderEnabled(hour: Int, minute: Int, enabled: Boolean) {
        val reminders = getHydrationReminders().toMutableList()
        val index = reminders.indexOfFirst { it.hour == hour && it.minute == minute }
        if (index != -1) {
            reminders[index] = reminders[index].copy(enabled = enabled)
            saveHydrationReminders(reminders)
        }
    }
    
    // ==================== Utility Methods ====================
    
    /**
     * Get today's date in yyyy-MM-dd format
     */
    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
    
    /**
     * Clear all data (for testing purposes)
     */
    fun clearAllData() {
        prefs.edit().clear().apply()
    }
}
