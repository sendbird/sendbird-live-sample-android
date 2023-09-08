package com.sendbird.live.audioliveeventsample.view.widget

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
import com.sendbird.live.audioliveeventsample.R
import com.sendbird.live.audioliveeventsample.databinding.ViewRadioSheetDialogBinding
import com.sendbird.live.audioliveeventsample.databinding.ViewRadioSheetDialogItemBinding
import com.sendbird.live.audioliveeventsample.model.TextBottomSheetDialogItem

internal class RadioSheetDialogView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: ViewRadioSheetDialogBinding =
        ViewRadioSheetDialogBinding.inflate(LayoutInflater.from(getContext()), this, true)

    fun setSheetBackground(@DrawableRes background: Int) {
        binding.textSheetRootView.setBackgroundResource(background)
    }
    fun setTitle(title: String, @StyleRes textAppearance: Int?) {
        binding.tvTextRadioSheetDialogTitle.visibility = VISIBLE
        binding.tvTextRadioSheetDialogTitle.text = title
        textAppearance?.let { binding.tvTextRadioSheetDialogTitle.setTextAppearance(it) }
    }

    fun setItems(items: List<TextBottomSheetDialogItem>, selectedPosition: Int = -1, @ColorRes backgroundColor: Int? = null, listener: (position: Int, item: TextBottomSheetDialogItem?) -> Unit) {
        val adapter = RadioSheetDialogAdapter(items, selectedPosition, backgroundColor)
        binding.rvTextSheetDialogItems.adapter = adapter
        val verticalDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        val verticalDivider = ContextCompat.getDrawable(context, R.drawable.shape_dialog_divider)
        verticalDivider?.let { verticalDecoration.setDrawable(it) }
        binding.rvTextSheetDialogItems.addItemDecoration(verticalDecoration)
        binding.tvApply.setOnClickListener { listener.invoke(adapter.selectedPosition, items[adapter.selectedPosition]) }
        binding.tvCancel.setOnClickListener { listener.invoke(adapter.selectedPosition, null) }
    }
}

internal class RadioSheetDialogAdapter(
    private val items: List<TextBottomSheetDialogItem>,
    var selectedPosition: Int,
    @ColorRes private val backgroundColor: Int? = null
) : RecyclerView.Adapter<RadioSheetDialogAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ViewRadioSheetDialogItemBinding.inflate(
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
        private val binding: ViewRadioSheetDialogItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TextBottomSheetDialogItem) {
            val context = binding.root.context
            binding.root.setOnClickListener {
                val lastSelectedPosition = selectedPosition
                selectedPosition = bindingAdapterPosition
                notifyItemChanged(lastSelectedPosition)
                notifyItemChanged(selectedPosition)
            }
            binding.tvTextRadioSheetDialogItemTitle.text = context.getString(item.title)
            item.textStyle?.let { binding.tvTextRadioSheetDialogItemTitle.setTextAppearance(it) }
            binding.rbRadioSheetDialogItem.isChecked = bindingAdapterPosition == selectedPosition
        }
    }
}
