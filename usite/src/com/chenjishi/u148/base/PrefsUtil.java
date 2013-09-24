package com.chenjishi.u148.base;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-7-18
 * Time: 下午4:37
 * To change this template use File | Settings | File Templates.
 */
public class PrefsUtil {
    private static final String CONFIG_FILE_NAME = "waqu_prefs";

    private static final String KEY_NEXT_TOKEN = "next_from";

    public static final String KEY_UPDATE_TIME = "last_update_time";

    private static void saveStringPreference(String key, String value, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void saveLongPreference(String key, long value, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static long getLongPreferences(String key, long defaultValue, Context context) {
        return context.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE).getLong(key, defaultValue);
    }

    public static String getStringPreference(String key, String defaultValue, Context context) {
        return context.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE).getString(key, defaultValue);
    }

    private static void saveIntPreference(String key, int value, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(key, value);
        editor.commit();
    }

    private static int getIntPreference(String key, int defaultValue, Context context) {
        return context.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE).getInt(key, defaultValue);
    }

    public static void saveNextToken(int value) {
        saveIntPreference(KEY_NEXT_TOKEN, value, AppApplication.getInstance());
    }

    public static int getNextToken() {
        return getIntPreference(KEY_NEXT_TOKEN, 0, AppApplication.getInstance());
    }
}
