@file:OptIn(ExperimentalTextApi::class)

package com.example.myapplication

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.Log
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.withRotation
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun WheelViewCompose(
    wheelItems: List<WheelItem>,
    spinConfig: SpinConfig,
    onSpinComplete: () -> Unit
) {

    val sliceAngle = remember { 360f / wheelItems.size }
    val marginLarge = remember {
        16.dpViewSystem
    }

    // Set defaults that won't resolve to 0 and crash the draw functions
    var height by remember { mutableIntStateOf(42) }
    var width by remember { mutableIntStateOf(42) }

    var buttonWidth by remember {
        mutableIntStateOf(42)
    }

    var buttonHeight by remember {
        mutableIntStateOf(42)
    }

    val wheelDiameter =
        remember(width, height) { (max(width, height) / PERCENT_WHEEL_SHOWN).toInt() }
    val wheelRadius = remember(wheelDiameter) { wheelDiameter / 2 }

    val minCenterDiameter = remember(buttonWidth) { (buttonWidth + (2f * marginLarge)).toInt() }
    val centerDiameter = remember(wheelDiameter, minCenterDiameter) {
        max(
            (wheelDiameter / 3),
            minCenterDiameter
        )
    }// Center should be 1/3 the wheel diameter - need a max?

    val wheelDrawable = remember(wheelRadius) {
        derivedStateOf {
            getWheelDrawable(
                wheelRadius,
                centerDiameter / 2,
                wheelItems
            ).asImageBitmap()
        }
    }

    val wheelXOutOfBounds = remember(wheelDiameter, width) {
        derivedStateOf { ((wheelDiameter - width) / 2).toFloat() }
    }
    val wheelYOutOfBounds = remember(wheelDiameter, height) {
        derivedStateOf { (wheelDiameter - height).toFloat() }
    }

    val localDensity = LocalDensity.current
    val buttonX =
        remember(width, buttonWidth) {
            derivedStateOf {
                with(localDensity) {
                    ((width / 2f) - (buttonWidth /
                            2f))
                        .toInt().toDp()
                }
            }
        }
    val buttonY =
        remember(width, buttonWidth) {
            derivedStateOf {
                with(localDensity) {
                    ((wheelRadius * 1f - (buttonHeight / 2f)) + wheelYOutOfBounds.value).toInt()
                        .toDp()
                }
            }
        }

    var animationRunning by remember {
        mutableStateOf(false)
    }

    var nextVibrationAngle by remember {
        mutableFloatStateOf(sliceAngle)

    }
    var job: Job? = null

    val randomAdditional = remember {
        if (spinConfig.duration != 0L) {
            Random.nextInt(5, sliceAngle.toInt() - 5)
        } else {
            (sliceAngle / 2).toInt()
        }
    }

    val inverseSelectedIndex = remember { (wheelItems.size - 1 - spinConfig.selectedIndex) }
    val finalAngle = remember { inverseSelectedIndex * sliceAngle + randomAdditional + 3600f }
    val context = LocalContext.current


    var wheelSpun by remember {
        mutableStateOf(false)
    }

    val targetValue by remember(animationRunning, spinConfig, finalAngle) {
        derivedStateOf {
            if (
                animationRunning
                || spinConfig is SpinConfig.SpinComplete
//                || wheelSpun
            )
                finalAngle
            else
                0f
        }
    }

    LaunchedEffect(targetValue) {
        Log.d("darran", "targetValue: $targetValue")
    }

    val angle: Float by animateFloatAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = spinConfig.duration.toInt()),
        label = "AnimateWheel",
        finishedListener = {
            wheelSpun = true
            job?.cancel()
            onSpinComplete.invoke()
        }
    )

    LaunchedEffect(animationRunning) {
        snapshotFlow { angle }.collectLatest { latestAngle ->
            if (latestAngle > nextVibrationAngle) {
                nextVibrationAngle += sliceAngle
                job?.cancel()
                job = launch {
                    VibrationCompat.vibrate(context, EffectType.WHEEL_TICK)
                }
                Log.d("WheelViewCompose", "Vibrating! $nextVibrationAngle")
            }
        }
    }

    val wheelOffsetX by remember(wheelDiameter, wheelXOutOfBounds) {
        derivedStateOf {
            (wheelDiameter / 2).toFloat() - wheelXOutOfBounds.value
        }
    }
    val wheelOffsetY by remember(wheelDiameter, wheelYOutOfBounds) {
        derivedStateOf {
            wheelYOutOfBounds.value +
                    (wheelDiameter / 2)
                        .toFloat()
        }
    }


    Box(modifier =
    Modifier
        .aspectRatio(1f)
        .fillMaxSize()
        .onGloballyPositioned {
            height = it.size.height
            width = it.size.width
        }
        .drawBehind {
            rotate(
                angle,
                Offset(
                    wheelOffsetX,
                    wheelOffsetY
                )
            ) {
                drawImage(
                    image = wheelDrawable.value,
                    topLeft = Offset(-wheelXOutOfBounds.value, wheelYOutOfBounds.value)
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .onGloballyPositioned {
                    buttonHeight = it.size.height
                    buttonWidth = it.size.width
                }
                .offset(x = buttonX.value, y = buttonY.value)
        ) {
            AnimatedVisibility(
                visible = spinConfig is SpinConfig.Spin && !(wheelSpun || animationRunning),
                exit = shrinkOut(
                    shrinkTowards = Alignment.Center
                )
            ) {
                Button(
                    modifier = Modifier.defaultMinSize(minWidth = 148.dp),
                    onClick = {
                        animationRunning = true
                    }
                ) {
                    Text(
                        "Spin!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


private fun getWheelDrawable(
    wheelRadius: Int,
    centerRadius: Int,
    wheelItems: List<WheelItem>
): Bitmap {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.DEFAULT_BOLD
    }

    val circleDiameter = wheelRadius * 2
    val numberSlices = wheelItems.size
    val sliceAngle = 360f / numberSlices

    val bitmap = Bitmap.createBitmap(circleDiameter, circleDiameter, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val longestLabel = wheelItems.maxByOrNull { it.text.length }?.text ?: ""
    val (fontSize, marginSize) = getFontAndMarginInfoForString(
        longestLabel,
        wheelRadius - centerRadius
    )
    if (fontSize != null) {
        paint.textSize = fontSize
    }
    val startX = (((canvas.width.toFloat()) / 2f) - wheelRadius)
    val rect = RectF(startX, 0f, circleDiameter + startX, circleDiameter.toFloat())
    wheelItems.forEachIndexed { index, wheelItem ->
        canvas.withRotation(
            (index * sliceAngle) - 90 + (sliceAngle / 2f),
            wheelRadius + startX,
            wheelRadius.toFloat()
        ) {
            paint.color = wheelItem.color
            drawArc(rect, 0f - sliceAngle / 2f, sliceAngle, true, paint)
            paint.color = android.graphics.Color.WHITE
            if (marginSize != null) {
                val xPos = (circleDiameter / 2f) + centerRadius + marginSize
                val yPos = (circleDiameter / 2 - (paint.descent() + paint.ascent()) / 2)
                drawText(wheelItem.text, xPos, yPos, paint)
            }
        }
    }

    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(
        wheelRadius + startX,
        wheelRadius.toFloat(),
        centerRadius.toFloat(),
        paint
    )
    return bitmap
}

private fun getFontAndMarginInfoForString(
    longestLabel: String,
    availableSpace: Int
): Pair<Float?, Float?> {
    val paint = Paint().apply { typeface = Typeface.DEFAULT_BOLD }
    for (i in MAX_FONT_SIZE downTo MIN_FONT_SIZE) {
        // If font getting too small try smaller margin
        val smallestMargin = if (i <= 14) MIN_TEXT_MARGIN else DESIRED_TEXT_MARGIN
        for (j in DESIRED_TEXT_MARGIN downTo smallestMargin) {
            paint.textSize = i.dpViewSystem.toFloat()
            val textLength = paint.measureText(longestLabel)
            val margin = j.dpViewSystem
            // I know this should be margin * 2  but was coming out a little scrunched
            if ((textLength + (margin * 2.5)) <= availableSpace) {
                return Pair(i.dpViewSystem.toFloat(), j.dpViewSystem.toFloat())
            }
        }
    }
    return Pair(null, null)
}

data class WheelItem(
    val text: String,
    @ColorInt val color: Int
)

val Int.dpViewSystem: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).roundToInt()

sealed class SpinConfig(
    open val selectedIndex: Int,
    open val duration: Long
) {
    data class SpinComplete(
        override val selectedIndex: Int,
    ) : SpinConfig(selectedIndex, duration = 0L)

    data class Spin(
        override val selectedIndex: Int,
    ) : SpinConfig(selectedIndex, duration = SPIN_DURATION)
}
