package com.chenjishi.u148.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.FileCache;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.service.DownloadService;
import com.chenjishi.u148.util.CommonUtil;
import com.chenjishi.u148.util.Constants;
import com.chenjishi.u148.util.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class LaunchActivity extends Activity {
    private static final long TWO_DAYS = 2 * 24 * 60 * 60 * 1000;
    private static final long TWO_HOURS = 2 * 60 * 60 * 1000;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        findViewById(android.R.id.content).setBackgroundColor(0xFFE3E3E3);

        context = this;
    }

    @Override
    protected void onStart() {
        super.onStart();
        new LoadTask().execute();
        Intent intent = new Intent(this, DownloadService.class);
        startService(intent);
    }

    private class LoadTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            //clear cache of 2 days before
            long lastClearCacheTime = PrefsUtil.getClearCacheTime();
            if (System.currentTimeMillis() > lastClearCacheTime) {
                FileUtils.clearCache();
                context.deleteDatabase("webview.db");
                context.deleteDatabase("webviewCache.db");
                PrefsUtil.setClearCacheTime(System.currentTimeMillis() + TWO_DAYS);
            }

            long lastUpdateCacheTime = PrefsUtil.getCacheUpdateTime();
            if (CommonUtil.didNetworkConnected(context) && System.currentTimeMillis() > lastUpdateCacheTime) {
                try {
                    Document doc = Jsoup.connect(Constants.BASE_URL + "/list/1.html").get();
                    Elements content = doc.getElementsByClass("u148content");
                    if (null != content && content.size() > 0) {
                        String data = content.get(0).html();
                        String path = FileCache.getDataCacheDirectory(context) + Constants.CACHED_FILE_NAME;
                        FileUtils.writeToFile(path, data);
                        PrefsUtil.setCacheUpdateTime(System.currentTimeMillis() + TWO_HOURS);
                    }
                } catch (IOException e) {
                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            findViewById(R.id.loading_layout).setVisibility(View.GONE);
            startActivity(new Intent(context, HomeActivity.class));
            finish();
        }
    }
}
