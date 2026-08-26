package com.tarbiyah.ailearn

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.tarbiyah.ailearn.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupFab()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        // Show/hide FAB based on current destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> {
                    // FAB shown only during prayer/study time (simulated here)
                    binding.fabCheckIn.visibility = View.VISIBLE
                }
                else -> {
                    binding.fabCheckIn.visibility = View.GONE
                }
            }
        }
    }

    private fun setupFab() {
        binding.fabCheckIn.setOnClickListener {
            // TODO: Implement check-in logic
            // For now show a simple toast
            android.widget.Toast.makeText(
                this,
                "Check-In berhasil! Jazakallah khairan.",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
}
