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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.withRotation
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.util.Locale
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
                    WheelViewCompose(
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
                                text = "Spin!",
                                color = androidx.compose.ui.graphics.Color.Yellow.toArgb()
                            ), WheelItem(
                                text = "7",
                                color = androidx.compose.ui.graphics.Color.Gray.toArgb()
                            ), WheelItem(
                                text = "Spin!",
                                color = androidx.compose.ui.graphics.Color.DarkGray.toArgb()
                            )
                        ),
                        spinConfig = SpinConfig.Spin(
                            selectedIndex = 1
                        ),
                        onSpinComplete = {
                                println("Spun to index")
                            }
                        )
                }
            }
        }
    }
}

