package com.sendbird.live.videoliveeventsample.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView.GONE
import androidx.recyclerview.widget.RecyclerView.VISIBLE
import coil.load
import com.sendbird.live.Host
import com.sendbird.live.LiveEvent
import com.sendbird.live.LiveEventListener
import com.sendbird.live.LiveEventRole
import com.sendbird.live.LiveEventState
import com.sendbird.live.ParticipantCountInfo
import com.sendbird.live.SendbirdLive
import com.sendbird.live.videoliveeventsample.R
import com.sendbird.live.videoliveeventsample.adapter.HostAdapter
import com.sendbird.live.videoliveeventsample.databinding.ActivityLiveEventBinding
import com.sendbird.live.videoliveeventsample.util.CountUpTimer
import com.sendbird.live.videoliveeventsample.util.INTENT_KEY_LIVE_EVENT_COVER_URL
import com.sendbird.live.videoliveeventsample.util.INTENT_KEY_LIVE_EVENT_DURATION
import com.sendbird.live.videoliveeventsample.util.INTENT_KEY_LIVE_EVENT_ENDED_AT
import com.sendbird.live.videoliveeventsample.util.INTENT_KEY_LIVE_EVENT_ENDED_BY
import com.sendbird.live.videoliveeventsample.util.INTENT_KEY_LIVE_EVENT_ID
import com.sendbird.live.videoliveeventsample.util.INTENT_KEY_LIVE_EVENT_PEAK_PARTICIPANTS
import com.sendbird.live.videoliveeventsample.util.INTENT_KEY_LIVE_EVENT_TOTAL_PARTICIPANTS
import com.sendbird.live.videoliveeventsample.util.displayFormat
import com.sendbird.live.videoliveeventsample.util.showToast
import com.sendbird.live.videoliveeventsample.util.toTimerFormat
import com.sendbird.webrtc.SendbirdException
import java.util.UUID

const val LIVE_EVENT_LISTENER_ID = "LIVE_EVENT_LISTENER_ID"
abstract class LiveEventActivity : AppCompatActivity() {
    private var liveEventId: String? = null
    private var countUpTimer: CountUpTimer? = null
    private var cachedCoverUrl: String? = null
    private lateinit var adapter: HostAdapter
    protected lateinit var binding: ActivityLiveEventBinding
    protected var liveEvent: LiveEvent? = null

