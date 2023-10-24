package com.sendbird.live.audioliveeventsample.view

import android.content.Intent
import android.view.View
import com.sendbird.live.*
import com.sendbird.live.audioliveeventsample.R
import com.sendbird.live.audioliveeventsample.model.TextBottomSheetDialogItem
import com.sendbird.live.audioliveeventsample.util.*
import com.sendbird.uikit.consts.KeyboardDisplayType
import com.sendbird.uikit.fragments.OpenChannelFragment
import com.sendbird.webrtc.SendbirdException
import java.util.*

class LiveEventForHostActivity : LiveEventActivity() {

    override fun attachToLiveEvent() {
        super.attachToLiveEvent()
        if (liveEvent?.state == LiveEventState.CREATED) {
            liveEvent?.setEventReady { e ->
                if (e != null) {
                    showToast(e.message ?: "")
                    finish()
                    return@setEventReady
                }
            }
        }
    }

    override fun initLiveEventView() {
        super.initLiveEventView()
        binding.ivClose.setOnClickListener { showEndDialog() }
        binding.tvTimer.setOnClickListener {
            liveEvent?.startEvent { e ->
                if (e != null) {
                    showToast(e.message ?: "")
                    return@startEvent
                }
                startTimer(0L)
                setLiveStateView(liveEvent?.state ?: LiveEventState.ONGOING)
                binding.ivActiveBackground.visibility = View.VISIBLE
            }
        }
        binding.ivMore.setOnClickListener {
            showAudioDeviceDialog()
        }
        binding.ivMic.setOnClickListener {
            val isAudioOn = liveEvent?.host?.isAudioOn ?: run {
                showToast("LiveEvent unavailable")
                return@setOnClickListener
            }
            val liveEvent = liveEvent ?: run {
                showToast("LiveEvent unavailable")
                return@setOnClickListener
            }
            if (isAudioOn) {
                liveEvent.muteAudioInput { e ->
                    if (e != null) {
                        showToast(e.message ?: "")
                    }
                    binding.ivMic.setImageResource(R.drawable.icon_audio_off)
                }
            } else {
                liveEvent.unmuteAudioInput { e ->
                    if (e != null) {
                        showToast(e.message ?: "")
                    }
                    binding.ivMic.setImageResource(R.drawable.icon_audio_on)
                }
            }
        }
    }

    override var liveEventListenerImpl = object : LiveEventListenerImpl() {
        override fun onDisconnected(liveEvent: LiveEvent, e: SendbirdException) {
            finish()
        }

        override fun onReactionCountUpdated(liveEvent: LiveEvent, key: String, count: Int) {
            runOnUiThread {
                distributeReactionAnimations(key, count)
            }
        }

        override fun onParticipantCountChanged(liveEvent: LiveEvent, participantCountInfo: ParticipantCountInfo) {
            binding.tvParticipantCount.text = "${participantCountInfo.participantCount}".attachAffix(getString(R.string.participant_count_affix))
        }
    }

    override fun customOnBackPressed() {
        showEndDialog()
    }

    private fun showEndDialog() {
        showAlertDialog(
            title = getString(R.string.dialog_message_host_end),
            posText = getString(R.string.dialog_message_host_do_end),
            negText = getString(R.string.cancel),
            backgroundDrawableResId = R.style.DarkDialog,
            positiveButtonFunction = { finishLiveEvent(true) }
        )
    }

    override fun finishLiveEvent(isEnded: Boolean) {
        super.finishLiveEvent(isEnded)
        liveEvent?.endEvent { e ->
            if (e != null) {
                showToast(e.message ?: "")
            }
        }
    }

    override fun getOpenChannelFragment(liveEventId: String) =
        OpenChannelFragment.Builder(liveEventId)
            .setCustomFragment(
                openChannelFragment.apply {
                    this.onHeaderRightButtonClickListener = View.OnClickListener {
                        val intent = Intent(this@LiveEventForHostActivity, LiveEventDetailActivity::class.java).apply {
                            putExtra(INTENT_KEY_LIVE_EVENT_ID, liveEventId)
                        }
                        startActivity(intent)
                    }
                    this.headerRightButtonResourceId = com.sendbird.uikit.R.drawable.icon_info
                    this.reactionButtonVisibility = View.GONE
                }
            )
            .setKeyboardDisplayType(KeyboardDisplayType.Dialog)
            .build()

    private fun showAudioDeviceDialog() {
        val audioDeviceItems = liveEvent?.availableAudioDevices?.toList() ?: return
        val availableAudioDialogItems = audioDeviceItems.mapIndexed { index, audioDevice -> TextBottomSheetDialogItem(index, audioDevice.audioNameResId(), R.style.Text16OnDark01) }
        showSheetRadioDialog(
            title = getString(R.string.audio_device),
            titleAppearance = R.style.Text18OnDark01,
            backgroundDrawableRes = R.drawable.shape_sheet_dialog_background,
            items = availableAudioDialogItems,
            checkedItemPosition = audioDeviceItems.indexOf(liveEvent?.currentAudioDevice)
        ) { position, dialogItem ->
            if (dialogItem != null) {
                liveEvent?.selectAudioDevice(audioDeviceItems[position]) { e ->
                    if (e != null) {
                        showToast(e.message ?: "")
                    }
                }
            }
        }
    }
}
