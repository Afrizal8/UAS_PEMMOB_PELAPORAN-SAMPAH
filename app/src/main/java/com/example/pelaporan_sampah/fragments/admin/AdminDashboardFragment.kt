package com.example.pelaporan_sampah.fragments.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.pelaporan_sampah.R
import com.example.pelaporan_sampah.databinding.FragmentAdminDashboardBinding
import com.example.pelaporan_sampah.repository.UserRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class AdminDashboardFragment : Fragment() {
    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!
    private var userRepository: UserRepository? = null
    private var firestore: FirebaseFirestore? = null

    // Data class untuk Recent Activity
    data class RecentActivityItem(
        val id: String,
        val timestamp: Date,
        val jenisSampah: String,
        val deskripsi: String,
        val alamat: String,
        val status: String
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            initializeComponents()
            setupUI()
            setupClickListeners()
        } catch (e: Exception) {
            Log.e("AdminDashboard", "Error in onViewCreated: ", e)
        }
    }

    private fun initializeComponents() {
        try {
            userRepository = UserRepository(requireContext())
            firestore = FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("AdminDashboard", "Error initializing components: ", e)
        }
    }

    private fun setupUI() {
        _binding?.let { binding ->
            try {
                val currentUser = userRepository?.getCurrentUserFromSession()
                binding.tvAdminName.text = currentUser?.nama ?: "Administrator"

                // Load data setelah UI setup
                loadStatistics()

            } catch (e: Exception) {
                Log.e("AdminDashboard", "Error setting up UI: ", e)
            }
        }
    }

    private fun setupClickListeners() {
        _binding?.let { binding ->
            try {
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
            } catch (e: Exception) {
                Log.e("AdminDashboard", "Error setting up click listeners: ", e)
            }
        }
    }

    private fun loadStatistics() {
        // Pastikan fragment masih terpasang dan binding tidak null
        if (!isAdded || _binding == null || firestore == null) {
            Log.w("AdminDashboard", "Fragment not ready for loading statistics")
            return
        }

        try {
            // Load total users
            firestore?.collection("users")
                ?.get()
                ?.addOnSuccessListener { documents ->
                    if (isAdded && _binding != null) {
                        try {
                            val totalUsers = documents.size()
                            _binding?.tvTotalUsers?.text = totalUsers.toString()
                            Log.d("AdminDashboard", "Total users: $totalUsers")
                        } catch (e: Exception) {
                            Log.e("AdminDashboard", "Error updating total users UI: ", e)
                        }
                    }
                }
                ?.addOnFailureListener { exception ->
                    if (isAdded && _binding != null) {
                        Log.e("AdminDashboard", "Error getting users: ", exception)
                        _binding?.tvTotalUsers?.text = "0"
                    }
                }

            // Load reports statistics
            firestore?.collection("reports")
                ?.get()
                ?.addOnSuccessListener { documents ->
                    if (isAdded && _binding != null) {
                        try {
                            val totalReports = documents.size()
                            var pendingReports = 0
                            var completedReports = 0

                            for (document in documents) {
                                when (document.getString("status")) {
                                    "Menunggu" -> pendingReports++
                                    "Selesai" -> completedReports++
                                }
                            }

                            _binding?.tvTotalReports?.text = totalReports.toString()
                            _binding?.tvPendingReports?.text = pendingReports.toString()
                            _binding?.tvCompletedReports?.text = completedReports.toString()

                            Log.d("AdminDashboard", "Reports loaded - Total: $totalReports, Pending: $pendingReports, Completed: $completedReports")

                            // Load recent activity setelah reports berhasil dimuat
                            loadRecentActivity()

                        } catch (e: Exception) {
                            Log.e("AdminDashboard", "Error updating reports UI: ", e)
                        }
                    }
                }
                ?.addOnFailureListener { exception ->
                    if (isAdded && _binding != null) {
                        Log.e("AdminDashboard", "Error getting reports: ", exception)
                        _binding?.tvTotalReports?.text = "0"
                        _binding?.tvPendingReports?.text = "0"
                        _binding?.tvCompletedReports?.text = "0"
                    }
                }
        } catch (e: Exception) {
            Log.e("AdminDashboard", "Error in loadStatistics: ", e)
        }
    }

    private fun loadRecentActivity() {
        if (!isAdded || _binding == null || firestore == null) {
            Log.w("AdminDashboard", "Fragment not ready for loading recent activity")
            return
        }

        try {
            firestore?.collection("reports")
                ?.orderBy("timestamp", Query.Direction.DESCENDING)
                ?.limit(5)
                ?.get()
                ?.addOnSuccessListener { documents ->
                    if (isAdded && _binding != null) {
                        try {
                            val activities = mutableListOf<RecentActivityItem>()

                            for (document in documents) {
                                try {
                                    val timestamp = document.getTimestamp("timestamp")
                                    val status = document.getString("status") ?: "Unknown"
                                    val jenisSampah = document.getString("jenisSampah") ?: "Unknown"
                                    val deskripsi = document.getString("deskripsi") ?: ""
                                    val alamat = document.getString("alamat") ?: ""
                                    val id = document.id

                                    if (timestamp != null) {
                                        activities.add(
                                            RecentActivityItem(
                                                id = id,
                                                timestamp = timestamp.toDate(),
                                                jenisSampah = jenisSampah,
                                                deskripsi = deskripsi,
                                                alamat = alamat,
                                                status = status
                                            )
                                        )
                                    }
                                } catch (e: Exception) {
                                    Log.e("AdminDashboard", "Error processing document: ", e)
                                }
                            }

                            updateRecentActivityUI(activities)

                        } catch (e: Exception) {
                            Log.e("AdminDashboard", "Error processing recent activity: ", e)
                        }
                    }
                }
                ?.addOnFailureListener { exception ->
                    if (isAdded && _binding != null) {
                        Log.e("AdminDashboard", "Error getting recent reports: ", exception)
                        updateRecentActivityUI(emptyList())
                    }
                }
        } catch (e: Exception) {
            Log.e("AdminDashboard", "Error in loadRecentActivity: ", e)
        }
    }

    private fun updateRecentActivityUI(activities: List<RecentActivityItem>) {
        if (!isAdded || _binding == null) {
            return
        }

        try {
            // Untuk sementara, karena ID mungkin belum ada di XML, kita log saja
            if (activities.isNotEmpty()) {
                val activityText = activities.joinToString("\n") { activity ->
                    val dateFormat = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
                    "${dateFormat.format(activity.timestamp)} - ${activity.jenisSampah} (${activity.status})"
                }
                Log.d("AdminDashboard", "Recent activities:\n$activityText")

                // Coba cari container, jika tidak ada akan fallback ke log
                val container = try {
                    binding.root.findViewById<LinearLayout>(R.id.layoutRecentActivityContainer)
                } catch (e: Exception) {
                    null
                }

                if (container != null) {
                    updateRecentActivityUIWithContainer(activities, container)
                } else {
                    Log.w("AdminDashboard", "Container not found, using log fallback")
                }
            } else {
                Log.d("AdminDashboard", "No recent activities found")
            }

        } catch (e: Exception) {
            Log.e("AdminDashboard", "Error in updateRecentActivityUI: ", e)
        }
    }

    private fun updateRecentActivityUIWithContainer(activities: List<RecentActivityItem>, container: LinearLayout) {
        try {
            val emptyState = try {
                binding.root.findViewById<LinearLayout>(R.id.layoutEmptyState)
            } catch (e: Exception) {
                null
            }

            // Clear existing views
            container.removeAllViews()

            if (activities.isNotEmpty()) {
                // Hide empty state
                emptyState?.visibility = View.GONE

                // Add activity items
                for (activity in activities) {
                    val itemView = createRecentActivityItemView(activity)
                    container.addView(itemView)
                }

                Log.d("AdminDashboard", "Recent activities updated in UI: ${activities.size} items")
            } else {
                // Show empty state
                emptyState?.let { container.addView(it) }
                emptyState?.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            Log.e("AdminDashboard", "Error updating UI with container: ", e)
        }
    }

    private fun createRecentActivityItemView(activity: RecentActivityItem): View {
        return try {
            val inflater = LayoutInflater.from(requireContext())
            val itemView = inflater.inflate(R.layout.item_recent_activity, null)

            // Set data
            val tvTanggal = itemView.findViewById<TextView>(R.id.tvTanggal)
            val tvStatus = itemView.findViewById<TextView>(R.id.tvStatus)
            val tvJenisSampah = itemView.findViewById<TextView>(R.id.tvJenisSampah)
            val tvDeskripsi = itemView.findViewById<TextView>(R.id.tvDeskripsi)
            val tvLokasi = itemView.findViewById<TextView>(R.id.tvLokasi)

            // Format tanggal
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            tvTanggal.text = dateFormat.format(activity.timestamp)

            // Set status dengan background yang sesuai
            tvStatus.text = activity.status.uppercase()
            tvStatus.background = getStatusBackground(activity.status)

            tvJenisSampah.text = activity.jenisSampah
            tvDeskripsi.text = if (activity.deskripsi.isNotEmpty()) activity.deskripsi else "Tidak ada deskripsi"
            tvLokasi.text = activity.alamat

            // Set click listener
            itemView.setOnClickListener {
                onRecentActivityItemClicked(activity)
            }

            itemView
        } catch (e: Exception) {
            Log.e("AdminDashboard", "Error creating item view: ", e)
            // Return empty view as fallback
            TextView(requireContext()).apply {
                text = "Error loading activity"
                setPadding(16, 16, 16, 16)
            }
        }
    }

    private fun getStatusBackground(status: String): android.graphics.drawable.Drawable? {
        return try {
            when (status.lowercase()) {
                "menunggu" -> ContextCompat.getDrawable(requireContext(), R.drawable.bg_status_menunggu)
                "diproses" -> ContextCompat.getDrawable(requireContext(), R.drawable.bg_status_diproses)
                "selesai" -> ContextCompat.getDrawable(requireContext(), R.drawable.bg_status_selesai)
                else -> ContextCompat.getDrawable(requireContext(), R.drawable.bg_status_menunggu)
            }
        } catch (e: Exception) {
            Log.e("AdminDashboard", "Error getting status background: ", e)
            null
        }
    }

    private fun onRecentActivityItemClicked(activity: RecentActivityItem) {
        try {
            Log.d("AdminDashboard", "Clicked on activity: ${activity.jenisSampah} - ${activity.id}")

            // Show detail dialog
            showActivityDetailDialog(activity)

        } catch (e: Exception) {
            Log.e("AdminDashboard", "Error handling activity click: ", e)
        }
    }

    private fun showActivityDetailDialog(activity: RecentActivityItem) {
        try {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Detail Aktivitas")

            val message = """
                Jenis Sampah: ${activity.jenisSampah}
                Status: ${activity.status}
                Tanggal: ${SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(activity.timestamp)}
                
                Deskripsi:
                ${activity.deskripsi.ifEmpty { "Tidak ada deskripsi" }}
                
                Lokasi:
                ${activity.alamat}
            """.trimIndent()

            builder.setMessage(message)
            builder.setPositiveButton("Tutup") { dialog, _ ->
                dialog.dismiss()
            }
            builder.setNeutralButton("Lihat Detail") { dialog, _ ->
                try {
                    Log.d("AdminDashboard", "Navigate to full detail for: ${activity.id}")

                    // Perbaikan: gunakan requireActivity() untuk mengakses activity
                    val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
                    bottomNav?.selectedItemId = R.id.nav_reports
                } catch (e: Exception) {
                    Log.e("AdminDashboard", "Error navigating: ", e)
                }

                dialog.dismiss()
            }

            builder.show()
        } catch (e: Exception) {
            Log.e("AdminDashboard", "Error showing detail dialog: ", e)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data dengan pengecekan null safety
        if (isAdded && _binding != null) {
            loadStatistics()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        userRepository = null
        firestore = null
    }
}