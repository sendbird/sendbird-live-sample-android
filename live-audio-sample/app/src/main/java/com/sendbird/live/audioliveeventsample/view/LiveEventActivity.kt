package com.sendbird.live.audioliveeventsample.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import coil.load
import com.sendbird.live.*
import com.sendbird.live.audioliveeventsample.R
import com.sendbird.live.audioliveeventsample.databinding.ActivityLiveEventBinding
import com.sendbird.live.audioliveeventsample.util.*
import com.sendbird.live.audioliveeventsample.view.fragment.LiveEventOpenChannelFragment
import com.sendbird.live.audioliveeventsample.view.widget.ReactionConstants
import com.sendbird.uikit.fragments.OpenChannelFragment
import com.sendbird.webrtc.SendbirdException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


abstract class LiveEventActivity : AppCompatActivity() {
    private var liveEventId: String? = null
    private var countUpTimer: CountUpTimer? = null
    private var cachedHostProfileUrl: String? = null
    protected lateinit var binding: ActivityLiveEventBinding
    protected lateinit var openChannelFragment: LiveEventOpenChannelFragment
    protected var liveEvent: LiveEvent? = null

    private val singleThreadExecutor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val reactionCountMap: ConcurrentHashMap<String, Int> = ConcurrentHashMap()

