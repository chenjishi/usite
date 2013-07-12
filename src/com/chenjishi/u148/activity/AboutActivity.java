package com.chenjishi.u148.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import com.chenjishi.u148.R;
import com.chenjishi.u148.util.CommonUtil;
import com.chenjishi.u148.util.FileUtils;
import com.chenjishi.u148.util.UIUtil;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-17
 * Time: 下午11:20
 * To change this template use File | Settings | File Templates.
 */
public class AboutActivity extends BaseActivity implements View.OnClickListener {
    private TextView mCacheText;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        findViewById(R.id.feedback).setOnClickListener(this);
        findViewById(R.id.weibo).setOnClickListener(this);
        findViewById(R.id.btn_clear).setOnClickListener(this);

        mCacheText = (TextView) findViewById(R.id.tag_cache);
        mCacheText.setText("计算中...");
        cacheThread.start();
        initView();
    }

    private Thread cacheThread = new Thread(new Runnable() {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            String size = FileUtils.getCurrentCacheSize();
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
                mCacheText.setText("当前缓存大小为：" + msg.obj);
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.about;
    }

    @Override
    protected void backIconClicked() {
        finish();
    }

    private void initView() {
        String versionName = CommonUtil.getVersionName(this);
        if (null != versionName) {
            TextView versionText = (TextView) findViewById(R.id.version_text);
            versionText.setText(String.format(getString(R.string.app_version), versionName));
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
            case R.id.icon_back:
                finish();
                break;
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
            UIUtil.showMsg(AboutActivity.this, "清除缓存成功!");
            mCacheText.setText("0KB");
        }
    }
}
