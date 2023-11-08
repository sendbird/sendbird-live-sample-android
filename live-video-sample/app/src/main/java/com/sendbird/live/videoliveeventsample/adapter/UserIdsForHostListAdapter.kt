package com.sendbird.live.videoliveeventsample.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.sendbird.live.videoliveeventsample.databinding.ViewUserListItemBinding
import com.sendbird.live.videoliveeventsample.model.SelectedHostUser

class UserIdsForHostListAdapter(
    private val onMoreButtonClickListener: (item: SelectedHostUser) -> Unit
) : RecyclerView.Adapter<UserIdsForHostListAdapter.ViewHolder>() {
    var selectedHostUsers: MutableList<SelectedHostUser> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ViewUserListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(selectedHostUsers[position])
    }

    override fun getItemCount(): Int {
        return selectedHostUsers.size
    }

    fun addItems(selectedHostUsers: List<SelectedHostUser>) {
        val newList = mutableListOf<SelectedHostUser>().apply {
            addAll(selectedHostUsers)
            addAll(this@UserIdsForHostListAdapter.selectedHostUsers)
        }
        val diffCallback = SelectedHostUserDiffUtilCallback(this.selectedHostUsers, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.selectedHostUsers.clear()
        this.selectedHostUsers.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    fun removeItems(selectedHostUsers: List<SelectedHostUser>) {
        val newList = mutableListOf<SelectedHostUser>().apply {
            addAll(this@UserIdsForHostListAdapter.selectedHostUsers)
            removeAll(selectedHostUsers)
        }
        val diffCallback = SelectedHostUserDiffUtilCallback(this.selectedHostUsers, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.selectedHostUsers.clear()
        this.selectedHostUsers.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(
        private val binding: ViewUserListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(selectedHostUser: SelectedHostUser) {
            with(binding.ivUserIdsForHostListItemMore) {
                visibility = if (selectedHostUser.isMyUser()) View.GONE else View.VISIBLE
                setOnClickListener { onMoreButtonClickListener(selectedHostUser) }
            }
            val userName = if (selectedHostUser.isMyUser()) {
                "${selectedHostUser.displayName} (You)"
            } else {
                selectedHostUser.displayName
            }

            binding.tvUserIdsForHostListItemUserName.text = userName
            selectedHostUser.profileUrl?.let { binding.ivUserIdsForHostListItemProfile.load(it) }
        }
    }

    class SelectedHostUserDiffUtilCallback(
        private val oldUsers: List<SelectedHostUser>,
        private val newUsers: List<SelectedHostUser>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldUsers.size
        }

        override fun getNewListSize(): Int {
            return newUsers.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldUsers[oldItemPosition].userId == newUsers[newItemPosition].userId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldUsers[oldItemPosition] == newUsers[newItemPosition]
        }
    }
}
