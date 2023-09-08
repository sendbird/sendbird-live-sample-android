package com.sendbird.live.audioliveeventsample.view.component

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.widget.ImageViewCompat
import coil.load
import com.sendbird.live.audioliveeventsample.R
import com.sendbird.live.audioliveeventsample.databinding.ViewHeaderLiveEventOpenChannelBinding
import com.sendbird.uikit.modules.components.OpenChannelHeaderComponent

internal class LiveEventChannelHeaderComponent: OpenChannelHeaderComponent() {
    private lateinit var binding: ViewHeaderLiveEventOpenChannelBinding

    var reactionButtonVisibility: Int = View.GONE
        set(value) {
            if (this::binding.isInitialized) { binding.ibReaction.visibility = value }
            field = value
        }
    var reactionButtonIconResId: Int = R.drawable.icon_heart
        set(value) {
            if (this::binding.isInitialized) {
                binding.ibReaction.load(value) {
                    error(R.drawable.icon_heart)
                }
            }
            field = value
        }
    var reactionButtonIconTint: ColorStateList? = null
        set(value) {
            if (value == null) return
            if (this::binding.isInitialized) {
                ImageViewCompat.setImageTintList(binding.ibReaction, value)
            }
            field = value
        }
    var rightButtonClickListener: View.OnClickListener? = null
        set(value) {
            if (this::binding.isInitialized) {
                binding.rightButton.setOnClickListener(value)
            }
            field = value
        }
    var chatButtonClickListener: View.OnClickListener? = null
        set(value) {
            if (this::binding.isInitialized) {
                binding.ibChat.setOnClickListener(value)
            }
            field = value
        }
    var reactionButtonClickListener: View.OnClickListener? = null
        set(value) {
            if (this::binding.isInitialized) {
                binding.ibReaction.setOnClickListener(value)
            }
            field = value
        }
    @DrawableRes var rightButtonImageResource: Int = R.drawable.icon_user
        set(value) { binding.rightButton.setImageResource(value) }

    override fun onCreateView(context: Context, inflater: LayoutInflater, parent: ViewGroup, args: Bundle?): View {
        binding = ViewHeaderLiveEventOpenChannelBinding.inflate(LayoutInflater.from(context))
        binding.rightButton.setOnClickListener(rightButtonClickListener)
        binding.ibReaction.setOnClickListener(reactionButtonClickListener)
        binding.ibReaction.visibility = reactionButtonVisibility
        binding.ibReaction.setImageResource(reactionButtonIconResId)
        if (reactionButtonIconTint != null) {
            binding.ibReaction.imageTintList = reactionButtonIconTint
        }
        binding.title.text = title
        binding.profileView.load(profileImageUrl) {
            error(R.drawable.icon_user)
        }
        binding.ibChat.setOnClickListener(chatButtonClickListener)
        return binding.root
    }

    fun setOnChatButtonClickListener(chatButtonClickListener: View.OnClickListener?) {
        this.chatButtonClickListener = chatButtonClickListener
    }

    var title: String? = null
        set(value) {
            if (this::binding.isInitialized) {
                binding.title.text = value
            }
            field = value
        }

    var profileImageUrl: String? = null
        set(value) {
            if (this::binding.isInitialized) {
                binding.profileView.load(value) {
                    error(R.drawable.icon_user)
                }
            }
            field = value
        }

    fun setChatIconImage(@DrawableRes res: Int?) {
        binding.ibChat.load(res) {
            error(R.drawable.icon_chat_hide)
        }
    }
}