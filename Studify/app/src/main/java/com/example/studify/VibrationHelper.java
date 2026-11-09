package com.example.studify;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

/**
 * Centralized haptic-feedback helper for Studify.
 * Gives a short confirmation buzz whenever the user marks a task as done.
 */
public class VibrationHelper {

    private static final long DONE_DURATION_MS = 40;

    /** Vibrate briefly to confirm a task was marked done. */
    public static void vibrateTaskDone(Context context) {
        if (context == null) return;
        try {
            Vibrator vibrator = getVibrator(context);
            if (vibrator == null || !vibrator.hasVibrator()) return;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(
                        DONE_DURATION_MS, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //noinspection deprecation
                vibrator.vibrate(DONE_DURATION_MS);
            }
        } catch (Exception ignored) {
            // Never let a haptics failure crash the task-completion flow.
        }
    }

    private static Vibrator getVibrator(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager manager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            return manager != null ? manager.getDefaultVibrator() : null;
        } else {
            return (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
    }
}
