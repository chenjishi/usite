package com.chenjishi.usite.parser;

import com.chenjishi.usite.entity.Article;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-4
 * Time: 下午8:48
 * To change this template use File | Settings | File Templates.
 */
public class ArticleParser {
    public static Article getContent(String url) {
        Article article = null;

        try {
            Document doc = Jsoup.connect(url).get();

            article = new Article();

            Elements title = doc.getElementsByClass("link2");
            article.title = title.get(0).text();

            Elements content = doc.getElementsByClass("content");

            Elements tags = content.get(0).getAllElements();
            StringBuilder sb = new StringBuilder();

            for (Element tag : tags) {
                if (tag.tagName().equalsIgnoreCase("p")) {
                    sb.append(tag.html() + "<br />");
                }
            }
            article.content = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return article;
    }

}
