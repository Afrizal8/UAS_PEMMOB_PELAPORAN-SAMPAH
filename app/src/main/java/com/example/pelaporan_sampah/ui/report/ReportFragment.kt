package com.example.pelaporan_sampah.ui.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.pelaporan_sampah.databinding.FragmentReportBinding

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnSubmitReport.setOnClickListener {
            handleSubmitReport()
        }
    }

    private fun handleSubmitReport() {
        val lokasi = binding.etLokasi.text.toString().trim()
        val deskripsi = binding.etDeskripsi.text.toString().trim()

        // Validasi input
        if (lokasi.isEmpty()) {
            binding.etLokasi.error = "Lokasi tidak boleh kosong"
            binding.etLokasi.requestFocus()
            return
        }

        if (deskripsi.isEmpty()) {
            binding.etDeskripsi.error = "Deskripsi tidak boleh kosong"
            binding.etDeskripsi.requestFocus()
            return
        }

        // Submit laporan
        submitReport(lokasi, deskripsi)
    }

    private fun submitReport(lokasi: String, deskripsi: String) {
        // Logic untuk menyimpan laporan
        Toast.makeText(context, "Laporan berhasil dikirim!", Toast.LENGTH_SHORT).show()

        // Clear form
        clearForm()
    }

    private fun clearForm() {
        binding.etLokasi.text?.clear()
        binding.etDeskripsi.text?.clear()
        binding.etLokasi.error = null
        binding.etDeskripsi.error = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}