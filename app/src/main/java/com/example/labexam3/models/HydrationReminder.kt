package com.example.labexam3.models

/**
 * Data model representing a hydration reminder time slot
 * @param hour Hour of the day (0-23)
 * @param minute Minute of the hour (0-59)
 * @param enabled Whether this reminder is active
 */
data class HydrationReminder(
    val hour: Int,
    val minute: Int,
    val enabled: Boolean = true
) {
    /**
     * Returns a display-friendly time string (e.g., "8:00 AM")
     */
    fun getDisplayTime(): String {
        val period = if (hour < 12) "AM" else "PM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return String.format("%d:%02d %s", displayHour, minute, period)
    }
}
