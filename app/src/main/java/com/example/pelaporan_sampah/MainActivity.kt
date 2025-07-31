package com.example.pelaporan_sampah

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.LinearLayout
import android.widget.FrameLayout
import com.example.pelaporan_sampah.ui.home.HomeFragment
import com.example.pelaporan_sampah.ui.report.ReportFragment
import com.example.pelaporan_sampah.ui.history.HistoryFragment
import com.example.pelaporan_sampah.ui.profile.ProfileFragment
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding


class MainActivity : AppCompatActivity() {

    // Fragment instances
    private lateinit var homeFragment: HomeFragment
    private lateinit var reportFragment: ReportFragment
    private lateinit var historyFragment: HistoryFragment
    private lateinit var profileFragment: ProfileFragment

    // Bottom navigation views
    private lateinit var navHome: LinearLayout
    private lateinit var navLapor: LinearLayout
    private lateinit var navRiwayat: LinearLayout
    private lateinit var navProfil: LinearLayout

    // Navigation icons and text
    private lateinit var iconHome: ImageView
    private lateinit var iconLapor: ImageView
    private lateinit var iconRiwayat: ImageView
    private lateinit var iconProfil: ImageView

    private lateinit var textHome: TextView
    private lateinit var textLapor: TextView
    private lateinit var textRiwayat: TextView
    private lateinit var textProfil: TextView

    // Layout containers
    private lateinit var bottomNavContainer: LinearLayout
    private lateinit var fragmentContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        changeStatusBarColor()

        // Setup edge-to-edge display
        setupEdgeToEdge()

        // Initialize fragments
        initializeFragments()

        // Initialize views
        initializeViews()

        // Setup system bars handling
        setupSystemBarsHandling()

        // Set up navigation
        setupBottomNavigation()

        // Load default fragment (Home)
        if (savedInstanceState == null) {
            loadFragment(homeFragment)
            setActiveNavigation(0) // Home is index 0
        }
    }
    private fun changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.green_primary)
        }
    }


    private fun setupEdgeToEdge() {
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Make navigation bar transparent
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        window.statusBarColor = android.graphics.Color.TRANSPARENT
    }

    private fun setupSystemBarsHandling() {
        // Get components based on the XML structure
        fragmentContainer = findViewById(R.id.fragment_container)
        bottomNavContainer = findViewById(R.id.bottom_navigation_container)

        // Handle status bar for the main content
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView.rootView) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Apply top padding for status bar to fragment container
            fragmentContainer.updatePadding(top = insets.top)

            // Apply bottom padding for navigation bar to bottom navigation
            bottomNavContainer.updatePadding(bottom = insets.bottom)

            windowInsets
        }
    }

    private fun initializeFragments() {
        homeFragment = HomeFragment()
        reportFragment = ReportFragment()
        historyFragment = HistoryFragment()
        profileFragment = ProfileFragment()
    }

    private fun initializeViews() {
        // Bottom navigation containers
        navHome = findViewById(R.id.navHome)
        navLapor = findViewById(R.id.navLapor)
        navRiwayat = findViewById(R.id.navRiwayat)
        navProfil = findViewById(R.id.navProfil)

        // Navigation icons
        iconHome = navHome.findViewById(R.id.iconHome)
        iconLapor = navLapor.findViewById(R.id.iconLapor)
        iconRiwayat = navRiwayat.findViewById(R.id.iconRiwayat)
        iconProfil = navProfil.findViewById(R.id.iconProfil)

        // Navigation texts
        textHome = navHome.findViewById(R.id.textHome)
        textLapor = navLapor.findViewById(R.id.textLapor)
        textRiwayat = navRiwayat.findViewById(R.id.textRiwayat)
        textProfil = navProfil.findViewById(R.id.textProfil)
    }

    private fun setupBottomNavigation() {
        navHome.setOnClickListener {
            loadFragment(homeFragment)
            setActiveNavigation(0)
        }

        navLapor.setOnClickListener {
            loadFragment(reportFragment)
            setActiveNavigation(1)
        }

        navRiwayat.setOnClickListener {
            loadFragment(historyFragment)
            setActiveNavigation(2)
        }

        navProfil.setOnClickListener {
            loadFragment(profileFragment)
            setActiveNavigation(3)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun setActiveNavigation(activeIndex: Int) {
        // Reset all navigation items to inactive state
        resetAllNavigation()

        // Set active navigation item
        when (activeIndex) {
            0 -> {
                iconHome.setColorFilter(ContextCompat.getColor(this, R.color.green_primary))
                textHome.setTextColor(ContextCompat.getColor(this, R.color.green_primary))
            }

            1 -> {
                iconLapor.setColorFilter(ContextCompat.getColor(this, R.color.green_primary))
                textLapor.setTextColor(ContextCompat.getColor(this, R.color.green_primary))
            }

            2 -> {
                iconRiwayat.setColorFilter(ContextCompat.getColor(this, R.color.green_primary))
                textRiwayat.setTextColor(ContextCompat.getColor(this, R.color.green_primary))
            }

            3 -> {
                iconProfil.setColorFilter(ContextCompat.getColor(this, R.color.green_primary))
                textProfil.setTextColor(ContextCompat.getColor(this, R.color.green_primary))
            }
        }
    }

    private fun resetAllNavigation() {
        val inactiveColor = ContextCompat.getColor(this, R.color.text_gray)

        iconHome.setColorFilter(inactiveColor)
        iconLapor.setColorFilter(inactiveColor)
        iconRiwayat.setColorFilter(inactiveColor)
        iconProfil.setColorFilter(inactiveColor)

        textHome.setTextColor(inactiveColor)
        textLapor.setTextColor(inactiveColor)
        textRiwayat.setTextColor(inactiveColor)
        textProfil.setTextColor(inactiveColor)
    }

    // Method untuk mendapatkan fragment yang sedang aktif
    fun getCurrentFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(R.id.fragment_container)
    }

    // Method untuk navigasi dari fragment lain
    fun navigateToFragment(fragmentIndex: Int) {
        when (fragmentIndex) {
            0 -> {
                loadFragment(homeFragment)
                setActiveNavigation(0)
            }
            1 -> {
                loadFragment(reportFragment)
                setActiveNavigation(1)
            }
            2 -> {
                loadFragment(historyFragment)
                setActiveNavigation(2)
            }
            3 -> {
                loadFragment(profileFragment)
                setActiveNavigation(3)
            }
        }
    }

    // Method untuk menangani perubahan orientasi atau konfigurasi
    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        // Re-apply system bars handling after configuration change
        setupSystemBarsHandling()
    }
}