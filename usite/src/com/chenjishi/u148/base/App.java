package com.chenjishi.u148.base;

import android.app.Application;
import com.chenjishi.u148.util.HttpUtils;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-8
 * Time: 下午9:57
 * To change this template use File | Settings | File Templates.
 */
public class App extends Application {
    private static App mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        /**
         * init
         */
        FileCache.init(this);
        HttpUtils.init(this);
        DBHelper.getInstance(this);
    }

    public static App getInstance() {
        return mInstance;
    }

    //todo
    //clear disk cache, implemented later
}
