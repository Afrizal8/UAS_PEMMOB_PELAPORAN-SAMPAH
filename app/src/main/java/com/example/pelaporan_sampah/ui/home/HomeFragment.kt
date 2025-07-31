package com.example.pelaporan_sampah.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.pelaporan_sampah.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Observing changes from ViewModel for the 'text' value (you are already doing this for the other TextView)
        homeViewModel.text.observe(viewLifecycleOwner) {
            // Set the value to some TextView if needed, for example:
            // tvSomeText.text = it
        }

        // Bindings for the statistics or other information
        val tvProcessedCount: TextView = binding.tvProcessedCount
        val tvTodayCount: TextView = binding.tvTodayCount
        val tvCompletedCount: TextView = binding.tvCompletedCount
        val tvTotalCount: TextView = binding.tvTotalCount

        // Set the text for statistics (these values should ideally come from your ViewModel or API)
        tvProcessedCount.text = "3"
        tvTodayCount.text = "1"
        tvCompletedCount.text = "8"
        tvTotalCount.text = "12"

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}