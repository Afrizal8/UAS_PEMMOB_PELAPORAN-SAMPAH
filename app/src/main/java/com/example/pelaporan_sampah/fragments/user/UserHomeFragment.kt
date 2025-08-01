package com.example.pelaporan_sampah.fragments.user

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.pelaporan_sampah.R
import com.example.pelaporan_sampah.databinding.FragmentUserHomeBinding
import com.example.pelaporan_sampah.models.Report
import com.example.pelaporan_sampah.repository.UserRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class UserHomeFragment : Fragment() {

    private var _binding: FragmentUserHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var userRepository: UserRepository
    private lateinit var firestore: FirebaseFirestore
    private var statisticsListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userRepository = UserRepository(requireContext())
        firestore = FirebaseFirestore.getInstance()

        setupUserInfo()
        setupClickListeners()
        setupStatisticsListener()
    }

    private fun setupUserInfo() {
        val currentUser = userRepository.getCurrentUserFromSession()
        binding.tvUserName.text = currentUser?.nama ?: "User"
    }

    private fun setupClickListeners() {
        binding.cardLaporSampah.setOnClickListener {
            // Navigate to report fragment
            navigateToBottomNav(R.id.nav_report)
        }

        binding.cardRiwayat.setOnClickListener {
            // Navigate to history fragment
            navigateToBottomNav(R.id.nav_history)
        }
    }

    private fun navigateToBottomNav(itemId: Int) {
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.selectedItemId = itemId
    }

    private fun setupStatisticsListener() {
        val currentUser = userRepository.getCurrentUserFromSession()
        if (currentUser?.uid == null) {
            Log.w("UserHomeFragment", "Current user UID is null")
            setDefaultStatistics()
            return
        }

        // Setup real-time listener untuk statistik
        statisticsListener = firestore.collection("reports")
            .whereEqualTo("userId", currentUser.uid)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    Log.w("UserHomeFragment", "Listen failed.", error)
                    setDefaultStatistics()
                    return@addSnapshotListener
                }

                if (querySnapshot != null) {
                    var totalLaporan = 0
                    var laporanSelesai = 0
                    var laporanProses = 0

                    for (document in querySnapshot.documents) {
                        val report = document.toObject(Report::class.java)
                        if (report != null) {
                            totalLaporan++

                            when (report.status) {
                                "Selesai" -> laporanSelesai++
                                "Diproses", "Menunggu" -> laporanProses++
                            }
                        }
                    }

                    // Update UI
                    updateStatisticsUI(totalLaporan, laporanSelesai, laporanProses)
                    Log.d("UserHomeFragment", "Real-time statistics updated: Total=$totalLaporan, Selesai=$laporanSelesai, Proses=$laporanProses")
                } else {
                    Log.d("UserHomeFragment", "Current data: null")
                    setDefaultStatistics()
                }
            }
    }

    private fun updateStatisticsUI(total: Int, selesai: Int, proses: Int) {
        // Pastikan UI update dilakukan di main thread
        if (activity != null && _binding != null) {
            binding.tvTotalLaporan.text = total.toString()
            binding.tvLaporanSelesai.text = selesai.toString()
            binding.tvLaporanProses.text = proses.toString()
        }
    }

    private fun setDefaultStatistics() {
        updateStatisticsUI(0, 0, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove listener untuk mencegah memory leaks
        statisticsListener?.remove()
        _binding = null
    }
}