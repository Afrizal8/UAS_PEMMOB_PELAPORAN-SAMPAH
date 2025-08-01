package com.example.pelaporan_sampah.fragments.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.pelaporan_sampah.R
import com.example.pelaporan_sampah.databinding.FragmentAdminDashboardBinding
import com.example.pelaporan_sampah.repository.UserRepository
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminDashboardFragment : Fragment() {
    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userRepository = UserRepository(requireContext())

        val currentUser = userRepository.getCurrentUserFromSession()
        binding.tvAdminName.text = currentUser?.nama ?: "Administrator"

        binding.cardKelolaLaporan.setOnClickListener {
            val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav?.selectedItemId = R.id.nav_reports
        }

        binding.cardKelolaUser.setOnClickListener {
            val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav?.selectedItemId = R.id.nav_users
        }

        binding.cardPengaturan.setOnClickListener {
            val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav?.selectedItemId = R.id.nav_settings
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}