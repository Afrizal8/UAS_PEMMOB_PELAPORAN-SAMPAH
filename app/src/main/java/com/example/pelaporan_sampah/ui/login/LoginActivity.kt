package com.example.pelaporan_sampah.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.pelaporan_sampah.MainActivity
import com.example.pelaporan_sampah.databinding.ActivityLoginBinding
import com.example.pelaporan_sampah.repository.UserRepository
import com.example.pelaporan_sampah.ui.register.RegisterActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
//    private val userRepository = UserRepository(this)
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide action bar sepenuhnya
        supportActionBar?.hide()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepository = UserRepository(this)

        // Cek apakah user sudah login
        if (userRepository.isUserLoggedIn()) {
            navigateToMain()
            return
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        when {
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
        }
        return true
    }

    private fun loginUser(email: String, password: String) {
        showLoading(true)

        lifecycleScope.launch {
            val result = userRepository.loginUser(email, password)

            showLoading(false)

            result.fold(
                onSuccess = { user ->
                    Toast.makeText(this@LoginActivity, "Login berhasil", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                },
                onFailure = { exception ->
                    val errorMessage = when {
                        exception.message?.contains("password is invalid") == true ->
                            "Password salah"
                        exception.message?.contains("no user record") == true ->
                            "Email tidak terdaftar"
                        exception.message?.contains("network error") == true ->
                            "Periksa koneksi internet"
                        else -> "Login gagal: ${exception.message}"
                    }
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
        binding.btnRegister.isEnabled = !isLoading
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}