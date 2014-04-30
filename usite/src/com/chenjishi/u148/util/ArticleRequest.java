package com.chenjishi.u148.util;

import com.chenjishi.u148.model.Article;
import com.chenjishi.u148.volley.NetworkResponse;
import com.chenjishi.u148.volley.ParseError;
import com.chenjishi.u148.volley.Request;
import com.chenjishi.u148.volley.Response;
import com.chenjishi.u148.volley.toolbox.HttpHeaderParser;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by chenjishi on 14-1-8.
 */
public class ArticleRequest extends Request<Article> {
    private final Response.Listener<Article> mListener;

    public ArticleRequest(int method, String url, Response.Listener<Article> listener,
                          Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
    }

    public ArticleRequest(String url, Response.Listener<Article> listener, Response.ErrorListener errorListener) {
        this(Method.GET, url, listener, errorListener);
    }


    @Override
    protected Response<Article> parseNetworkResponse(NetworkResponse response) {
        String json = new String(response.data);

        try {
            JSONObject jObj = new JSONObject(json);
            JSONObject dataObj = jObj.getJSONObject("data");

            String result = dataObj.getString("content");
            Article article = parseU148Content(result);
            return Response.success(article, HttpHeaderParser.parseCacheHeaders(response));
        } catch (JSONException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(Article response) {
        mListener.onResponse(response);
    }

    private Article parseU148Content(String html) {
        Document doc = Jsoup.parse(html);
        if (null == doc) return null;

        Article article = new Article();

        Elements images = doc.select("img");
        for (Element image : images) {
            article.imageList.add(image.attr("src"));
        }

        Elements videos = doc.select("embed");
        for (Element video : videos) {
            String videoUrl = video.attr("src");
            if (videoUrl.contains("xiami")) {
                video.parent().html("<img src=\"file:///android_asset/audio.png\" title=\"" + videoUrl + "\" />");
            } else {
                video.parent().html("<img src=\"file:///android_asset/video.png\" title=\"" + videoUrl + "\" />");
            }
        }
        article.content = doc.html();
        return article;
    }
}
