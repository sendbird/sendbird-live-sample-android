package com.sendbird.live.uikit.sample.model

import com.sendbird.live.uikit.sample.R
import com.sendbird.webrtc.AudioDevice

enum class LiveAudioDevice(val nameResId: Int) {
    SYSTEM_DEFAULT(R.string.audio_device_system_default),
    EARPIECE(R.string.audio_device_earpiece),
    SPEAKERPHONE(R.string.audio_device_speakerphone),
    WIRED_HEADSET(R.string.audio_device_wired_headset),
    BLUETOOTH(R.string.audio_device_bluetooth)
}

fun AudioDevice?.toLiveAudioDevice(): LiveAudioDevice {
    return when (this) {
        AudioDevice.EARPIECE -> LiveAudioDevice.EARPIECE
        AudioDevice.SPEAKERPHONE -> LiveAudioDevice.SPEAKERPHONE
        AudioDevice.WIRED_HEADSET -> LiveAudioDevice.WIRED_HEADSET
        AudioDevice.BLUETOOTH -> LiveAudioDevice.BLUETOOTH
        null -> LiveAudioDevice.SYSTEM_DEFAULT
    }
}
fun LiveAudioDevice.toAudioDevice(): AudioDevice? {
    return when (this) {
        LiveAudioDevice.SYSTEM_DEFAULT -> null
        LiveAudioDevice.EARPIECE -> AudioDevice.EARPIECE
        LiveAudioDevice.SPEAKERPHONE -> AudioDevice.SPEAKERPHONE
        LiveAudioDevice.WIRED_HEADSET -> AudioDevice.WIRED_HEADSET
        LiveAudioDevice.BLUETOOTH -> AudioDevice.BLUETOOTH
    }
}