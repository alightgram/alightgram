package org.telegram.owpengram;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Central storage for AlightGram custom features.
 * All values are mirrored into volatile fields so that render loops
 * (snowflake overlay, image hooks) can read them with zero locking.
 */
public final class CustomFeatures {

    public static final String PREFS_NAME = "alightgram_custom";

    // Snowflakes
    public static final float SNOW_SIZE_MIN = 0.5f;   // x0.5
    public static final float SNOW_SIZE_MAX = 3.0f;   // x3.0
    public static final float SNOW_SPEED_MIN = 0.25f; // x0.25
    public static final float SNOW_SPEED_MAX = 3.0f;  // x3.0

    // Frame shape: 100 = fully round (circle), 0 = square
    public static final int FRAME_MIN = 0;
    public static final int FRAME_MAX = 100;

    private static final String KEY_SNOW_ENABLED = "snow_enabled";
    private static final String KEY_SNOW_SIZE = "snow_size";
    private static final String KEY_SNOW_SPEED = "snow_speed";
    private static final String KEY_FRAME_SHAPE = "frame_shape";

    // Volatile runtime values
    public static volatile boolean snowEnabled = true;
    public static volatile float snowSize = 1.0f;
    public static volatile float snowSpeed = 1.0f;
    public static volatile int frameShape = 100;

    private static SharedPreferences prefs;
    private static volatile boolean loaded = false;

    private CustomFeatures() {}

    public static void init(Context context) {
        if (loaded || context == null) {
            return;
        }
        synchronized (CustomFeatures.class) {
            if (loaded) {
                return;
            }
            try {
                prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                snowEnabled = prefs.getBoolean(KEY_SNOW_ENABLED, true);
                snowSize = clamp(prefs.getFloat(KEY_SNOW_SIZE, 1.0f), SNOW_SIZE_MIN, SNOW_SIZE_MAX);
                snowSpeed = clamp(prefs.getFloat(KEY_SNOW_SPEED, 1.0f), SNOW_SPEED_MIN, SNOW_SPEED_MAX);
                frameShape = clampInt(prefs.getInt(KEY_FRAME_SHAPE, 100), FRAME_MIN, FRAME_MAX);
            } catch (Throwable t) {
                // Never crash on settings: keep safe defaults
                snowEnabled = true;
                snowSize = 1.0f;
                snowSpeed = 1.0f;
                frameShape = 100;
            }
            loaded = true;
        }
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static void setSnowEnabled(boolean value) {
        snowEnabled = value;
        if (prefs != null) {
            prefs.edit().putBoolean(KEY_SNOW_ENABLED, value).apply();
        }
    }

    public static void setSnowSize(float value) {
        snowSize = clamp(value, SNOW_SIZE_MIN, SNOW_SIZE_MAX);
        if (prefs != null) {
            prefs.edit().putFloat(KEY_SNOW_SIZE, snowSize).apply();
        }
    }

    public static void setSnowSpeed(float value) {
        snowSpeed = clamp(value, SNOW_SPEED_MIN, SNOW_SPEED_MAX);
        if (prefs != null) {
            prefs.edit().putFloat(KEY_SNOW_SPEED, snowSpeed).apply();
        }
    }

    public static void setFrameShape(int value) {
        frameShape = clampInt(value, FRAME_MIN, FRAME_MAX);
        if (prefs != null) {
            prefs.edit().putInt(KEY_FRAME_SHAPE, frameShape).apply();
        }
    }

    /** Factor applied to avatar/image round radius. 1.0 = keep requested (circle). */
    public static float frameFactor() {
        int s = frameShape;
        if (s >= 100) {
            return 1.0f;
        }
        if (s <= 0) {
            return 0.0f;
        }
        return s / 100.0f;
    }

    /** Maps slider position (0..100) to snow size factor. */
    public static float sliderToSnowSize(int position) {
        return SNOW_SIZE_MIN + (SNOW_SIZE_MAX - SNOW_SIZE_MIN) * clampInt(position, 0, 100) / 100.0f;
    }

    /** Maps slider position (0..100) to snow speed factor. */
    public static float sliderToSnowSpeed(int position) {
        return SNOW_SPEED_MIN + (SNOW_SPEED_MAX - SNOW_SPEED_MIN) * clampInt(position, 0, 100) / 100.0f;
    }

    public static int snowSizeToPosition(float factor) {
        return clampInt(Math.round((clamp(factor, SNOW_SIZE_MIN, SNOW_SIZE_MAX) - SNOW_SIZE_MIN) / (SNOW_SIZE_MAX - SNOW_SIZE_MIN) * 100.0f), 0, 100);
    }

    public static int snowSpeedToPosition(float factor) {
        return clampInt(Math.round((clamp(factor, SNOW_SPEED_MIN, SNOW_SPEED_MAX) - SNOW_SPEED_MIN) / (SNOW_SPEED_MAX - SNOW_SPEED_MIN) * 100.0f), 0, 100);
    }

    private static float clamp(float v, float min, float max) {
        return v < min ? min : (v > max ? max : v);
    }

    private static int clampInt(int v, int min, int max) {
        return v < min ? min : (v > max ? max : v);
    }
}
