package com.zkt.testtoll.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.zkt.testtoll.logcat.Buffer;
import com.zkt.testtoll.logcat.Format;
import com.zkt.testtoll.logcat.Level;


public class PrefUtils {

    /**
     * Preferences from {link com.anrapps.ultimatelogcat.ActivitySettings}
     */
    public static final String PREFERENCE_KEY_LOG_FORMAT = "preference_log_format";
    public static final String PREFERENCE_KEY_LOG_BUFFER = "preference_log_buffer";
    public static final String PREFERENCE_KEY_TEXT_SIZE = "preference_text_size";
    public static final String PREFERENCE_KEY_TEXT_FONT = "preference_text_font";
    public static final String PREFERENCE_KEY_CF_VIBRATE = "preference_crash_finder_vibrate";
    public static final String PREFERENCE_KEY_CF_SOUND = "preference_crash_finder_sound";

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

    public static Buffer getBuffer(Context c) {
        return Buffer.valueOf(sp(c).getString(PREFERENCE_KEY_LOG_BUFFER, Buffer.MAIN.toString()));
    }

    public static String getSearchFilter(Context c) {
        return sp(c).getString(PREF_KEY_SEARCH_FILTER, "");
    }

    public static void setSearchFilter(Context c, String filter) {
        if (TextUtils.isEmpty(filter)) removeSearchFilter(c);
        else spe(c).putString(PREF_KEY_SEARCH_FILTER, filter).apply();
    }

    public static void removeSearchFilter(Context c) {
        spe(c).remove(PREF_KEY_SEARCH_FILTER).apply();
    }

    public static float getTextSize(Context c) {
        return Float.parseFloat(sp(c).getString(PREFERENCE_KEY_TEXT_SIZE, "12"));
    }

    public static Typeface getTextFont(Context c) {
        int index = Integer.parseInt(sp(c).getString(PREFERENCE_KEY_TEXT_FONT, "0"));
        switch (index) {
            case 0:
                return Typeface.DEFAULT;
            case 1:
                return Typeface.DEFAULT_BOLD;
            case 2:
                return Typeface.MONOSPACE;
            case 3:
                return Typeface.SERIF;
            case 4:
                return Typeface.SANS_SERIF;
            default:
                return Typeface.DEFAULT;
        }
    }


    public static SharedPreferences sp(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c);
    }

    public static SharedPreferences.Editor spe(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).edit();
    }
}
