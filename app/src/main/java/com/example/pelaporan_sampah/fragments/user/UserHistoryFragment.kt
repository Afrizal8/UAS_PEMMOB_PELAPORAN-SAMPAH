package com.example.pelaporan_sampah.fragments.user

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pelaporan_sampah.adapters.HistoryReportAdapter
import com.example.pelaporan_sampah.databinding.FragmentUserHistoryBinding
import com.example.pelaporan_sampah.models.Report
import com.example.pelaporan_sampah.repository.ReportRepository
import com.example.pelaporan_sampah.utils.ReportDetailDialog
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class UserHistoryFragment : Fragment() {

    private var _binding: FragmentUserHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var historyAdapter: HistoryReportAdapter
    private lateinit var reportRepository: ReportRepository
    private lateinit var auth: FirebaseAuth

    companion object {
        private const val TAG = "UserHistoryFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated called")
        initializeComponents()
        setupRecyclerView()
        loadHistoryData()
    }

    private fun initializeComponents() {
        reportRepository = ReportRepository()
        auth = FirebaseAuth.getInstance()

        Log.d(TAG, "Components initialized")
        Log.d(TAG, "Current user: ${auth.currentUser?.uid}")
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryReportAdapter { report ->
            onReportItemClick(report)
        }

        binding.rvHistory.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        Log.d(TAG, "RecyclerView setup completed")
    }

    private fun onReportItemClick(report: Report) {
        Log.d(TAG, "Report item clicked: ${report.id}")
        showReportDetail(report)
    }

    private fun showReportDetail(report: Report) {
        // Show detail dialog
        ReportDetailDialog.show(requireContext(), report)
    }

    private fun loadHistoryData() {
        Log.d(TAG, "Starting to load history data")

        // Check if user is authenticated
        if (auth.currentUser == null) {
            Log.w(TAG, "User not authenticated")
            showEmptyState()
            Toast.makeText(requireContext(), "Anda harus login terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "User authenticated: ${auth.currentUser!!.uid}")
        showLoading(true)

        lifecycleScope.launch {
            try {
                Log.d(TAG, "Calling getUserReports()")
                val result = reportRepository.getUserReports()

                showLoading(false)

                if (result.isSuccess) {
                    val reports = result.getOrNull() ?: emptyList()
                    Log.d(TAG, "Reports received: ${reports.size} items")

                    reports.forEachIndexed { index, report ->
                        Log.d(TAG, "Report $index: ${report.jenisSampah} - ${report.status}")
                    }

                    if (reports.isEmpty()) {
                        Log.d(TAG, "No reports found, showing empty state")
                        showEmptyState()
                    } else {
                        Log.d(TAG, "Showing ${reports.size} reports")
                        showHistoryData(reports)
                    }
                } else {
                    val error = result.exceptionOrNull()
                    Log.e(TAG, "Failed to load reports: ${error?.message}", error)
                    showEmptyState()
                    Toast.makeText(
                        requireContext(),
                        "Gagal memuat riwayat: ${error?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in loadHistoryData: ${e.message}", e)
                showLoading(false)
                showEmptyState()
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        Log.d(TAG, "showLoading: $isLoading")
        if (isLoading) {
            binding.rvHistory.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.GONE
            // TODO: Add progress bar to layout if needed
        }
    }

    private fun showEmptyState() {
        Log.d(TAG, "Showing empty state")
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.rvHistory.visibility = View.GONE
    }

    private fun showHistoryData(reports: List<Report>) {
        Log.d(TAG, "Showing history data with ${reports.size} items")
        binding.layoutEmptyState.visibility = View.GONE
        binding.rvHistory.visibility = View.VISIBLE
        historyAdapter.submitList(reports)
    }

    // Method to refresh data (can be called from parent activity/fragment)
    fun refreshData() {
        Log.d(TAG, "Manual refresh requested")
        loadHistoryData()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called - refreshing data")
        // Refresh data when fragment becomes visible
        loadHistoryData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView called")
        _binding = null
    }
}