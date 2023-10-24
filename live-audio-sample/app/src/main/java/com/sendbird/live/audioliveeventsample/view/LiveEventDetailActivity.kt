package com.sendbird.live.audioliveeventsample.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.sendbird.live.LiveEvent
import com.sendbird.live.SendbirdLive
import com.sendbird.live.audioliveeventsample.R
import com.sendbird.live.audioliveeventsample.databinding.ActivityLiveEventDetailBinding
import com.sendbird.live.audioliveeventsample.util.INTENT_KEY_LIVE_EVENT_ID
import com.sendbird.live.audioliveeventsample.util.displayFormat
import com.sendbird.live.audioliveeventsample.util.toDateFormat

class LiveEventDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLiveEventDetailBinding
    private lateinit var liveEvent: LiveEvent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val liveEventId = intent.getStringExtra(INTENT_KEY_LIVE_EVENT_ID) ?: run {
            finish()
            return
        }
        liveEvent = SendbirdLive.getCachedLiveEvent(liveEventId)  ?: run {
            finish()
            return
        }
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.clLiveEventDetailParticipant.setOnClickListener {
            val intent = Intent(this, ParticipantListActivity::class.java).apply {
                putExtra(INTENT_KEY_LIVE_EVENT_ID, liveEventId)
            }
            startActivity(intent)
        }

        binding.clLiveEventDetailHost.setOnClickListener {
            val intent = Intent(this, HostAndCandidatesListActivity::class.java).apply {
                putExtra(INTENT_KEY_LIVE_EVENT_ID, liveEventId)
            }
            startActivity(intent)
        }
        initLiveEventDetailView(liveEvent)
    }

    private fun initLiveEventDetailView(liveEvent: LiveEvent) {
        with(binding) {
            liveEvent.coverUrl?.let { ivLiveEventDetailCover.load(it) {
                error(R.drawable.icon_default_user)
                placeholder(R.drawable.icon_default_user)
            } }
            tvLiveEventDetailTitle.text = if (!liveEvent.title.isNullOrEmpty()) liveEvent.title else getString(R.string.live_event)
            tvLiveEventDetailHostCount.text = liveEvent.userIdsForHost.size.toString()
            tvLiveEventDetailParticipantCount.text = liveEvent.participantCount.displayFormat()
            switchLiveEventDetailFreezeChannel.isSelected = liveEvent.openChannel?.isFrozen ?: false
            tvLiveEventDetailLiveEventId.text = liveEvent.liveEventId
            tvLiveEventDetailCreatedAt.text = liveEvent.createdAt.toDateFormat()
            tvLiveEventDetailCreatedBy.text = liveEvent.createdBy
            tvLiveEventDetailStartedAt.text = liveEvent.startedAt?.toDateFormat() ?: ""
            tvLiveEventDetailStartedBy.text = liveEvent.startedBy ?: ""
            switchLiveEventDetailFreezeChannel.isChecked = liveEvent.openChannel?.isFrozen ?: false
        }
    }
}
