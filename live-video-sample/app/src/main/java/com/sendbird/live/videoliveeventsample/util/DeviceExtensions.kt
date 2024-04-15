
package com.sendbird.live.videoliveeventsample.util

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.sendbird.live.videoliveeventsample.R
import com.sendbird.live.videoliveeventsample.model.LiveAudioDevice
import com.sendbird.live.videoliveeventsample.model.toLiveAudioDevice
import com.sendbird.webrtc.AudioDevice
import com.sendbird.webrtc.VideoDevice
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator

internal fun VideoDevice.cameraNameResId(): Int {
    return when (this.position) {
        VideoDevice.Position.FRONT -> R.string.camera_device_front
        VideoDevice.Position.BACK -> R.string.camera_device_back
        else -> R.string.camera_device_unknown
    }
}

internal fun CameraManager.getAvailableVideoDevices(context: Context): List<VideoDevice>{
    val videoDevices = mutableListOf<VideoDevice>()
    val cameraEnumerator = if (Camera2Enumerator.isSupported(context)) {
        Camera2Enumerator(context)
    } else {
        Camera1Enumerator(true)
    }

    cameraEnumerator.deviceNames.forEach {
        val position = when {
            cameraEnumerator.isBackFacing(it) -> VideoDevice.Position.BACK
            cameraEnumerator.isFrontFacing(it) -> VideoDevice.Position.FRONT
            else -> VideoDevice.Position.UNSPECIFIED
        }

        val device = VideoDevice.createVideoDevice(it, position, this.getCameraCharacteristics(it))
        if (!canCauseCrash(device)) {
            videoDevices.add(device)
        }
    }
    return videoDevices
}

private fun canCauseCrash(videoDevice: VideoDevice?): Boolean {
    videoDevice?.cameraCharacteristics ?: return false
    val characteristics = videoDevice.cameraCharacteristics
    val streamMap = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) ?: return true
    val nativeSizes = streamMap.getOutputSizes(SurfaceTexture::class.java)
    return nativeSizes == null
}

fun AudioDevice.audioNameResId(): Int {
    return when (this) {
        AudioDevice.EARPIECE -> R.string.audio_device_earpiece
        AudioDevice.SPEAKERPHONE -> R.string.audio_device_speakerphone
        AudioDevice.WIRED_HEADSET -> R.string.audio_device_wired_headset
        AudioDevice.BLUETOOTH -> R.string.audio_device_bluetooth
    }
}

internal fun AudioManager.getAvailableLiveAudioDevice(context: Context): Array<LiveAudioDevice> {
    val audioDevices: MutableList<LiveAudioDevice> = mutableListOf()
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter = bluetoothManager.adapter
    audioDevices.add(LiveAudioDevice.SYSTEM_DEFAULT)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            if (bluetoothAdapter != null && BluetoothAdapter.STATE_CONNECTED == bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET)) {
                audioDevices.add(AudioDevice.BLUETOOTH.toLiveAudioDevice())
            }
        }
    } else {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
            if (bluetoothAdapter != null && BluetoothAdapter.STATE_CONNECTED == bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET)) {
                audioDevices.add(AudioDevice.BLUETOOTH.toLiveAudioDevice())
            }
        }
    }

    if (hasWiredHeadset()) {
        // If a wired headset is connected, then it is the only possible option.
        audioDevices.add(AudioDevice.WIRED_HEADSET.toLiveAudioDevice())
    } else {
        // No wired headset, hence the audio-device list can contain speaker
        // phone (on a tablet), or speaker phone and earpiece (on mobile phone).
        audioDevices.add(AudioDevice.SPEAKERPHONE.toLiveAudioDevice())
        if (context.hasEarpiece()) {
            audioDevices.add(AudioDevice.EARPIECE.toLiveAudioDevice())
        }
    }
    return audioDevices.toTypedArray()
}

internal fun Context.hasEarpiece(): Boolean { // [CALLS]
    return this.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
}

private fun AudioManager.hasWiredHeadset(): Boolean {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        this.isWiredHeadsetOn
    } else {
        val devices = this.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        for (device in devices) {
            val type = device.type
            if (type == AudioDeviceInfo.TYPE_WIRED_HEADSET) {
                // Log.d(TAG, "hasWiredHeadset: found wired headset");
                return true
            } else if (type == AudioDeviceInfo.TYPE_USB_DEVICE) {
                // Log.d(TAG, "hasWiredHeadset: found USB audio device");
                return true
            }
        }
        false
    }
}
