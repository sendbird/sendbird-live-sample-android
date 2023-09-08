package com.sendbird.live.audioliveeventsample

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.InitResultHandler
import com.sendbird.live.SendbirdLive
import com.sendbird.live.audioliveeventsample.util.Event
import com.sendbird.live.audioliveeventsample.util.PrefManager
import com.sendbird.live.audioliveeventsample.util.changeValue
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.adapter.SendbirdUIKitAdapter
import com.sendbird.uikit.interfaces.UserInfo

class BaseApplication : Application() {
    lateinit var prefManager: PrefManager
    private val _initResultLiveData = MutableLiveData<Event<Boolean>>()

    val initResultLiveData: LiveData<Event<Boolean>>
        get() = _initResultLiveData

    override fun onCreate() {
        super.onCreate()
        prefManager = PrefManager(applicationContext)
        val appId = prefManager.appId
        val userId = prefManager.userId
        val accessToken = prefManager.accessToken
        if (appId == null || userId == null) {
            _initResultLiveData.changeValue(Event(false))
            return
        }
        initSendbirdSDK(appId, userId, accessToken)
    }

    private fun initSendbirdSDK(appId: String, userId: String, accessToken: String?) {
        SendbirdUIKit.init(object : SendbirdUIKitAdapter {
            override fun getAppId() = appId
            override fun getAccessToken() = accessToken
            override fun getUserInfo() = object : UserInfo {
                override fun getUserId(): String = userId
                override fun getNickname(): String? = null
                override fun getProfileUrl(): String? = null
            }

            override fun getInitResultHandler() = object : InitResultHandler {
                override fun onInitFailed(e: SendbirdException) {
                    _initResultLiveData.changeValue(Event(false))
                }
                override fun onInitSucceed() {
                    SendbirdUIKit.setDefaultThemeMode(SendbirdUIKit.ThemeMode.Dark)
                    val params = com.sendbird.live.InitParams(appId, applicationContext)
                    SendbirdLive.init(params, object : com.sendbird.live.handler.InitResultHandler {
                        override fun onInitFailed(e: com.sendbird.webrtc.SendbirdException) {
                            _initResultLiveData.changeValue(Event(false))
                        }

                        override fun onInitSucceed() {
                            _initResultLiveData.changeValue(Event(true))
                        }

                        override fun onMigrationStarted() {
                        }
                    })
                }
                override fun onMigrationStarted() {
                }
            }

        }, applicationContext)
    }
}