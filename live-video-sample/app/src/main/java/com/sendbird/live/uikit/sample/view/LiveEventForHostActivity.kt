package com.sendbird.live.uikit.sample.view

import com.sendbird.live.Host
import com.sendbird.live.LiveEvent
import com.sendbird.live.LiveEventState
import com.sendbird.live.ParticipantCountInfo
import com.sendbird.live.SendbirdLive
import com.sendbird.live.uikit.sample.R
import com.sendbird.live.uikit.sample.model.TextBottomSheetDialogItem
import com.sendbird.live.uikit.sample.util.audioNameResId
import com.sendbird.live.uikit.sample.util.displayFormat
import com.sendbird.live.uikit.sample.util.showSheetDialog
import com.sendbird.live.uikit.sample.util.showSheetRadioDialog
import com.sendbird.live.uikit.sample.util.showToast
import com.sendbird.webrtc.SendbirdException
import com.sendbird.webrtc.handler.CompletionHandler

class LiveEventForHostActivity : LiveEventActivity() {
    private var doStartVideo: Boolean = false
    private val currentHost: Host?
        get() =
            if (liveEvent?.currentLiveEventUser !is Host) null
            else liveEvent?.currentLiveEventUser as Host?


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
                    return@setEventReady
                }
            }
        }
    }

    override fun initLiveEventView() {
        super.initLiveEventView()
        binding.ivClose.setOnClickListener { showEndDialog() }
        binding.tvTimer.setOnClickListener {
            if (liveEvent?.state == LiveEventState.ONGOING || liveEvent?.state == LiveEventState.ENDED) return@setOnClickListener
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
                    updateHostsVideoView()
                }
            } else {
                liveEvent.unmuteAudioInput { e ->
                    if (e != null) {
                        showToast(e.message ?: "")
                    }
                    binding.ivMic.setImageResource(R.drawable.icon_audio_on)
                    updateHostsVideoView()
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
            if (currentHost.isVideoOn) {
                liveEvent.stopVideo { e ->
                    if (e != null) {
                        showToast(e.message ?: "")
                    }
                    binding.ivVideo.setImageResource(R.drawable.icon_video_off)
                    updateHostsVideoView()
                }
            } else {
                liveEvent.startVideo { e ->
                    if (e != null) {
                        showToast(e.message ?: "")
                    }
                    binding.ivVideo.setImageResource(R.drawable.icon_video_on)
                    updateHostsVideoView()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (doStartVideo) {
            liveEvent?.startVideo(null)
        }
        doStartVideo = false
    }

    override fun onPause() {
        super.onPause()
        val host = currentHost ?: return
        doStartVideo = host.isVideoOn
        if (doStartVideo) {
            liveEvent?.stopVideo(null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        liveEvent?.exitAsHost(null)
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
        override fun onExited(liveEvent: LiveEvent, e: SendbirdException) {
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
        val handler = CompletionHandler { finishLiveEvent(isEnded) }
        if (doEnd) {
            liveEvent?.endEvent(handler)
        } else {
            liveEvent?.exitAsHost(handler)
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
