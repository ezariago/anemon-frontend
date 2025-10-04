package me.ezar.anemon.ui.screens.trip.components

import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationSlider(
    modifier: Modifier = Modifier,
    text: String,
    isEnabled: Boolean,
    onSuccessfulSlide: () -> Unit
) {
    Log.d("ConfirmationSlider", isEnabled.toString())
    var sliderPosition by remember { mutableFloatStateOf(0.2f) }
    val haptic = LocalHapticFeedback.current
    var touched by remember { mutableStateOf(false) }
    val ctx = LocalContext.current

    var anchored by remember { mutableStateOf(false) }
    anchored = sliderPosition > 0.9f

    var shownText by remember { mutableStateOf(text) }
    shownText = if (!isEnabled) {
        "Deketin dulu tujuannya!"
    } else if (anchored) {
        "Lepaskan!"
    } else {
        text
    }

    val animatedColor by animateColorAsState(
        targetValue = if (isEnabled) MaterialTheme.colorScheme.primaryContainer else Color.Gray,
        label = "color"
    )
    val colors =
        SliderDefaults.colors(
            inactiveTrackColor = animatedColor
        )

    val animatedSliderPosition: Float by animateFloatAsState(
        targetValue = if (touched) {
            if (anchored) 1f
            else sliderPosition
        } else if (isEnabled) {
            0.2f
        } else {
            0f
        },
        label = "",
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        )
    )

    LaunchedEffect(anchored) {
        if (anchored) {
            haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.GestureEnd)
        }
    }

    if (anchored && !touched) {
        LaunchedEffect(Unit) {
            onSuccessfulSlide()
            sliderPosition = 0.2f
            anchored = false

            val timings: LongArray = longArrayOf(
                50, 50, 50, 50, 50, 100
            )
            val amplitudes: IntArray = intArrayOf(
                33, 51, 75, 113, 170, 255
            )
            val repeatIndex = -1

            val vibrator = ctx.getSystemService(Vibrator::class.java)

            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    timings, amplitudes, repeatIndex
                )
            )

        }
    }


    Column(
        modifier = modifier,
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            contentAlignment = Alignment.Center
        ) {
            Slider(
                value = animatedSliderPosition,
                onValueChange = {
                    if (isEnabled) {
                        sliderPosition = it
                        touched = true
                    }
                },
                modifier = modifier.fillMaxWidth(),
                track = { sliderState ->
                    SliderDefaults.Track(
                        modifier = modifier
                            .height(60.dp)
                            .clip(RoundedCornerShape(32.dp)),
                        colors = colors,
                        enabled = true,
                        sliderState = sliderState,
                        thumbTrackGapSize = if (isEnabled) 2.dp else 0.dp,
                    )
                },
                thumb = {},
                onValueChangeFinished = {
                    touched = false
                    if (!isEnabled)
                        haptic.performHapticFeedback(HapticFeedbackType.Reject)
                }
            )
            Text(shownText)
        }
    }
}

