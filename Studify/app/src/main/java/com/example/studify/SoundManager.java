package com.example.studify;

import android.content.Context;
import android.media.AudioManager;
import android.view.SoundEffectConstants;
import android.view.View;

/**
 * Centralized sound-effect helper for Studify.
 * Uses the device's built-in system click sound (via AudioManager)
 * so no extra audio asset is bundled with the app, and respects the
 * user's "Sound Effects" preference stored in SessionManager.
 */
public class SoundManager {

    /** Play a UI click sound if the user has sound effects enabled. */
    public static void playClick(Context context) {
        if (context == null) return;
        SessionManager session = new SessionManager(context);
        if (!session.isSoundEnabled()) return;

        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            am.playSoundEffect(AudioManager.FX_KEY_CLICK);
        }
    }

    /** Play a slightly different tone for toggles/switches. */
    public static void playToggle(Context context, View view) {
        if (context == null) return;
        SessionManager session = new SessionManager(context);
        if (!session.isSoundEnabled()) return;

        if (view != null) {
            view.playSoundEffect(SoundEffectConstants.CLICK);
        }
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
        }
    }
}
