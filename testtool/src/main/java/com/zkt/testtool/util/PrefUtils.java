package com.zkt.testtool.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.zkt.testtool.logcat.Format;
import com.zkt.testtool.logcat.Level;


public class PrefUtils {

    /**
     * Preferences from {link com.anrapps.ultimatelogcat.ActivitySettings}
     */
    public static final String PREFERENCE_KEY_LOG_FORMAT = "preference_log_format";

    private static final String PREF_KEY_LOG_LEVEL = "pref_log_level";
    private static final String PREF_KEY_SEARCH_FILTER = "pref_log_filter";

    public static Level getLevel(Context c) {
        return Level.valueOf(sp(c).getString(PREF_KEY_LOG_LEVEL, Level.V.toString()));
    }

    public static void setLevel(Context c, Level level) {
        spe(c).putString(PREF_KEY_LOG_LEVEL, level.toString()).apply();
    }

    public static Format getFormat(Context c) {
        return Format.valueOf(sp(c).getString(PREFERENCE_KEY_LOG_FORMAT, Format.BRIEF.toString()));
    }

    public static String getSearchFilter(Context c) {
        return sp(c).getString(PREF_KEY_SEARCH_FILTER, "");
    }

    public static void setSearchFilter(Context c, String filter) {
        if (TextUtils.isEmpty(filter)) {
            removeSearchFilter(c);
        } else {
            spe(c).putString(PREF_KEY_SEARCH_FILTER, filter).apply();
        }
    }

    public static void removeSearchFilter(Context c) {
        spe(c).remove(PREF_KEY_SEARCH_FILTER).apply();
    }


    public static SharedPreferences sp(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c);
    }

    public static SharedPreferences.Editor spe(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).edit();
    }
}
