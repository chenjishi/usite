package com.chenjishi.usite.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.chenjishi.usite.R;
import com.chenjishi.usite.base.BaseActivity;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-23
 * Time: 下午10:59
 * To change this template use File | Settings | File Templates.
 */
public class LoginActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        WebView webView = (WebView) findViewById(R.id.login_webview);
        webView.getSettings().setJavaScriptEnabled(true);


        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i("test", "shouldOverrideUrlLoading " + url);
                return super.shouldOverrideUrlLoading(view, url);    //To change body of overridden methods use File | Settings | File Templates.
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i("test", "onPageFinished " + url);

                super.onPageFinished(view, url);    //To change body of overridden methods use File | Settings | File Templates.
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.i("test", "onPageStarted " + url);

                super.onPageStarted(view, url, favicon);    //To change body of overridden methods use File | Settings | File Templates.
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                Log.i("test", "onLoadResource " + url);

                super.onLoadResource(view, url);    //To change body of overridden methods use File | Settings | File Templates.
            }
        });

        webView.loadUrl("http://www.zhihu.com/");
    }
}
