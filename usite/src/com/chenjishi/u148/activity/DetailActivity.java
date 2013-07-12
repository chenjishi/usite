package com.chenjishi.u148.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;
import com.chenjishi.u148.R;
import com.chenjishi.u148.entity.Comment;
import com.chenjishi.u148.util.ApiUtils;
import com.chenjishi.u148.util.JavascriptBridge;
import com.chenjishi.u148.util.StringUtil;
import com.chenjishi.u148.util.UIUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

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
    private String mUrl;

    private String mTitle;
    private String mContent;
    private ArrayList<String> mImageUrls = new ArrayList<String>();
    private ArrayList<Comment> commentList = new ArrayList<Comment>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        mUrl = ApiUtils.BASE_URL + bundle.getString("link");
        mTitle = bundle.getString("title");

        findViewById(R.id.ic_comments).setOnClickListener(this);
        findViewById(R.id.ic_share).setOnClickListener(this);

        mWebView = (WebView) findViewById(R.id.webview_content);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        mJsBridge = new JavascriptBridge(this);

        mWebView.addJavascriptInterface(mJsBridge, "U148");
        //for debug javascript only
//        mWebView.setWebChromeClient(new MyWebChromeClient());

        loadData();
    }

    @Override
    public void onClick(View v) {
        if (R.id.ic_share == v.getId()) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(mUrl));
            intent.putExtra(Intent.EXTRA_TEXT, mUrl);
            startActivity(Intent.createChooser(intent, "分享"));
        } else if (R.id.ic_comments == v.getId()) {
            Intent intent = new Intent(this, CommentActivity.class);
            intent.putParcelableArrayListExtra("comments", commentList);
            startActivity(intent);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.detail;
    }

    @Override
    protected void backIconClicked() {
        finish();
    }

    private void loadData() {
        if (StringUtil.isEmpty(mUrl)) return;

        Runnable action = new Runnable() {
            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect(mUrl).get();
                    Elements u148main = doc.getElementsByClass("u148main");
                    if (u148main.size() > 0) {
                        Elements content = u148main.get(0).getElementsByClass("u148content");
                        if (content.size() > 0) {
                            Elements mainContent = content.get(0).getElementsByClass("content");
                            if (mainContent.size() > 0) {
                                Element article = mainContent.get(0);

                                Elements images = article.select("img");
                                for (Element image : images) {
                                    mImageUrls.add(image.attr("src"));
                                }

                                Elements videos = article.select("embed");
                                for (Element video : videos) {
                                    String videoUrl = video.attr("src");
                                    video.parent().html("<img src=\"file:///android_asset/video.png\" title=\"" + videoUrl + "\" />");
                                }

                                mContent = article.html();
                                mJsBridge.setImageUrls(mImageUrls);
                            }

                            Element floors = content.get(0).getElementById("floors");
                            Elements items = floors.select("ul");
                            for (Element e : items) {
                                Comment comment = new Comment();
                                Elements el = e.select("li");
                                if (el.size() > 0) {
                                    Element _el = el.get(0);

                                    Element imgEl = _el.getElementsByClass("uhead").get(0);
                                    comment.avatar = imgEl.attr("src");

                                    Element userEl = _el.getElementsByClass("reply").get(0);
                                    Element userInfo = userEl.getElementsByClass("uinfo").get(0);
                                    comment.userName = userInfo.select("a").get(0).text();
                                    comment.time = userInfo.select("span").get(0).text();

                                    comment.content = userInfo.nextElementSibling().text();
                                }

                                commentList.add(comment);
                            }
                        }
                    }
                } catch (IOException e) {
                }
            }
        };

        Runnable postAction = new Runnable() {
            @Override
            public void run() {
                renderPage();
            }
        };

        UIUtil.runWithoutMessage(action, postAction);
    }

    private void renderPage() {
        String template = readFromAssets(DetailActivity.this, "usite.html");

        if (null != mTitle) {
            template = template.replace("{TITLE}", mTitle);
        }

        if (null != mContent) {
            template = template.replace("{CONTENT}", mContent);
        }

        mWebView.loadDataWithBaseURL(null, template, "text/html", "UTF-8", null);
        mWebView.setVisibility(View.VISIBLE);
        findViewById(R.id.article_bottom).setVisibility(View.VISIBLE);
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
