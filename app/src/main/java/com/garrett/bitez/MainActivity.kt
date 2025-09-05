package com.garrett.bitez

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

import dagger.hilt.android.AndroidEntryPoint

import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView

import com.garrett.bitez.ui.home.HomeFragment
import com.garrett.bitez.ui.explore.ExploreFragment
import com.garrett.bitez.ui.profile.ProfileFragment

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val TAG = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Only switch to tab if this is the first time the activity is created
        if (savedInstanceState == null) {
            switchTab(R.id.navigation_home)
        }

        val bottomNavView: BottomNavigationView = findViewById(R.id.bottom_nav_bar)

        // Set up click listeners for bottom navigation tabs
        bottomNavView.setOnItemSelectedListener(object : NavigationBarView.OnItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                return switchTab(item.itemId)
            }
        })
    }

    // Helper function to switch between fragment tabs
    private fun switchTab(itemId: Int): Boolean {
        // Figure out which tab to switch to
        when (itemId) {
            R.id.navigation_home -> {
                Log.d(TAG, "Switching to home tab")
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()
            }
            R.id.navigation_explore -> {
                Log.d(TAG, "Switching to explore tab")
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ExploreFragment())
                    .commit()
            }
            R.id.navigation_profile -> {
                Log.d(TAG, "Switching to profile tab")
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment())
                    .commit()
            }
            else -> {
                // Should never get here
                Log.w(TAG, "Clicked on bottom nav view but couldn't find tab.")
                return false
            }
        }
        return true
    }
}
