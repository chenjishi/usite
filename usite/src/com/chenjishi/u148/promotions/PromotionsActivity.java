package com.chenjishi.u148.promotions;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import com.chenjishi.u148.R;
import com.chenjishi.u148.activity.BaseActivity;
import com.chenjishi.u148.model.Feed;
import com.chenjishi.u148.util.Constants;
import com.chenjishi.u148.util.IntentUtils;

/**
 * Created by chenjishi on 15/9/9.
 */
public class PromotionsActivity extends BaseActivity implements View.OnClickListener {
    private Feed mFeed;

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotions);
        setRightButtonIcon(R.drawable.ic_comment);

        Bundle bundle = getIntent().getExtras();

        if (null != bundle) {
            mFeed = bundle.getParcelable(Constants.KEY_FEED);
        } else {
            finish();
        }

        mWebView = (WebView) findViewById(R.id.web_view);
        mWebView.getSettings().setJavaScriptEnabled(true);
        findViewById(R.id.title_bar).setOnClickListener(this);

        String url = "http://app.goudaifu.com" + mFeed.status;
        mWebView.loadUrl(url);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_bar) {
            mWebView.scrollTo(0, 0);
        }
    }

    @Override
    public void onRightButtonClicked(View v) {
        Intent intent = new Intent(this, PromotionsCommentActivity.class);
        intent.putExtra(PromotionsCommentActivity.KEY_TOPIC_ID, mFeed.id);
        IntentUtils.startPreviewActivity(this, intent);
    }
}
