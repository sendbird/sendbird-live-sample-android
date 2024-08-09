package com.sendbird.live.uikit.sample.model

import android.os.Parcelable
import com.sendbird.live.SendbirdLive
import kotlinx.parcelize.Parcelize

@Parcelize
data class SelectedHostUser(val userId: String, val nickname: String, val profileUrl: String? = null) : Parcelable {
    fun isMyUser(): Boolean {
        return userId == SendbirdLive.currentUser?.userId
    }

    val displayName: String
        get() = nickname.takeIf { it.isNotEmpty() } ?: userId
}
