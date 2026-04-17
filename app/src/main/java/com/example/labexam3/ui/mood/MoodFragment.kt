package com.example.labexam3.ui.mood

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.labexam3.R
import com.example.labexam3.data.PreferencesManager
import com.example.labexam3.databinding.FragmentMoodBinding
import com.example.labexam3.models.MoodEntry
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment for logging mood entries and viewing mood trends
 */
class MoodFragment : Fragment() {

    private var _binding: FragmentMoodBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefsManager: PreferencesManager
    private lateinit var moodAdapter: MoodAdapter

    private var selectedEmoji: String? = null

    // Available mood emojis with sentiment scores (1-10)
    private val moodEmojis = listOf(
        "😢" to 2f,  // Very sad
        "😟" to 4f,  // Sad
        "😐" to 5f,  // Neutral
        "🙂" to 7f,  // Happy
        "😊" to 8f,  // Very happy
        "😍" to 9f,  // Excited
        "😎" to 8f,  // Cool
        "😴" to 5f,  // Tired
        "😰" to 3f,  // Anxious
        "🤗" to 9f   // Grateful
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsManager = PreferencesManager(requireContext())
        setupEmojiSelector()
        setupRecyclerView()
        setupClickListeners()
        loadMoods()
    }

    override fun onResume() {
        super.onResume()
        // Refresh mood list to update relative times (Today/Yesterday)
        loadMoods()
    }

    /**
     * Set up emoji selector grid
     */
    private fun setupEmojiSelector() {
        moodEmojis.forEach { (emoji, _) ->
            val emojiView = TextView(requireContext()).apply {
                text = emoji
                textSize = 32f
                setPadding(16, 16, 16, 16)
                setOnClickListener {
                    selectEmoji(emoji)
                }
            }
            binding.gridEmojis.addView(emojiView)
        }
    }

    /**
     * Handle emoji selection
     */
    private fun selectEmoji(emoji: String) {
        selectedEmoji = emoji

        // Visual feedback - highlight selected emoji
        for (i in 0 until binding.gridEmojis.childCount) {
            val child = binding.gridEmojis.getChildAt(i) as TextView
            child.setBackgroundColor(
                if (child.text == emoji) {
                    ContextCompat.getColor(requireContext(), R.color.ocean_blue_light)
                } else {
                    Color.TRANSPARENT
                }
            )
        }

        binding.btnLogMood.isEnabled = true
    }

    /**
     * Set up RecyclerView for mood history
     */
    private fun setupRecyclerView() {
        moodAdapter = MoodAdapter()
        binding.rvMoods.adapter = moodAdapter
    }

    /**
     * Set up click listeners
     */
    private fun setupClickListeners() {
        binding.btnLogMood.setOnClickListener {
            logMood()
        }

        binding.btnShare.setOnClickListener {
            shareMoodSummary()
        }
    }

    /**
     * Log the selected mood
     */
    private fun logMood() {
        val emoji = selectedEmoji ?: return

        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.format(Date(now))

        val entry = MoodEntry(
            id = UUID.randomUUID().toString(),
            emoji = emoji,
            timestamp = now,
            date = date
        )

        prefsManager.addMoodEntry(entry)
        loadMoods()

        // Reset selection
        selectedEmoji = null
        binding.btnLogMood.isEnabled = false
        for (i in 0 until binding.gridEmojis.childCount) {
            val child = binding.gridEmojis.getChildAt(i) as TextView
            child.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    /**
     * Load mood entries and update UI
     */
    private fun loadMoods() {
        val moods = prefsManager.getMoodEntries()
        // Submit null first to force adapter to refresh and recalculate relative times
        moodAdapter.submitList(null)
        moodAdapter.submitList(moods)

        binding.layoutEmpty.isVisible = moods.isEmpty()
        binding.rvMoods.isVisible = moods.isNotEmpty()
    }

    /**
     * Share mood summary using implicit intent
     */
    private fun shareMoodSummary() {
        val moods = prefsManager.getMoodEntriesForLastDays(7)

        if (moods.isEmpty()) {
            return
        }

        val summary = buildString {
            appendLine("📊 My Weekly Mood Summary")
            appendLine()
            appendLine("Total entries: ${moods.size}")
            appendLine()
            appendLine("Recent moods:")
            moods.take(5).forEach { entry ->
                val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                appendLine("${entry.emoji} - ${sdf.format(Date(entry.timestamp))}")
            }
            appendLine()
            appendLine("Shared from Wellness App")
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "My Mood Summary")
            putExtra(Intent.EXTRA_TEXT, summary)
        }

        startActivity(Intent.createChooser(shareIntent, "Share mood summary via"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
