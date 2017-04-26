package com.chenjishi.u148;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import com.chenjishi.u148.home.MainActivity;
import com.chenjishi.u148.utils.FileUtils;
import com.chenjishi.u148.utils.Utils;

import java.io.File;

/**
 * Created by jishichen on 2017/4/14.
 */
public class SplashActivity extends BaseActivity {
    private static final int REQUEST_PERMISSION = 233;
    private static final long SPLASH_TIME = 3000;

    private long startTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
        setContentView(R.layout.activity_splash, true);
        startTime = System.currentTimeMillis();
        checkPermission();
    }

    private void startClean() {
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
                startHome();
            }
        }, SPLASH_TIME - (System.currentTimeMillis() - startTime));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != REQUEST_PERMISSION) return;

        if (grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startClean();
        } else {
            Utils.showToast(this, R.string.permission_denied);
            finish();
        }
    }

    private void startHome() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            startClean();
        } else {
            String[] permissions = new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE};

            boolean flag = true;
            for (String s : permissions) {
                if (checkSelfPermission(s) != PackageManager.PERMISSION_GRANTED) {
                    flag = false;
                    break;
                }
            }

            if (flag) {
                startClean();
            } else {
                requestPermissions(permissions, REQUEST_PERMISSION);
            }
        }
    }
}
