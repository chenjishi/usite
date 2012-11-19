package com.chenjishi.usite.base;

import android.app.Application;
import android.os.Environment;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-8
 * Time: 下午9:57
 * To change this template use File | Settings | File Templates.
 */
public class App extends Application {
    private static App instance = null;

    @Override
    public void onCreate() {
        super.onCreate();

        File imageDir = new File(getImageDirectory());
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }

        File cacheDir = new File(getCacheDirectory());
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }

    public static App instance() {
        if (null == instance) {
            instance = new App();
        }
        return instance;
    }

    public String getRootDirectory()  {
        File path;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            path = Environment.getExternalStorageDirectory();
        } else {
            path = this.getCacheDir();
        }
        return path + "/u148";
    }

    public String getImageDirectory() {
        return getRootDirectory() + "/images/";
    }

    public String getCacheDirectory() {
        return getRootDirectory() + "/caches/";
    }
}
