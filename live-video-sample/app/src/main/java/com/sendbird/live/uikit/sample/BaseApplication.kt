package com.sendbird.live.uikit.sample

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sendbird.android.SendbirdChat
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.InitResultHandler
import com.sendbird.android.params.InitParams
import com.sendbird.live.SendbirdLive
import com.sendbird.live.uikit.sample.util.Event
import com.sendbird.live.uikit.sample.util.PrefManager
import com.sendbird.live.uikit.sample.util.changeValue
import com.sendbird.webrtc.internal.util.LogLevel

class BaseApplication : Application() {
    lateinit var prefManager: PrefManager
    private val _initResultLiveData = MutableLiveData<Event<Boolean>>()

    val initResultLiveData: LiveData<Event<Boolean>>
        get() = _initResultLiveData

    override fun onCreate() {
        super.onCreate()
        prefManager = PrefManager(applicationContext)
        val appId = prefManager.appId
        if (appId == null) {
            _initResultLiveData.changeValue(Event(false))
            return
        }
        initSendbirdSDK(appId)
    }

    private fun initSendbirdSDK(appId: String) {
        SendbirdChat.init(InitParams(appId, applicationContext, true), object : InitResultHandler {
            override fun onInitFailed(e: SendbirdException) {
                _initResultLiveData.changeValue(Event(false))
            }

            override fun onInitSucceed() {
                val params = com.sendbird.live.InitParams(appId, applicationContext)
                SendbirdLive.init(params, object : com.sendbird.live.handler.InitResultHandler {
                    override fun onInitFailed(e: com.sendbird.webrtc.SendbirdException) {
                        _initResultLiveData.changeValue(Event(false))
                    }

                    override fun onInitSucceed() {
                        SendbirdLive.setLoggerLevel(LogLevel.VERBOSE)
                        _initResultLiveData.changeValue(Event(true))
                    }

                    override fun onMigrationStarted() {
                    }
                })
            }

            override fun onMigrationStarted() {
            }
        })
    }
}