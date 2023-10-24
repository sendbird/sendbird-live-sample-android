package com.sendbird.live.uikit.sample

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.live.uikit.sample.databinding.ActivitySignInManuallyBinding
import com.sendbird.live.uikit.sample.utils.AuthUtils

class SignInManuallyActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignInManuallyBinding
    var isAuthenticating: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInManuallyBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        val sharedPreference = this.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        val savedAppId = sharedPreference.getString(KEY_APP_ID, "")
        val savedUserId = sharedPreference.getString(KEY_USER_ID, "")
        val savedAccessToken = sharedPreference.getString(KEY_ACCESS_TOKEN, "")

        binding.etApplicationId.setText(savedAppId)
        binding.etUserId.setText(savedUserId)
        binding.etAccessToken.setText(savedAccessToken)
        binding.btSignIn.setOnClickListener {
            if (isAuthenticating) return@setOnClickListener
            isAuthenticating = true

            val applicationId = binding.etApplicationId.text?.toString() ?: return@setOnClickListener
            val userId = binding.etUserId.text?.toString() ?: return@setOnClickListener
            val accessToken = binding.etAccessToken.text?.toString()

            AuthUtils.auth(this, applicationId, userId, accessToken) { isSuccess, errorMessage ->
                isAuthenticating = false
                if (isSuccess) {
                    with(sharedPreference.edit()) {
                        putString(KEY_APP_ID, applicationId)
                        putString(KEY_USER_ID, userId)
                        putString(KEY_ACCESS_TOKEN, accessToken)
                        apply()
                    }
                    isAuthenticating
                    val intent = Intent(this, SendbirdLiveActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                } else {
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.ivHeaderLeftButton.setOnClickListener { finish() }
    }

    companion object {
        const val SHARED_PREFERENCES_NAME = "authentication_shared_preferences"
        const val KEY_APP_ID = "app_id"
        const val KEY_USER_ID = "user_id"
        const val KEY_ACCESS_TOKEN = "access_token"
    }
}
