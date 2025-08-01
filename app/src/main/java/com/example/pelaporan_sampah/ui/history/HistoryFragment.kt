package com.example.pelaporan_sampah.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.pelaporan_sampah.databinding.FragmentHistoryBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pelaporan_sampah.HistoryAdapter
import com.example.pelaporan_sampah.HistoryItem

class HistoryFragment : Fragment() {
    private lateinit var rvHistory: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var llSelectedItem: LinearLayout
    private lateinit var tvSelectedLocation: TextView
    private var _binding: FragmentHistoryBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val historyViewModel =
            ViewModelProvider(this).get(HistoryViewModel::class.java)

        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()

        val textView: TextView = binding.textHistory
        historyViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    private fun setupRecyclerView() {
        rvHistory = binding.rvHistory
        llSelectedItem = binding.llSelectedItem
        tvSelectedLocation = binding.tvSelectedLocation

        // Data sesuai dengan gambar
        val historyList = listOf(
            HistoryItem(1, "Laporan Organik - Jl. Merdeka", "Tumpukan sampah dapur dan daun.", "Selesai", "24 Juli 2025"),
            HistoryItem(2, "Laporan Anorganik - Taman Kota", "Botol plastik dan kaleng minuman berserakan.", "Diproses", "26 Juli 2025"),
            HistoryItem(3, "Laporan Bahan Berbahaya - Dekat Sekolah", "Baterai bekas dan kabel elektronik.", "Menunggu", "28 Juli 2025")
        )

        historyAdapter = HistoryAdapter(historyList) { selectedItem ->
            showSelectedItem(selectedItem)
            Toast.makeText(requireContext(), "Dipilih: ${selectedItem.location}", Toast.LENGTH_SHORT).show()
        }

        rvHistory.layoutManager = LinearLayoutManager(requireContext())
        rvHistory.adapter = historyAdapter
    }

    private fun showSelectedItem(item: HistoryItem) {
        llSelectedItem.visibility = View.VISIBLE
        tvSelectedLocation.text = "Dipilih: ${item.location}"

        // Hide after 3 seconds
        llSelectedItem.postDelayed({
            llSelectedItem.visibility = View.GONE
        }, 3000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}