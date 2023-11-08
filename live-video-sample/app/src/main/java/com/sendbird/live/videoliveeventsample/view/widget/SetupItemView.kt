
package com.sendbird.live.videoliveeventsample.view.widget

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import coil.load
import com.sendbird.live.videoliveeventsample.R
import com.sendbird.live.videoliveeventsample.databinding.ViewSetupListItemBinding

class SetupItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    private val binding: ViewSetupListItemBinding = ViewSetupListItemBinding.inflate(LayoutInflater.from(getContext()), this, true)

    fun setIcon(@DrawableRes drawableRes: Int) {
        binding.ivLeftIcon.load(drawableRes)
    }

    fun setIconTint(tint: ColorStateList?) {
        if (tint != null) {
            ImageViewCompat.setImageTintList(binding.ivLeftIcon, tint)
        }
    }

    fun setTitle(text: String) {
        binding.tvTitle.text = text
    }

    fun setTag(text: String) {
        binding.tvTag.text = text
    }

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.SetupItemView, defStyle, 0)
        try {
            val titleTextAppearance = a.getResourceId(R.styleable.SetupItemView_title_text_appearance, R.style.Text16OnDark01)
            val tagTextAppearance = a.getResourceId(R.styleable.SetupItemView_tag_text_appearance, R.style.Text16OnDark02)
            val leftIcon = a.getResourceId(R.styleable.SetupItemView_left_icon, 0)
            val leftIconTint = a.getColorStateList(R.styleable.SetupItemView_left_icon_tint) ?: ContextCompat.getColorStateList(context, R.color.primary_200)
            val title = a.getResourceId(R.styleable.SetupItemView_title_text, 0)
            if (leftIcon != 0) binding.ivLeftIcon.setImageResource(leftIcon)
            ImageViewCompat.setImageTintList(binding.ivLeftIcon, leftIconTint)
            if (title != 0) binding.tvTitle.text = context.getString(title)
            binding.tvTitle.setTextAppearance(titleTextAppearance)
            binding.tvTag.setTextAppearance(tagTextAppearance)
        } finally {
            a.recycle()
        }
    }
}
