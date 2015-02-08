package com.chenjishi.u148.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.FileCache;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.util.FileUtils;
import com.flurry.android.FlurryAgent;

import java.io.File;

public class LaunchActivity extends Activity {
    private static final long TWO_DAYS = 2 * 24 * 60 * 60 * 1000;

    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        findViewById(android.R.id.content).setBackgroundColor(0xFFF0F0F0);

        context = this;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, "YYHS4STVXPMH6Y9GJ8WD");
        new LoadTask().execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    class LoadTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            //clear cache of 2 days before
            long lastClearCacheTime = PrefsUtil.getClearCacheTime();
            if (System.currentTimeMillis() > lastClearCacheTime) {
                context.deleteDatabase("webview.db");
                context.deleteDatabase("webviewCache.db");
                PrefsUtil.setClearCacheTime(System.currentTimeMillis() + TWO_DAYS);
            }


            //clear temp files, such as shared image or temp upgrade apk
            File tempFile = new File(FileCache.getTempCacheDir());
            if (tempFile.exists()) {
                FileUtils.delete(tempFile);
            }

            String upgradeApkPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/u148.apk";
            File apkFile = new File(upgradeApkPath);
            if (apkFile.exists()) {
                FileUtils.delete(apkFile);
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            Handler mainThread = new Handler(Looper.getMainLooper());

            mainThread.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(context, HomeActivity.class));
                    finish();
                }
            }, 3000);
        }
    }
}
