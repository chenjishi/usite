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
        mkDirs(getVideoDirectory(context));
        mkDirs(getImageCacheDirectory(context));
        mkDirs(getDataCacheDirectory(context));
        mkDirs(getTempDirectory(context));
    }

    public static String getImageCacheDirectory(Context context) {
        return getRootDirectory(context) + "cache/";
    }

    public static String getTempDirectory(Context context) {
        return getSDCardDirectory(context) + "temp/";
    }

    public static String getDataCacheDirectory(Context context) {
        return getRootDirectory(context) + "data/";
    }

    public static void mkDirs(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists())
            file.mkdirs();
    }

    public static String getVideoDirectory(Context context) {
        return getSDCardDirectory(context) + "video/";
    }

    public static String getSDCardDirectory(Context context) {
        String path = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            path = Environment.getExternalStorageDirectory() + "/u148/";
        }

        return path;
    }

    public static String getRootDirectory(Context context) {
        String rootPath = null;
        File cacheDir = context.getCacheDir();
        if (cacheDir.exists()) {
            rootPath = cacheDir + "/u148/";
        } else {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                rootPath = Environment.getExternalStorageDirectory() + "/u148/";
            }
        }

        return rootPath;
    }
}
