package org.telegram.owpengram;

import android.view.View;
import android.view.ViewGroup;

import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.IconBackgroundColors;
import org.telegram.ui.Components.UItem;

/**
 * Static glue injected from smali:
 *  - settings row creation / click handling;
 *  - global avatar (and round image) shape adjustment;
 *  - snowflake overlay installation.
 */
public final class CustomHooks {

    /** Custom UItem id for the "Кастомные функции" row (never used by stock rows). */
    public static final int CUSTOM_ROW_ID = 0x1F50000;

    private CustomHooks() {}

    /**
     * Called from SettingsActivity.fillItems before the first standard row.
     * Returns a fully built row or null on any failure.
     */
    public static UItem settingsRow() {
        try {
            UItem item = org.telegram.ui.SettingsActivity.SettingCell.Factory.of(
                    1,
                    IconBackgroundColors.CYAN.top,
                    IconBackgroundColors.CYAN.bottom,
                    R.drawable.msg_customize,
                    "Кастомные функции",
                    "Рамка профиля, снежинки");
            if (item != null) {
                item.id = CUSTOM_ROW_ID;
            }
            return item;
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Called at the beginning of SettingsActivity.onClick.
     * Returns true when the click was consumed here.
     */
    public static boolean onSettingsClick(BaseFragment fragment, UItem item) {
        try {
            if (fragment == null || item == null || item.id != CUSTOM_ROW_ID) {
                return false;
            }
            return fragment.presentFragment(new CustomFunctionsActivity());
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Called at the beginning of ImageReceiver.setRoundRadius([I)V.
     * Adjusts the requested corner radii according to the user's
     * profile frame shape setting (100 = keep untouched).
     * The input array is mutated in place and returned.
     */
    public static int[] adjustRoundRadius(int[] radii) {
        try {
            if (radii == null || radii.length != 4) {
                return radii;
            }
            int r = radii[0];
            if (r <= 0) {
                return radii;
            }
            if (radii[1] != r || radii[2] != r || radii[3] != r) {
                return radii; // non-uniform corners: leave as is
            }
            float factor = CustomFeatures.frameFactor();
            if (factor >= 0.999f) {
                return radii;
            }
            int nr = Math.round(r * factor);
            if (nr < 0) {
                nr = 0;
            }
            if (nr == r) {
                return radii;
            }
            radii[0] = nr;
            radii[1] = nr;
            radii[2] = nr;
            radii[3] = nr;
        } catch (Throwable ignore) {
            // cosmetic feature — never crash
        }
        return radii;
    }

    /**
     * Scalar variant of {@link #adjustRoundRadius(int[])} for views that store
     * a single radius value (e.g. the profile page avatar clip path), so the
     * frame shape applies everywhere — own profile and other users' profiles.
     */
    public static int adjustRoundRadiusScalar(int radius) {
        if (radius <= 0) {
            return radius;
        }
        int[] r = adjustRoundRadius(new int[]{radius, radius, radius, radius});
        return r == null ? radius : r[0];
    }

    /**
     * Called from LaunchActivity.onCreate (after content view setup).
     * Installs the snowfall overlay on the window's decor view.
     */
    public static void initSnowflakes(android.app.Activity activity) {
        SnowflakeOverlayView.attachToActivity(activity);
    }

    /** Convenience: nothing should ever hold a strong ref to the overlay here. */
    public static View findOverlay(ViewGroup parent) {
        if (parent == null) {
            return null;
        }
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof SnowflakeOverlayView) {
                return child;
            }
        }
        return null;
    }
}
