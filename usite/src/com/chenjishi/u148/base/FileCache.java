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
        mkDirs(getImageCacheDirectory(context));
        mkDirs(getDataCacheDirectory(context));
        mkDirs(getSavedImageDirectory(context));
    }

    public static String getImageCacheDirectory(Context context) {
        return getRootDirectory(context) + "cache/";
    }

    public static String getSavedImageDirectory(Context context) {
        return getRootDirectory(context) + "images/";
    }

    public static String getDataCacheDirectory(Context context) {
        return getRootDirectory(context) + "data/";
    }

    public static void mkDirs(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists())
            file.mkdirs();
    }

    public static String getRootDirectory(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return Environment.getExternalStorageDirectory() + "/u148/";
        } else {
            return context.getCacheDir() + "/u148/";
        }
    }
}
