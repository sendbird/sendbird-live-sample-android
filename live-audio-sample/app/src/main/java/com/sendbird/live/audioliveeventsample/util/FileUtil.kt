package com.sendbird.live.audioliveeventsample.util

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.sendbird.uikit.log.Logger
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class FileUtil {
    fun uriToFile(context: Context, uri: Uri): File {
        return File(uriToPath(context, uri))
    }

    private fun uriToPath(context: Context, uri: Uri): String {
        val tempFileName =
            "Temp_" + System.currentTimeMillis() + "." + extractExtension(context, uri)
        val dstFile = createCachedDirFile(context, tempFileName)
        return copyFromUri(context, uri, dstFile)
    }

    fun extractExtension(context: Context, uri: Uri): String {
        var extension = "temp"
        val scheme = uri.scheme
        if (scheme != null && scheme == ContentResolver.SCHEME_CONTENT) {
            val type = context.contentResolver.getType(uri)
            if (type != null) {
                extension = extractExtension(type)!!
            }
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        }
        if (extension.isEmpty()) extension = "temp"
        return extension
    }

    private fun extractExtension(mimeType: String): String? {
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(mimeType)
    }

    private fun copyFromUri(context: Context, uri: Uri, dstFile: File): String {
        if (!dstFile.exists() || dstFile.length() <= 0) {
            val inputStream: InputStream?
            try {
                inputStream = context.contentResolver.openInputStream(uri)
                val outputStream: OutputStream = FileOutputStream(dstFile)
                copy(inputStream, outputStream)
            } catch (e: Exception) {
                Logger.e(e)
            }
        }
        return dstFile.absolutePath
    }

    @Throws(java.lang.Exception::class)
    private fun copy(input: InputStream?, output: OutputStream): Int {
        val BUFFER_SIZE = 1024 * 2
        val buffer = ByteArray(BUFFER_SIZE)
        val `in` = BufferedInputStream(input, BUFFER_SIZE)
        val out = BufferedOutputStream(output, BUFFER_SIZE)
        var count = 0
        var n: Int
        try {
            while (`in`.read(buffer, 0, BUFFER_SIZE).also { n = it } != -1) {
                out.write(buffer, 0, n)
                count += n
            }
            out.flush()
        } finally {
            out.close()
            `in`.close()
        }
        return count
    }

    fun createCachedDirFile(context: Context, fileName: String): File {
        val dir = context.cacheDir
        return File(dir, fileName)
    }

    fun createImageFileUri(context: Context): Uri? {
        val contentResolver = context.contentResolver
        val cv = ContentValues()
        val fileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        cv.put(MediaStore.Images.Media.TITLE, fileName)
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
    }
}