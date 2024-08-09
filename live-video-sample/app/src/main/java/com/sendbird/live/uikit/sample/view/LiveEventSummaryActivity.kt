package com.sendbird.live.uikit.sample.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.sendbird.live.uikit.sample.R
import com.sendbird.live.uikit.sample.databinding.ActivityLiveEventSummaryBinding
import com.sendbird.live.uikit.sample.util.INTENT_KEY_LIVE_EVENT_COVER_URL
import com.sendbird.live.uikit.sample.util.INTENT_KEY_LIVE_EVENT_DURATION
import com.sendbird.live.uikit.sample.util.INTENT_KEY_LIVE_EVENT_ENDED_AT
import com.sendbird.live.uikit.sample.util.INTENT_KEY_LIVE_EVENT_ENDED_BY
import com.sendbird.live.uikit.sample.util.INTENT_KEY_LIVE_EVENT_PEAK_PARTICIPANTS
import com.sendbird.live.uikit.sample.util.INTENT_KEY_LIVE_EVENT_TOTAL_PARTICIPANTS
import com.sendbird.live.uikit.sample.util.convertDateString
import com.sendbird.live.uikit.sample.util.threeQuotes
import com.sendbird.live.uikit.sample.util.toTimerFormat


class LiveEventSummaryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLiveEventSummaryBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val coverUrl = intent.getStringExtra(INTENT_KEY_LIVE_EVENT_COVER_URL) ?: ""
        val totalParticipants = intent.getIntExtra(INTENT_KEY_LIVE_EVENT_TOTAL_PARTICIPANTS, 0)
        val peakParticipants = intent.getIntExtra(INTENT_KEY_LIVE_EVENT_PEAK_PARTICIPANTS, 0)
        val duration = intent.getLongExtra(INTENT_KEY_LIVE_EVENT_DURATION, 0L)
        val endedAt = intent.getLongExtra(INTENT_KEY_LIVE_EVENT_ENDED_AT, 0L)
        val endedBy = intent.getStringExtra(INTENT_KEY_LIVE_EVENT_ENDED_BY) ?: ""
        binding = ActivityLiveEventSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView(coverUrl, totalParticipants, peakParticipants, duration, endedAt, endedBy)
    }

    private fun initView(coverUrl: String, totalParticipants: Int, peakParticipants: Int, duration: Long, endedAt: Long, endedBy: String) {
        binding.ivClose.setOnClickListener { finish() }
        binding.ivLiveThumbnail.load(coverUrl) {
            error(R.drawable.icon_default_user)
        }
        binding.tvLiveEventSummaryTotalParticipants.text = totalParticipants.threeQuotes()
        binding.tvLiveEventSummaryPeakParticipant.text = peakParticipants.threeQuotes()
        binding.tvLiveEventSummaryDuration.text = duration.toTimerFormat()
        binding.tvLiveEventSummaryEndedAt.text = endedAt.convertDateString()
        binding.tvLiveEventSummaryEndedBy.text = endedBy
    }
}
