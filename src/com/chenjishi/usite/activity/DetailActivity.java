package com.chenjishi.usite.activity;

import android.app.Activity;
import android.os.Bundle;
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
public class DetailActivity extends Activity {
    private WebView mWebView;

    private JSInterface mJsInterface;

    private String mUrl;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        mWebView = (WebView) findViewById(R.id.content_webview);

        mUrl = ApiUtils.BASE_URL + getIntent().getExtras().getString("link");

        mWebView.getSettings().setJavaScriptEnabled(true);

        mJsInterface = new JSInterface();

        mWebView.addJavascriptInterface(mJsInterface, "jsobj");
        mWebView.loadUrl("file:///android_asset/test.html");

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();;
                return true;
            }
        });

        new Thread() {
            @Override
            public void run() {
                Article result = ArticleParser.getContent(mUrl);
                mJsInterface.setContent(result);
            }
        }.start();



    }

    class JSInterface {
        public void setContent(Article article) {
            String p1 = "{'title':\'" + article.title + "\'}";
            String p2 = "{'content':\'" + article.content + "\'}";
            mWebView.loadUrl("javascript:setArticle(" + p1 + ", " + p2 + ")");

        }

    }
}
