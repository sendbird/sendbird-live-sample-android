package com.sendbird.live.videoliveeventsample.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.sendbird.live.Host
import com.sendbird.live.LiveEvent
import com.sendbird.live.videoliveeventsample.R
import com.sendbird.live.videoliveeventsample.databinding.ListItemHostBinding

class HostAdapter(private val liveEvent: LiveEvent) :
    RecyclerView.Adapter<HostAdapter.HostViewHolder>() {
    private var hostMap: LinkedHashMap<String, Host> = LinkedHashMap()

    private var parentWidth: Int = 0
    private var parentHeight: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HostViewHolder {
        parentWidth = parent.width
        parentHeight = parent.height
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        val binding: ListItemHostBinding = ListItemHostBinding.inflate(inflater, parent, false)
        return HostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HostViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        if (hostMap.size <= 1) {
            holder.binding.root.layoutParams = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            holder.binding.clVideoViewContainer.layoutParams = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        } else if (hostMap.size in 2..4) {
            holder.binding.root.layoutParams = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                parentHeight.div(2)
            )
            holder.binding.clVideoViewContainer.layoutParams = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        } else {
            holder.binding.root.layoutParams = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                parentHeight.div(3)
            )
            holder.binding.clVideoViewContainer.layoutParams = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        holder.bind(liveEvent, hostMap.values.elementAt(position))
    }

    override fun getItemCount(): Int = hostMap.size

    fun addItems(hosts: List<Host>) {
        val newHostMap = LinkedHashMap<String, Host>().apply {
            this.putAll(hostMap)
        }
        hosts.forEach {
            newHostMap[it.hostId] = it
        }
        val diffCallback = HostDiffUtilCallback(hostMap.values.toList(), newHostMap.values.toList())
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        diffResult.dispatchUpdatesTo(this)
        this.hostMap.clear()
        this.hostMap.putAll(newHostMap)
        notifyDataSetChanged()
    }

    fun removeItems(hosts: List<Host>) {
        val newHostMap = LinkedHashMap<String, Host>().apply {
            this.putAll(hostMap)
        }
        hosts.forEach {
            newHostMap.remove(it.hostId)
        }
        val diffCallback = HostDiffUtilCallback(hostMap.values.toList(), newHostMap.values.toList())
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        diffResult.dispatchUpdatesTo(this)
        this.hostMap.clear()
        this.hostMap.putAll(newHostMap)
        notifyDataSetChanged()
    }

    fun updateItemView(hostId: String) {
        val targetPosition = hostMap.keys.indexOfFirst { it == hostId }
        notifyItemChanged(targetPosition)
    }


    class HostViewHolder(
        val binding: ListItemHostBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        var hostVideoView: View? = null

        fun bind(liveEvent: LiveEvent, host: Host) {
            binding.tvHostId.text = host.userId
            isVideoOn(binding, host.isVideoOn)
            isAudioOn(binding, host.isAudioOn)
            if (hostVideoView != binding.svvHost) {
                hostVideoView = binding.svvHost
                liveEvent.setVideoViewForLiveEvent(binding.svvHost, host.hostId)
            }
            binding.ivHostProfile.load(host.profileURL) {
                crossfade(true)
                placeholder(R.drawable.icon_default_user)
                error(R.drawable.icon_default_user)
            }
        }

        private fun isVideoOn(binding: ListItemHostBinding, on: Boolean) {
            if (on) {
                binding.svvHost.visibility = View.VISIBLE
                binding.cvHostProfile.visibility = View.GONE
            } else {
                binding.svvHost.visibility = View.INVISIBLE
                binding.cvHostProfile.visibility = View.VISIBLE
            }
        }

        private fun isAudioOn(binding: ListItemHostBinding, on: Boolean) {
            if (on) {
                binding.ivHostMuted.visibility = View.GONE
            } else {
                binding.ivHostMuted.visibility = View.VISIBLE
            }
        }
    }

    class HostDiffUtilCallback(
        private val oldHosts: List<Host>,
        private val newHosts: List<Host>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldHosts.size
        }

        override fun getNewListSize(): Int {
            return newHosts.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldHosts[oldItemPosition] == newHosts[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldHosts[oldItemPosition].hostId == newHosts[newItemPosition].hostId &&
                    oldHosts[oldItemPosition].isAudioOn == newHosts[newItemPosition].isAudioOn &&
                    oldHosts[oldItemPosition].isVideoOn == newHosts[newItemPosition].isVideoOn
        }
    }
}

fun Int.spanCount() =
    if (this <= 1) 1
    else 2

fun Int.ratioDivisor() =
    if (this <= 1) 1
    else if (this in 2..4) 2
    else 3
