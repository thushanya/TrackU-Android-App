package com.example.labexam3.ui.trends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.labexam3.R
import com.example.labexam3.data.PreferencesManager
import com.example.labexam3.databinding.FragmentTrendsBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment for viewing mood trends and analytics
 */
class TrendsFragment : Fragment() {

    private var _binding: FragmentTrendsBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefsManager: PreferencesManager

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
        _binding = FragmentTrendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsManager = PreferencesManager(requireContext())
        setupChart()
        updateStats()
    }

    /**
     * Set up the mood trend chart
     */
    private fun setupChart() {
        val moods = prefsManager.getMoodEntriesForLastDays(7)

        if (moods.isEmpty()) {
            binding.moodChart.clear()
            binding.moodChart.setNoDataText("No mood data for the past week")
            return
        }

        // Group moods by day and calculate average sentiment
        val dailyMoods = moods.groupBy { it.date }
            .mapValues { (_, entries) ->
                entries.map { entry ->
                    moodEmojis.find { it.first == entry.emoji }?.second ?: 5f
                }.average().toFloat()
            }
            .toSortedMap()

        // Create chart entries
        val entries = dailyMoods.entries.mapIndexed { index, (_, score) ->
            Entry(index.toFloat(), score)
        }

        val dataSet = LineDataSet(entries, "Mood Score").apply {
            color = ContextCompat.getColor(requireContext(), R.color.ocean_blue)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.coral))
            lineWidth = 3f
            circleRadius = 6f
            setDrawValues(true)
            valueTextSize = 12f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(requireContext(), R.color.ocean_blue_light)
        }

        binding.moodChart.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false

            // Configure X-axis
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        if (index >= 0 && index < dailyMoods.size) {
                            val date = dailyMoods.keys.elementAt(index)
                            val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
                            return sdf.format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)!!)
                        }
                        return ""
                    }
                }
            }

            // Configure Y-axis
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 10f
                setDrawGridLines(true)
            }

            invalidate()
        }
    }

    /**
     * Update statistics cards
     */
    private fun updateStats() {
        val moods = prefsManager.getMoodEntriesForLastDays(7)
        
        if (moods.isEmpty()) {
            binding.tvTotalEntries.text = "0"
            binding.tvAverageMood.text = "N/A"
            binding.tvMostFrequent.text = "N/A"
            return
        }

        // Total entries
        binding.tvTotalEntries.text = moods.size.toString()

        // Average mood score
        val avgScore = moods.map { entry ->
            moodEmojis.find { it.first == entry.emoji }?.second ?: 5f
        }.average()
        binding.tvAverageMood.text = String.format("%.1f/10", avgScore)

        // Most frequent emoji
        val mostFrequent = moods.groupBy { it.emoji }
            .maxByOrNull { it.value.size }
            ?.key ?: "N/A"
        binding.tvMostFrequent.text = mostFrequent
    }

    override fun onResume() {
        super.onResume()
        setupChart()
        updateStats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
