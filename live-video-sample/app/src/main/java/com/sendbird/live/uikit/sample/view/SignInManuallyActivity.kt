package com.sendbird.live.uikit.sample.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.android.SendbirdChat
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.InitResultHandler
import com.sendbird.android.params.InitParams
import com.sendbird.android.params.UserUpdateParams
import com.sendbird.live.AuthenticateParams
import com.sendbird.live.SendbirdLive
import com.sendbird.live.uikit.sample.R
import com.sendbird.live.uikit.sample.databinding.ActivitySignInManuallyBinding
import com.sendbird.live.uikit.sample.util.PrefManager
import com.sendbird.live.uikit.sample.util.showToast

class SignInManuallyActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInManuallyBinding
    private lateinit var prefManager: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInManuallyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefManager = PrefManager(this)
        initView()
    }

    private fun initView() {
        val versionInfo = "${getString(R.string.sdk)} ${SendbirdLive.VERSION}"
        binding.tvVersionInfo.text = versionInfo
        binding.btnSignIn.setOnClickListener {
            binding.btnSignIn.isEnabled = false
            val appId = "${binding.etAppId.text}"
            val userId = "${binding.etUserId.text}"
            val accessToken = "${binding.etAccessToken.text}"
            authenticate(appId, userId, accessToken)
        }
    }

    private fun initSendbirdSDK(
        appId: String,
        userId: String,
        accessToken: String?,
        isSucceed: (Boolean) -> Unit
    ) {
        SendbirdChat.init(InitParams(appId, applicationContext, true), object : InitResultHandler {
            override fun onInitFailed(e: SendbirdException) {
                isSucceed.invoke(false)
            }

            override fun onInitSucceed() {
                val params = com.sendbird.live.InitParams(appId, applicationContext)
                SendbirdLive.init(params, object : com.sendbird.live.handler.InitResultHandler {
                    override fun onInitFailed(e: com.sendbird.webrtc.SendbirdException) {
                        isSucceed.invoke(false)
                    }

                    override fun onInitSucceed() {
                        isSucceed.invoke(true)
                    }

                    override fun onMigrationStarted() {
                    }
                })
            }

            override fun onMigrationStarted() {
            }
        })
    }

    private fun authenticate(appId: String, userId: String, accessToken: String?) {
        if (appId.isBlank()) {
            showToast(R.string.error_message_enter_the_app_id)
            binding.btnSignIn.isEnabled = true
            return
        }
        if (userId.isBlank()) {
            showToast(R.string.error_message_enter_the_user_id)
            binding.btnSignIn.isEnabled = true
            return
        }
        initSendbirdSDK(appId, userId, accessToken) {
            if (it) {
                val params = AuthenticateParams(userId = userId, accessToken = accessToken).apply {
                }
                SendbirdLive.authenticate(params) auth@{ user, e ->
                    if (e != null || user == null) {
                        showToast(e?.message ?: getString(R.string.error_message_sign_in))
                        binding.btnSignIn.isEnabled = true
                        return@auth
                    }
                    SendbirdChat.updateCurrentUserInfo(UserUpdateParams().apply {
                        nickname = userId
                    }, null)
                    prefManager.appId = appId
                    prefManager.userId = userId
                    prefManager.accessToken = accessToken
                    startActivity(Intent(this@SignInManuallyActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}