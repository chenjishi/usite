package com.chenjishi.usite.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;
import com.chenjishi.usite.R;
import com.chenjishi.usite.entity.Article;
import com.chenjishi.usite.parser.ArticleParser;
import com.chenjishi.usite.util.ApiUtils;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-4
 * Time: 下午7:56
 * To change this template use File | Settings | File Templates.
 */
public class DetailActivity extends Activity implements View.OnClickListener {
    private WebView mWebView;
    private JSInterface mJsInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);


        String url = ApiUtils.BASE_URL + getIntent().getExtras().getString("link");
        
        findViewById(R.id.icon_back).setOnClickListener(this);

        mWebView = (WebView) findViewById(R.id.content_webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mJsInterface = new JSInterface();
        mWebView.addJavascriptInterface(mJsInterface, "jsobj");
        mWebView.loadUrl("file:///android_asset/test.html");
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
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
                mJsInterface.setContent(article);
            }
        }
    }

    class JSInterface {
        public void setContent(Article article) {
            String p1 = "{'title':\'" + article.title + "\'}";
            String p2 = "{'content':\'" + article.content + "\'}";
            mWebView.loadUrl("javascript:setArticle(" + p1 + ", " + p2 + ")");
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
