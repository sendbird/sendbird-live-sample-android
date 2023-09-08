package com.sendbird.live.audioliveeventsample.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.live.audioliveeventsample.databinding.ViewUserListItemBinding

internal class HostListAdapter : RecyclerView.Adapter<HostListAdapter.ViewHolder>() {
    private val hostUserIds = mutableListOf<String>()

//    var hostUserIds: List<String> = emptyList()
//        set(value) {
//            val diffCallback = HostUserIdDiffUtilCallback(field, value)
//            val diffResult = DiffUtil.calculateDiff(diffCallback)
//            field = value
//            diffResult.dispatchUpdatesTo(this)
//        }

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
        holder.bind(hostUserIds[position])
    }

    override fun getItemCount(): Int {
        return hostUserIds.size
    }

    fun addItems(users: List<String>) {
        val cachedParticipants = mutableListOf<String>().apply {
            addAll(hostUserIds.toMutableList())
            addAll(users)
        }
        val diffCallback = HostUserIdDiffUtilCallback(hostUserIds, cachedParticipants)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        diffResult.dispatchUpdatesTo(this)
        hostUserIds.clear()
        hostUserIds.addAll(cachedParticipants)
    }

    class ViewHolder(
        private val binding: ViewUserListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(hostUserId: String) {
            binding.tvUserIdsForHostListItemUserName.text = hostUserId
        }
    }

    class HostUserIdDiffUtilCallback(
        private val oldHostUserIds: List<String>,
        private val newHostUserIds: List<String>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldHostUserIds.size
        }

        override fun getNewListSize(): Int {
            return newHostUserIds.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldHostUserIds[oldItemPosition] == newHostUserIds[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldHostUserIds[oldItemPosition] == newHostUserIds[newItemPosition]
        }
    }
}
