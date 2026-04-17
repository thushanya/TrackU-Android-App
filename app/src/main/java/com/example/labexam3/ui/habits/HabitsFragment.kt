package com.example.labexam3.ui.habits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.labexam3.data.PreferencesManager
import com.example.labexam3.databinding.DialogAddHabitBinding
import com.example.labexam3.databinding.FragmentHabitsBinding
import com.example.labexam3.models.Habit
import java.util.*

/**
 * Fragment for managing daily wellness habits
 * Allows users to add, complete, and delete habits
 */
class HabitsFragment : Fragment() {

    private var _binding: FragmentHabitsBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefsManager: PreferencesManager
    private lateinit var habitAdapter: HabitAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsManager = PreferencesManager(requireContext())
        setupRecyclerView()
        setupClickListeners()
        loadHabits()
    }

    /**
     * Set up the RecyclerView with adapter
     */
    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter(
            onHabitChecked = { habit, isChecked ->
                handleHabitToggle(habit)
            },
            onHabitDelete = { habit ->
                showDeleteConfirmation(habit)
            }
        )
        binding.rvHabits.adapter = habitAdapter
    }

    /**
     * Set up click listeners for UI elements
     */
    private fun setupClickListeners() {
        binding.fabAddHabit.setOnClickListener {
            showAddHabitDialog()
        }
    }

    /**
     * Load habits from SharedPreferences and update UI
     */
    private fun loadHabits() {
        val habits = prefsManager.getHabits()
        habitAdapter.submitList(habits)

        // Update empty state visibility
        binding.layoutEmpty.isVisible = habits.isEmpty()
        binding.rvHabits.isVisible = habits.isNotEmpty()

        // Update progress
        updateProgress()
    }

    /**
     * Update today's completion percentage
     */
    private fun updateProgress() {
        val percentage = prefsManager.getTodayCompletionPercentage()
        binding.tvCompletionPercentage.text = "$percentage%"
        binding.progressBar.progress = percentage
    }

    /**
     * Handle habit completion toggle
     */
    private fun handleHabitToggle(habit: Habit) {
        prefsManager.toggleHabitCompletion(habit.id)
        updateProgress()
    }

    /**
     * Show dialog to add a new habit
     */
    private fun showAddHabitDialog() {
        val dialogBinding = DialogAddHabitBinding.inflate(layoutInflater)

        AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { _, _ ->
                val habitName = dialogBinding.etHabitName.text.toString().trim()
                if (habitName.isNotEmpty()) {
                    addHabit(habitName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Add a new habit to the list
     */
    private fun addHabit(name: String) {
        val habit = Habit(
            id = UUID.randomUUID().toString(),
            name = name
        )
        prefsManager.addHabit(habit)
        loadHabits()
    }

    /**
     * Show confirmation dialog before deleting a habit
     */
    private fun showDeleteConfirmation(habit: Habit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete \"${habit.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                deleteHabit(habit)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Delete a habit from the list
     */
    private fun deleteHabit(habit: Habit) {
        prefsManager.deleteHabit(habit.id)
        loadHabits()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
