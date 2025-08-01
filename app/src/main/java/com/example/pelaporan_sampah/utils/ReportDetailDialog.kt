package com.example.pelaporan_sampah.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.pelaporan_sampah.R
import com.example.pelaporan_sampah.databinding.DialogReportDetailBinding
import com.example.pelaporan_sampah.models.Report
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*

object ReportDetailDialog {
    fun show(context: Context, report: Report) {
        val dialog = BottomSheetDialog(context)
        val binding = DialogReportDetailBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)

        // Biar tinggi wrap_content, tidak fullscreen
        val bottomSheet = dialog.findViewById<ViewGroup>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT

        dialog.setCancelable(true)

        with(binding) {
            tvDetailJenisSampah.text = report.jenisSampah

            val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
            tvDetailTanggal.text = dateFormat.format(report.timestamp.toDate())

            tvDetailStatus.text = report.status.uppercase()
            val statusBackground = when (report.status.lowercase()) {
                "menunggu" -> R.drawable.bg_status_menunggu
                "diproses" -> R.drawable.bg_status_diproses
                "selesai" -> R.drawable.bg_status_selesai
                else -> R.drawable.bg_status_menunggu
            }
            tvDetailStatus.setBackgroundResource(statusBackground)

            tvDetailLokasi.text = if (report.alamat.isNotEmpty()) {
                report.alamat
            } else {
                "Lat: ${report.latitude}, Long: ${report.longitude}"
            }

            tvDetailDeskripsi.text = report.deskripsi

            btnClose.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}
