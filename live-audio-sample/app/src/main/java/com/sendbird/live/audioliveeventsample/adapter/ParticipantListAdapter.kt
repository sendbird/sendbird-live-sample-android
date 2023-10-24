package com.sendbird.live.audioliveeventsample.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.sendbird.android.user.User
import com.sendbird.live.audioliveeventsample.R
import com.sendbird.live.audioliveeventsample.databinding.ViewUserListItemBinding

internal class ParticipantListAdapter : RecyclerView.Adapter<ParticipantListAdapter.ViewHolder>() {
    private val participants = mutableListOf<User>()

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
        holder.bind(participants[position])
    }

    override fun getItemCount(): Int {
        return participants.size
    }

    fun addItems(users: List<User>) {
        val cachedParticipants = mutableListOf<User>().apply {
            addAll(participants.toMutableList())
            addAll(users)
        }
        val diffCallback = UserDiffUtilCallback(participants, cachedParticipants)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        diffResult.dispatchUpdatesTo(this)
        participants.clear()
        participants.addAll(cachedParticipants)
    }

    class ViewHolder(
        private val binding: ViewUserListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.ivUserIdsForHostListItemMore.visibility = View.INVISIBLE
            binding.tvUserIdsForHostListItemUserName.text = user.displayName
            binding.ivUserIdsForHostListItemProfile.load(user.profileUrl) {
                error(R.drawable.icon_default_user)
                placeholder(R.drawable.icon_default_user)
            }
        }
    }

    class UserDiffUtilCallback(
        private val oldUsers: List<User>,
        private val newUsers: List<User>
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

private val User.displayName: String
    get() = nickname.ifEmpty { userId }