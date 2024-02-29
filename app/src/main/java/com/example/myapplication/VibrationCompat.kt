package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object VibrationCompat {

    private fun getVibrator(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    fun vibrate(context: Context, effectType: EffectType) {
        val vibrator = getVibrator(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && effectType.effectId != null &&
            vibrator.areAllEffectsSupported(
                effectType.effectId
            ) == Vibrator.VIBRATION_EFFECT_SUPPORT_YES
        ) {
            vibrator.vibrate(VibrationEffect.createPredefined(effectType.effectId))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(effectType.compatPattern, -1)
        }
    }
}

@SuppressLint("InlinedApi")
// Values from here https://cs.android.com/android/platform/superproject/+/master:frameworks/base/services/core/java/com/android/server/vibrator/VibrationSettings.java;l=121;
enum class EffectType(val compatPattern: LongArray, val effectId: Int? = null) {
    CONFIRM(longArrayOf(0, 10, 20, 30), VibrationEffect.EFFECT_CLICK),
    WHEEL_TICK(longArrayOf(0, 25))
}
