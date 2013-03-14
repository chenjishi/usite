package com.chenjishi.usite.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;
import com.chenjishi.usite.R;
import com.chenjishi.usite.base.BaseActivity;
import com.chenjishi.usite.entity.Article;
import com.chenjishi.usite.parser.ArticleParser;
import com.chenjishi.usite.util.ApiUtils;
import com.chenjishi.usite.util.JavascriptBridge;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-4
 * Time: 下午7:56
 * To change this template use File | Settings | File Templates.
 */
public class DetailActivity extends BaseActivity implements View.OnClickListener {
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
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        mJsBridge = new JavascriptBridge(this, mWebView);

        mWebView.addJavascriptInterface(mJsBridge, "U148");
        //for debug javascript only
//        mWebView.setWebChromeClient(new MyWebChromeClient());
//        mWebView.loadUrl("file:///android_asset/usite.html");

        new LoadPageTask().execute(url);
    }

    class LoadPageTask extends AsyncTask<String, Void, Article> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(DetailActivity.this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.show();
        }

        @Override
        protected Article doInBackground(String... strings) {
            return ArticleParser.getContent(strings[0]);
        }

        @Override
        protected void onPostExecute(Article article) {
            if (null != article) {
                String template = readFromAssets(DetailActivity.this, "usite.html");

                String title = article.getTitle();
                if (null != title) {
                    template = template.replace("{TITLE}", title);
                }

                String content = article.getContent();
                if (null != content) {
                    template = template.replace("{CONTENT}", content);
                }

                mWebView.loadDataWithBaseURL(null, template, "text/html", "UTF-8", null);
                mJsBridge.setContent(article);
//                mWebView.loadData(template, "text/html", "UTF-8");
//                mJsBridge.setContent(article);
                progressDialog.dismiss();
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

    private String readFromAssets(Context context, String fileName) {
        InputStream is;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            is = context.getAssets().open(fileName);
            byte buf[] = new byte[1024];
            int len;
            while ((len = is.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
            baos.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toString();
    }

    class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Toast.makeText(DetailActivity.this, message, Toast.LENGTH_SHORT).show();
            result.cancel();
            return true;
        }
    }
}
