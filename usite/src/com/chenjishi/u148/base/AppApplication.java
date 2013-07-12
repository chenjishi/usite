package com.chenjishi.u148.base;

import android.app.Application;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-8
 * Time: 下午9:57
 * To change this template use File | Settings | File Templates.
 */
public class AppApplication extends Application {
    private static AppApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        FileCache.init(this);
    }

    public static AppApplication getInstance() {
        return mInstance;
    }

    //todo
    //clear disk cache, implemented later
}
