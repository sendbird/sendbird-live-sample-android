package com.sendbird.live.audioliveeventsample.util

import android.os.Looper
import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<T>.changeValue(value: T) {
    if (Looper.getMainLooper() == Looper.myLooper()) {
        setValue(value)
    } else {
        postValue(value)
    }
}