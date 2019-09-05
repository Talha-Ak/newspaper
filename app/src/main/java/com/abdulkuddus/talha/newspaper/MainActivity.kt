package com.abdulkuddus.talha.newspaper

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.abdulkuddus.talha.newspaper.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        // Show Onboarding if first time
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean(getString(R.string.pref_key_onboarding), true)) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
        }

        // Use Data Binding to setContentView while getting references to views
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setSupportActionBar(binding.toolbar)
        setupNavigation()
    }

    /**
     * Setup Navigation for the Activity
     */
    private fun setupNavigation() {
        // Find navController, set it up with Bottom Nav
        val navController = findNavController(R.id.nav_host_fragment)
        // The default animations are overridden with custom animations in the anim directory.
        // Using setupWithNavController forces default transitions to "comply" with
        // material design (which they really don't).
        // See: https://stackoverflow.com/a/56160538
        binding.bottomNav.setupWithNavController(navController)

        val appBarConfig = AppBarConfiguration.Builder(binding.bottomNav.menu).build()
        binding.toolbar.setupWithNavController(navController, appBarConfig)

        // When the destination changes, find the menuItem that it belongs to and update it.
        navController.addOnDestinationChangedListener { _, dest, _ ->
            binding.bottomNav.visibility = if (appBarConfig.topLevelDestinations.contains(dest.id)) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

    }

}
