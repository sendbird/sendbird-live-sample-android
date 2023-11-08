package com.sendbird.live.videoliveeventsample.view.widget

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.live.videoliveeventsample.databinding.ViewTextBottomSheetDialogBinding
import com.sendbird.live.videoliveeventsample.databinding.ViewTextBottomSheetDialogItemBinding
import com.sendbird.live.videoliveeventsample.model.TextBottomSheetDialogItem
import com.sendbird.live.videoliveeventsample.util.OnItemClickListener

internal class TextBottomSheetDialogView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: ViewTextBottomSheetDialogBinding =
        ViewTextBottomSheetDialogBinding.inflate(LayoutInflater.from(getContext()), this, true)

    fun setTitle(title: String, @ColorRes backgroundColor: Int) {
        binding.tvTextBottomSheetDialogTitle.visibility = VISIBLE
        binding.tvTextBottomSheetDialogTitle.text = title
        binding.tvTextBottomSheetDialogTitle.setBackgroundResource(backgroundColor)
    }

    fun setItems(items: List<TextBottomSheetDialogItem>, @ColorRes backgroundColor: Int, listener: OnItemClickListener<TextBottomSheetDialogItem>) {
        binding.rvTextBottomSheetDialogItems.adapter = TextBottomSheetDialogAdapter(items, backgroundColor, listener)
    }
}

internal class TextBottomSheetDialogAdapter(
    private val items: List<TextBottomSheetDialogItem>,
    @ColorRes private val backgroundColor: Int,
    private val listener: OnItemClickListener<TextBottomSheetDialogItem>
) : RecyclerView.Adapter<TextBottomSheetDialogAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ViewTextBottomSheetDialogItemBinding.inflate(
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
        private val binding: ViewTextBottomSheetDialogItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TextBottomSheetDialogItem) {
            val context = binding.root.context
            binding.root.setOnClickListener { v -> listener.onItemClick(v, adapterPosition, item) }
//            binding.root.setBackgroundResource(backgroundColor)
            binding.tvTextBottomSheetDialogItemTitle.text = context.getString(item.title)
            item.textStyle?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    binding.tvTextBottomSheetDialogItemTitle.setTextAppearance(it)
                } else {
                    binding.tvTextBottomSheetDialogItemTitle.setTextAppearance(context, it)
                }
            }
            binding.ivTextBottomSheetDialogItemSelected.visibility = if (item.isSelected) View.VISIBLE else View.INVISIBLE
        }
    }
}
