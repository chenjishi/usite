package com.chenjishi.u148.base;

import android.content.Context;
import android.content.SharedPreferences;
import com.weibo.sdk.android.Oauth2AccessToken;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-7-18
 * Time: 下午4:37
 * To change this template use File | Settings | File Templates.
 */
public class PrefsUtil {
    private static final long VERSION_CHECK_INTERVAL = 24 * 60 * 60 * 1000;

    private static final String CONFIG_FILE_NAME = "u148_prefs";

    private static final String KEY_NEXT_TOKEN = "next_from";

    public static final String KEY_UPDATE_TIME = "last_update_time";

    public static final String KEY_CHECK_VERSION = "check_version";

    public static final String KEY_VIDEO_UPDATE_TIME = "last_update_time";

    private static final String KEY_ACESS_TOKEN = "access_token";
    private static final String KEY_EXPIRE_IN = "expire_in";
    private static final String KEY_CACHE_CLEAR_TIME = "cache_clear_time";
    private static final String KEY_CACHE_UPDATE_TIME = "cache_update_time";

    public static void setCacheUpdateTime(long t) {
        saveLongPreference(KEY_CACHE_UPDATE_TIME, t);
    }

    public static long getCacheUpdateTime() {
        return getLongPreferences(KEY_CACHE_UPDATE_TIME);
    }

    public static void setClearCacheTime(long t) {
        saveLongPreference(KEY_CACHE_CLEAR_TIME, t);
    }

    public static long getClearCacheTime() {
        return getLongPreferences(KEY_CACHE_CLEAR_TIME);
    }

    public static void saveAccessToken(Oauth2AccessToken token) {
        SharedPreferences.Editor editor = AppApplication.getInstance().getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_ACESS_TOKEN, token.getToken());
        editor.putLong(KEY_EXPIRE_IN, token.getExpiresTime());
        editor.commit();
    }

    public static Oauth2AccessToken getAccessToken() {
        Context context = AppApplication.getInstance();
        Oauth2AccessToken token = new Oauth2AccessToken();
        SharedPreferences prfs = context.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE);
        token.setToken(prfs.getString(KEY_ACESS_TOKEN, ""));
        token.setExpiresTime(prfs.getLong(KEY_EXPIRE_IN, 0L));
        return token;
    }

    private static void saveStringPreference(String key, String value) {
        Context context = AppApplication.getInstance();
        SharedPreferences.Editor editor = context.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void saveLongPreference(String key, long value) {
        Context context = AppApplication.getInstance();
        SharedPreferences.Editor editor = context.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static long getLongPreferences(String key) {
        Context context = AppApplication.getInstance();
        return context.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE).getLong(key, -1L);
    }

    public static String getStringPreference(String key) {
        Context context = AppApplication.getInstance();
        return context.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE).getString(key, "");
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

    public static void saveCheckVersionTime(long l) {
        saveLongPreference(KEY_CHECK_VERSION, l + VERSION_CHECK_INTERVAL);
    }

    public static long getCheckVersionTime() {
        return getLongPreferences(KEY_CHECK_VERSION);
    }
}
