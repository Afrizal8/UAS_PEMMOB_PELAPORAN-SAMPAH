package com.example.pelaporan_sampah.models

enum class UserRole(val displayName: String) {
    ADMIN("Admin"),
    PETUGAS("Petugas"),
    USER("User");

    companion object {
        fun fromString(role: String): UserRole {
            return when (role.lowercase()) {
                "admin" -> ADMIN
                "petugas" -> PETUGAS
                "user" -> USER
                else -> USER // default role
            }
        }
    }
}