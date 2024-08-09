package com.sendbird.live.uikit.sample.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.core.app.ActivityCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sendbird.live.uikit.sample.R
import com.sendbird.live.uikit.sample.model.TextBottomSheetDialogItem
import com.sendbird.live.uikit.sample.view.SignInManuallyActivity
import com.sendbird.live.uikit.sample.view.widget.RadioSheetDialogView
import com.sendbird.live.uikit.sample.view.widget.TextBottomSheetDialogView
import com.sendbird.live.uikit.sample.view.widget.TextSheetDialogView


fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.showToast(@StringRes resId:  Int) {
    Toast.makeText(this, getString(resId), Toast.LENGTH_SHORT).show()
}

fun Context.showAlertDialog(
    title: String? = null,
    message: String? = null,
    posText: String,
    negText: String? = null,
    @StyleRes backgroundDrawableResId: Int? = null,
    positiveButtonFunction: (() -> Unit)? = null,
    negativeButtonFunction: (() -> Unit)? = null,

    onDismissListener: DialogInterface.OnDismissListener? = null
) {
    val builder =
        (if (backgroundDrawableResId != null) AlertDialog.Builder(this, backgroundDrawableResId)
        else AlertDialog.Builder(this)).apply {
            if (!title.isNullOrBlank()) {
                setTitle(title)
            }
            if (!message.isNullOrBlank()) setMessage(message)
            setPositiveButton(posText) { _, _ ->
                positiveButtonFunction?.invoke()
            }
            if (!negText.isNullOrBlank()) {
                setNegativeButton(negText) { _, _ ->
                negativeButtonFunction?.invoke()
            }
        }
    }
    builder.setOnDismissListener(onDismissListener)
    builder.show()
}

fun Context.showEditTextDialog(
    title: String? = null,
    message: String? = null,
    posText: String,
    negText: String? = null,
    hintText: String? = null,
    @StyleRes backgroundDrawableResId: Int? = null,
    positiveButtonFunction: ((String) -> Unit)? = null,
    negativeButtonFunction: (() -> Unit)? = null,

    onDismissListener: DialogInterface.OnDismissListener? = null
) {
    val parentLayout = LinearLayout(this)

    val layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply { setMargins(24.dp, 24.dp, 24.dp, 24.dp) }
    val editText = EditText(this).apply {
        hint = hintText
        this.layoutParams = layoutParams
        backgroundDrawableResId?.let {
            setTextAppearance(it)
        }
        maxLines = 1
    }
    parentLayout.addView(editText)
    val builder =
        (if (backgroundDrawableResId != null) AlertDialog.Builder(this, backgroundDrawableResId)
        else AlertDialog.Builder(this)).apply {
            if (!title.isNullOrBlank()) {
                setTitle(title)
            }
            setView(parentLayout)
            if (!message.isNullOrBlank()) setMessage(message)
            setPositiveButton(posText) { _, _ ->
                positiveButtonFunction?.invoke(editText.text.toString())
            }
            if (!negText.isNullOrBlank()) {
                setNegativeButton(negText) { _, _ ->
                negativeButtonFunction?.invoke()
            }
        }
    }
    builder.setOnDismissListener(onDismissListener)
    builder.show()
}

internal fun Context.showSheetRadioDialog(
    title: String? = null,
    @StyleRes titleAppearance: Int? = null,
    @DrawableRes backgroundDrawableRes: Int,
    items: List<TextBottomSheetDialogItem>,
    checkedItemPosition: Int = -1,
    listener: (position: Int, dialogItem: TextBottomSheetDialogItem?) -> Unit
) {
    val dialogView = RadioSheetDialogView(this).apply {
        title?.let { setTitle(it, titleAppearance) }
        setSheetBackground(backgroundDrawableRes)
    }
    val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(this, R.style.SheetListDialog)
    dialogBuilder.setCustomTitle(dialogView)
    val dialog = dialogBuilder.create()
    dialogView.setItems(items, checkedItemPosition) { position, dialogItem ->
        listener.invoke(position, dialogItem)
        dialog.dismiss()
    }

    dialog.show()
    dialog.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}
internal fun Context.showListDialog(
    title: String? = null,
    message: String? = null,
    listItem: List<String>,
    onClickListener: DialogInterface.OnClickListener
) {
    val items: Array<CharSequence> = listItem.toTypedArray()
    val builder = AlertDialog.Builder(this).apply {
        if (!title.isNullOrBlank()) setTitle(title)
        if (!message.isNullOrBlank()) setMessage(message)
        setItems(items, onClickListener)
    }
    builder.show()
}

internal fun Context.showBottomSheetDialog(
    items: List<TextBottomSheetDialogItem>,
    title: String? = null,
    @ColorRes backgroundColor: Int,
    itemClickListener: OnItemClickListener<TextBottomSheetDialogItem>? = null
): BottomSheetDialog {
    val dialogView = TextBottomSheetDialogView(this).apply {
        title?.let { setTitle(it, backgroundColor) }
    }
    val dialog = BottomSheetDialog(this, R.style.SheetDialog)
    dialog.setContentView(dialogView)
    dialogView.setItems(items, backgroundColor
    ) { view, position, data ->
        dialog.dismiss()
        itemClickListener?.onItemClick(view, position, data)
    }
    dialog.show()
    return dialog
}

internal fun Context.showSheetDialog(
    title: String? = null,
    @StyleRes titleAppearance: Int? = null,
    @DrawableRes backgroundDrawableRes: Int,
    items: List<TextBottomSheetDialogItem>,
    itemClickListener: OnItemClickListener<TextBottomSheetDialogItem>? = null
) {
    val dialogView = TextSheetDialogView(this).apply {
        title?.let { setTitle(it, titleAppearance) }
        setSheetBackground(backgroundDrawableRes)
    }
    val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(this, R.style.SheetListDialog)
    dialogBuilder.setCustomTitle(dialogView)
    val dialog = dialogBuilder.create()
    dialogView.setItems(items) { view: View, position: Int, data: TextBottomSheetDialogItem ->
        dialog.dismiss()
        itemClickListener?.onItemClick(view, position, data)
    }
    dialog.show()
    dialog.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}

fun Context.showPermissionDenyDialog(shouldFinish: Boolean = false, deniedList: List<String> = emptyList()) {
    val text = this.getString(R.string.permission_dialog_denied_message, this.getString(R.string.app_name), deniedList.joinToString { it.toPermissionText(this) })
    showAlertDialog(
        getString(R.string.permission_dialog_denied_title),
        text,
        posText = getString(R.string.permission_dialog_denied_apply),
        positiveButtonFunction = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
            if (shouldFinish) (this as Activity).finish()
        },
        negText = getString(R.string.permission_dialog_denied_deny),
        negativeButtonFunction = {
            if (shouldFinish) (this as Activity).finish()
        }
    ) { if (shouldFinish) (this as Activity).finish() }
}

fun Context.areAnyPermissionsGranted(permissions: Array<String>): Boolean {
    for (permission in permissions) {
        if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
    }
    return true
}

fun Activity.signOut() {
    PrefManager(this).removeAll()
    val intent = Intent(this, SignInManuallyActivity::class.java)
    this.startActivity(intent)
    this.finishAffinity()
}
