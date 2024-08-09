package com.sendbird.live.uikit.sample.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.live.SendbirdLive
import com.sendbird.live.uikit.sample.R
import com.sendbird.live.uikit.sample.adapter.HostListAdapter
import com.sendbird.live.uikit.sample.databinding.ActivityHostAndCandidatesListBinding
import com.sendbird.live.uikit.sample.util.INTENT_KEY_LIVE_EVENT_ID

class HostAndCandidatesListActivity : AppCompatActivity() {
    lateinit var binding: ActivityHostAndCandidatesListBinding
    private lateinit var liveEventId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        liveEventId = intent.getStringExtra(INTENT_KEY_LIVE_EVENT_ID) ?: run {
            finish()
            return
        }
        binding = ActivityHostAndCandidatesListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.ivBack.setOnClickListener { finish() }
        initHostListView()
    }

    private fun initHostListView() {
        val adapter = HostListAdapter()
        binding.rvHostList.adapter = adapter

        SendbirdLive.getLiveEvent(liveEventId) { liveEvent, e ->
            if (liveEvent == null) {
                finish()
                return@getLiveEvent
            }
            val currentUserId = SendbirdLive.currentUser?.userId
            adapter.addItems(liveEvent.userIdsForHost.map { if (it == currentUserId) "$it (${getString(R.string.you)})" else it })
        }
    }
}