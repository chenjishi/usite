package com.chenjishi.usite.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import com.chenjishi.usite.R;
import com.chenjishi.usite.entity.Article;
import com.chenjishi.usite.parser.ArticleParser;
import com.chenjishi.usite.util.ApiUtils;
import com.chenjishi.usite.util.JavascriptBridge;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-4
 * Time: 下午7:56
 * To change this template use File | Settings | File Templates.
 */
public class DetailActivity extends Activity implements View.OnClickListener {
    private WebView mWebView;
    private JavascriptBridge mJsBridge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);


        String url = ApiUtils.BASE_URL + getIntent().getExtras().getString("link");
        
        findViewById(R.id.icon_back).setOnClickListener(this);

        mWebView = (WebView) findViewById(R.id.content_webview);
        mWebView.getSettings().setJavaScriptEnabled(true);

        mJsBridge = new JavascriptBridge(this, mWebView);

        mWebView.addJavascriptInterface(mJsBridge, "U148");
        mWebView.loadUrl("file:///android_asset/usite.html");

        new LoadPageTask().execute(url);
    }

    class LoadPageTask extends AsyncTask<String, Void, Article> {
        @Override
        protected Article doInBackground(String... strings) {
            return ArticleParser.getContent(strings[0]);
        }

        @Override
        protected void onPostExecute(Article article) {
            if (null != article) {
                mJsBridge.setContent(article);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.icon_back:
                finish();
                break;
        }
    }
}
