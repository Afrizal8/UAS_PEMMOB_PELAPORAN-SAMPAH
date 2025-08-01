package com.example.pelaporan_sampah.fragments.user
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.pelaporan_sampah.ui.login.LoginActivity
import com.example.pelaporan_sampah.databinding.FragmentUserProfileBinding
import com.example.pelaporan_sampah.repository.UserRepository

class UserProfileFragment : Fragment() {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userRepository = UserRepository(requireContext())

        setupUserInfo()
        setupClickListeners()
    }

    private fun setupUserInfo() {
        val currentUser = userRepository.getCurrentUserFromSession()
        binding.tvUserName.text = currentUser?.nama ?: "User"
        binding.tvUserEmail.text = currentUser?.email ?: "user@email.com"
        binding.tvUserRole.text = currentUser?.role?.uppercase() ?: "USER"
    }

    private fun setupClickListeners() {
        binding.layoutEditProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Fitur Edit Profil akan segera hadir", Toast.LENGTH_SHORT).show()
        }

        binding.layoutNotifikasi.setOnClickListener {
            Toast.makeText(requireContext(), "Pengaturan Notifikasi", Toast.LENGTH_SHORT).show()
        }

        binding.layoutTentang.setOnClickListener {
            showAboutDialog()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Tentang EcoGreen")
            .setMessage("EcoGreen v1.0\n\nAplikasi pelaporan sampah untuk menjaga kebersihan lingkungan.\n\nDikembangkan untuk UAS Pemrograman Mobile.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Logout")
            .setMessage("Apakah Anda yakin ingin keluar?")
            .setPositiveButton("Ya") { _, _ ->
                logout()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun logout() {
        userRepository.logoutUser()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}