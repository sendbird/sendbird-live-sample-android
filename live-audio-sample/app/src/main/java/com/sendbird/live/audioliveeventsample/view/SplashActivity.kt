package com.sendbird.live.audioliveeventsample.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.live.AuthenticateParams
import com.sendbird.live.SendbirdLive
import com.sendbird.live.audioliveeventsample.BaseApplication
import com.sendbird.live.audioliveeventsample.databinding.ActivitySplashBinding
import com.sendbird.live.audioliveeventsample.util.EventObserver
import com.sendbird.live.audioliveeventsample.util.PrefManager
import com.sendbird.live.audioliveeventsample.util.showToast

class SplashActivity : AppCompatActivity() {
    private lateinit var prefManager: PrefManager
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefManager = PrefManager(this)
        (application as BaseApplication).initResultLiveData.observe(this, EventObserver {
            if (it) {
                autoAuthenticate { isSucceed, e ->
                    if (e != null) showToast(e)
                    val intent = if (isSucceed) Intent(this, MainActivity::class.java) else Intent(this, SignInManuallyActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } else {
                val intent = Intent(this, SignInManuallyActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
    }

    private fun autoAuthenticate(callback: (Boolean, String?) -> Unit) {
        val appId = prefManager.appId
        val userId = prefManager.userId
        val accessToken = prefManager.accessToken
        if (appId == null || userId == null) {
            callback.invoke(false, null)
            return
        }

        val params = AuthenticateParams(userId, accessToken)
        SendbirdLive.authenticate(params) { user, e ->
            if (e != null || user == null) {
                callback.invoke(false, "${e?.message}")
                return@authenticate
            }
            callback.invoke(true, null)
        }
    }
}