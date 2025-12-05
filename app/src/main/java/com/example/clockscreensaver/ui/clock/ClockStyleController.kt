package com.example.clockscreensaver.ui.clock

import android.content.Context
import android.view.MotionEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs

class ClockStyleController(context: Context) {
    private val prefs = com.example.clockscreensaver.data.UserPreferencesRepository(context.applicationContext)
    private val _styleFlow = MutableStateFlow(ClockStyle.BASIC)
    val styleFlow: StateFlow<ClockStyle> = _styleFlow

    private var accumulatedX = 0f

    suspend fun loadInitial(styleId: String) {
        _styleFlow.emit(ClockStyle.fromId(styleId))
    }

    suspend fun setStyle(style: ClockStyle) {
        _styleFlow.emit(style)
        prefs.updateClockStyle(style.id)
    }

    fun onTouch(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                accumulatedX = 0f
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = ev.x - ev.downX()
                accumulatedX = dx
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val threshold = 48f
                when {
                    accumulatedX > threshold -> {
                        shift(1)
                        return true
                    }
                    accumulatedX < -threshold -> {
                        shift(-1)
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun shift(delta: Int) {
        val current = _styleFlow.value
        val next = current.shift(delta)
        _styleFlow.value = next
    }
}

private fun MotionEvent.downX(): Float = if (historySize > 0) getHistoricalX(0) else x
