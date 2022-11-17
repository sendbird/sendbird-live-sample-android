package com.sendbird.live.uikit.sample

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.ScanOptions
import com.sendbird.live.uikit.sample.databinding.ActivityAuthenticationBinding
import com.sendbird.live.uikit.sample.utils.AuthUtils
import com.sendbird.live.uikit.sample.utils.QRUtils

class AuthenticationActivity : AppCompatActivity() {
    lateinit var binding: ActivityAuthenticationBinding
    private val qrCodeLauncher = QRUtils.createQRLauncher(this) { isSuccess, qrString ->
        if (isSuccess && !qrString.isNullOrBlank()) {
            val (applicationId, userId, accessToken) = QRUtils.getAuthInfoFromQRCode(qrString)
            if (applicationId.isNullOrBlank() || userId.isNullOrBlank()) {
                Toast.makeText(this, getString(R.string.text_invalid_qrcode), Toast.LENGTH_SHORT).show()
                return@createQRLauncher
            }
            AuthUtils.auth(this, applicationId, userId, accessToken) { isAuthSuccess, errorMessage ->
                if (isAuthSuccess) {
                    val intent = Intent(this, SendbirdLiveActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                } else {
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.btSignInWithQR.setOnClickListener {
            val qrCodeOptions = ScanOptions().apply {
                this.setOrientationLocked(true)
                this.setPrompt(getString(R.string.text_qr_code_prop))
                this.setBeepEnabled(false)
            }
            qrCodeLauncher.launch(qrCodeOptions)
        }
        binding.btSignInWithId.setOnClickListener {
            val intent = Intent(this, SignInManuallyActivity::class.java)
            startActivity(intent)
        }
    }
}