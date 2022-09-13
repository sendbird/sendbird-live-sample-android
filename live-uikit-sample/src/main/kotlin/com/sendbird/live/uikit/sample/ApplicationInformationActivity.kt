package com.sendbird.live.uikit.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import com.sendbird.live.uikit.SendbirdLiveUIKit
import com.sendbird.live.uikit.sample.databinding.ActivityApplicationInformationBinding

class ApplicationInformationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityApplicationInformationBinding.inflate(
            LayoutInflater.from(this)
        )

        binding.tvApplicationInformationId.text = SendbirdLiveUIKit.applicationId
        binding.ivApplicationInformationLeftButton.setOnClickListener { finish() }

        setContentView(binding.root)
    }
}