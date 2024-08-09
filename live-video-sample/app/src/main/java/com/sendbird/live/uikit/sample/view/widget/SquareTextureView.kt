package com.sendbird.live.uikit.sample.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.TextureView

class SquareTextureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextureView(context, attrs, defStyleAttr) {
    private var cameraWidth = 0
    private var cameraHeight = 0

    fun setAspectRatio(width: Int, height: Int) {
        require(!(width < 0 || height < 0)) { "Size cannot be negative." }
        cameraWidth = width
        cameraHeight = height
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        if (0 == cameraWidth || 0 == cameraHeight) {
            setMeasuredDimension(width, height)
        } else {
            val size = if (width < height) width else height
            setMeasuredDimension(size, size)
        }
    }

}
