package com.example.myapplication

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.ColorInt
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.withRotation
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

const val MAX_FONT_SIZE = 32
const val MIN_FONT_SIZE = 12
const val DESIRED_TEXT_MARGIN = 24
const val MIN_TEXT_MARGIN = 4
const val PERCENT_WHEEL_SHOWN = .86
const val SPIN_DURATION = 7000L

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Turn2Turn(
                        listOf(
                            WheelItem(
                                text = "foo",
                                color = androidx.compose.ui.graphics.Color.Blue.toArgb()
                            ), WheelItem(
                                text = "bar",
                                color = androidx.compose.ui.graphics.Color.Cyan.toArgb()
                            ), WheelItem(
                                text = "baz",
                                color = androidx.compose.ui.graphics.Color.Green.toArgb()
                            ), WheelItem(
                                text = "foobarbaz",
                                color = androidx.compose.ui.graphics.Color.Magenta.toArgb()
                            ), WheelItem(
                                text = "5",
                                color = androidx.compose.ui.graphics.Color.Red.toArgb()
                            ), WheelItem(
                                text = "6",
                                color = androidx.compose.ui.graphics.Color.Yellow.toArgb()
                            ), WheelItem(
                                text = "7",
                                color = androidx.compose.ui.graphics.Color.Gray.toArgb()
                            ), WheelItem(
                                text = "8",
                                color = androidx.compose.ui.graphics.Color.DarkGray.toArgb()
                            )
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun Turn2Turn(wheelItems: List<WheelItem>) {

    val resources = LocalContext.current.resources
    var height by remember { mutableIntStateOf(42) }
    var width by remember { mutableIntStateOf(42) }

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

//        button.x = (width / 2f) - (button.measuredWidth / 2f)
//        button.y = wheelRadius * 1f - (button.measuredHeight / 2f)

    // Ensure the button is fully visible with 16dp bottom margin on the screen. This is
    // the "best" UX fix for now because placing this custom view within a ScrollView
    // presented a number of issues on either small or large screens
//        val buttonBottom = button.y + top + button.measuredHeight + 16.dp
//        if (buttonBottom > bottom) {
//            button.y -= buttonBottom - bottom
//        }

    val wheelXOutOfBounds = (wheelDiameter - width) / 2
//        wheelYOutOfBounds = wheelDiameter - height

    var currentAngle by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            currentAngle += 2
            delay(3) // Or better logic for when rotation ends
        }
    }

    Box(modifier =
    Modifier
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
                            (wheelDiameter / 2).toFloat() - wheelXOutOfBounds,
                            (wheelDiameter / 2)
                                .toFloat()
                        )
                    )
                }) {
                    drawImage(
                        image = wheelDrawable.value,
                        topLeft = Offset(-wheelXOutOfBounds.toFloat(), 0f)
                    )
                }
            }
        )
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
    val canvas = Canvas(bitmap)
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
            paint.color = Color.WHITE
            if (marginSize != null) {
                val xPos = (circleDiameter / 2f) + centerRadius + marginSize
                val yPos = (circleDiameter / 2 - (paint.descent() + paint.ascent()) / 2)
                drawText(wheelItem.text.uppercase(Locale.getDefault()), xPos, yPos, paint)
            }
        }
    }

    paint.color = Color.WHITE
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
            paint.textSize = i.dp.toFloat()
            val textLength = paint.measureText(longestLabel)
            val margin = j.dp
            // I know this should be margin * 2  but was coming out a little scrunched
            if ((textLength + (margin * 2.5)) <= availableSpace) {
                return Pair(i.dp.toFloat(), j.dp.toFloat())
            }
        }
    }
    return Pair(null, null)
}

data class WheelItem(
    val text: String,
    @ColorInt val color: Int
)

val Int.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).roundToInt()

