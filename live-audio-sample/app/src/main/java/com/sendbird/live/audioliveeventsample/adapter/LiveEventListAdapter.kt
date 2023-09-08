package com.sendbird.live.audioliveeventsample.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.sendbird.live.LiveEvent
import com.sendbird.live.LiveEventState
import com.sendbird.live.audioliveeventsample.R
import com.sendbird.live.audioliveeventsample.databinding.ListItemLiveEventBinding
import com.sendbird.live.audioliveeventsample.util.attachAffix
import com.sendbird.uikit.activities.viewholder.BaseViewHolder
import com.sendbird.uikit.interfaces.OnItemClickListener

open class LiveEventListAdapter : RecyclerView.Adapter<LiveEventListAdapter.LiveEventListHolder>() {
    private val liveEventList = mutableListOf<LiveEvent>()
    private val cachedLiveEventInfoList = mutableListOf<LiveEventInfo>()
    var onItemClickListener: OnItemClickListener<LiveEvent>? = null
    var emptyStateView: View? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LiveEventListHolder {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        val binding: ListItemLiveEventBinding = ListItemLiveEventBinding.inflate(inflater, parent, false)
        return LiveEventListHolder(binding)
    }

    override fun onBindViewHolder(holder: LiveEventListHolder, position: Int) {
        val liveEvent = liveEventList[position]
        holder.bind(liveEvent)
        holder.itemView.setOnClickListener {
            val channelPosition = holder.bindingAdapterPosition
            if (channelPosition != RecyclerView.NO_POSITION && onItemClickListener != null) {
                onItemClickListener?.onItemClick(it, channelPosition, liveEventList[channelPosition])
            }
        }
    }

    override fun getItemCount() = liveEventList.size

    fun addItems(liveEvents: List<LiveEvent>?) {
        if (liveEvents != null) {
            val list = mutableListOf<LiveEvent>().apply {
                addAll(liveEventList)
                addAll(liveEvents)
            }
            submitList(list.toList())
        }
    }

    fun submitList(liveEvents: List<LiveEvent>) {
        val diffCallback = LiveEventDiffCallback(cachedLiveEventInfoList, liveEvents)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        cachedLiveEventInfoList.clear()
        cachedLiveEventInfoList.addAll(LiveEventInfo.toLiveEventInfoList(liveEvents))
        liveEventList.clear()
        liveEventList.addAll(liveEvents)
        diffResult.dispatchUpdatesTo(this)
        checkEmpty()
    }

    private fun checkEmpty() {
        val visibility = if (liveEventList.isEmpty()) View.VISIBLE else View.GONE
        emptyStateView?.visibility = visibility
    }

    class LiveEventListHolder(private val binding: ListItemLiveEventBinding) :
        BaseViewHolder<LiveEvent>(binding.root) {
        override fun bind(liveEvent: LiveEvent) {
            binding.ivLiveThumbnail.load(liveEvent.coverUrl) {
                crossfade(true)
                placeholder(R.drawable.icon_user)
                error(R.drawable.icon_user)
            }
            binding.tvTitle.text = liveEvent.title
            binding.tvParticipantCount.text = "${liveEvent.participantCount}".attachAffix(binding.root.context.getString(R.string.watching_count_affix))

            val (stringResId, textAppearance, backgroundResId) = when (liveEvent.state) {
                LiveEventState.CREATED -> Triple(R.string.upcoming, R.style.Text12Primary300Bold, R.drawable.shape_live_state_created_background)
                LiveEventState.READY -> Triple(R.string.open, R.style.Text12Open300Bold, R.drawable.shape_live_state_ready_background)
                LiveEventState.ONGOING -> Triple(R.string.live, R.style.Text12OnDark01Bold, R.drawable.shape_live_state_ongoing_background)
                LiveEventState.ENDED -> Triple(R.string.ended, R.style.Text12OnLight02Bold, R.drawable.shape_live_state_ended_background)
            }
            with(binding.tvLiveEventStatus) {
                this.text = context.getString(stringResId)
                setTextAppearance(textAppearance)
                setBackgroundResource(backgroundResId)
            }

        }
    }

    private class LiveEventDiffCallback(
        private val oldItems: List<LiveEventInfo>,
        private val newItems: List<LiveEvent>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldItems.size
        }

        override fun getNewListSize(): Int {
            return newItems.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldItems[oldItemPosition]
            val newItem = newItems[newItemPosition]
            return oldItem.liveEventId == newItem.liveEventId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldItems[oldItemPosition]
            val newItem = newItems[newItemPosition]

            return oldItem.state == newItem.state
                    && oldItem.title == newItem.title
                    && oldItem.coverUrl == newItem.coverUrl
                    && oldItem.hostName == newItem.host?.nickname
                    && oldItem.isHostStreaming == newItem.isHostStreaming
                    && oldItem.participantCount == newItem.participantCount
        }
    }

    internal class LiveEventInfo(val liveEvent: LiveEvent) {
        val liveEventId: String = liveEvent.liveEventId
        val participantCount: Int = liveEvent.participantCount
        val title: String? = liveEvent.title
        val hostName: String? = liveEvent.host?.nickname
        val state: LiveEventState = liveEvent.state
        val isHostStreaming: Boolean = liveEvent.isHostStreaming
        val coverUrl: String? = liveEvent.coverUrl

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as LiveEventInfo
            if (liveEvent != other.liveEvent) return false
            if (liveEventId != other.liveEventId) return false
            if (participantCount != other.participantCount) return false
            if (title != other.title) return false
            if (hostName != other.hostName) return false
            if (state != other.state) return false
            if (isHostStreaming != other.isHostStreaming) return false
            if (coverUrl != other.coverUrl) return false
            return true
        }

        override fun hashCode(): Int {
            var result = liveEvent.hashCode()
            result = 31 * result + liveEventId.hashCode()
            result = 31 * result + participantCount
            result = 31 * result + title.hashCode()
            result = 31 * result + (hostName?.hashCode() ?: 0)
            result = 31 * result + state.hashCode()
            result = 31 * result + isHostStreaming.hashCode()
            result = 31 * result + coverUrl.hashCode()
            return result
        }

        companion object {
            fun toLiveEventInfoList(liveEventList: List<LiveEvent>): List<LiveEventInfo> {
                val results: MutableList<LiveEventInfo> = ArrayList()
                for (liveEvent in liveEventList) {
                    results.add(LiveEventInfo(liveEvent))
                }
                return results
            }
        }
    }
}
