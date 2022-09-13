package com.sendbird.live.uikit.sample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.android.SendbirdChat
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.InitResultHandler
import com.sendbird.live.AuthenticateParams
import com.sendbird.live.SendbirdLive
import com.sendbird.live.uikit.sample.databinding.ActivityAuthenticationBinding
import com.sendbird.live.uikit.SendbirdLiveUIKit
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.adapter.SendbirdUIKitAdapter
import com.sendbird.uikit.interfaces.UserInfo

class AuthenticationActivity : AppCompatActivity() {
    lateinit var binding: ActivityAuthenticationBinding
    var isAuthenticating: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        val sharedPreference = this.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        val savedUserId = sharedPreference.getString(KEY_USER_ID, "")
        val savedAccessToken = sharedPreference.getString(KEY_ACCESS_TOKEN, "")

        binding.etUserId.setText(savedUserId)
        binding.etAccessToken.setText(savedAccessToken)
        binding.tvLiveSdkVersion.text = "Live ${SendbirdLive.VERSION}"
        binding.tvChatSdkVersion.text = "SDK ${SendbirdChat.sdkVersion}"
        binding.buttonSignIn.setOnClickListener {
            if (isAuthenticating) return@setOnClickListener
            isAuthenticating = true

            val applicationId = binding.etApplicationId.text?.toString() ?: return@setOnClickListener
            val userId = binding.etUserId.text?.toString() ?: return@setOnClickListener
            val accessToken = binding.etAccessToken.text?.toString()

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
                                isAuthenticating = false
                                Toast.makeText(this@AuthenticationActivity, e.toString(), Toast.LENGTH_SHORT).show()
                            }

                            override fun onInitSucceed() {
                                Log.e("nathan", "onInitSucceed()")
                                isAuthenticating = false

                                with(sharedPreference.edit()) {
                                    putString(KEY_USER_ID, userId)
                                    putString(KEY_ACCESS_TOKEN, accessToken)
                                    apply()
                                }

                                SendbirdLiveUIKit.connect { _, e ->
                                    if (e != null) {
                                        Toast.makeText(this@AuthenticationActivity, e.toString(), Toast.LENGTH_SHORT).show()
                                        return@connect
                                    }

                                    val intent = Intent(this@AuthenticationActivity, SendbirdLiveActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            }

                            override fun onMigrationStarted() {

                            }
                        }
                    }
                },
                this
            )
        }
    }

    companion object {
        const val SHARED_PREFERENCES_NAME = "authentication_shared_preferences"
        const val KEY_USER_ID = "user_id"
        const val KEY_ACCESS_TOKEN = "access_token"
    }
}
