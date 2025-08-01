package com.example.pelaporan_sampah

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.example.pelaporan_sampah.databinding.ActivityMainBinding
import com.example.pelaporan_sampah.models.UserRole
import com.example.pelaporan_sampah.repository.UserRepository
import com.example.pelaporan_sampah.fragments.user.*
import com.example.pelaporan_sampah.fragments.petugas.*
import com.example.pelaporan_sampah.ui.login.LoginActivity
import com.example.pelaporan_sampah.fragments.admin.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var userRepository: UserRepository
    private lateinit var currentUserRole: UserRole

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide action bar sepenuhnya
        supportActionBar?.hide()
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle back press dengan cara baru
        onBackPressedDispatcher.addCallback(this) {
            // Jangan allow back ke login screen
            moveTaskToBack(true)
        }

        userRepository = UserRepository(this)

        // Cek apakah user sudah login
        if (!userRepository.isUserLoggedIn()) {
            navigateToLogin()
            return
        }

        setupRoleBasedNavigation()
        setupBottomNavigation()

        // Load fragment pertama
        loadInitialFragment()
    }

    private fun setupRoleBasedNavigation() {
        currentUserRole = userRepository.getUserRole()

        // Set menu berdasarkan role
        val menuRes = when (currentUserRole) {
            UserRole.ADMIN -> R.menu.bottom_nav_admin
            UserRole.PETUGAS -> R.menu.bottom_nav_petugas
            UserRole.USER -> R.menu.bottom_nav_user
        }

        binding.bottomNavigation.menu.clear()
        binding.bottomNavigation.inflateMenu(menuRes)

        // Show welcome message
        val user = userRepository.getCurrentUserFromSession()
        Toast.makeText(this, "Selamat datang, ${user?.nama} (${currentUserRole.displayName})",
            Toast.LENGTH_SHORT).show()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (currentUserRole) {
                UserRole.USER -> handleUserNavigation(item.itemId)
                UserRole.PETUGAS -> handlePetugasNavigation(item.itemId)
                UserRole.ADMIN -> handleAdminNavigation(item.itemId)
            }
        }
    }

    private fun handleUserNavigation(itemId: Int): Boolean {
        val fragment = when (itemId) {
            R.id.nav_home -> UserHomeFragment()
            R.id.nav_report -> UserReportFragment()
            R.id.nav_history -> UserHistoryFragment()
            R.id.nav_profile -> UserProfileFragment()
            else -> return false
        }
        loadFragment(fragment)
        return true
    }

    private fun handlePetugasNavigation(itemId: Int): Boolean {
        val fragment = when (itemId) {
            R.id.nav_home -> PetugasHomeFragment()
            R.id.nav_reports -> PetugasReportsFragment()
            R.id.nav_tasks -> PetugasTasksFragment()
            R.id.nav_profile -> PetugasProfileFragment()
            else -> return false
        }
        loadFragment(fragment)
        return true
    }

    private fun handleAdminNavigation(itemId: Int): Boolean {
        val fragment = when (itemId) {
            R.id.nav_dashboard -> AdminDashboardFragment()
            R.id.nav_reports -> AdminReportsFragment()
            R.id.nav_users -> AdminUsersFragment()
            R.id.nav_settings -> AdminSettingsFragment()
            else -> return false
        }
        loadFragment(fragment)
        return true
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }

    private fun loadInitialFragment() {
        // Load fragment pertama berdasarkan role
        val initialFragment = when (currentUserRole) {
            UserRole.USER -> UserHomeFragment()
            UserRole.PETUGAS -> PetugasHomeFragment()
            UserRole.ADMIN -> AdminDashboardFragment()
        }
        loadFragment(initialFragment)

        // Set selected item pertama
        if (binding.bottomNavigation.menu.size() > 0) {
            binding.bottomNavigation.selectedItemId = binding.bottomNavigation.menu.getItem(0).itemId
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}