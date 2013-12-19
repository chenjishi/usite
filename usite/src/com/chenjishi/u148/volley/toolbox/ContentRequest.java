package com.chenjishi.u148.volley.toolbox;

import android.util.Log;
import com.chenjishi.u148.model.Article;
import com.chenjishi.u148.volley.NetworkResponse;
import com.chenjishi.u148.volley.Request;
import com.chenjishi.u148.volley.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import static com.chenjishi.u148.util.Constants.SOURCE_JIANDAN;
import static com.chenjishi.u148.util.Constants.SOURCE_NEWS;
import static com.chenjishi.u148.util.Constants.SOURCE_U148;

/**
 * Created by chenjishi on 13-12-7.
 */
public class ContentRequest extends Request<Article> {
    private final int mSource;
    private final String mUrl;
    private final Response.Listener<Article> mListener;

    public ContentRequest(int method, int source, String url,
                          Response.Listener<Article> listener,
                          Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
        mSource = source;
        mUrl = url;
    }

    public ContentRequest(int source, String url, Response.Listener<Article> listener, Response.ErrorListener errorListener) {
        this(Method.GET, source, url, listener, errorListener);
    }

    @Override
    protected Response<Article> parseNetworkResponse(NetworkResponse response) {
        String html = new String(response.data);
        Article article = null;
        if (mSource == SOURCE_U148) {
            article = parseU148Content(html);
        }

        if (mSource == SOURCE_JIANDAN) {
            article = parseJianDan(html);
        }

        if (mSource == SOURCE_NEWS) {
            article = parseNews(html);
        }

        return Response.success(article, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(Article response) {
        mListener.onResponse(response);
    }

    private Article parseNews(String html) {
        Document doc = Jsoup.parse(html);
        if (null == doc) return null;

        Article article = new Article();
        article.url = mUrl;
        article.source = mSource;

        Element content = doc.getElementById("news_body");
        if (null != content) {
            Elements images = content.select("img");
            for (Element image : images) {
                article.imageList.add(image.attr("src"));
            }

            Elements videos = content.select("embed");
            for (Element video : videos) {
                String videoUrl = video.attr("src");
                video.parent().html("<img src=\"file:///android_asset/video.png\" title=\"" + videoUrl + "\" />");
            }
            article.content = content.html();
        }

        return article;
    }

    private Article parseJianDan(String html) {
        Document doc = Jsoup.parse(html);
        if (null == doc) return null;

        Article article = new Article();
        article.url = mUrl;
        article.source = mSource;

        Element content = doc.getElementById("content");
        if (null != content) {
            Elements elements = content.getElementsByClass("post");
            if (null != elements && elements.size() > 0) {
                Element post = elements.get(0);

                Elements images = post.select("img");
                for (Element image : images) {
                    article.imageList.add(image.attr("src"));
                }

                Elements videos = post.select("embed");
                for (Element video : videos) {
                    String videoUrl = video.attr("src");
                    video.parent().html("<img src=\"file:///android_asset/video.png\" title=\"" + videoUrl + "\" />");
                }

                Elements paragraphs = post.getElementsByTag("p");
                StringBuilder sb = new StringBuilder();
                for (Element p : paragraphs) {
                    sb.append(p.outerHtml());
                }
                article.content = sb.toString();
            }
        }

        return article;
    }

    private Article parseU148Content(String html) {
        Document doc = Jsoup.parse(html);
        if (null == doc) return null;

        Article article = new Article();
        article.url = mUrl;
        article.source = mSource;

        Elements content = doc.getElementsByClass("content");
        if (null != content && content.size() > 0) {
            Element element = content.get(0);

            Elements images = element.select("img");
            for (Element image : images) {
                article.imageList.add(image.attr("src"));
            }

            Elements videos = element.select("embed");
            for (Element video : videos) {
                String videoUrl = video.attr("src");
                if (videoUrl.contains("xiami")) {
                    video.parent().html("<img src=\"file:///android_asset/audio.png\" title=\"" + videoUrl + "\" />");
                } else {
                    video.parent().html("<img src=\"file:///android_asset/video.png\" title=\"" + videoUrl + "\" />");
                }
            }
            article.content = element.html();
        }

        Element comment = doc.getElementById("floors");
        article.comment = comment.html();

        return article;
    }
}
