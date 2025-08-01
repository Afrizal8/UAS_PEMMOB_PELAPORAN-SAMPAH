package com.example.pelaporan_sampah.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.pelaporan_sampah.models.User
import com.example.pelaporan_sampah.utils.SessionManager
import kotlinx.coroutines.tasks.await

class UserRepository(private val context: Context) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private val sessionManager = SessionManager(context)

    // Mendapatkan user yang sedang login
    fun getCurrentUser() = auth.currentUser

    // Registrasi user baru
    suspend fun registerUser(email: String, password: String, nama: String): Result<User> {
        return try {
            // Buat akun di Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // Buat object User dengan role default "user"
                val user = User(
                    uid = firebaseUser.uid,
                    nama = nama,
                    email = email,
                    role = "user" // Default role untuk user baru
                )

                // Simpan data user ke Firestore
                usersCollection.document(firebaseUser.uid).set(user).await()

                // Simpan session user
                sessionManager.saveUserSession(user)

                Result.success(user)
            } else {
                Result.failure(Exception("Gagal membuat akun"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Login user
    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            // Login dengan Firebase Auth
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // Ambil data user dari Firestore
                val userDoc = usersCollection.document(firebaseUser.uid).get().await()
                val user = userDoc.toObject(User::class.java)

                if (user != null) {
                    // Simpan session user
                    sessionManager.saveUserSession(user)
                    Result.success(user)
                } else {
                    Result.failure(Exception("Data user tidak ditemukan"))
                }
            } else {
                Result.failure(Exception("Login gagal"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Mendapatkan data user berdasarkan UID
    suspend fun getUserByUid(uid: String): Result<User> {
        return try {
            val userDoc = usersCollection.document(uid).get().await()
            val user = userDoc.toObject(User::class.java)

            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User tidak ditemukan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Logout user
    fun logoutUser() {
        auth.signOut()
        sessionManager.clearSession()
    }

    // Cek apakah user sudah login
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null && sessionManager.isLoggedIn()
    }

    // Ambil user dari session
    fun getCurrentUserFromSession(): User? {
        return sessionManager.getCurrentUser()
    }

    // Ambil role user dari session
    fun getUserRole() = sessionManager.getUserRole()
}