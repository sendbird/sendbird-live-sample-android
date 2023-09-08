package com.sendbird.live.uikit.sample.utils

import android.app.Activity
import android.text.TextUtils
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException

object QRUtils {
    fun createQRLauncher(
        activity: Activity,
        resultHandler: (isSuccess: Boolean, qrString: String?) -> Unit
    ): ActivityResultLauncher<ScanOptions> {
        val qrCodeLauncher = (activity as ComponentActivity).registerForActivityResult(
            ScanContract()
        ) { result: ScanIntentResult ->
            if (result.contents == null) {
                resultHandler.invoke(false, null)
            } else {
                resultHandler.invoke(true, result.contents)
            }
        }
        return qrCodeLauncher
    }

    fun getAuthInfoFromQRCode(encodedAuthInfo: String): Triple<String?, String?, String?> {
        var applicationId: String? = null
        var userId: String? = null
        var accessToken: String? = null
        try {
            if (!TextUtils.isEmpty(encodedAuthInfo)) {
                val jsonString = String(Base64.decode(encodedAuthInfo, Base64.DEFAULT), Charsets.UTF_8)
                val jsonObject = JSONObject(jsonString)
                applicationId = jsonObject.getString("app_id")
                userId = jsonObject.getString("user_id")
                accessToken = jsonObject.getString("access_token")
            }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return Triple(applicationId, userId, accessToken)
    }
}