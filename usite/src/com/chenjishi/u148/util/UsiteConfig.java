package com.chenjishi.u148.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-19
 * Time: 下午4:34
 * To change this template use File | Settings | File Templates.
 */
public class UsiteConfig {
    private static final String PREFERENCE_FILE_NAME = "usite_preference";

    public static final long FOUR_HOURS = 4 * 60 * 60 * 1000;

    private static final String KEY_UPDATE_TIME = "update_time";

    public static void saveUpdateTime(Context context, long l) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putLong(KEY_UPDATE_TIME, l);
        editor.commit();
    }

    public static long getUpdateTime(Context context) {
        return context.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE).getLong(KEY_UPDATE_TIME, -1);
    }
}
