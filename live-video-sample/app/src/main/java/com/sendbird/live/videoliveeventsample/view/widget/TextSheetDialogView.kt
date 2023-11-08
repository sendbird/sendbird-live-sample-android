package com.sendbird.live.videoliveeventsample.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.live.videoliveeventsample.R
import com.sendbird.live.videoliveeventsample.databinding.ViewTextSheetDialogBinding
import com.sendbird.live.videoliveeventsample.databinding.ViewTextSheetDialogItemBinding
import com.sendbird.live.videoliveeventsample.model.TextBottomSheetDialogItem
import com.sendbird.live.videoliveeventsample.util.OnItemClickListener

internal class TextSheetDialogView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: ViewTextSheetDialogBinding = ViewTextSheetDialogBinding.inflate(LayoutInflater.from(getContext()), this, true)

    fun setSheetBackground(@DrawableRes background: Int) {
        binding.textSheetRootView.setBackgroundResource(background)
    }
    fun setTitle(title: String, @StyleRes textAppearance: Int?) {
        binding.tvTextBottomSheetDialogTitle.visibility = VISIBLE
        binding.tvTextBottomSheetDialogTitle.text = title
        textAppearance?.let { binding.tvTextBottomSheetDialogTitle.setTextAppearance(it) }
    }

    fun setItems(items: List<TextBottomSheetDialogItem>, @ColorRes backgroundColor: Int? = null, listener: OnItemClickListener<TextBottomSheetDialogItem>) {
        binding.rvTextBottomSheetDialogItems.adapter = TextSheetDialogAdapter(items, backgroundColor, listener)
        val verticalDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        val verticalDivider = ContextCompat.getDrawable(context, R.drawable.shape_dialog_divider)
        verticalDivider?.let { verticalDecoration.setDrawable(it) }
        binding.rvTextBottomSheetDialogItems.addItemDecoration(verticalDecoration)
    }
}

internal class TextSheetDialogAdapter(
    private val items: List<TextBottomSheetDialogItem>,
    @ColorRes private val backgroundColor: Int? = null,
    private val listener: OnItemClickListener<TextBottomSheetDialogItem>
) : RecyclerView.Adapter<TextSheetDialogAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ViewTextSheetDialogItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(
        private val binding: ViewTextSheetDialogItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TextBottomSheetDialogItem) {
            val context = binding.root.context
            binding.root.setOnClickListener { v -> listener.onItemClick(v, adapterPosition, item) }
//            binding.root.setBackgroundResource(backgroundColor)
            binding.tvTextBottomSheetDialogItemTitle.text = context.getString(item.title)
            item.textStyle?.let {
                binding.tvTextBottomSheetDialogItemTitle.setTextAppearance(it)
            }
        }
    }
}
