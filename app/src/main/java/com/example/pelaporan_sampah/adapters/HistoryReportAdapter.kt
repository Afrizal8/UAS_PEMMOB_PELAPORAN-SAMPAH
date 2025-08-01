package com.example.pelaporan_sampah.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pelaporan_sampah.R
import com.example.pelaporan_sampah.databinding.ItemHistoryReportBinding
import com.example.pelaporan_sampah.models.Report
import java.text.SimpleDateFormat
import java.util.*

class HistoryReportAdapter(
    private val onItemClick: (Report) -> Unit
) : ListAdapter<Report, HistoryReportAdapter.HistoryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryReportBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HistoryViewHolder(
        private val binding: ItemHistoryReportBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(report: Report) {
            with(binding) {
                // Format tanggal
                val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                tvTanggal.text = dateFormat.format(report.timestamp.toDate())

                // Set jenis sampah
                tvJenisSampah.text = report.jenisSampah

                // Set deskripsi
                tvDeskripsi.text = report.deskripsi

                // Set lokasi
                tvLokasi.text = if (report.alamat.isNotEmpty()) {
                    report.alamat
                } else {
                    "Lat: ${report.latitude}, Long: ${report.longitude}"
                }

                // Set status dengan warna yang sesuai
                tvStatus.text = report.status.uppercase()
                val statusBackground = when (report.status.lowercase()) {
                    "menunggu" -> R.drawable.bg_status_menunggu
                    "diproses" -> R.drawable.bg_status_diproses
                    "selesai" -> R.drawable.bg_status_selesai
                    else -> R.drawable.bg_status_menunggu
                }
                tvStatus.setBackgroundResource(statusBackground)

                // Set click listener
                btnLihatDetail.setOnClickListener {
                    onItemClick(report)
                }

                root.setOnClickListener {
                    onItemClick(report)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Report>() {
        override fun areItemsTheSame(oldItem: Report, newItem: Report): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Report, newItem: Report): Boolean {
            return oldItem == newItem
        }
    }
}