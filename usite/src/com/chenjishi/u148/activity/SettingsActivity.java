package com.chenjishi.u148.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.util.CommonUtil;
import com.chenjishi.u148.util.Constants;
import com.chenjishi.u148.util.FileUtils;

/**
 * Created by chenjishi on 14-1-11.
 */
public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(R.string.app_settings);

        cacheThread.start();
    }

    private Thread cacheThread = new Thread(new Runnable() {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            String size = FileUtils.getImageCacheSize();
            Message msg = Message.obtain();
            msg.obj = size;
            msg.what = 1;
            handler.sendMessage(msg);
        }
    });

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1 && !isFinishing()) {
                String cache = String.format(getString(R.string.cache_clear), msg.obj);
                ((TextView) findViewById(R.id.tv_cache)).setText(cache);
            }
        }
    };

    public void onCacheClicked(View v) {
        new ClearCacheTask().execute();
    }

    public void onFeedbackClicked(View v) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"chenjishi313@gmail.com", "webmaster@u148.net"});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback2));

        startActivity(Intent.createChooser(intent, getString(R.string.email_choose)));
    }

    private class ClearCacheTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog progress = new ProgressDialog(SettingsActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setCancelable(false);
            progress.setMessage("正在清除...");
            progress.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            FileUtils.clearCache();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            progress.dismiss();
            CommonUtil.showToast("清除缓存成功!");
            String cache = String.format(getString(R.string.cache_clear), "0KB");
            ((TextView) findViewById(R.id.tv_cache)).setText(cache);
        }
    }

    @Override
    protected void applyTheme() {
        super.applyTheme();
        final LinearLayout settingsView = (LinearLayout) findViewById(R.id.settings_view);
        final TextView cacheText = (TextView) findViewById(R.id.tv_cache);
        final TextView feedText = (TextView) findViewById(R.id.tv_feedback);
        final View split = findViewById(R.id.split_h1);

        if (Constants.MODE_NIGHT == PrefsUtil.getThemeMode()) {
            settingsView.setBackgroundResource(R.drawable.settings_bkg_night);
            cacheText.setTextColor(getResources().getColor(R.color.text_color_weak));
            feedText.setTextColor(getResources().getColor(R.color.text_color_weak));
            split.setBackgroundColor(0xFF666666);
        } else {
            settingsView.setBackgroundResource(R.drawable.settings_bkg);
            cacheText.setTextColor(getResources().getColor(R.color.text_color_regular));
            feedText.setTextColor(getResources().getColor(R.color.text_color_regular));
            split.setBackgroundColor(0xFFC9C9C9);
        }
    }
}
