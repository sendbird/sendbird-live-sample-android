package com.sendbird.live.uikit.sample.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sendbird.live.uikit.sample.R
import com.sendbird.live.uikit.sample.util.INTENT_KEY_LIVE_EVENT_ID
import com.sendbird.live.uikit.sample.view.LiveEventForHostActivity
import com.sendbird.live.uikit.sample.view.LiveEventForParticipantActivity

class LiveEventService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val liveEventId = intent?.getStringExtra(INTENT_KEY_LIVE_EVENT_ID)
        val asHost = intent?.getBooleanExtra(EXTRA_AS_HOST, false) ?: false
        val notification = buildNotification(liveEventId, asHost)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val type = if (asHost) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
            } else {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            }
            startForeground(NOTIFICATION_ID, notification, type)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        return START_NOT_STICKY
    }

    private fun buildNotification(liveEventId: String?, asHost: Boolean): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
        val targetClass = if (asHost) {
            LiveEventForHostActivity::class.java
        } else {
            LiveEventForParticipantActivity::class.java
        }
        val openIntent = Intent(this, targetClass).apply {
            liveEventId?.let { putExtra(INTENT_KEY_LIVE_EVENT_ID, it) }
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.live_event_service_notification_text))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 100
        private const val CHANNEL_ID = "live_event_channel"
        private const val EXTRA_AS_HOST = "EXTRA_AS_HOST"

        fun start(context: Context, liveEventId: String, asHost: Boolean) {
            val intent = Intent(context, LiveEventService::class.java).apply {
                putExtra(INTENT_KEY_LIVE_EVENT_ID, liveEventId)
                putExtra(EXTRA_AS_HOST, asHost)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, LiveEventService::class.java))
        }
    }
}
