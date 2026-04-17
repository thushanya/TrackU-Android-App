package com.example.labexam3

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.labexam3.data.PreferencesManager
import com.example.labexam3.databinding.ActivityMainBinding
import com.example.labexam3.utils.ReminderScheduler

/**
 * Main Activity hosting the navigation graph and bottom navigation
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupNavigation()
        requestNotificationPermission()
        scheduleHydrationReminders()
    }
    
    /**                                                   
     * Set up Navigation Component with BottomNavigationView
     */
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        // Connect bottom navigation with navigation controller
        binding.bottomNavigation.setupWithNavController(navController)
    }
    
    /**
     * Request notification permission for Android 13+
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }
    
    /**
     * Schedule hydration reminders on app start
     */
    private fun scheduleHydrationReminders() {
        val prefsManager = PreferencesManager(this)
        val reminders = prefsManager.getHydrationReminders()
        ReminderScheduler.scheduleReminders(this, reminders)
    }
}