package com.sendbird.live.audioliveeventsample.util

import java.util.*

internal class CountUpTimer(baseTime: Long = 0, handler: (Long) -> Unit) : Timer() {
    var time: Long = baseTime // millisecond
    private var timerTask: TimerTask = object : TimerTask() {
        override fun run() {
            time += 1000
            handler.invoke(time)
        }
    }
    internal fun start() {
        this.schedule(timerTask, 0, 1000)
    }
    internal fun stop() {
        this.cancel()
        this.purge()
    }
}
