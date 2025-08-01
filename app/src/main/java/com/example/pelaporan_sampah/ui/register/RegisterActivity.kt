package com.example.pelaporan_sampah.ui.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.pelaporan_sampah.MainActivity
import com.example.pelaporan_sampah.databinding.ActivityRegisterBinding
import com.example.pelaporan_sampah.repository.UserRepository
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // Hide action bar sepenuhnya
            supportActionBar?.hide()

            binding = ActivityRegisterBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Initialize repository after context is ready
            userRepository = UserRepository(this)

            setupClickListeners()

        } catch (e: Exception) {
            Log.e("RegisterActivity", "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error initializing: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val nama = binding.etNama.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (validateInput(nama, email, password, confirmPassword)) {
                registerUser(nama, email, password)
            }
        }
    }

    private fun validateInput(nama: String, email: String, password: String, confirmPassword: String): Boolean {
        when {
            nama.isEmpty() -> {
                binding.etNama.error = "Nama tidak boleh kosong"
                binding.etNama.requestFocus()
                return false
            }
            nama.length < 2 -> {
                binding.etNama.error = "Nama minimal 2 karakter"
                binding.etNama.requestFocus()
                return false
            }
            email.isEmpty() -> {
                binding.etEmail.error = "Email tidak boleh kosong"
                binding.etEmail.requestFocus()
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.etEmail.error = "Format email tidak valid"
                binding.etEmail.requestFocus()
                return false
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Password tidak boleh kosong"
                binding.etPassword.requestFocus()
                return false
            }
            password.length < 6 -> {
                binding.etPassword.error = "Password minimal 6 karakter"
                binding.etPassword.requestFocus()
                return false
            }
            confirmPassword.isEmpty() -> {
                binding.etConfirmPassword.error = "Konfirmasi password tidak boleh kosong"
                binding.etConfirmPassword.requestFocus()
                return false
            }
            password != confirmPassword -> {
                binding.etConfirmPassword.error = "Password tidak sama"
                binding.etConfirmPassword.requestFocus()
                return false
            }
        }
        return true
    }

    private fun registerUser(nama: String, email: String, password: String) {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val result = userRepository.registerUser(email, password, nama)

                showLoading(false)

                result.fold(
                    onSuccess = { user ->
                        Toast.makeText(this@RegisterActivity,
                            "Registrasi berhasil! Selamat datang ${user.nama}",
                            Toast.LENGTH_SHORT).show()

                        // Navigasi ke MainActivity
                        navigateToMain()
                    },
                    onFailure = { exception ->
                        Log.e("RegisterActivity", "Registration failed: ${exception.message}", exception)
                        val errorMessage = when {
                            exception.message?.contains("email address is already in use") == true ->
                                "Email sudah terdaftar"
                            exception.message?.contains("weak password") == true ->
                                "Password terlalu lemah"
                            exception.message?.contains("network error") == true ->
                                "Periksa koneksi internet"
                            else -> "Registrasi gagal: ${exception.message}"
                        }
                        Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                )
            } catch (e: Exception) {
                showLoading(false)
                Log.e("RegisterActivity", "Unexpected error during registration: ${e.message}", e)
                Toast.makeText(this@RegisterActivity, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        try {
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnRegister.isEnabled = !isLoading
        } catch (e: Exception) {
            Log.e("RegisterActivity", "Error in showLoading: ${e.message}", e)
        }
    }

    private fun navigateToMain() {
        try {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e("RegisterActivity", "Error navigating to main: ${e.message}", e)
            Toast.makeText(this, "Error navigating: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}