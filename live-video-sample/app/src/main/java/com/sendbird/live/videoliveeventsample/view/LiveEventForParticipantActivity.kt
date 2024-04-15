package com.sendbird.live.videoliveeventsample.view

import android.view.View
import com.sendbird.live.Host
import com.sendbird.live.LiveEvent
import com.sendbird.live.LiveEventState
import com.sendbird.live.ParticipantCountInfo
import com.sendbird.live.videoliveeventsample.R
import com.sendbird.live.videoliveeventsample.util.displayFormat
import com.sendbird.live.videoliveeventsample.util.showToast
import com.sendbird.webrtc.SendbirdException

class LiveEventForParticipantActivity : LiveEventActivity() {

    override fun initLiveEventView() {
        super.initLiveEventView()
        binding.ivClose.setOnClickListener { finishLiveEvent(false) }
        if (liveEvent?.state == LiveEventState.READY) {
            showBanner(getString(R.string.banner_message_live_event_ready))
        }
    }

    override fun customOnBackPressed() {
        finishLiveEvent(false)
    }

    override fun finishLiveEvent(isEnded: Boolean) {
        super.finishLiveEvent(isEnded)
        liveEvent?.exit(null)
    }

    override var liveEventListenerImpl = object : LiveEventListenerImpl() {
        override fun onDisconnected(liveEvent: LiveEvent, e: SendbirdException) {
            finish()
        }
        override fun onExited(liveEvent: LiveEvent, e: SendbirdException) {
            showToast(R.string.error_message_network)
            finish()
        }
        override fun onHostConnected(liveEvent: LiveEvent, host: Host) {
            showBanner(null)
        }
        override fun onHostDisconnected(liveEvent: LiveEvent, host: Host) {
            showBanner(getString(R.string.banner_message_host_disconnected))
        }
        override fun onHostEntered(liveEvent: LiveEvent, host: Host) {
            updateToolbarView()
            addHostVideoView(host)
        }
        override fun onHostExited(liveEvent: LiveEvent, host: Host) {
            updateToolbarView()
            removeHostVideoView(host)
        }
        override fun onHostStartVideo(liveEvent: LiveEvent, host: Host) {
            updateHostsVideoView()
        }
        override fun onHostStopVideo(liveEvent: LiveEvent, host: Host) {
            updateHostsVideoView()
        }
        override fun onHostMuteAudio(liveEvent: LiveEvent, host: Host) {
            updateHostsVideoView()
        }
        override fun onHostUnmuteAudio(liveEvent: LiveEvent, host: Host) {
            updateHostsVideoView()
        }
        override fun onLiveEventEnded(liveEvent: LiveEvent) {
            finishLiveEvent(true)
        }
        override fun onLiveEventStarted(liveEvent: LiveEvent) {
            showBanner(null)
            startTimer(0L)
            setLiveStateView(liveEvent.state)
            binding.tvParticipantCount.text = liveEvent.participantCount.displayFormat()
        }

        override fun onParticipantCountChanged(liveEvent: LiveEvent, participantCountInfo: ParticipantCountInfo) {
            binding.tvParticipantCount.text = liveEvent.participantCount.displayFormat()
        }

        override fun onReactionCountUpdated(liveEvent: LiveEvent, key: String, count: Int) {
        }
    }

    private fun showBanner(text: String?) {
        if (text == null) {
            binding.tvBanner.visibility = View.GONE
        } else {
            binding.tvBanner.text = text
            binding.tvBanner.visibility = View.VISIBLE
        }
    }
}
