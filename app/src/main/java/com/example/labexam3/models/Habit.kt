package com.example.labexam3.models

/**
 * Data model representing a daily wellness habit
 * @param id Unique identifier for the habit
 * @param name Name of the habit (e.g., "Drink Water", "Meditate")
 * @param completedDates Set of dates (in "yyyy-MM-dd" format) when habit was completed
 */
data class Habit(
    val id: String,
    val name: String,
    val completedDates: MutableSet<String> = mutableSetOf()
)
