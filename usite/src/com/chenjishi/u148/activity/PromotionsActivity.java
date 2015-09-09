package com.chenjishi.u148.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import com.chenjishi.u148.R;
import com.chenjishi.u148.util.IntentUtils;

/**
 * Created by chenjishi on 15/9/9.
 */
public class PromotionsActivity extends BaseActivity {
    public static final String KEY_URL = "url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotions);
        setRightButtonIcon(R.drawable.ic_comment);

        String url = getIntent().getExtras().getString(KEY_URL);
        WebView webView = (WebView) findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.loadUrl(url);
    }

    @Override
    public void onRightButtonClicked(View v) {
        Intent intent = new Intent(this, PromotionsCommentActivity.class);
        intent.putExtra("article_id", "-1");
        IntentUtils.startPreviewActivity(this, intent);
    }
}
