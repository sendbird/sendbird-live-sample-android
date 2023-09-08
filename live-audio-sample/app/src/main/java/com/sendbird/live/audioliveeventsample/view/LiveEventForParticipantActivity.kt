package com.sendbird.live.audioliveeventsample.view

import android.content.Intent
import android.util.Log
import android.view.View
import com.sendbird.android.SendbirdChat
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.handler.OpenChannelHandler
import com.sendbird.android.message.BaseMessage
import com.sendbird.live.*
import com.sendbird.live.audioliveeventsample.R
import com.sendbird.live.audioliveeventsample.util.INTENT_KEY_LIVE_EVENT_ID
import com.sendbird.live.audioliveeventsample.util.attachAffix
import com.sendbird.uikit.consts.KeyboardDisplayType
import com.sendbird.uikit.fragments.OpenChannelFragment
import com.sendbird.webrtc.SendbirdException
import java.util.*

const val KEY_LIVE_EVENT_LIKE_REACTION = "LIKE"

class LiveEventForParticipantActivity : LiveEventActivity() {

    override fun initLiveEventView() {
        super.initLiveEventView()
        binding.ivClose.setOnClickListener { finishLiveEvent(false) }
        if (liveEvent?.state == LiveEventState.READY) {
            showBanner(getString(R.string.banner_message_live_event_ready))
        }
    }

    override fun attachToLiveEvent() {
        super.attachToLiveEvent()
        SendbirdChat.addChannelHandler("${UUID.randomUUID()}", object : OpenChannelHandler() {
            override fun onMessageReceived(channel: BaseChannel, message: BaseMessage) {}
            override fun onChannelChanged(channel: BaseChannel) {
                openChannelFragment.title = liveEvent?.host?.userId?.attachAffix(binding.root.context.getString(R.string.live_event_title_affix)) ?: liveEvent?.title ?: binding.root.context.getString(R.string.live_event)
                openChannelFragment.profileImageUrl = liveEvent?.host?.profileURL ?: liveEvent?.coverUrl
            }
        })
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
        override fun onHostConnected(liveEvent: LiveEvent, host: Host) {
            showBanner(null)
            binding.ivActiveBackground.visibility = View.VISIBLE
        }
        override fun onHostDisconnected(liveEvent: LiveEvent, host: Host) {
            showBanner(getString(R.string.banner_message_host_disconnected))
            binding.ivActiveBackground.visibility = View.GONE
        }
        override fun onHostMuteAudio(liveEvent: LiveEvent, host: Host) {
            showBanner(getString(R.string.banner_message_host_muted))
        }
        override fun onHostUnmuteAudio(liveEvent: LiveEvent, host: Host) {
            showBanner(null)
        }
        override fun onLiveEventEnded(liveEvent: LiveEvent) {
            finishLiveEvent(true)
        }
        override fun onLiveEventStarted(liveEvent: LiveEvent) {
            showBanner(null)
            startTimer(0L)
            setLiveStateView(liveEvent.state)
            binding.tvParticipantCount.text = "${liveEvent.participantCount}".attachAffix(getString(
                R.string.participant_count_affix))
            binding.ivActiveBackground.visibility = View.VISIBLE
        }

        override fun onParticipantCountChanged(liveEvent: LiveEvent, participantCountInfo: ParticipantCountInfo) {
            binding.tvParticipantCount.text = "${liveEvent.participantCount}".attachAffix(getString(
                R.string.participant_count_affix))
        }

        override fun onReactionCountUpdated(liveEvent: LiveEvent, key: String, count: Int) {
            runOnUiThread {
                distributeReactionAnimations(key, count)
            }
        }
    }

    override fun getOpenChannelFragment(liveEventId: String) =
        OpenChannelFragment.Builder(liveEventId)
            .setCustomFragment(
                openChannelFragment.apply {
                    this.onHeaderRightButtonClickListener = View.OnClickListener {
                        val intent = Intent(this@LiveEventForParticipantActivity, ParticipantListActivity::class.java).apply {
                            putExtra(INTENT_KEY_LIVE_EVENT_ID, liveEventId)
                        }
                        startActivity(intent)
                    }
                    this.headerRightButtonResourceId = com.sendbird.uikit.R.drawable.icon_members
                    this.onHeaderReactionButtonClickListener = View.OnClickListener {
                        increaseReactionCount()
                    }
                    this.reactionButtonVisibility = View.VISIBLE
                }
            )
            .setKeyboardDisplayType(KeyboardDisplayType.Dialog)
            .useOverlayMode()
            .build()

    private fun showBanner(text: String?) {
        if (text == null) {
            binding.tvBanner.visibility = View.GONE
        } else {
            binding.tvBanner.text = text
            binding.tvBanner.visibility = View.VISIBLE
        }
    }

    private fun increaseReactionCount() {
        liveEvent?.increaseReactionCount(KEY_LIVE_EVENT_LIKE_REACTION) increaseReactionCountLabel@{ reactionCountMap, e ->
            if (e != null) {
                return@increaseReactionCountLabel
            }
            reactionCountMap?.forEach {
                distributeReactionAnimations(it.key, it.value)
            }
        }
    }
}
