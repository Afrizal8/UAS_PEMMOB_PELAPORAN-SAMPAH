package com.example.pelaporan_sampah.models

data class User(
    val uid: String = "",
    val nama: String = "",
    val email: String = "",
    val role: String = "user", // default role adalah "user"
    val createdAt: Long = System.currentTimeMillis()
) {
    // Constructor kosong untuk Firestore
    constructor() : this("", "", "", "user", System.currentTimeMillis())
}
