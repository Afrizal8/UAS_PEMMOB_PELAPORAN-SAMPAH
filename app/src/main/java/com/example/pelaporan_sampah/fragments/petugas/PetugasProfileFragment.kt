package com.example.pelaporan_sampah.fragments.petugas

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.pelaporan_sampah.ui.login.LoginActivity
import com.example.pelaporan_sampah.databinding.FragmentPetugasProfileBinding
import com.example.pelaporan_sampah.repository.UserRepository

class PetugasProfileFragment : Fragment() {
    private var _binding: FragmentPetugasProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPetugasProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userRepository = UserRepository(requireContext())

        val currentUser = userRepository.getCurrentUserFromSession()
        binding.tvPetugasName.text = currentUser?.nama ?: "Petugas"
        binding.tvPetugasEmail.text = currentUser?.email ?: "petugas@email.com"

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