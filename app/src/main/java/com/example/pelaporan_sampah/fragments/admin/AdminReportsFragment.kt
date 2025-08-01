package com.example.pelaporan_sampah.fragments.admin

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pelaporan_sampah.R
import com.example.pelaporan_sampah.databinding.FragmentAdminReportsBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class AdminReportsFragment : Fragment() {
    private var _binding: FragmentAdminReportsBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private lateinit var reportsAdapter: ReportsAdapter
    private val reportsList = mutableListOf<ReportItem>()
    private val filteredReportsList = mutableListOf<ReportItem>()
    private var currentFilter = "Semua"
    private var isLoading = false

    // Data class untuk Report
    data class ReportItem(
        val id: String,
        val jenisSampah: String,
        val deskripsi: String,
        val alamat: String,
        val status: String,
        val timestamp: Date,
        val userId: String,
        val namaPelapor: String,
        val estimasiMulai: Date? = null,
        val estimasiSelesai: Date? = null,
        val catatan: String? = null
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            initializeComponents()
            setupRecyclerView()
            setupTabLayout()
            setupSwipeRefresh()
            loadReports()
        } catch (e: Exception) {
            Log.e("AdminReports", "Error in onViewCreated: ", e)
        }
    }

    private fun initializeComponents() {
        firestore = FirebaseFirestore.getInstance()
    }

    private fun setupRecyclerView() {
        reportsAdapter = ReportsAdapter(filteredReportsList) { report ->
            showManageReportDialog(report)
        }

        binding.rvReports.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reportsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentFilter = when (tab?.position) {
                    0 -> "Semua"
                    1 -> "Menunggu"
                    2 -> "Diproses"
                    3 -> "Selesai"
                    else -> "Semua"
                }
                filterReports()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupSwipeRefresh() {
        // Jika Anda menambahkan SwipeRefreshLayout, uncomment ini
        /*
        binding.swipeRefresh?.setOnRefreshListener {
            loadReports()
        }
        binding.swipeRefresh?.setColorSchemeResources(R.color.green_primary)
        */
    }

    private fun loadReports() {
        if (isLoading) return

        isLoading = true
        Log.d("AdminReports", "Loading reports...")

        firestore.collection("reports")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                reportsList.clear()
                val totalDocuments = documents.size()
                var processedDocuments = 0

                if (totalDocuments == 0) {
                    isLoading = false
                    filterReports()
                    // binding.swipeRefresh?.isRefreshing = false
                    return@addOnSuccessListener
                }

                for (document in documents) {
                    try {
                        val timestamp = document.getTimestamp("timestamp")
                        val userId = document.getString("userId") ?: ""

                        if (timestamp != null) {
                            // Get user name
                            getUserName(userId) { namaPelapor ->
                                try {
                                    val report = ReportItem(
                                        id = document.id,
                                        jenisSampah = document.getString("jenisSampah") ?: "Unknown",
                                        deskripsi = document.getString("deskripsi") ?: "Tidak ada deskripsi",
                                        alamat = document.getString("alamat") ?: "Lokasi tidak diketahui",
                                        status = document.getString("status") ?: "Menunggu",
                                        timestamp = timestamp.toDate(),
                                        userId = userId,
                                        namaPelapor = namaPelapor,
                                        estimasiMulai = document.getTimestamp("estimasiMulai")?.toDate(),
                                        estimasiSelesai = document.getTimestamp("estimasiSelesai")?.toDate(),
                                        catatan = document.getString("catatan")
                                    )

                                    reportsList.add(report)
                                    processedDocuments++

                                    if (processedDocuments >= totalDocuments) {
                                        isLoading = false
                                        filterReports()
                                        // binding.swipeRefresh?.isRefreshing = false
                                        Log.d("AdminReports", "Loaded ${reportsList.size} reports")
                                    }
                                } catch (e: Exception) {
                                    Log.e("AdminReports", "Error creating report item: ", e)
                                    processedDocuments++
                                    if (processedDocuments >= totalDocuments) {
                                        isLoading = false
                                        filterReports()
                                        // binding.swipeRefresh?.isRefreshing = false
                                    }
                                }
                            }
                        } else {
                            processedDocuments++
                            if (processedDocuments >= totalDocuments) {
                                isLoading = false
                                filterReports()
                                // binding.swipeRefresh?.isRefreshing = false
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("AdminReports", "Error processing document: ", e)
                        processedDocuments++
                        if (processedDocuments >= totalDocuments) {
                            isLoading = false
                            filterReports()
                            // binding.swipeRefresh?.isRefreshing = false
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                isLoading = false
                // binding.swipeRefresh?.isRefreshing = false
                Log.e("AdminReports", "Error getting reports: ", exception)
                if (isAdded) {
                    Toast.makeText(requireContext(), "Gagal memuat laporan", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun getUserName(userId: String, callback: (String) -> Unit) {
        if (userId.isEmpty()) {
            callback("Unknown User")
            return
        }

        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val name = document.getString("nama") ?: "Unknown User"
                callback(name)
            }
            .addOnFailureListener { exception ->
                Log.e("AdminReports", "Error getting user name: ", exception)
                callback("Unknown User")
            }
    }

    private fun filterReports() {
        if (!isAdded || _binding == null) return

        try {
            filteredReportsList.clear()

            val filtered = when (currentFilter) {
                "Semua" -> reportsList
                else -> reportsList.filter { it.status == currentFilter }
            }

            filteredReportsList.addAll(filtered)
            reportsAdapter.notifyDataSetChanged()

            Log.d("AdminReports", "Filtered ${filteredReportsList.size} reports for filter: $currentFilter")
        } catch (e: Exception) {
            Log.e("AdminReports", "Error filtering reports: ", e)
        }
    }

    private fun showManageReportDialog(report: ReportItem) {
        try {
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_manage_report, null)

            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create()

            setupManageReportDialog(dialogView, report, dialog)
            dialog.show()
        } catch (e: Exception) {
            Log.e("AdminReports", "Error showing manage dialog: ", e)
            Toast.makeText(requireContext(), "Gagal membuka dialog", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupManageReportDialog(dialogView: View, report: ReportItem, dialog: AlertDialog) {
        try {
            // Setup detail views
            val tvDetailJenisSampah = dialogView.findViewById<TextView>(R.id.tvDetailJenisSampah)
            val tvDetailPelapor = dialogView.findViewById<TextView>(R.id.tvDetailPelapor)
            val tvDetailTanggal = dialogView.findViewById<TextView>(R.id.tvDetailTanggal)
            val tvDetailDeskripsi = dialogView.findViewById<TextView>(R.id.tvDetailDeskripsi)
            val tvDetailLokasi = dialogView.findViewById<TextView>(R.id.tvDetailLokasi)

            val spinnerStatus = dialogView.findViewById<Spinner>(R.id.spinnerStatus)
            val etTanggalMulai = dialogView.findViewById<TextInputEditText>(R.id.etTanggalMulai)
            val etTanggalSelesai = dialogView.findViewById<TextInputEditText>(R.id.etTanggalSelesai)
            val etCatatan = dialogView.findViewById<TextInputEditText>(R.id.etCatatan)

            val btnBatal = dialogView.findViewById<MaterialButton>(R.id.btnBatal)
            val btnSimpan = dialogView.findViewById<MaterialButton>(R.id.btnSimpan)

            // Set detail data
            tvDetailJenisSampah.text = "Jenis Sampah: ${report.jenisSampah}"
            tvDetailPelapor.text = "Pelapor: ${report.namaPelapor}"
            tvDetailTanggal.text = "Tanggal Laporan: ${SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(report.timestamp)}"
            tvDetailDeskripsi.text = "Deskripsi: ${report.deskripsi}"
            tvDetailLokasi.text = "Lokasi: ${report.alamat}"

            // Setup status spinner
            val statusOptions = arrayOf("Menunggu", "Diproses", "Selesai")
            val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statusOptions)
            statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerStatus.adapter = statusAdapter

            // Set current status
            val currentStatusIndex = statusOptions.indexOf(report.status)
            if (currentStatusIndex >= 0) {
                spinnerStatus.setSelection(currentStatusIndex)
            }

            // Set existing dates if available
            report.estimasiMulai?.let {
                etTanggalMulai.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it))
            }
            report.estimasiSelesai?.let {
                etTanggalSelesai.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it))
            }
            etCatatan.setText(report.catatan ?: "")

            // Setup date pickers
            setupDatePicker(etTanggalMulai)
            setupDatePicker(etTanggalSelesai)

            // Setup buttons
            btnBatal.setOnClickListener {
                dialog.dismiss()
            }

            btnSimpan.setOnClickListener {
                updateReport(report, dialogView, dialog)
            }
        } catch (e: Exception) {
            Log.e("AdminReports", "Error setting up dialog: ", e)
        }
    }

    private fun setupDatePicker(editText: TextInputEditText) {
        editText.setOnClickListener {
            try {
                val calendar = Calendar.getInstance()

                DatePickerDialog(
                    requireContext(),
                    { _, year, month, dayOfMonth ->
                        val selectedDate = Calendar.getInstance()
                        selectedDate.set(year, month, dayOfMonth)
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        editText.setText(dateFormat.format(selectedDate.time))
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            } catch (e: Exception) {
                Log.e("AdminReports", "Error showing date picker: ", e)
            }
        }
    }

    private fun updateReport(report: ReportItem, dialogView: View, dialog: AlertDialog) {
        try {
            val spinnerStatus = dialogView.findViewById<Spinner>(R.id.spinnerStatus)
            val etTanggalMulai = dialogView.findViewById<TextInputEditText>(R.id.etTanggalMulai)
            val etTanggalSelesai = dialogView.findViewById<TextInputEditText>(R.id.etTanggalSelesai)
            val etCatatan = dialogView.findViewById<TextInputEditText>(R.id.etCatatan)

            val newStatus = spinnerStatus.selectedItem.toString()
            val tanggalMulaiText = etTanggalMulai.text.toString().trim()
            val tanggalSelesaiText = etTanggalSelesai.text.toString().trim()
            val catatan = etCatatan.text.toString().trim()

            val updateData = mutableMapOf<String, Any>(
                "status" to newStatus,
                "catatan" to catatan,
                "updatedAt" to Date()
            )

            // Parse dates if provided
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            if (tanggalMulaiText.isNotEmpty()) {
                try {
                    val estimasiMulai = dateFormat.parse(tanggalMulaiText)
                    if (estimasiMulai != null) {
                        updateData["estimasiMulai"] = estimasiMulai
                    }
                } catch (e: Exception) {
                    Log.e("AdminReports", "Error parsing start date: ", e)
                    Toast.makeText(requireContext(), "Format tanggal mulai tidak valid", Toast.LENGTH_SHORT).show()
                    return
                }
            }

            if (tanggalSelesaiText.isNotEmpty()) {
                try {
                    val estimasiSelesai = dateFormat.parse(tanggalSelesaiText)
                    if (estimasiSelesai != null) {
                        updateData["estimasiSelesai"] = estimasiSelesai
                    }
                } catch (e: Exception) {
                    Log.e("AdminReports", "Error parsing end date: ", e)
                    Toast.makeText(requireContext(), "Format tanggal selesai tidak valid", Toast.LENGTH_SHORT).show()
                    return
                }
            }

            // Validate dates
            if (tanggalMulaiText.isNotEmpty() && tanggalSelesaiText.isNotEmpty()) {
                try {
                    val mulai = dateFormat.parse(tanggalMulaiText)
                    val selesai = dateFormat.parse(tanggalSelesaiText)
                    if (mulai != null && selesai != null && mulai.after(selesai)) {
                        Toast.makeText(requireContext(), "Tanggal mulai tidak boleh setelah tanggal selesai", Toast.LENGTH_SHORT).show()
                        return
                    }
                } catch (e: Exception) {
                    Log.e("AdminReports", "Error validating dates: ", e)
                }
            }

            // Update in Firestore
            firestore.collection("reports")
                .document(report.id)
                .update(updateData)
                .addOnSuccessListener {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Laporan berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        loadReports() // Reload data
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("AdminReports", "Error updating report: ", exception)
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Gagal memperbarui laporan", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: Exception) {
            Log.e("AdminReports", "Error in updateReport: ", e)
            Toast.makeText(requireContext(), "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
        }
    }

    // Method untuk membuka laporan tertentu berdasarkan ID (untuk navigation dari dashboard)
    fun openSpecificReport(reportId: String) {
        // Cari report berdasarkan ID dan buka dialog
        val report = reportsList.find { it.id == reportId }
        if (report != null) {
            showManageReportDialog(report)
        } else {
            // Load data dulu jika belum ada
            loadReports()
            // Bisa menggunakan handler untuk delay
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                val foundReport = reportsList.find { it.id == reportId }
                foundReport?.let { showManageReportDialog(it) }
            }, 1000)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data saat fragment kembali visible
        if (!isLoading && reportsList.isEmpty()) {
            loadReports()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Adapter Class
    inner class ReportsAdapter(
        private val reports: MutableList<ReportItem>,
        private val onItemClick: (ReportItem) -> Unit
    ) : RecyclerView.Adapter<ReportsAdapter.ReportViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_admin_report, parent, false)
            return ReportViewHolder(view)
        }

        override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
            holder.bind(reports[position])
        }

        override fun getItemCount(): Int = reports.size

        inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvTanggal = itemView.findViewById<TextView>(R.id.tvTanggal)
            private val tvStatus = itemView.findViewById<TextView>(R.id.tvStatus)
            private val tvJenisSampah = itemView.findViewById<TextView>(R.id.tvJenisSampah)
            private val tvPelapor = itemView.findViewById<TextView>(R.id.tvPelapor)
            private val tvDeskripsi = itemView.findViewById<TextView>(R.id.tvDeskripsi)
            private val tvLokasi = itemView.findViewById<TextView>(R.id.tvLokasi)
            private val btnKelola = itemView.findViewById<TextView>(R.id.btnKelola)

            fun bind(report: ReportItem) {
                try {
                    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    tvTanggal.text = dateFormat.format(report.timestamp)

                    tvStatus.text = report.status.uppercase()
                    tvStatus.background = getStatusBackground(report.status)

                    tvJenisSampah.text = report.jenisSampah
                    tvPelapor.text = report.namaPelapor
                    tvDeskripsi.text = report.deskripsi
                    tvLokasi.text = report.alamat

                    // Set click listeners
                    itemView.setOnClickListener {
                        onItemClick(report)
                    }

                    btnKelola.setOnClickListener {
                        onItemClick(report)
                    }
                } catch (e: Exception) {
                    Log.e("ReportsAdapter", "Error binding data: ", e)
                }
            }

            private fun getStatusBackground(status: String): android.graphics.drawable.Drawable? {
                return try {
                    when (status.lowercase()) {
                        "menunggu" -> ContextCompat.getDrawable(itemView.context, R.drawable.bg_status_menunggu)
                        "diproses" -> ContextCompat.getDrawable(itemView.context, R.drawable.bg_status_diproses)
                        "selesai" -> ContextCompat.getDrawable(itemView.context, R.drawable.bg_status_selesai)
                        else -> ContextCompat.getDrawable(itemView.context, R.drawable.bg_status_menunggu)
                    }
                } catch (e: Exception) {
                    Log.e("ReportsAdapter", "Error getting status background: ", e)
                    null
                }
            }
        }
    }
}