package com.chenjishi.usite.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.chenjishi.usite.R;
import com.chenjishi.usite.base.BaseActivity;
import com.chenjishi.usite.util.CommonUtil;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-17
 * Time: 下午11:20
 * To change this template use File | Settings | File Templates.
 */
public class AboutActivity extends BaseActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        findViewById(R.id.icon_back).setOnClickListener(this);
        findViewById(R.id.feedback).setOnClickListener(this);
        findViewById(R.id.weibo).setOnClickListener(this);


        initView();

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
        }
    }
}
