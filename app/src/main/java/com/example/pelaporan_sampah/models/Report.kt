package com.example.pelaporan_sampah.models

import com.google.firebase.Timestamp

data class Report(
    val id: String = "",
    val userId: String = "",
    val jenisSampah: String = "",
    val deskripsi: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val alamat: String = "",
    val status: String = "Menunggu", // Menunggu, Diproses, Selesai
    val timestamp: Timestamp = Timestamp.now(),
    val imageUrl: String? = null
){
    // Constructor kosong untuk Firestore
    constructor() : this("", "", "", "", 0.0, 0.0, "", "Menunggu", Timestamp.now(), null)
}
