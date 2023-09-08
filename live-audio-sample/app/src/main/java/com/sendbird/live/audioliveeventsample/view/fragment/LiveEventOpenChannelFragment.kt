package com.sendbird.live.audioliveeventsample.view.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import com.sendbird.live.audioliveeventsample.R
import com.sendbird.live.audioliveeventsample.view.component.LiveEventChannelHeaderComponent
import com.sendbird.uikit.fragments.OpenChannelFragment
import com.sendbird.uikit.modules.OpenChannelModule
import com.sendbird.uikit.modules.components.OpenChannelMessageInputComponent
import com.sendbird.uikit.modules.components.OpenChannelMessageListComponent
import com.sendbird.uikit.modules.components.StatusComponent

class LiveEventOpenChannelFragment: OpenChannelFragment() {
    private lateinit var openChannelMessageListComponent: OpenChannelMessageListComponent
    private lateinit var openChannelMessageInputComponent: OpenChannelMessageInputComponent
    private lateinit var openChannelStateComponent: StatusComponent
    private val liveEventChannelHeader: LiveEventChannelHeaderComponent = LiveEventChannelHeaderComponent()

    internal val isVisible: Boolean
        get() = openChannelMessageListComponent.rootView?.isVisible ?: false && openChannelMessageInputComponent.rootView?.isVisible ?: false

    var onHeaderRightButtonClickListener: View.OnClickListener?
        get() = liveEventChannelHeader.rightButtonClickListener
        set(value) { liveEventChannelHeader.rightButtonClickListener = value }

    var onHeaderChatButtonClickListener: View.OnClickListener?
        get() = liveEventChannelHeader.chatButtonClickListener
        set(value) { liveEventChannelHeader.setOnChatButtonClickListener(value) }

    var onHeaderReactionButtonClickListener: View.OnClickListener?
        get() = liveEventChannelHeader.reactionButtonClickListener
        set(value) { liveEventChannelHeader.reactionButtonClickListener = value }

    var headerRightButtonResourceId: Int? = null

    var title: String? = null
        set(value) {
            liveEventChannelHeader.title = if (value.isNullOrBlank()) getString(R.string.live_event) else value
            field = value
        }

    var profileImageUrl: String? = null
        set(value) {
            liveEventChannelHeader.profileImageUrl = value
            field = value
        }

    @DrawableRes
    var chatIconRes: Int = R.drawable.icon_chat_hide
        set(value) {
            liveEventChannelHeader.setChatIconImage(value)
            field = value
        }

    var reactionButtonVisibility: Int = View.GONE
        set(value) {
            liveEventChannelHeader.reactionButtonVisibility = value
            field = value
        }

    var openChannelListVisibility: Int = View.GONE
        set(value) {
            openChannelMessageListComponent.rootView?.visibility = value
            openChannelMessageInputComponent.rootView?.visibility = value
            val count = openChannelMessageListComponent.adapter?.itemCount ?: 0
            if (value == View.VISIBLE && count != 0) {
                openChannelStateComponent.rootView?.visibility = View.GONE
            } else {
                openChannelStateComponent.rootView?.visibility = value
            }
            field = value
        }

    override fun onCreateModule(args: Bundle): OpenChannelModule {
        val module = super.onCreateModule(args)
        module.setHeaderComponent(liveEventChannelHeader)
        openChannelMessageListComponent = module.messageListComponent
        openChannelMessageInputComponent = module.messageInputComponent
        openChannelStateComponent = module.statusComponent
        return module
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerRightButtonResourceId?.let { liveEventChannelHeader.rightButtonImageResource = it }
        onHeaderRightButtonClickListener?.let { liveEventChannelHeader.rightButtonClickListener = it }
    }
}