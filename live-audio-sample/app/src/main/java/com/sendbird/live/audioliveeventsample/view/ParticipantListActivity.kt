package com.sendbird.live.audioliveeventsample.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.handler.UsersHandler
import com.sendbird.android.user.query.ParticipantListQuery
import com.sendbird.live.SendbirdLive
import com.sendbird.live.audioliveeventsample.adapter.ParticipantListAdapter
import com.sendbird.live.audioliveeventsample.databinding.ActivityParticipantListBinding
import com.sendbird.live.audioliveeventsample.util.INTENT_KEY_LIVE_EVENT_ID
import com.sendbird.live.audioliveeventsample.util.showToast

class ParticipantListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityParticipantListBinding
    private lateinit var liveEventId: String

    private var participantListQuery: ParticipantListQuery? = null
    private var adapter: ParticipantListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        liveEventId = intent.getStringExtra(INTENT_KEY_LIVE_EVENT_ID) ?: run {
            finish()
            return
        }
        binding = ActivityParticipantListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.ivBack.setOnClickListener { finish() }
        initParticipantListView()
    }

    private fun initParticipantListView() {
        adapter = ParticipantListAdapter()
        binding.rvParticipantList.adapter = adapter
        binding.rvParticipantList.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (!recyclerView.canScrollVertically(1)) {
                        loadParticipants()
                    }
                }
            }
        )
        loadParticipants()
    }

    private fun loadParticipants() {
        val handler = UsersHandler { users, e ->
            when {
                users != null -> adapter?.addItems(users.toMutableList())
                e != null -> showToast(e.message ?: "")
            }
        }

        if (participantListQuery == null) {
            SendbirdLive.getLiveEvent(liveEventId) { liveEvent, _ ->
                this.participantListQuery = liveEvent?.openChannel?.createParticipantListQuery()
                participantListQuery?.next(handler)
            }
        } else {
            participantListQuery?.next(handler)
        }
    }
}