package com.example.pelaporan_sampah.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.pelaporan_sampah.models.User
import com.example.pelaporan_sampah.models.UserRole

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "KitaBersihSession"
        private const val KEY_USER_UID = "user_uid"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    // Simpan session user setelah login
    fun saveUserSession(user: User) {
        val editor = prefs.edit()
        editor.putString(KEY_USER_UID, user.uid)
        editor.putString(KEY_USER_NAME, user.nama)
        editor.putString(KEY_USER_EMAIL, user.email)
        editor.putString(KEY_USER_ROLE, user.role)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
    }

    // Ambil data user dari session
    fun getCurrentUser(): User? {
        return if (isLoggedIn()) {
            User(
                uid = prefs.getString(KEY_USER_UID, "") ?: "",
                nama = prefs.getString(KEY_USER_NAME, "") ?: "",
                email = prefs.getString(KEY_USER_EMAIL, "") ?: "",
                role = prefs.getString(KEY_USER_ROLE, "user") ?: "user"
            )
        } else null
    }

    // Ambil role user
    fun getUserRole(): UserRole {
        val roleString = prefs.getString(KEY_USER_ROLE, "user") ?: "user"
        return UserRole.fromString(roleString)
    }

    // Cek apakah user sudah login
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // Logout - hapus semua session
    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }

    // Update role user (untuk admin yang mengubah role user lain)
    fun updateUserRole(newRole: String) {
        val editor = prefs.edit()
        editor.putString(KEY_USER_ROLE, newRole)
        editor.apply()
    }
}