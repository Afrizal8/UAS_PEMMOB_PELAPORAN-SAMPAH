package com.example.pelaporan_sampah.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pelaporan_sampah.R
import com.example.pelaporan_sampah.databinding.ItemUserBinding
import com.example.pelaporan_sampah.models.User
import java.text.SimpleDateFormat
import java.util.*

class UserAdapter(
    private val onRoleChangeClick: (User, String) -> Unit
) : ListAdapter<User, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(
        private val binding: ItemUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.apply {
                tvUserName.text = user.nama
                tvUserEmail.text = user.email
                tvUserRole.text = user.role.uppercase()

                // Format tanggal
                val date = Date(user.createdAt)
                val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                tvUserJoinDate.text = "Bergabung: ${formatter.format(date)}"

                // Set role color
                when (user.role.lowercase()) {
                    "admin" -> {
                        tvUserRole.setBackgroundResource(R.drawable.bg_role_admin)
                        tvUserRole.setTextColor(binding.root.context.getColor(android.R.color.white))
                    }
                    "petugas" -> {
                        tvUserRole.setBackgroundResource(R.drawable.bg_role_petugas)
                        tvUserRole.setTextColor(binding.root.context.getColor(android.R.color.white))
                    }
                    else -> {
                        tvUserRole.setBackgroundResource(R.drawable.bg_role_user)
                        tvUserRole.setTextColor(binding.root.context.getColor(R.color.green_primary))
                    }
                }

                // Menu untuk mengubah role
                btnMoreOptions.setOnClickListener { view ->
                    showRoleMenu(view, user)
                }
            }
        }

        private fun showRoleMenu(view: View, user: User) {
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.menu_user_role, popup.menu)

            // Disable current role
            when (user.role.lowercase()) {
                "admin" -> popup.menu.findItem(R.id.role_admin)?.isEnabled = false
                "petugas" -> popup.menu.findItem(R.id.role_petugas)?.isEnabled = false
                "user" -> popup.menu.findItem(R.id.role_user)?.isEnabled = false
            }

            popup.setOnMenuItemClickListener { item ->
                val newRole = when (item.itemId) {
                    R.id.role_admin -> "admin"
                    R.id.role_petugas -> "petugas"
                    R.id.role_user -> "user"
                    else -> return@setOnMenuItemClickListener false
                }
                onRoleChangeClick(user, newRole)
                true
            }
            popup.show()
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}