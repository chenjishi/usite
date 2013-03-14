package com.chenjishi.usite.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.WebView;
import com.chenjishi.usite.activity.PictureViewActivity;
import com.chenjishi.usite.activity.VideoPlayerActivity;
import com.chenjishi.usite.entity.Article;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-8
 * Time: 上午11:20
 * To change this template use File | Settings | File Templates.
 */
public class JavascriptBridge {
    private WebView webView;
    private Context context;
    private static Article article;

    public JavascriptBridge(Context context, WebView webView) {
        this.webView = webView;
        this.context = context;
    }

    public void setContent(Article article) {
        this.article = article;
//        String p1 = "{'title':\'" + article.title + "\'}";
//        String p2 = "{'content':\'" + article.content + "\'}";
//        webView.loadUrl("javascript:setArticle(" + p1 + ", " + p2 + ")");
    }

    public void onImageClick(String src) {
        Intent intent = new Intent(context, PictureViewActivity.class);
        intent.putExtra("imgsrc", src);
        context.startActivity(intent);
    }

    public void onVideoClick(String src) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra("videourl", src);
        context.startActivity(intent);
    }

    public static List<String> getImageList() {
        if (null != article) {
            return article.getImgUrls();
        }
        return null;
    }
}