    protected abstract var liveEventListenerImpl: LiveEventListenerImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveEventBinding.inflate(layoutInflater)
        liveEventId = intent.getStringExtra(INTENT_KEY_LIVE_EVENT_ID)
        setContentView(binding.root)
//        openChannelFragment = LiveEventOpenChannelFragment()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                this@LiveEventActivity.customOnBackPressed()
            }
        })
        getLiveEvent()
    }

    abstract fun customOnBackPressed()

    protected open fun attachToLiveEvent() {
        liveEvent ?: run {
            showToast("LiveEvent unavailable")
            return
        }
    }

    protected open fun initLiveEventView() {
        val optionVisibility = if (liveEvent?.isActiveHost == true) View.VISIBLE else View.GONE
        binding.ivMore.visibility = optionVisibility
        binding.ivSwitch.visibility = optionVisibility
        binding.ivMic.visibility = optionVisibility
        binding.ivVideo.visibility = optionVisibility
        binding.tvTimer.visibility = optionVisibility
        setLiveStateView(liveEvent?.state ?: LiveEventState.CREATED)
        if (liveEvent?.state == LiveEventState.ONGOING) {
            startTimer(liveEvent?.duration ?: 0L)
        }
        binding.viewSetting.setOnClickListener {
            if (binding.clSetting.isVisible) {
                binding.clSetting.visibility = GONE
                binding.viewOverlay.visibility = GONE
            } else {
                binding.clSetting.visibility = VISIBLE
                binding.viewOverlay.visibility = VISIBLE
            }
        }
        binding.tvParticipantCount.text = (liveEvent?.participantCount ?: 0).displayFormat()
        updateToolbarView()
    }

    protected fun updateToolbarView() {
        cachedCoverUrl = liveEvent?.coverUrl
        binding.ivCover.load(liveEvent?.coverUrl) {
            error(R.drawable.icon_logo_live_onlight_03)
        }
        binding.tvTitle.text = if (!liveEvent?.title.isNullOrEmpty()) liveEvent?.title else getString(R.string.live_event)
        binding.tvDescription.text = liveEvent?.hosts?.joinToString(", ") { it.userId } ?: ""
//        binding.tvDescription.text = liveEvent?.host?.userId ?: ""
    }

    protected fun initHostView() {
        val hosts = liveEvent?.hosts
        if (!hosts.isNullOrEmpty()) {
            adapter.addItems(hosts)
        }
    }

    protected fun addHostVideoView(host: Host) {
        adapter.addItems(listOf(host))
    }

    protected fun removeHostVideoView(host: Host) {
        adapter.removeItems(listOf(host))
    }

    protected fun updateHostsVideoView() {
        adapter.notifyDataSetChanged()
    }

    protected open fun finishLiveEvent(isEnded: Boolean = false) {
        stopTimer()
        if (isEnded) {
            val activity = if (liveEvent?.myRole == LiveEventRole.HOST) LiveEventSummaryActivity::class.java else LiveEventEndedActivity::class.java
            val intent = Intent(this, activity)
            intent.putExtra(INTENT_KEY_LIVE_EVENT_COVER_URL, cachedCoverUrl)
            intent.putExtra(INTENT_KEY_LIVE_EVENT_TOTAL_PARTICIPANTS, liveEvent?.cumulativeParticipantCount)
            intent.putExtra(INTENT_KEY_LIVE_EVENT_PEAK_PARTICIPANTS, liveEvent?.peakParticipantCount)
            intent.putExtra(INTENT_KEY_LIVE_EVENT_DURATION, liveEvent?.duration)
            intent.putExtra(INTENT_KEY_LIVE_EVENT_ENDED_AT, liveEvent?.endedAt)
            intent.putExtra(INTENT_KEY_LIVE_EVENT_ENDED_BY, liveEvent?.endedBy)
            startActivity(intent)
        }
        liveEvent?.removeListener(LIVE_EVENT_LISTENER_ID)
        finish()
    }

    private fun getLiveEvent() {
        val liveEventId = liveEventId ?: run {
            finish()
            return
        }
        val liveEvent = SendbirdLive.getCachedLiveEvent(liveEventId) ?: run {
            finish()
            return
        }
        this.liveEvent = liveEvent
        adapter = HostAdapter(liveEvent)
        binding.rvLiveView.adapter = adapter
        binding.rvLiveView.layoutManager = GridLayoutManager(this, 2).apply {
            spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val listSize = adapter.itemCount
                    return when {
                        listSize <= 2 -> 2
                        listSize == 3 && position == 0 -> 2
                        listSize == 3 -> 1
                        listSize == 4 -> 1
                        listSize == 5 -> 1
                        else -> 1
                    }
                }
            }
        }

        liveEvent.addListener(LIVE_EVENT_LISTENER_ID, liveEventListenerImpl)
        attachToLiveEvent()
        initHostView()
        initLiveEventView()
    }

    protected fun setLiveStateView(state: LiveEventState) {
//        val (indicatorRes, stateName) = when(state) {
//            LiveEventState.CREATED, LiveEventState.READY -> Pair(R.drawable.shape_live_event_pause_indicator, getString(R.string.open))
//            LiveEventState.ONGOING ->  Pair(R.drawable.shape_live_event_ongoing_indicator, getString(R.string.live))
//            LiveEventState.ENDED ->  Pair(R.drawable.shape_live_event_pause_indicator, getString(R.string.ended))
//        }
//        binding.indicator.setBackgroundResource(indicatorRes)
//        binding.tvState.text = stateName
    }

    protected fun startTimer(baseTime: Long) {
        if (countUpTimer == null) {
            binding.tvTimer.visibility = View.VISIBLE
            binding.tvTimer.setTextAppearance(R.style.TimerButtonRed01Style)
            binding.tvTimer.setBackgroundResource(R.drawable.shape_timer_background_red)
            countUpTimer = CountUpTimer(baseTime) {
                runOnUiThread {
                    binding.tvTimer.text = it.toTimerFormat()
                }
            }.apply { start() }
        }
    }

    private fun stopTimer() {
        countUpTimer?.stop()
        countUpTimer = null
    }

    open class LiveEventListenerImpl : LiveEventListener {
        override fun onCustomItemsDelete(liveEvent: LiveEvent, customItems: Map<String, String>, deletedKeys: List<String>) {}
        override fun onCustomItemsUpdate(liveEvent: LiveEvent, customItems: Map<String, String>, updatedKeys: List<String>) {}
        override fun onDisconnected(liveEvent: LiveEvent, e: SendbirdException) {}
        override fun onExited(liveEvent: LiveEvent, e: SendbirdException) {}
        override fun onHostConnected(liveEvent: LiveEvent, host: Host) {}
        override fun onHostDisconnected(liveEvent: LiveEvent, host: Host) {}
        override fun onHostEntered(liveEvent: LiveEvent, host: Host) {}
        override fun onHostExited(liveEvent: LiveEvent, host: Host) {}
        override fun onHostMuteAudio(liveEvent: LiveEvent, host: Host) {}
        override fun onHostStartVideo(liveEvent: LiveEvent, host: Host) {}
        override fun onHostStopVideo(liveEvent: LiveEvent, host: Host) {}
        override fun onHostUnmuteAudio(liveEvent: LiveEvent, host: Host) {}
        override fun onLiveEventEnded(liveEvent: LiveEvent) {}
        override fun onLiveEventInfoUpdated(liveEvent: LiveEvent) {}
        override fun onLiveEventReady(liveEvent: LiveEvent) {}
        override fun onLiveEventStarted(liveEvent: LiveEvent) {}
        override fun onParticipantCountChanged(liveEvent: LiveEvent, participantCountInfo: ParticipantCountInfo) {}
        override fun onReactionCountUpdated(liveEvent: LiveEvent, key: String, count: Int) {}
        override fun onReconnected(liveEvent: LiveEvent) {}


    }

}