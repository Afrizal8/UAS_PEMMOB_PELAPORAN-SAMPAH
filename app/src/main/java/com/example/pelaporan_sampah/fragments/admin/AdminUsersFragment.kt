package com.example.pelaporan_sampah.fragments.admin

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pelaporan_sampah.adapter.UserAdapter
import com.example.pelaporan_sampah.databinding.FragmentAdminUsersBinding
import com.example.pelaporan_sampah.models.User
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminUsersFragment : Fragment() {
    private var _binding: FragmentAdminUsersBinding? = null
    private val binding get() = _binding!!

    private lateinit var userAdapter: UserAdapter
    private val db = FirebaseFirestore.getInstance()
    private var allUsers = mutableListOf<User>()
    private var currentFilter = "Semua"

    companion object {
        private const val TAG = "AdminUsersFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupTabLayout()
        loadUsers()
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter { user, newRole ->
            showRoleChangeConfirmation(user, newRole)
        }

        binding.rvUsers.apply {
            adapter = userAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentFilter = tab?.text.toString()
                filterUsers()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadUsers() {
        db.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error loading users", error)
                    Toast.makeText(context, "Gagal memuat data user", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                allUsers.clear()
                snapshot?.documents?.forEach { document ->
                    try {
                        val user = document.toObject(User::class.java)
                        if (user != null) {
                            allUsers.add(user)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing user document: ${document.id}", e)
                    }
                }

                // Sort di sini setelah data dimuat
                allUsers.sortByDescending { it.createdAt }
                filterUsers()
            }
    }

    private fun filterUsers() {
        val filteredUsers = when (currentFilter) {
            "Admin" -> allUsers.filter { it.role.equals("admin", ignoreCase = true) }
            "Petugas" -> allUsers.filter { it.role.equals("petugas", ignoreCase = true) }
            "User" -> allUsers.filter { it.role.equals("user", ignoreCase = true) }
            else -> allUsers // "Semua"
        }

        userAdapter.submitList(filteredUsers)
    }

    private fun showRoleChangeConfirmation(user: User, newRole: String) {
        val roleText = when (newRole) {
            "admin" -> "Admin"
            "petugas" -> "Petugas"
            "user" -> "User"
            else -> newRole
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Ubah Role User")
            .setMessage("Apakah Anda yakin ingin mengubah role ${user.nama} menjadi $roleText?")
            .setPositiveButton("Ya") { _, _ ->
                updateUserRole(user, newRole)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateUserRole(user: User, newRole: String) {
        if (user.uid.isEmpty()) {
            Toast.makeText(context, "UID user tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(user.uid)
            .update("role", newRole)
            .addOnSuccessListener {
                Toast.makeText(
                    context,
                    "Role ${user.nama} berhasil diubah menjadi ${newRole.uppercase()}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d(TAG, "User role updated successfully: ${user.uid} -> $newRole")
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Error updating user role", error)
                Toast.makeText(
                    context,
                    "Gagal mengubah role user: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}