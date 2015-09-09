package com.chenjishi.u148.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.*;
import android.text.TextUtils;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.FileCache;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.util.FileUtils;
import com.chenjishi.u148.util.HttpUtils;
import com.chenjishi.u148.volley.Response;
import com.chenjishi.u148.volley.VolleyError;
import com.flurry.android.FlurryAgent;

import java.io.File;

public class LaunchActivity extends Activity implements Response.Listener<String>, Response.ErrorListener {
    private static final long TWO_DAYS = 2 * 24 * 60 * 60 * 1000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        HttpUtils.get("http://app.goudaifu.com/funclub/v1/funclubget", this, this);
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

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            PrefsUtil.saveAdsJson(response);
        }
    }

    class LoadTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            //clear cache of 2 days before
            long lastClearCacheTime = PrefsUtil.getClearCacheTime();
            if (System.currentTimeMillis() > lastClearCacheTime) {
                LaunchActivity.this.deleteDatabase("webview.db");
                LaunchActivity.this.deleteDatabase("webviewCache.db");
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
                    startActivity(new Intent(LaunchActivity.this, HomeActivity.class));
                    finish();
                }
            }, 3000);
        }
    }
}
