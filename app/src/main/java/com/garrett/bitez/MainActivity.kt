package com.garrett.bitez

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.garrett.bitez.ui.theme.BitezTheme
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Only add default fragment if this is first creation
//        if (savedInstanceState == null) {
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container, Fragment())
//                .commit()
//        }

        val bottomNavView: BottomNavigationView = findViewById(R.id.bottom_nav_bar)

        // Set up click listeners for bottom navigation tabs
        bottomNavView.setOnItemSelectedListener(object : NavigationBarView.OnItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                // Figure out which tab to switch to
                when (item.itemId) {
                    R.id.navigation_home -> {
                        println("Clicked on Home")
                    }
                    R.id.navigation_search -> {
                        println("Clicked on search")
                    }
                    R.id.navigation_profile -> {
                        println("Clicked on profile")
                    }
                    else -> {
                        // Should never get here
                        println("Clicked on bottom nav view but couldn't find tab.")
                        return false
                    }
                }
                return true
            }
        })
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BitezTheme {
        Greeting("Android")
    }
}