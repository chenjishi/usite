package com.chenjishi.u148.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.chenjishi.u148.R;
import com.chenjishi.u148.util.CommonUtil;
import com.chenjishi.u148.util.FileUtils;
import com.chenjishi.u148.view.FireworksView;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-17
 * Time: 下午11:20
 * To change this template use File | Settings | File Templates.
 */
public class AboutActivity extends BaseActivity implements View.OnClickListener {
    private Context mContext;
    private MediaPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        setTitleText(R.string.about);
        mContext = this;

        findViewById(R.id.feedback).setOnClickListener(this);
        findViewById(R.id.weibo).setOnClickListener(this);
        findViewById(R.id.btn_clear).setOnClickListener(this);

        cacheThread.start();
        initView();
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
                ((Button) findViewById(R.id.btn_clear)).setText(cache);
            }
        }
    };

    private void initView() {
        String versionName = CommonUtil.getVersionName(this);
        if (null != versionName) {
            Button versionText = (Button) findViewById(R.id.version_text);
            versionText.setText(versionName);
        }
    }

    private void toWeibo() {
        Uri uri = Uri.parse("http://weibo.com/u/1958533587");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private void sendFeedback() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.developer)});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback));

        startActivity(Intent.createChooser(intent, getString(R.string.email_choose)));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.feedback:
                sendFeedback();
                break;
            case R.id.weibo:
                toWeibo();
                break;
            case R.id.btn_clear:
                new ClearCacheTask().execute();
                break;
        }
    }

    private int count;
    public void easterEgg(View v) {
        count++;
        if (count == 3) {
            Toast.makeText(this, "再点击两次有惊喜哦~", Toast.LENGTH_SHORT).show();
            return;
        }

        if (count == 5) {
            FrameLayout rootView = (FrameLayout) findViewById(android.R.id.content);
            final FireworksView fireworksView = new FireworksView(this);
            rootView.addView(fireworksView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            count = 0;
            mPlayer = MediaPlayer.create(this, R.raw.fireworks);
            mPlayer.setLooping(true);
            mPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mPlayer) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    private class ClearCacheTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog progress = new ProgressDialog(AboutActivity.this);

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
            mContext.deleteDatabase("webview.db");
            mContext.deleteDatabase("webviewCache.db");
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            progress.dismiss();
            CommonUtil.showToast("清除缓存成功!");
            String cache = String.format(mContext.getString(R.string.cache_clear), "0KB");
            ((Button) findViewById(R.id.btn_clear)).setText(cache);
        }
    }
}
