package com.sendbird.live.videoliveeventsample.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.sendbird.live.videoliveeventsample.R
import com.sendbird.live.videoliveeventsample.databinding.ActivityLiveEventEndedBinding
import com.sendbird.live.videoliveeventsample.util.INTENT_KEY_LIVE_EVENT_COVER_URL


class LiveEventEndedActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLiveEventEndedBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val coverUrl = intent.getStringExtra(INTENT_KEY_LIVE_EVENT_COVER_URL) ?: ""
        binding = ActivityLiveEventEndedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView(coverUrl)
    }

    private fun initView(coverUrl: String) {
        binding.ivClose.setOnClickListener { finish() }
        binding.ivLiveThumbnail.load(coverUrl) {
            error(R.drawable.icon_default_user)
        }
    }
}