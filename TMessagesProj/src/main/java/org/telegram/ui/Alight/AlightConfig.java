package org.telegram.ui.Alight;

import android.content.Context;
import android.content.SharedPreferences;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;

/**
 * AlightGram custom settings storage.
 */
public class AlightConfig {
    public static final String SERVER_ADDRESS = "2.27.200.20";
    public static final String SERVER_ENDPOINT = SERVER_ADDRESS + ":443";
    public static final String DOMAIN = "ag.cc";

    public static boolean ghostMode = false;
    public static boolean editHistory = true;
    public static boolean hideSponsored = false;

    private static boolean loaded = false;

    private static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences("alightconfig", Context.MODE_PRIVATE);
    }

    public static void load() {
        try {
            Context ctx = ApplicationLoader.applicationContext;
            if (ctx == null) {
                return;
            }
            SharedPreferences p = prefs();
            ghostMode = p.getBoolean("ghostMode", false);
            editHistory = p.getBoolean("editHistory", true);
            hideSponsored = p.getBoolean("hideSponsored", false);
            loaded = true;
        } catch (Throwable t) {
            FileLog.e(t);
        }
    }

    public static void ensureLoaded() {
        if (!loaded) {
            load();
        }
    }

    public static void setGhostMode(boolean value) {
        ghostMode = value;
        save("ghostMode", value);
    }

    public static void setEditHistory(boolean value) {
        editHistory = value;
        save("editHistory", value);
    }

    public static void setHideSponsored(boolean value) {
        hideSponsored = value;
        save("hideSponsored", value);
    }

    private static void save(String key, boolean value) {
        try {
            prefs().edit().putBoolean(key, value).apply();
        } catch (Throwable t) {
            FileLog.e(t);
        }
    }
}
