package com.chenjishi.u148.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.FileCache;
import com.chenjishi.u148.entity.FeedItem;
import com.chenjishi.u148.parser.FeedItemParser;
import com.chenjishi.u148.util.ApiUtils;
import com.chenjishi.u148.util.CommonUtil;
import com.chenjishi.u148.util.FileUtils;
import com.chenjishi.u148.util.UsiteConfig;

import java.io.File;
import java.util.ArrayList;

public class LaunchActivity extends Activity {
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        context = this;

        new LoadFirstPageTask().execute(ApiUtils.BASE_URL + "/list/1.html");
    }

    class LoadFirstPageTask extends AsyncTask<String, Void, Boolean> {
        String filePath;

        @Override
        protected Boolean doInBackground(String... strings) {
            if (!CommonUtil.didNetworkConnected(context)) {
                return false;
            }

            String oldVersionCachePath = FileCache.getDataCacheDirectory(LaunchActivity.this) + "cache.obj";
            if (new File(oldVersionCachePath).exists()) {
                FileUtils.deleteFile(oldVersionCachePath);
            }

            filePath = FileCache.getDataCacheDirectory(LaunchActivity.this) + ApiUtils.CACHED_FILE_NAME;
            File cacheFile = new File(filePath);
            if (!cacheFile.exists()) {
                ArrayList<FeedItem> feedItems = FeedItemParser.getMainList(strings[0]);
                if (null != feedItems && feedItems.size() > 0) {
                    FileUtils.serializeObject(filePath, feedItems);
                    UsiteConfig.saveUpdateTime(LaunchActivity.this, System.currentTimeMillis());
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            findViewById(R.id.loading_layout).setVisibility(View.GONE);
            if (aBoolean) {
                Intent intent = new Intent(context, HomeActivity.class);
                intent.putExtra("file_path", filePath);
                startActivity(intent);
                finish();
            } else {
                CommonUtil.showToast(context, getString(R.string.net_error));
            }
        }
    }
}
