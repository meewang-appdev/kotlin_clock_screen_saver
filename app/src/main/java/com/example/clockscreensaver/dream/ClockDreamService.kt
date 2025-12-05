package com.example.clockscreensaver.dream

import android.os.Bundle
import android.service.dreams.DreamService
import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.clockscreensaver.ui.clock.ClockScreen
import com.example.clockscreensaver.ui.theme.ClockSaverTheme
import kotlin.math.abs

class ClockDreamService : DreamService(), LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val internalViewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val composeView by lazy {
        ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@ClockDreamService)
            setViewTreeSavedStateRegistryOwner(this@ClockDreamService)
            setViewTreeViewModelStoreOwner(this@ClockDreamService)
        }
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Allow touch interaction so swipes can be handled without exiting.
        isInteractive = true
        isFullscreen = true
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(Bundle())
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        composeView.setContent { DreamContent() }
        setContentView(composeView)
    }

    override fun onDreamingStopped() {
        super.onDreamingStopped()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        viewModelStore.clear()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                downY = ev.y
                downTime = ev.eventTime
            }
            MotionEvent.ACTION_UP -> {
                val dx = ev.x - downX
                val dy = ev.y - downY
                val dt = ev.eventTime - downTime
                val absDx = abs(dx)
                val absDy = abs(dy)
                // Swipe: let Compose handle (style 변경), consume? pass through.
                if (absDx > swipeThresholdPx && absDx > absDy) {
                    return super.dispatchTouchEvent(ev)
                }
                // Tap: treat as exit.
                if (absDx < tapSlopPx && absDy < tapSlopPx && dt < tapTimeMs) {
                    finish()
                    return true
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    @Composable
    private fun DreamContent() {
        ClockSaverTheme(darkTheme = true) {
            ClockScreen()
        }
    }

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore
        get() = internalViewModelStore

    private var downX = 0f
    private var downY = 0f
    private var downTime = 0L
    private val density by lazy { resources.displayMetrics.density }
    private val swipeThresholdPx by lazy { 32f * density }
    private val tapSlopPx by lazy { 12f * density }
    private val tapTimeMs = 300L
}
