package com.chenjishi.u148.base;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-7-9
 * Time: 下午3:52
 * To change this template use File | Settings | File Templates.
 */
public class FileCache {

    public static void init(Context context) {
        mkDirs(getImageCacheDir(context));
        mkDirs(getDataCacheDir(context));
    }

    public static void mkDirs(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) file.mkdirs();
    }

    public static String getImageCacheDir(Context context) {
        return getInternalCacheDir(context) + "/image/";
    }

    public static String getDataCacheDir(Context context) {
        return getInternalCacheDir(context) + "/data/";
    }

    public static String getInternalCacheDir(Context context) {
        String path = null;
        File cacheDir = context.getCacheDir();
        if (cacheDir.exists()) path = cacheDir.getAbsolutePath();

        return path;
    }

    public static String getTempCacheDir() {
        return getSDCardDirectory() + "/u148/";
    }

    public static String getSDCardDirectory() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return Environment.getExternalStorageDirectory().getPath();
        }
        return null;
    }
}
