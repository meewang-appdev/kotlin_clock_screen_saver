package com.example.clockscreensaver.ui.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.clockscreensaver.data.UserPreferences
import com.example.clockscreensaver.data.UserPreferencesRepository
import com.example.clockscreensaver.ui.clock.ClockStyle
import com.example.clockscreensaver.ui.clock.ClockScreen
import com.example.clockscreensaver.ui.clock.FullscreenPreviewActivity
import com.example.clockscreensaver.ui.theme.ClockSaverTheme
import com.example.clockscreensaver.ui.theme.DarkGold
import com.example.clockscreensaver.ui.theme.TextDim
import com.example.clockscreensaver.ui.theme.TextPrimary
import com.example.clockscreensaver.ui.theme.TextRed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = UserPreferencesRepository(applicationContext)
        setContent {
            ClockSaverTheme {
                SettingsScreen(repository)
            }
        }
    }
}

@Composable
private fun SettingsScreen(repository: UserPreferencesRepository) {
    val prefs by repository.preferencesFlow.collectAsState(initial = UserPreferences())
    val scope = remember { CoroutineScope(Dispatchers.IO) }
    val context = LocalContext.current
    val colorOptions = listOf(
        "#E0E0E0" to TextPrimary,
        "#B00020" to TextRed,
        "#444444" to TextDim,
        "#B08D57" to DarkGold
    )
    val styleOptions = listOf(
        ClockStyle.BASIC,
        ClockStyle.SPLIT,
        ClockStyle.MINIMAL
    )
    val touchGuardEnabled = remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Clock Screen Saver 설정", style = MaterialTheme.typography.titleMedium, color = TextPrimary)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("24시간 형식", color = TextPrimary)
            Switch(
                checked = prefs.is24Hour,
                onCheckedChange = { checked -> scope.launch { repository.update24Hour(checked) } }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("번인 보호", color = TextPrimary)
            Switch(
                checked = prefs.burnInProtection,
                onCheckedChange = { checked -> scope.launch { repository.updateBurnIn(checked) } }
            )
        }

        Text("시계 색상", color = TextPrimary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            colorOptions.forEach { (hex, color) ->
                ColorChip(selected = prefs.textColorHex == hex, color = color) {
                    scope.launch { repository.updateTextColor(hex) }
                }
            }
        }

        Text("시계 스타일", color = TextPrimary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            styleOptions.forEach { style ->
                Button(
                    onClick = { scope.launch { repository.updateClockStyle(style.id) } },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (prefs.clockStyle == style.id) TextDim else Color.DarkGray
                    )
                ) {
                    Text(style.name.lowercase().replaceFirstChar { it.titlecase() }, color = TextPrimary)
                }
            }
        }

        Divider(color = TextDim.copy(alpha = 0.4f))

        Text("미리보기", color = TextPrimary)
        PreviewCard(prefs)

        Button(
            onClick = {
                val intent = Intent(context, FullscreenPreviewActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("전체 화면 미리보기 실행")
        }

        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_DREAM_SETTINGS)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("시스템 화면보호기 설정 열기")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("터치 종료 방지", color = TextPrimary)
            Switch(
                checked = touchGuardEnabled.value,
                onCheckedChange = { touchGuardEnabled.value = it }
            )
        }
    }
}

@Composable
private fun ColorChip(selected: Boolean, color: Color, onClick: () -> Unit) {
    val indicator = if (selected) "*" else ""
    Button(
        onClick = onClick,
        modifier = Modifier.size(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(indicator, color = Color.Black)
    }
}

@Composable
private fun PreviewCard(prefs: UserPreferences) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF111111))
            .padding(8.dp)
    ) {
        ClockScreen(prefsOverride = prefs)
    }
}
