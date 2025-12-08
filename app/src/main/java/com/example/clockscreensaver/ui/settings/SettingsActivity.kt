package com.example.clockscreensaver.ui.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.clockscreensaver.data.UserPreferences
import com.example.clockscreensaver.data.UserPreferencesRepository
import com.example.clockscreensaver.ui.clock.ClockStyle
import com.example.clockscreensaver.ui.clock.ClockScreen
import com.example.clockscreensaver.ui.clock.FullscreenPreviewActivity
import com.example.clockscreensaver.ui.theme.ClockSaverTheme
import com.example.clockscreensaver.ui.theme.DarkGold
import com.example.clockscreensaver.ui.theme.NeonCyan
import com.example.clockscreensaver.ui.theme.TextDim
import com.example.clockscreensaver.ui.theme.TextPrimary
import com.example.clockscreensaver.ui.theme.TextRed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
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
        "#B08D57" to DarkGold,
        "#4DF3FF" to NeonCyan
    )
    val styleOptions = listOf(
        ClockStyle.BASIC,
        ClockStyle.SPLIT,
        ClockStyle.MINIMAL
    )
    val touchGuardEnabled = remember { mutableStateOf(true) }
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0B0B0F), Color(0xFF0F111A), Color(0xFF0B0B0F))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(vertical = 10.dp)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 6.dp)
        ) {
            Text(
                "Clock Screen Saver",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                "색상과 스타일을 고르고 전체 화면에서 바로 확인하세요.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextDim
            )
        }

        SettingsSectionCard(
            title = "기본 설정",
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            SettingRow(
                title = "24시간 형식",
                subtitle = "AM/PM 대신 24시간제로 표시",
                trailing = {
                    Switch(
                        checked = prefs.is24Hour,
                        onCheckedChange = { checked -> scope.launch { repository.update24Hour(checked) } }
                    )
                }
            )
            SettingRow(
                title = "번인 보호",
                subtitle = "화면에 잔상이 남지 않도록 픽셀 시프트",
                trailing = {
                    Switch(
                        checked = prefs.burnInProtection,
                        onCheckedChange = { checked -> scope.launch { repository.updateBurnIn(checked) } }
                    )
                }
            )
        }

        SettingsSectionCard(
            title = "스타일",
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text("시계 색상", color = TextDim, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                colorOptions.forEach { (hex, color) ->
                    ColorChip(
                        selected = prefs.textColorHex == hex,
                        color = color,
                        onClick = { scope.launch { repository.updateTextColor(hex) } }
                    )
                }
            }

            Text(
                "시계 스타일",
                color = TextDim,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                styleOptions.forEach { style ->
                    StylePill(
                        text = style.name.lowercase().replaceFirstChar { it.titlecase() },
                        selected = prefs.clockStyle == style.id,
                        onClick = { scope.launch { repository.updateClockStyle(style.id) } }
                    )
                }
            }
        }

        SettingsSectionCard(
            title = "미리보기",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
        ) {
            PreviewCard(
                prefs = prefs,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Button(
            onClick = {
                val intent = Intent(context, FullscreenPreviewActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Text("전체 화면 미리보기 실행")
        }

        Button(
            onClick = { context.startActivity(Intent(Settings.ACTION_DREAM_SETTINGS)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkGold,
                contentColor = Color.Black
            )
        ) {
            Text("시스템 화면보호기 설정 열기")
        }

        SettingsSectionCard(
            title = "실험실",
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            SettingRow(
                title = "터치 종료 방지",
                subtitle = "실험적 옵션 · Dream 종료 탭 무시",
                trailing = {
                    Switch(
                        checked = touchGuardEnabled.value,
                        onCheckedChange = { touchGuardEnabled.value = it }
                    )
                }
            )
        }
    }
}

@Composable
private fun ColorChip(selected: Boolean, color: Color, onClick: () -> Unit) {
    val border = if (selected) BorderStroke(2.dp, DarkGold) else null
    Card(
        onClick = onClick,
        modifier = Modifier
            .size(52.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        border = border
    ) {}
}

@Composable
private fun PreviewCard(prefs: UserPreferences, modifier: Modifier = Modifier) {
    val previewScale = 0.7f
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1118)),
        border = BorderStroke(1.dp, Color(0xFF1E2333))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0E1118))
                .padding(horizontal = 4.dp, vertical = 16.dp)
                .height(280.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = previewScale,
                        scaleY = previewScale
                    )
            ) {
                ClockScreen(prefsOverride = prefs, applyInsets = false)
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF10131D)),
        border = BorderStroke(1.dp, Color(0xFF1E2333))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun SettingRow(title: String, subtitle: String, trailing: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextDim, style = MaterialTheme.typography.bodyMedium)
        }
        trailing()
    }
}

@Composable
private fun StylePill(text: String, selected: Boolean, onClick: () -> Unit) {
    val container = if (selected) DarkGold.copy(alpha = 0.2f) else Color(0xFF161B25)
    val stroke = if (selected) BorderStroke(1.dp, DarkGold) else BorderStroke(1.dp, Color(0xFF1E2333))
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = container),
        border = stroke
    ) {
        Text(
            text = text,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}
