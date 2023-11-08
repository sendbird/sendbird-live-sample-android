package com.sendbird.live.videoliveeventsample.view

import android.util.Log
import com.sendbird.live.Host
import com.sendbird.live.LiveEvent
import com.sendbird.live.LiveEventState
import com.sendbird.live.ParticipantCountInfo
import com.sendbird.live.SendbirdLive
import com.sendbird.live.videoliveeventsample.R
import com.sendbird.live.videoliveeventsample.model.TextBottomSheetDialogItem
import com.sendbird.live.videoliveeventsample.util.audioNameResId
import com.sendbird.live.videoliveeventsample.util.displayFormat
import com.sendbird.live.videoliveeventsample.util.showSheetDialog
import com.sendbird.live.videoliveeventsample.util.showSheetRadioDialog
import com.sendbird.live.videoliveeventsample.util.showToast
import com.sendbird.webrtc.SendbirdException

class LiveEventForHostActivity : LiveEventActivity() {
    private var hostExitDialogItems: List<TextBottomSheetDialogItem> = listOf(
        TextBottomSheetDialogItem(
            DIALOG_ITEM_END_LIVE_EVENT,
            R.string.dialog_message_host_do_end,
            R.style.Text14Error200
        ),
        TextBottomSheetDialogItem(
            DIALOG_ITEM_EXIT_LIVE_EVENT,
            R.string.dialog_message_host_do_exit,
            R.style.Text14Primary200
        ),
        TextBottomSheetDialogItem(
            DIALOG_ITEM_CANCEL_LIVE_EVENT,
            R.string.cancel,
            R.style.Text14Primary200
        )
    )

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
            }
        }
        binding.ivMore.setOnClickListener {
            showAudioDeviceDialog()
        }
        binding.ivSwitch.setOnClickListener {
            liveEvent?.hosts?.firstOrNull { it.userId == SendbirdLive.currentUser?.userId } ?: run {
                showToast(R.string.error_message_error_description)
                return@setOnClickListener
            }
            val liveEvent = liveEvent ?: run {
                showToast(R.string.error_message_error_description)
                return@setOnClickListener
            }
            liveEvent.switchCamera { e ->
                if (e != null) {
                    showToast(e.message ?: "")
                }
            }
        }
        binding.ivMic.setOnClickListener {
            val currentHost = liveEvent?.hosts?.firstOrNull { it.hostId == (liveEvent?.currentLiveEventUser as Host).hostId } ?: run {
                showToast(R.string.error_message_error_description)
                return@setOnClickListener
            }
            val liveEvent = liveEvent ?: run {
                showToast(R.string.error_message_error_description)
                return@setOnClickListener
            }
            if (currentHost.isAudioOn) {
                liveEvent.muteAudioInput { e ->
                    if (e != null) {
                        showToast(e.message ?: "")
                    }
                    binding.ivMic.setImageResource(R.drawable.icon_audio_off)
                    updateHostVideoView(currentHost)
                }
            } else {
                liveEvent.unmuteAudioInput { e ->
                    if (e != null) {
                        showToast(e.message ?: "")
                    }
                    binding.ivMic.setImageResource(R.drawable.icon_audio_on)
                    updateHostVideoView(currentHost)
                }
            }
        }
        binding.ivVideo.setOnClickListener {
            val currentHost = liveEvent?.hosts?.firstOrNull { it.hostId == (liveEvent?.currentLiveEventUser as Host).hostId } ?: run {
                showToast(R.string.error_message_error_description)
                return@setOnClickListener
            }
            val liveEvent = liveEvent ?: run {
                showToast(R.string.error_message_error_description)
                return@setOnClickListener
            }
            Log.v("zzzzzz", "currentHost.isVideoOn ${currentHost.isVideoOn}")
            if (currentHost.isVideoOn) {
                liveEvent.stopVideo { e ->
                    if (e != null) {
                        showToast(e.message ?: "")
                    }
                    Log.v("zzzzzz", "currentHost.isVideoOn result ${currentHost.isVideoOn}")
                    binding.ivVideo.setImageResource(R.drawable.icon_video_off)
                    updateHostVideoView(currentHost)
                }
            } else {
                liveEvent.startVideo { e ->
                    if (e != null) {
                        showToast(e.message ?: "")
                    }
                    Log.v("zzzzzz", "currentHost.isVideoOn result ${currentHost.isVideoOn}")
                    binding.ivVideo.setImageResource(R.drawable.icon_video_on)
                    updateHostVideoView(currentHost)
                }
            }
        }
    }

    override var liveEventListenerImpl = object : LiveEventListenerImpl() {
        override fun onLiveEventStarted(liveEvent: LiveEvent) {
            startTimer(0L)
            setLiveStateView(liveEvent.state)
            binding.tvParticipantCount.text = liveEvent.participantCount.displayFormat()
        }
        override fun onDisconnected(liveEvent: LiveEvent, e: SendbirdException) {
            finish()
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
            updateHostVideoView(host)
        }
        override fun onHostStopVideo(liveEvent: LiveEvent, host: Host) {
            updateHostVideoView(host)
        }
        override fun onHostMuteAudio(liveEvent: LiveEvent, host: Host) {
            updateHostVideoView(host)
        }
        override fun onHostUnmuteAudio(liveEvent: LiveEvent, host: Host) {
            updateHostVideoView(host)
        }
        override fun onLiveEventEnded(liveEvent: LiveEvent) {
            finishLiveEvent(true)
        }
        override fun onParticipantCountChanged(liveEvent: LiveEvent, participantCountInfo: ParticipantCountInfo) {
            binding.tvParticipantCount.text = participantCountInfo.participantCount.displayFormat()
        }
    }

    override fun customOnBackPressed() {
        showEndDialog()
    }

    private fun showEndDialog() {
        showSheetDialog(
            title = getString(R.string.dialog_message_host_end),
            titleAppearance = R.style.Text18OnDark01,
            backgroundDrawableRes = R.drawable.shape_sheet_dialog_background,
            items = hostExitDialogItems
        ) { view, _, data ->
            when (data.id) {
                DIALOG_ITEM_END_LIVE_EVENT -> finishLiveEvent(isEnded = true, doEnd = true)
                DIALOG_ITEM_EXIT_LIVE_EVENT -> finishLiveEvent(isEnded = false, doEnd = false)
                else -> {}
            }
        }
    }

    private fun finishLiveEvent(isEnded: Boolean, doEnd: Boolean) {
        finishLiveEvent(isEnded)
        if (doEnd) {
            liveEvent?.endEvent(null)
        } else {
            liveEvent?.exitAsHost(null)
        }
    }

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

    private companion object {
        const val DIALOG_ITEM_END_LIVE_EVENT = 0
        const val DIALOG_ITEM_EXIT_LIVE_EVENT = 1
        const val DIALOG_ITEM_CANCEL_LIVE_EVENT = 2
    }
}
