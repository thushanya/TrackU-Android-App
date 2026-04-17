package com.example.labexam3.ui.mood

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.labexam3.databinding.ItemMoodBinding
import com.example.labexam3.models.MoodEntry
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for displaying mood entries in a RecyclerView
 */
class MoodAdapter : ListAdapter<MoodEntry, MoodAdapter.MoodViewHolder>(MoodDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val binding = ItemMoodBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MoodViewHolder(
        private val binding: ItemMoodBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: MoodEntry) {
            binding.tvEmoji.text = entry.emoji
            binding.tvDate.text = formatRelativeTime(entry.timestamp)
            binding.tvTimestamp.text = formatFullDate(entry.timestamp)
        }

        private fun formatRelativeTime(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            // Get calendar instances for date comparison
            val nowCal = Calendar.getInstance()
            val entryCal = Calendar.getInstance().apply { timeInMillis = timestamp }
            
            // Check if same day
            val isSameDay = nowCal.get(Calendar.YEAR) == entryCal.get(Calendar.YEAR) &&
                           nowCal.get(Calendar.DAY_OF_YEAR) == entryCal.get(Calendar.DAY_OF_YEAR)
            
            // Check if yesterday
            val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            val isYesterday = yesterdayCal.get(Calendar.YEAR) == entryCal.get(Calendar.YEAR) &&
                             yesterdayCal.get(Calendar.DAY_OF_YEAR) == entryCal.get(Calendar.DAY_OF_YEAR)

            return when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000} minutes ago"
                isSameDay -> {
                    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                    "Today, ${sdf.format(Date(timestamp))}"
                }
                isYesterday -> {
                    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                    "Yesterday, ${sdf.format(Date(timestamp))}"
                }
                else -> {
                    val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                    sdf.format(Date(timestamp))
                }
            }
        }

        private fun formatFullDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    class MoodDiffCallback : DiffUtil.ItemCallback<MoodEntry>() {
        override fun areItemsTheSame(oldItem: MoodEntry, newItem: MoodEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MoodEntry, newItem: MoodEntry): Boolean {
            return oldItem == newItem
        }
    }
}
