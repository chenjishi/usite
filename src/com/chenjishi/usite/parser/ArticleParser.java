package com.chenjishi.usite.parser;

import com.chenjishi.usite.entity.Article;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-4
 * Time: 下午8:48
 * To change this template use File | Settings | File Templates.
 */
public class ArticleParser {
    public static Article getContent(String url) {
        Article article;

        try {
            Document doc = Jsoup.connect(url).get();

            article = new Article();

            Elements title = doc.getElementsByClass("link2");
            String articleTitle = title.get(0).text();
            article.setTitle(articleTitle);

            Elements content = doc.getElementsByClass("content");
            String s = "";
            List<String> imgUrls = new ArrayList<String>();
            if (content.size() > 0) {
                Element c = content.get(0);

                Elements imgs = c.select("img");
                for (Element e : imgs) {
                    imgUrls.add(e.attr("src"));
                }

                Elements videos = c.select("embed");
                for (Element e : videos) {
                    String videoUrl = e.attr("src");

                    e.parent().html("<img src=\"file:///android_asset/video.png\" title=\"" + videoUrl + "\" />");
                }
                s = c.html();
            }

            article.setContent(s);
            article.setImgUrls(imgUrls);
        } catch (IOException e) {
            return null;
        }

        return article;
    }

}
