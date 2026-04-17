package com.example.labexam3.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.example.labexam3.data.PreferencesManager
import com.example.labexam3.databinding.FragmentSettingsBinding
import com.example.labexam3.utils.ReminderScheduler

/**
 * Fragment for app settings and hydration reminder configuration
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefsManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsManager = PreferencesManager(requireContext())
        setupReminderSwitches()
        setupClickListeners()
    }

    /**
     * Set up switches for each hydration reminder time
     */
    private fun setupReminderSwitches() {
        val reminders = prefsManager.getHydrationReminders()

        binding.layoutReminders.removeAllViews()

        reminders.forEach { reminder ->
            val switchView = SwitchCompat(requireContext()).apply {
                text = reminder.getDisplayTime()
                textSize = 16f
                isChecked = reminder.enabled
                setPadding(0, 24, 0, 24)

                setOnCheckedChangeListener { _, isChecked ->
                    updateReminder(reminder.hour, reminder.minute, isChecked)
                }
            }

            binding.layoutReminders.addView(
                switchView,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }
    }

    /**
     * Update a reminder's enabled state
     */
    private fun updateReminder(hour: Int, minute: Int, enabled: Boolean) {
        prefsManager.updateReminderEnabled(hour, minute, enabled)

        // Reschedule all reminders
        val reminders = prefsManager.getHydrationReminders()
        ReminderScheduler.scheduleReminders(requireContext(), reminders)
    }

    /**
     * Set up click listeners for buttons
     */
    private fun setupClickListeners() {
        binding.btnClearData.setOnClickListener {
            showClearDataConfirmation()
        }
    }

    /**
     * Show confirmation dialog before clearing all data
     */
    private fun showClearDataConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear All Data")
            .setMessage("Are you sure you want to delete all habits, mood entries, and settings? This action cannot be undone.")
            .setPositiveButton("Clear") { _, _ ->
                clearAllData()
            }
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    /**
     * Clear all app data
     */
    private fun clearAllData() {
        prefsManager.clearAllData()
        ReminderScheduler.cancelAllReminders(requireContext())

        // Reload reminder switches with defaults
        setupReminderSwitches()

        // Show confirmation
        AlertDialog.Builder(requireContext())
            .setTitle("Data Cleared")
            .setMessage("All data has been successfully deleted.")
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
