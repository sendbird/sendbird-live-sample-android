package com.sendbird.live.videoliveeventsample.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.sendbird.live.videoliveeventsample.R
import com.sendbird.live.videoliveeventsample.databinding.ActivityLiveEventSummaryBinding
import com.sendbird.live.videoliveeventsample.util.INTENT_KEY_LIVE_EVENT_COVER_URL
import com.sendbird.live.videoliveeventsample.util.INTENT_KEY_LIVE_EVENT_DURATION
import com.sendbird.live.videoliveeventsample.util.INTENT_KEY_LIVE_EVENT_PEAK_PARTICIPANTS
import com.sendbird.live.videoliveeventsample.util.INTENT_KEY_LIVE_EVENT_TOTAL_PARTICIPANTS
import com.sendbird.live.videoliveeventsample.util.threeQuotes
import com.sendbird.live.videoliveeventsample.util.toTimerFormat


class LiveEventSummaryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLiveEventSummaryBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val coverUrl = intent.getStringExtra(INTENT_KEY_LIVE_EVENT_COVER_URL) ?: ""
        val totalParticipants = intent.getIntExtra(INTENT_KEY_LIVE_EVENT_TOTAL_PARTICIPANTS, 0)
        val peakParticipants = intent.getIntExtra(INTENT_KEY_LIVE_EVENT_PEAK_PARTICIPANTS, 0)
        val duration = intent.getLongExtra(INTENT_KEY_LIVE_EVENT_DURATION, 0L)
        binding = ActivityLiveEventSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView(coverUrl, totalParticipants, peakParticipants, duration)
    }

    private fun initView(coverUrl: String, totalParticipants: Int, peakParticipants: Int, duration: Long) {
        binding.ivClose.setOnClickListener { finish() }
        binding.ivLiveThumbnail.load(coverUrl) {
            error(R.drawable.icon_default_user)
        }
        binding.tvLiveEventSummaryTotalParticipants.text = totalParticipants.threeQuotes()
        binding.tvLiveEventSummaryPeakParticipant.text = peakParticipants.threeQuotes()
        binding.tvLiveEventSummaryDuration.text = duration.toTimerFormat()
    }
}