package com.sendbird.live.videoliveeventsample.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

fun Context.hasIntent(intent: Intent): Boolean {
    return intent.resolveActivity(this.packageManager) != null
}
fun Context.cameraIntent(uri: Uri): Intent {
    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
    grantWritePermission(this, intent, uri)
    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    return intent
}

fun imageGalleryIntent(): Intent {
    return getGalleryIntent(arrayOf("image/*"))
}

fun galleryIntent(): Intent {
    return getGalleryIntent(arrayOf("image/*", "video/*"))
}

private fun getGalleryIntent(mimetypes: Array<String>): Intent {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return Intent.createChooser(intent, null)
    }
    val intent = Intent(MediaStore.ACTION_PICK_IMAGES)
    if (mimetypes.size == 1) {
        // ACTION_PICK_IMAGES supports only two mimetypes.
        intent.type = mimetypes[0]
    }
    return intent
}
private fun grantWritePermission(context: Context, intent: Intent, uri: Uri) {
    val resInfoList =
        context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
    for (resolveInfo in resInfoList) {
        val packageName = resolveInfo.activityInfo.packageName
        context.grantUriPermission(
            packageName,
            uri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }
}