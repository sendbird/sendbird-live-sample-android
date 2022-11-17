package com.sendbird.live.uikit.sample.utils

import android.content.Context
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.InitResultHandler
import com.sendbird.live.uikit.SendbirdLiveUIKit
import com.sendbird.live.uikit.sample.R
import com.sendbird.uikit.adapter.SendbirdUIKitAdapter
import com.sendbird.uikit.interfaces.UserInfo

object AuthUtils {
    fun auth(
        context: Context,
        applicationId: String,
        userId: String,
        accessToken: String? = null,
        completionHandler: (isSuccess: Boolean, errorMessage: String?) -> Unit
    ) {
        if (applicationId.isBlank() || userId.isBlank()) {
            completionHandler.invoke(false, context.getString(R.string.text_invalid_qrcode))
            return
        }

        SendbirdLiveUIKit.init(
            object : SendbirdUIKitAdapter {
                override fun getAppId(): String = applicationId

                override fun getAccessToken(): String? = accessToken

                override fun getUserInfo(): UserInfo = object : UserInfo {
                    override fun getUserId(): String = userId

                    override fun getNickname(): String? = null

                    override fun getProfileUrl(): String? = null
                }

                override fun getInitResultHandler(): InitResultHandler {
                    return object : InitResultHandler {
                        override fun onInitFailed(e: SendbirdException) {
                            completionHandler.invoke(false, e.toString())
                        }

                        override fun onInitSucceed() {
                            SendbirdLiveUIKit.connect { _, e ->
                                if (e != null) {
                                    completionHandler.invoke(false, e.toString())
                                    return@connect
                                }
                                completionHandler.invoke(true, null)
                            }
                        }

                        override fun onMigrationStarted() {

                        }
                    }
                }
            },
            context
        )
    }
}