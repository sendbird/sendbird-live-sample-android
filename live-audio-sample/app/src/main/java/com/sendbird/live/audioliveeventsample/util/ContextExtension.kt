package com.sendbird.live.audioliveeventsample.util

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
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.core.app.ActivityCompat
import com.sendbird.live.audioliveeventsample.R
import com.sendbird.live.audioliveeventsample.model.TextBottomSheetDialogItem
import com.sendbird.live.audioliveeventsample.view.widget.RadioSheetDialogView
import com.sendbird.live.audioliveeventsample.view.widget.TextSheetDialogView
import com.sendbird.uikit.interfaces.OnItemClickListener


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
        if (ActivityCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
    }
    return true
}