    protected abstract var liveEventListenerImpl: LiveEventListenerImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveEventBinding.inflate(layoutInflater)
        liveEventId = intent.getStringExtra(INTENT_KEY_LIVE_EVENT_ID)
        setContentView(binding.root)
        openChannelFragment = LiveEventOpenChannelFragment()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                this@LiveEventActivity.customOnBackPressed()
            }
        })
        getLiveEvent()
        initOpenChannelView()
    }

    abstract fun customOnBackPressed()

    protected open fun attachToLiveEvent() {
        liveEvent ?: run {
            showToast("LiveEvent unavailable")
            return
        }
    }

    protected open fun initLiveEventView() {
        val optionVisibility = if (liveEvent?.myRole == LiveEventRole.HOST) View.VISIBLE else View.GONE
        binding.ivMore.visibility = optionVisibility
        binding.ivMic.visibility = optionVisibility
        binding.tvTimer.visibility = optionVisibility
        binding.ivHostProfile.load(liveEvent?.coverUrl ?: liveEvent?.host?.profileURL) {
            crossfade(true)
            placeholder(R.drawable.icon_user)
            error(R.drawable.icon_user)
        }
        setLiveStateView(liveEvent?.state ?: LiveEventState.CREATED)
        if (liveEvent?.state == LiveEventState.ONGOING) {
            startTimer(liveEvent?.duration ?: 0L)
        }
        binding.tvParticipantCount.text = "${liveEvent?.participantCount ?: 0}".attachAffix(getString(R.string.participant_count_affix))
        cachedHostProfileUrl = liveEvent?.host?.profileURL
    }

    protected open fun finishLiveEvent(isEnded: Boolean = false) {
        stopTimer()
        if (isEnded) {
            val activity = if (liveEvent?.myRole == LiveEventRole.HOST) LiveEventSummaryActivity::class.java else LiveEventEndedActivity::class.java
            val intent = Intent(this, activity)
            intent.putExtra(INTENT_KEY_LIVE_EVENT_COVER_URL, cachedHostProfileUrl)
            intent.putExtra(INTENT_KEY_LIVE_EVENT_TOTAL_PARTICIPANTS, liveEvent?.cumulativeParticipantCount)
            intent.putExtra(INTENT_KEY_LIVE_EVENT_PEAK_PARTICIPANTS, liveEvent?.peakParticipantCount)
            intent.putExtra(INTENT_KEY_LIVE_EVENT_DURATION, liveEvent?.duration)
            startActivity(intent)
        }
        finish()
    }

    private fun getLiveEvent() {
        val liveEventId = liveEventId ?: run {
            finish()
            return
        }
        liveEvent = SendbirdLive.getCachedLiveEvent(liveEventId)
        liveEvent?.addListener("${UUID.randomUUID()}", liveEventListenerImpl)
        attachToLiveEvent()
        initLiveEventView()
    }

    protected fun setLiveStateView(state: LiveEventState) {
        val (indicatorRes, stateName) = when(state) {
            LiveEventState.CREATED, LiveEventState.READY -> Pair(R.drawable.shape_live_event_pause_indicator, getString(R.string.open))
            LiveEventState.ONGOING ->  Pair(R.drawable.shape_live_event_ongoing_indicator, getString(R.string.live))
            LiveEventState.ENDED ->  Pair(R.drawable.shape_live_event_pause_indicator, getString(R.string.ended))
        }
        binding.indicator.setBackgroundResource(indicatorRes)
        binding.tvState.text = stateName
    }

    protected fun startTimer(baseTime: Long) {
        if (countUpTimer == null) {
            binding.tvTimer.visibility = View.VISIBLE
            binding.tvTimer.setTextAppearance(R.style.TimerButtonRed01Style)
            binding.tvTimer.setBackgroundResource(R.drawable.shape_timer_background_red)
            countUpTimer = CountUpTimer(baseTime) {
                runOnUiThread {
                    binding.tvTimer.text = it.toTimerFormat()
                }
            }.apply { start() }
        }
    }

    private fun stopTimer() {
        countUpTimer?.stop()
        countUpTimer = null
    }

    private fun initOpenChannelView() {
        val liveEventId = liveEventId ?: return
        supportFragmentManager.commit {
            replace(R.id.fcvChat, getOpenChannelFragment(liveEventId))
        }
        openChannelFragment.title = liveEvent?.title
        openChannelFragment.profileImageUrl = liveEvent?.host?.profileURL ?: liveEvent?.coverUrl
        openChannelFragment.onHeaderChatButtonClickListener = View.OnClickListener { setChatViewVisibility(!openChannelFragment.isVisible) }
    }

    private fun setChatViewVisibility(isVisible: Boolean) {
        val (height, visibility, iconRes) =
            if (isVisible) Triple(374.dp, View.VISIBLE, R.drawable.icon_chat_hide)
            else Triple(ViewGroup.LayoutParams.WRAP_CONTENT, View.GONE, R.drawable.icon_chat_show)
        with(binding.fcvChat) {
            layoutParams.height = height
            requestLayout()
        }
        with(openChannelFragment) {
            openChannelListVisibility = visibility
            chatIconRes = iconRes
        }
    }

    protected fun distributeReactionAnimations(key: String, count: Int) {
        var increasedReactionCount = count - (reactionCountMap[key] ?: 0)
        if (increasedReactionCount < 0)
            return
        else if (increasedReactionCount > ReactionConstants.REACTION_MAXIMUM_COUNT)
            increasedReactionCount = ReactionConstants.REACTION_MAXIMUM_COUNT
        reactionCountMap[key] = count

        for(i in 0 until increasedReactionCount) {
            runOnSingleThreadPoolWithDelay(millisecond = (ReactionConstants.REACTION_SERVER_PUSH_INTERVAL / increasedReactionCount * i).toLong()) {
                runOnUiThread {
                    binding.lervLikeReaction.startAnimation()
                }
            }
        }
    }

    private fun runOnSingleThreadPoolWithDelay(millisecond: Long, runnable: Runnable) {
        if (!singleThreadExecutor.isShutdown) {
            singleThreadExecutor.schedule(runnable, millisecond, TimeUnit.MILLISECONDS)
        }
    }

    abstract fun getOpenChannelFragment(liveEventId: String): OpenChannelFragment


    open class LiveEventListenerImpl : LiveEventListener {
        override fun onCustomItemsDelete(liveEvent: LiveEvent, customItems: Map<String, String>, deletedKeys: List<String>) {}
        override fun onCustomItemsUpdate(liveEvent: LiveEvent, customItems: Map<String, String>, updatedKeys: List<String>) {}
        override fun onDisconnected(liveEvent: LiveEvent, e: SendbirdException) {}
        override fun onHostConnected(liveEvent: LiveEvent, host: Host) {}
        override fun onHostDisconnected(liveEvent: LiveEvent, host: Host) {}
        override fun onHostEntered(liveEvent: LiveEvent, host: Host) {}
        override fun onHostExited(liveEvent: LiveEvent, host: Host) {}
        override fun onHostMuteAudio(liveEvent: LiveEvent, host: Host) {}
        override fun onHostStartVideo(liveEvent: LiveEvent, host: Host) {}
        override fun onHostStopVideo(liveEvent: LiveEvent, host: Host) {}
        override fun onHostUnmuteAudio(liveEvent: LiveEvent, host: Host) {}
        override fun onLiveEventEnded(liveEvent: LiveEvent) {}
        override fun onLiveEventInfoUpdated(liveEvent: LiveEvent) {}
        override fun onLiveEventReady(liveEvent: LiveEvent) {}
        override fun onLiveEventStarted(liveEvent: LiveEvent) {}
        override fun onParticipantCountChanged(liveEvent: LiveEvent, participantCountInfo: ParticipantCountInfo) {}
        override fun onReactionCountUpdated(liveEvent: LiveEvent, key: String, count: Int) {}
    }

}