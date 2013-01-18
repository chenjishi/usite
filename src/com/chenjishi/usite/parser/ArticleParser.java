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
        Article article = null;

        try {
            Document doc = Jsoup.connect(url).get();

            article = new Article();

            Elements title = doc.getElementsByClass("link2");
            String articleTitle = title.get(0).text();
            if (null != article) {
                article.setTitle(articleTitle);
            }

            Elements content = doc.getElementsByClass("content");

            Elements tags = content.get(0).getAllElements();
            StringBuilder sb = new StringBuilder();

            List<String> imgUrls = new ArrayList<String>();
            for (Element tag : tags) {
                if (tag.tagName().equalsIgnoreCase("p")) {
                    sb.append(tag.html() + "<br />");
                }

                if (tag.tagName().equalsIgnoreCase("img")) {
                    imgUrls.add(tag.attr("src"));
                }
            }
            article.setContent(sb.toString());
            article.setImgUrls(imgUrls);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return article;
    }

}
