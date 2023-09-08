package com.sendbird.live.audioliveeventsample.view.fragment

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.live.*
import com.sendbird.live.audioliveeventsample.R
import com.sendbird.live.audioliveeventsample.adapter.LiveEventListAdapter
import com.sendbird.live.audioliveeventsample.databinding.FragmentLiveEventListBinding
import com.sendbird.live.audioliveeventsample.util.*
import com.sendbird.live.audioliveeventsample.view.KEY_LIVE_EVENT_LIKE_REACTION
import com.sendbird.live.audioliveeventsample.view.LiveEventForHostActivity
import com.sendbird.live.audioliveeventsample.view.LiveEventForParticipantActivity
import com.sendbird.uikit.interfaces.OnItemClickListener

class LiveEventListFragment :
    BaseFragment<FragmentLiveEventListBinding>(FragmentLiveEventListBinding::inflate) {

    private var liveEventListQuery: LiveEventListQuery? = null
    private val params =
        LiveEventListQuery.Params(hostTypes = listOf(HostType.SINGLE_HOST_AUDIO_ONLY))
    private val permissions = mutableListOf(
        Manifest.permission.BLUETOOTH
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            addAll(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        }
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (it.all { permission -> permission.value }) {
            requireContext().showToast(R.string.permission_dialog_granted)
        } else {
            requireContext().showPermissionDenyDialog(
                true,
                it.filter { permission -> !permission.value }.keys.toList()
            )
        }
    }

    lateinit var adapter: LiveEventListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLiveEventListView()
        initHeaderView()
    }

    private fun initHeaderView() {
        binding.tvGoLive.setOnClickListener {
            createLiveEvent()
        }
    }

    private fun createLiveEvent() {
        val currentUser = SendbirdLive.currentUser ?: run {
            requireContext().showToast("LiveEvent unavailable")
            return@createLiveEvent
        }
        val params = LiveEventCreateParams(listOf(currentUser.userId)).apply {
            title = currentUser.userId.attachAffix(binding.root.context.getString(R.string.live_event_title_affix))
            if (currentUser.profileUrl.isNotEmpty()) coverUrl = currentUser.profileUrl
            hostType = HostType.SINGLE_HOST_AUDIO_ONLY
            reactionKeys = listOf(KEY_LIVE_EVENT_LIKE_REACTION)
        }
        SendbirdLive.createLiveEvent(params) createLiveEventLabel@{ liveEvent, e ->
            if (e != null || liveEvent == null) {
                requireContext().showToast("LiveEvent unavailable")
                return@createLiveEventLabel
            }
            enterTheLiveEvent(liveEvent)
        }
    }

    private fun initLiveEventListView() {
        adapter = LiveEventListAdapter()
        liveEventListQuery = SendbirdLive.createLiveEventListQuery(params)
        binding.rvLiveEvents.adapter = adapter
        adapter.onItemClickListener = OnItemClickListener { _, position, liveEvent ->
            getLiveEvent(liveEvent.liveEventId) { newLiveEvent ->
                if (position != -1) adapter.notifyItemChanged(position)
                enterTheLiveEvent(newLiveEvent)
            }
        }
        adapter.emptyStateView = binding.clEmpty
        binding.rvLiveEvents.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)) {
                    loadNext()
                }
            }
        })
        binding.srlLiveEvent.setOnRefreshListener {
            liveEventListQuery = SendbirdLive.createLiveEventListQuery(params)
            loadNext(true)
        }
        loadNext()
    }

    private fun enterTheLiveEvent(liveEvent: LiveEvent) {
        if (liveEvent.state == LiveEventState.ENDED) {
            requireActivity().showAlertDialog(
                message = getString(R.string.error_message_ended_enter_dialog_description),
                posText = getString(R.string.okay)
            )
            return
        }

        if (liveEvent.myRole == LiveEventRole.HOST) {
            val requestPermissions = permissions.toMutableList().apply {
                addAll(
                    arrayOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_PHONE_STATE
                    )
                )
            }.toTypedArray()
            if (!requireContext().areAnyPermissionsGranted(requestPermissions)) {
                requestPermissionLauncher.launch(requestPermissions)
                return
            }
            liveEvent.enterAsHost(MediaOptions()) { e ->
                if (e != null) {
                    requireActivity().showToast(e.message ?: "")
                    return@enterAsHost
                }
                val intent = Intent(requireContext(), LiveEventForHostActivity::class.java)
                intent.putExtra(INTENT_KEY_LIVE_EVENT_ID, liveEvent.liveEventId)
                requireContext().startActivity(intent)
            }
        } else {
            when (liveEvent.state) {
                LiveEventState.CREATED -> {
                    requireActivity().showAlertDialog(
                        message = getString(R.string.error_message_created_enter_dialog_description),
                        posText = getString(R.string.okay)
                    )
                    return
                }
                LiveEventState.READY, LiveEventState.ONGOING -> {
                    val requestPermissions = permissions.toTypedArray()
                    if (!requireContext().areAnyPermissionsGranted(requestPermissions)) {
                        requestPermissionLauncher.launch(requestPermissions)
                        return
                    }
                    liveEvent.enter { e ->
                        if (e != null) {
                            requireActivity().showToast(e.message ?: "")
                            return@enter
                        }
                        val intent = Intent(requireContext(), LiveEventForParticipantActivity::class.java)
                        intent.putExtra(INTENT_KEY_LIVE_EVENT_ID, liveEvent.liveEventId)
                        requireContext().startActivity(intent)
                    }
                }
                else -> {

                }
            }
        }
    }

    private fun loadNext(isRefresh: Boolean = false) {
        if (liveEventListQuery?.hasNext == true) {
            liveEventListQuery?.next { list, e ->
                if (isRefresh) {
                    binding.srlLiveEvent.isRefreshing = false
                    binding.rvLiveEvents.scrollToPosition(0)
                }
                if (e != null) {
                    requireContext().showToast(e.message ?: "")
                    return@next
                }
                if (isRefresh) {
                    adapter.submitList(list ?: emptyList())
                } else {
                    adapter.addItems(list)
                }
            }
            return
        }
        if (isRefresh) binding.srlLiveEvent.isRefreshing = false
    }

    private fun getLiveEvent(liveEventId: String, callback: (LiveEvent) -> Unit) {
        SendbirdLive.getLiveEvent(liveEventId) getLiveEventLabel@{ liveEvent, e ->
            if (e != null || liveEvent == null) {
                requireContext().showToast(e?.message ?: "")
                return@getLiveEventLabel
            }
            callback.invoke(liveEvent)
        }
    }
}