package com.example.labexam3.models

/**
 * Data model representing a mood journal entry
 * @param id Unique identifier for the entry
 * @param emoji Selected emoji representing the mood
 * @param timestamp Timestamp in milliseconds when entry was created
 * @param date Date string in "yyyy-MM-dd" format for easy grouping
 * @param note Optional note about the mood (can be added in future)
 */
data class MoodEntry(
    val id: String,
    val emoji: String,
    val timestamp: Long,
    val date: String,
    val note: String = ""
)
