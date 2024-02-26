package com.example.myapplication

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.graphics.withRotation
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun WheelViewCompose(wheelItems: List<WheelItem>) {

    var height by remember { mutableIntStateOf(42) }
    var width by remember { mutableIntStateOf(42) }
    var buttonWidth by remember {
        mutableIntStateOf(42)
    }

    var buttonHeight by remember {
        mutableIntStateOf(42)
    }

    val wheelDiameter = (max(width, height) / PERCENT_WHEEL_SHOWN).toInt()
    val wheelRadius = wheelDiameter / 2

//        val minCenterDiameter = (button.measuredWidth + (2f * marginLarge)).toInt()
    val minCenterDiameter = 80
    val centerDiameter = max(
        (wheelDiameter / 3),
        minCenterDiameter
    ) // Center should be 1/3 the wheel diameter - need a max?

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

    val buttonX =  with(LocalDensity.current) { ((width / 2f) - (buttonWidth / 2f)).toInt().toDp() }
    val buttonY = with(LocalDensity.current) {
        ((wheelRadius * 1f - (buttonHeight / 2f)) + wheelYOutOfBounds.value).toInt()
        .toDp()
    }

    // Ensure the button is fully visible with 16dp bottom margin on the screen. This is
    // the "best" UX fix for now because placing this custom view within a ScrollView
    // presented a number of issues on either small or large screens
//        val buttonBottom = button.y + top + button.measuredHeight + 16.dp
//        if (buttonBottom > bottom) {
//            button.y -= buttonBottom - bottom
//        }



    var currentAngle by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
//            currentAngle += 2
            delay(3) // Or better logic for when rotation ends
        }
    }

    Box(modifier =
    Modifier
        .background(Color.DarkGray)
        .aspectRatio(1f)
        .fillMaxSize()
        .onGloballyPositioned {
            height = it.size.height
            width = it.size.width
        }) {



        Canvas(
            modifier =
            Modifier
                .fillMaxSize(), onDraw = {
                withTransform({
                    rotate(
                        currentAngle.toFloat(),
                        Offset(
                            (wheelDiameter / 2).toFloat() - wheelXOutOfBounds.value,
                            wheelYOutOfBounds.value + (wheelDiameter / 2)
                                .toFloat()
                        )
                    )
                }) {
                    drawImage(
                        image = wheelDrawable.value,
                        topLeft = Offset(-wheelXOutOfBounds.value, wheelYOutOfBounds.value)
                    )
                }
            }
        )

        Box(
            modifier = Modifier.onGloballyPositioned {
                buttonHeight = it.size.height
                buttonWidth = it.size.width
            }.offset(x = buttonX, y = buttonY)
        ) {
            Button(onClick = { /*TODO*/ }) {
                Text("Spin!")
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
                drawText(wheelItem.text.uppercase(Locale.getDefault()), xPos, yPos, paint)
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

