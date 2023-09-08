package com.sendbird.live.audioliveeventsample.view.widget

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.animation.*
import android.view.animation.Animation.AnimationListener
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat
import com.sendbird.live.audioliveeventsample.R
import com.sendbird.live.audioliveeventsample.databinding.ViewLiveEventReactionBinding
import com.sendbird.live.audioliveeventsample.util.dp

object ReactionConstants {
    const val DIRECTION_START_RIGHT = -1
    const val DIRECTION_START_LEFT = 1
    const val REACTION_MAXIMUM_COUNT = 30
    const val REACTION_SERVER_PUSH_INTERVAL = 1000 //millisecond
    const val REACTION_DEFAULT_DURATION = 4000L
    const val REACTION_DEFAULT_MAX_X_REPEAT_COUNT = 5
    const val REACTION_DEFAULT_MAX_TRANSLATE_WIDTH = 16
    const val REACTION_DEFAULT_TRANSLATE_HEIGHT_RATIO = 0.7f
    val REACTION_DEFAULT_REACTION_COLOR_RES_LIST = listOf(
        R.color.reaction_white,
        R.color.reaction_violet,
        R.color.reaction_purple,
        R.color.reaction_yellow,
        R.color.reaction_green,
        R.color.reaction_blue,
        R.color.reaction_orange,
        R.color.reaction_red
    )
}

internal class LiveEventReactionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: ViewLiveEventReactionBinding

    internal var reactionDuration = ReactionConstants.REACTION_DEFAULT_DURATION
    internal var maxXRepeatCount = ReactionConstants.REACTION_DEFAULT_MAX_X_REPEAT_COUNT
    internal var maxTranslateWidth = ReactionConstants.REACTION_DEFAULT_MAX_TRANSLATE_WIDTH
    internal var translateHeightRatio = ReactionConstants.REACTION_DEFAULT_TRANSLATE_HEIGHT_RATIO
        set(value) = if (value > 1) field = 1f else field = value
    @DrawableRes
    internal var reactionIconResId = R.drawable.icon_heart_filled
    internal var reactionColorResList = ReactionConstants.REACTION_DEFAULT_REACTION_COLOR_RES_LIST
        set(value) {
            if (value.isEmpty()) return
            else field = value
        }

    init {
        binding = ViewLiveEventReactionBinding.inflate(LayoutInflater.from(context), this, true)
    }

    @UiThread
    fun startAnimation() {
        val animationSet = createAnimationSet(reactionDuration)
        val layoutParams = LayoutParams(24.dp, 24.dp).apply {
            this.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        }
        val imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this.context, reactionColorResList.random()))
        val imageView: ImageView = ImageView(this.context).apply {
            this.layoutParams = layoutParams
            this.setImageResource(reactionIconResId)
            this.imageTintList = imageTintList
        }
        binding.flRoot.addView(imageView)
        animationSet.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationRepeat(p0: Animation?) {}
            override fun onAnimationEnd(p0: Animation?) {
                binding.flRoot.post {
                    binding.flRoot.removeView(imageView)
                }
            }
        })
        imageView.startAnimation(animationSet)
    }

    private fun createAnimationSet(reactionDuration: Long): AnimationSet {
        val animationSet = AnimationSet(false).apply {
            fillAfter = true
        }
        val fadeInDuration = 1000L

        val xRepeatCount = (0..maxXRepeatCount).random()
        val translateWidth = (0..maxTranslateWidth).random().dp.toFloat()
        val startDirection = listOf(ReactionConstants.DIRECTION_START_RIGHT, ReactionConstants.DIRECTION_START_LEFT).random()
        val fadeOutDuration = reactionDuration - fadeInDuration

        val yDuration = fadeInDuration + fadeOutDuration
        val xDuration = yDuration / if (xRepeatCount == 0) 1 else xRepeatCount
        val fadeOutAnimation = AlphaAnimation(1f, 0f).apply {
            duration = fadeOutDuration
            startOffset = fadeInDuration
        }
        val fadeInAlphaAnimation = AlphaAnimation(0f, 1f).apply {
            duration = fadeInDuration
        }
        val fadeInScaleAnimation = ScaleAnimation(0.5f, 1f, 0.5f, 1f).apply {
            duration = fadeInDuration
        }
        val translateXAnimation = TranslateAnimation(startDirection * translateWidth, startDirection * -1 * translateWidth, 0f, 0f).apply {
            repeatCount = xRepeatCount
            duration = xDuration
            repeatMode = Animation.REVERSE
        }
        val translateYAnimation = TranslateAnimation(0f, 0f, 0f, -1 * this.height * translateHeightRatio).apply {
            duration = yDuration
        }
        with(animationSet) {
            addAnimation(fadeOutAnimation)
            addAnimation(fadeInAlphaAnimation)
            addAnimation(fadeInScaleAnimation)
            addAnimation(translateYAnimation)
            addAnimation(translateXAnimation)
        }
        return animationSet
    }
}
