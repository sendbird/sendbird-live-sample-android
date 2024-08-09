package com.sendbird.live.uikit.sample.util

import android.view.View

fun interface OnItemClickListener<T> {
    fun onItemClick(view: View, position: Int, data: T)
}
