package com.example.clockscreensaver.ui.clock

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.clockscreensaver.data.UserPreferences
import com.example.clockscreensaver.data.UserPreferencesRepository
import com.example.clockscreensaver.ui.theme.Black
import com.example.clockscreensaver.ui.theme.DarkGold
import com.example.clockscreensaver.ui.theme.TextDim
import com.example.clockscreensaver.ui.theme.TextPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

@Composable
fun ClockScreen(
    prefsOverride: UserPreferences? = null,
    applyInsets: Boolean = true
) {
    val context = LocalContext.current.applicationContext
    val repository = remember { UserPreferencesRepository(context) }
    val prefsFromFlow by repository.preferencesFlow.collectAsState(initial = UserPreferences())
    val prefs = prefsOverride ?: prefsFromFlow

    val scope = rememberCoroutineScope()
    var clockStyle by rememberSaveable { mutableStateOf(ClockStyle.fromId(prefs.clockStyle)) }
    val styleController = remember { ClockStyleController(context) }
    val sharedStyle by styleController.styleFlow.collectAsState()

    LaunchedEffect(prefs.clockStyle) {
        clockStyle = ClockStyle.fromId(prefs.clockStyle)
        styleController.loadInitial(prefs.clockStyle)
    }

    LaunchedEffect(sharedStyle) {
        clockStyle = sharedStyle
    }

    val timeText by produceState(initialValue = formatTime(prefs.is24Hour, LocalTime.now()), prefs.is24Hour) {
        while (isActive) {
            value = formatTime(prefs.is24Hour, LocalTime.now())
            val millisToNextMinute = 60_000 - (System.currentTimeMillis() % 60_000)
            delay(millisToNextMinute)
        }
    }

    val offset by produceState(initialValue = 0f to 0f, prefs.burnInProtection) {
        while (isActive) {
            if (!prefs.burnInProtection) {
                value = 0f to 0f
                delay(Long.MAX_VALUE)
            } else {
                // 첫 1분은 중앙 유지, 이후부터 픽셀 시프트
                delay(60_000L)
                value = randomOffset()
            }
        }
    }

    ClockScreenContent(
        timeText = timeText,
        prefs = prefs,
        offsetX = offset.first,
        offsetY = offset.second,
        clockStyle = clockStyle,
        onSwipeStyleChange = { delta ->
            clockStyle = clockStyle.shift(delta)
            scope.launch { styleController.setStyle(clockStyle) }
        },
        applyInsets = applyInsets
    )
}

@Composable
private fun ClockScreenContent(
    timeText: String,
    prefs: UserPreferences,
    offsetX: Float,
    offsetY: Float,
    clockStyle: ClockStyle,
    onSwipeStyleChange: (Int) -> Unit,
    applyInsets: Boolean
) {
    val textColor = prefs.textColorHex.toColorOrNull() ?: TextPrimary

    val baseModifier = Modifier
        .fillMaxSize()
        .background(Black)
        .then(if (applyInsets) Modifier.systemBarsPadding() else Modifier)
        .pointerInput(clockStyle) {
            var totalDrag = 0f
            detectHorizontalDragGestures(
                onDragEnd = {
                    val threshold = 24f
                    when {
                        totalDrag > threshold -> onSwipeStyleChange(-1)
                        totalDrag < -threshold -> onSwipeStyleChange(1)
                    }
                    totalDrag = 0f
                }
            ) { _, dragAmount ->
                totalDrag += dragAmount
            }
        }

    Box(
        modifier = baseModifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Astell&Kern",
            color = DarkGold.copy(alpha = 0.7f),
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp)
        )
        when (clockStyle) {
            ClockStyle.BASIC -> BasicClock(timeText, textColor, offsetX, offsetY)
            ClockStyle.SPLIT -> SplitClock(timeText, textColor, offsetX, offsetY)
            ClockStyle.MINIMAL -> MinimalClock(timeText, textColor, offsetX, offsetY)
        }
    }
}

@Composable
private fun BoxScope.BasicClock(timeText: String, color: Color, offsetX: Float, offsetY: Float) {
    Text(
        text = timeText,
        color = color,
        style = androidx.compose.material3.MaterialTheme.typography.displayLarge,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .align(Alignment.Center)
            .offset(x = offsetX.dp, y = offsetY.dp)
            .padding(horizontal = 8.dp)
    )
}

@Composable
private fun BoxScope.SplitClock(timeText: String, color: Color, offsetX: Float, offsetY: Float) {
    val parts = timeText.split(":")
    val hour = parts.getOrNull(0) ?: "--"
    val minute = parts.getOrNull(1) ?: "--"

    Row(
        modifier = Modifier
            .align(Alignment.Center)
            .offset(x = offsetX.dp, y = offsetY.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = hour,
            color = color,
            style = androidx.compose.material3.MaterialTheme.typography.displayLarge,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip
        )
        Text(
            text = minute,
            color = color.copy(alpha = 0.8f),
            style = androidx.compose.material3.MaterialTheme.typography.displayLarge,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip
        )
    }
}

@Composable
private fun BoxScope.MinimalClock(timeText: String, color: Color, offsetX: Float, offsetY: Float) {
    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .offset(x = offsetX.dp, y = offsetY.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = timeText,
            color = color.copy(alpha = 0.9f),
            style = androidx.compose.material3.MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.W300
            ),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Swipe left/right to change style",
            color = color.copy(alpha = 0.4f),
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

private fun formatTime(is24h: Boolean, time: LocalTime): String {
    val pattern = if (is24h) "HH:mm" else "hh:mm"
    return time.format(DateTimeFormatter.ofPattern(pattern))
}

private fun randomOffset(maxShiftDp: Int = 12): Pair<Float, Float> {
    val min = -maxShiftDp
    val max = maxShiftDp + 1 // until upper bound is exclusive
    val x = Random.nextInt(min, max).toFloat()
    val y = Random.nextInt(min, max).toFloat()
    return x to y
}

private fun String.toColorOrNull(): Color? = try {
    Color(android.graphics.Color.parseColor(this))
} catch (e: IllegalArgumentException) {
    null
}

fun ClockStyle.shift(delta: Int): ClockStyle {
    val all = ClockStyle.values()
    val nextIndex = (ordinal + delta).floorMod(all.size)
    return all[nextIndex]
}

fun Int.floorMod(mod: Int): Int {
    val r = this % mod
    return if (r < 0) r + mod else r
}
