package com.example.labexam3.ui.habits

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.labexam3.databinding.ItemHabitBinding
import com.example.labexam3.models.Habit
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for displaying habits in a RecyclerView
 */
class HabitAdapter(
    private val onHabitChecked: (Habit, Boolean) -> Unit,
    private val onHabitDelete: (Habit) -> Unit
) : ListAdapter<Habit, HabitAdapter.HabitViewHolder>(HabitDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HabitViewHolder(
        private val binding: ItemHabitBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(habit: Habit) {
            val today = getTodayDate()
            val isCompleted = habit.completedDates.contains(today)

            binding.cbHabit.text = habit.name
            binding.cbHabit.isChecked = isCompleted

            // Handle checkbox toggle
            binding.cbHabit.setOnCheckedChangeListener { _, isChecked ->
                onHabitChecked(habit, isChecked)
            }

            // Handle delete button
            binding.btnDelete.setOnClickListener {
                onHabitDelete(habit)
            }
        }

        private fun getTodayDate(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }
    }

    class HabitDiffCallback : DiffUtil.ItemCallback<Habit>() {
        override fun areItemsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem == newItem
        }
    }
}
