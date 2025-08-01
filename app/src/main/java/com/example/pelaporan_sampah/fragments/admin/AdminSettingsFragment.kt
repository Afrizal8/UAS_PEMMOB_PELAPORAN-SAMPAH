package com.example.pelaporan_sampah.fragments.admin

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.pelaporan_sampah.ui.login.LoginActivity
import com.example.pelaporan_sampah.databinding.FragmentAdminSettingsBinding
import com.example.pelaporan_sampah.repository.UserRepository

class AdminSettingsFragment : Fragment() {
    private var _binding: FragmentAdminSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userRepository = UserRepository(requireContext())

        val currentUser = userRepository.getCurrentUserFromSession()
        binding.tvAdminName.text = currentUser?.nama ?: "Administrator"
        binding.tvAdminEmail.text = currentUser?.email ?: "admin@email.com"

        binding.layoutBackup.setOnClickListener {
            Toast.makeText(requireContext(), "Fitur Backup akan segera hadir", Toast.LENGTH_SHORT).show()
        }

        binding.layoutExport.setOnClickListener {
            Toast.makeText(requireContext(), "Fitur Export akan segera hadir", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            userRepository.logoutUser()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}