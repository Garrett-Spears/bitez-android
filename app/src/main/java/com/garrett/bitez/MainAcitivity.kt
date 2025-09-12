package com.garrett.bitez

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get the fragment container and root nav controller attached to it
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            as NavHostFragment
        navController = navHostFragment.navController

        // Setup the bottom navigation view with navController
        val bottomNavigationView: BottomNavigationView =
            findViewById<BottomNavigationView>(R.id.bottom_nav_bar)
        bottomNavigationView.setupWithNavController(navController)

//        // Setup the ActionBar with navController and 3 top level destinations for the ta
//        appBarConfiguration = AppBarConfiguration(
//            setOf(R.id.home_screen, R.id.explore_screen,  R.id.profile_screen)
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    // Navigates to last fragment on tab's stack, and exits app if on root fragment of tab
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}