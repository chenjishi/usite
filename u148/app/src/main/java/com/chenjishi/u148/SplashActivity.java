package com.chenjishi.u148;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import com.chenjishi.u148.home.MainActivity;
import com.chenjishi.u148.utils.FileUtils;
import com.chenjishi.u148.utils.Utils;

import java.io.File;

/**
 * Created by jishichen on 2017/4/14.
 */
public class SplashActivity extends BaseActivity {
    private static final long SPLASH_TIME = 3000;

    private long startTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash, true);
        startTime = System.currentTimeMillis();

        new Thread() {
            @Override
            public void run() {
                clearCache();
            }
        }.start();

    }

    private void clearCache() {
        //clear cache of 2 days before
        long lastTime = Config.getClearCacheTime(this);
        if (System.currentTimeMillis() >= lastTime) {
            deleteDatabase("webview.db");
            deleteDatabase("webviewCache.db");
            Config.setClearCacheTime(this, System.currentTimeMillis());
        }

        //clear temp files, such as shared image or temp upgrade apk
        File tempFile = new File(FileUtils.getTempCacheDir());
        if (tempFile.exists()) {
            Utils.delete(tempFile);
        }

        String upgradeApkPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/u148.apk";
        File apkFile = new File(upgradeApkPath);
        if (apkFile.exists()) {
            Utils.delete(apkFile);
        }

        Handler mainThread = new Handler(Looper.getMainLooper());

        mainThread.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, SPLASH_TIME - (System.currentTimeMillis() - startTime));
    }
}
