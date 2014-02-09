package com.chenjishi.u148.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.FileCache;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.util.FileUtils;

import java.io.File;

public class LaunchActivity extends Activity {
    private static final long TWO_DAYS = 2 * 24 * 60 * 60 * 1000;

    private static final long TWO_HOURS = 2 * 60 * 60 * 1000;
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
        new LoadTask().execute();
    }

    private class LoadTask extends AsyncTask<Void, Void, Boolean> {

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
