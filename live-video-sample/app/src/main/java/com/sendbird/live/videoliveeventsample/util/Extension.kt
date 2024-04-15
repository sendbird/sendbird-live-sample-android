package com.sendbird.live.videoliveeventsample.util

import android.Manifest
import android.content.Context
import android.content.res.Resources
import com.sendbird.live.videoliveeventsample.R
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

fun String.toPermissionText(context: Context) : String {
    return when (this){
        Manifest.permission.CAMERA -> context.getString(R.string.permission_text_camera)
        Manifest.permission.RECORD_AUDIO -> context.getString(R.string.permission_text_record_audio)
        Manifest.permission.CALL_PHONE -> context.getString(R.string.permission_text_call_phone)
        Manifest.permission.READ_PHONE_STATE -> context.getString(R.string.permission_text_read_phone_state)
        Manifest.permission.BLUETOOTH_ADMIN -> context.getString(R.string.permission_text_bluetooth_admin)
        Manifest.permission.BLUETOOTH -> context.getString(R.string.permission_text_bluetooth)
        Manifest.permission.BLUETOOTH_CONNECT -> context.getString(R.string.permission_text_bluetooth_connect)
        else -> context.getString(R.string.permission_text_unknown)
    }
}

fun String.attachAffix(text: String) = String.format(text, this)

fun Long.toTimerFormat(): String {
    val time = this / 1000
    val sec = (time % 60).toTwoDigits()
    val min = (time / 60 % 60).toTwoDigits()
    val hour = (time / 60 / 60).toTwoDigits()
    return "$hour:$min:$sec"
}

fun Long.toTwoDigits() = "%02d".format(this)

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).roundToInt()

fun Int.threeQuotes(): String {
    val decimalFormat = DecimalFormat("#,###")
    return decimalFormat.format(this)
}


fun Long.toDateFormat(): String {
    return try {
        val date = Date(this)
        val dateFormat = SimpleDateFormat("EEE dd, yyyy 'at' h:mm a", Locale.getDefault())
        dateFormat.format(date)
    } catch (e: Exception) {
        ""
    }
}

fun Int.displayFormat(): String {
    return if (this > 10000) {
        "${this.div(1000)}K"
    } else if (this > 1000) {
        String.format("%.1fK", this.toDouble().div(1000))
    } else {
        this.toString()
    }
}

fun Long.convertDateString(): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.KOREA)
    val date = Date(this)
    return dateFormat.format(date)
}