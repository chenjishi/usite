package com.chenjishi.u148;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.chenjishi.u148.home.UserInfo;
import com.chenjishi.u148.utils.Constants;

/**
 * Created by jishichen on 2017/4/14.
 */
public class Config {

    private static final long TWO_DAYS = 2 * 24 * 60 * 60 * 1000;

    private static final String CONFIG_FILE_NAME = "u148_config";

    private static final long VERSION_CHECK_INTERVAL = 24 * 60 * 60 * 1000;

    private static final String KEY_NEXT_TOKEN = "next_from";

    public static final String KEY_UPDATE_TIME = "last_update_time";

    public static final String KEY_CHECK_UPDATE_TIME = "last_check_time";

    public static final String KEY_CHECK_VERSION = "check_version";

    public static final String KEY_VIDEO_UPDATE_TIME = "last_update_time";

    private static final String KEY_CACHE_CLEAR_TIME = "cache_clear_time";
    private static final String KEY_CACHE_UPDATE_TIME = "cache_update_time";

    private static final String KEY_REGISTER_TIME = "register_time";

    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_SEX = "user_sex";
    private static final String KEY_USER_ICON = "user_icon";
    private static final String KEY_USER_TOKEN = "user_token";

    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String SURPRISE_TITLE = "surprise_title";
    private static final String SURPRISE_DESC = "surprise_desc";

    private Config() {
    }

    public static boolean isLogin(Context ctx) {
        return null != getUser(ctx);
    }

    public static int getThemeMode(Context ctx) {
        return getInt(ctx, KEY_THEME_MODE, Constants.MODE_DAY);
    }

    public static void setThemeMode(Context ctx, int mode) {
        putInt(ctx, KEY_THEME_MODE, mode);
    }

    public static void setRegisterTime(Context ctx, long time) {
        putLong(ctx, KEY_REGISTER_TIME, time);
    }

    public static long getRegisterTime(Context ctx) {
        return getLong(ctx, KEY_REGISTER_TIME, -1L);
    }

    public static void setUser(Context ctx, UserInfo user) {
        if (null != user && !TextUtils.isEmpty(user.token)) {
            putString(ctx, KEY_USER_NAME, user.nickname);
            putString(ctx, KEY_USER_SEX, user.sexStr);
            putString(ctx, KEY_USER_ICON, user.icon);
            putString(ctx, KEY_USER_TOKEN, user.token);
        } else {
            putString(ctx, KEY_USER_NAME, "");
            putString(ctx, KEY_USER_SEX, "");
            putString(ctx, KEY_USER_ICON, "");
            putString(ctx, KEY_USER_TOKEN, "");
        }
    }

    public static UserInfo getUser(Context ctx) {
        String token = getString(ctx, KEY_USER_TOKEN, "");

        if (!TextUtils.isEmpty(token)) {
            UserInfo user = new UserInfo();

            user.nickname = getString(ctx, KEY_USER_NAME, "");
            user.sexStr = getString(ctx, KEY_USER_SEX, "");
            user.icon = getString(ctx, KEY_USER_ICON, "");
            user.token = getString(ctx, KEY_USER_TOKEN, "");

            return user;
        } else {
            return null;
        }
    }

    public static void saveSurpriseTitle(Context ctx, String title) {
        putString(ctx, SURPRISE_TITLE, title);
    }

    public static void saveSurpriseDesc(Context ctx, String desc) {
        putString(ctx, SURPRISE_DESC, desc);
    }

    public static String getSurpriseTitle(Context ctx) {
        return getString(ctx, SURPRISE_TITLE, "");
    }

    public static String getSurpriseDesc(Context ctx) {
        return getString(ctx, SURPRISE_DESC, "");
    }

    public static void setClearCacheTime(Context ctx, long t) {
        putLong(ctx, KEY_CACHE_CLEAR_TIME, t + TWO_DAYS);
    }

    public static long getClearCacheTime(Context ctx) {
        return getLong(ctx, KEY_CACHE_CLEAR_TIME, -1L);
    }

    public static int getInt(Context context, String key, int defaultValue) {
        return getPreferences(context).getInt(key, defaultValue);
    }

    public static void putInt(Context context, String key, int value) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static long getLong(Context context, String key, long defaultValue) {
        return getPreferences(context).getLong(key, defaultValue);
    }

    public static void putLong(Context context, String key, long value) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public static String getString(Context context, String key, String defaultValue) {
        return getPreferences(context).getString(key, defaultValue);
    }

    public static void putString(Context context, String key, String value) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(key, value);
        editor.apply();
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE);
    }
}
