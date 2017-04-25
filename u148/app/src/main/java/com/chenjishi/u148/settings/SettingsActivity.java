package com.chenjishi.u148.settings;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.*;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import com.chenjishi.u148.BaseActivity;
import com.chenjishi.u148.Config;
import com.chenjishi.u148.R;
import com.chenjishi.u148.utils.Constants;
import com.chenjishi.u148.utils.Utils;

/**
 * Created by jishichen on 2017/4/14.
 */
public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(R.string.app_settings);
        cacheThread.start();
    }

    private Thread cacheThread = new Thread(new Runnable() {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            String size = Utils.getImageCacheSize(SettingsActivity.this);
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
        String text = getString(R.string.phone_info,
                Utils.getVersionName(this),
                Utils.getDeviceName(),
                Build.VERSION.RELEASE);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"chenjishi313@gmail.com", "webmaster@u148.net"});
        intent.putExtra(Intent.EXTRA_SUBJECT, text);

        startActivity(Intent.createChooser(intent, getString(R.string.email_choose)));
    }

    private class ClearCacheTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog progress = new ProgressDialog(SettingsActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setCancelable(false);
            progress.setMessage(getString(R.string.cache_clean));
            progress.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Utils.clearCache(SettingsActivity.this);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            progress.dismiss();
            Utils.showToast(SettingsActivity.this, R.string.clear_success);
            String cache = String.format(getString(R.string.cache_clear), "0KB");
            ((TextView) findViewById(R.id.tv_cache)).setText(cache);
        }
    }

    private int count;

    public void onButtonClicked(View v) {
        count++;
        if (count == 7) {
            Intent intent = new Intent(this, Build.VERSION.SDK_INT >= 11
                    ? SurpriseActivity.class : FireworksActivity.class);
            startActivity(intent);
            count = 0;
        }
    }

    @Override
    protected void applyTheme() {
        super.applyTheme();
        boolean isNight = Config.getThemeMode(this) == Constants.MODE_NIGHT;
        findViewById(R.id.settings_view).setBackgroundResource(isNight ?
                R.drawable.settings_bkg_night : R.drawable.settings_bkg);
        findViewById(R.id.split_h1).setBackgroundColor(isNight ? 0xFF666666 :
                0xFFC9C9C9);
        ((TextView) findViewById(R.id.tv_cache)).setTextColor(isNight ?
                0xFF999999 : 0xFF333333);
        ((TextView) findViewById(R.id.tv_feedback)).setTextColor(isNight ?
                0xFF999999 : 0xFF333333);
    }
}
