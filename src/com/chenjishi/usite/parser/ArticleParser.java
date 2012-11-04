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
        Article article = new Article();

        try {
            Document doc = Jsoup.connect(url).get();

            Elements title = doc.getElementsByClass("link2");
            article.title = title.get(0).text();

            Elements content = doc.getElementsByClass("content");

            Elements tags = content.get(0).getAllElements();

//            Elements links = content.get(0).getElementsByTag("p");
//
            StringBuilder sb = new StringBuilder();
//            for (Element link : links) {
//                for (Element img : link.getElementsByTag("img")) {
//                    String imgUrl = img.attr("src");
//                    if (imgUrl.length() > 0) {
//                        sb.append("<img src=" + imgUrl + " />");
//                    }
//                }
//
//                for (Element stg : link.getElementsByTag("strong")) {
//                    String _stg = stg.text();
//                    if (_stg.length() > 0) {
//                        sb.append("<strong>" + _stg + "</strong><br />");
//                    }
//                }
//
//                for (Node node : link.childNodes()) {
//                    if (node instanceof TextNode) {
//                        String s = ((TextNode) node).text();
//                        if (s.length() > 0) {
//                            sb.append(s + "<br /><br />");
//                        }
//                    }
//
//                }
//            }

            for (Element tag : tags) {
//                if (tag.tagName().equalsIgnoreCase("p")) {
//                    Elements brs = tag.getAllElements();
//                    for (Element el : brs) {
//                        Log.i("test", "inside p " + el.tagName() + " inside p " + el.text());
//                    }
//
////                    String s = tag.text().trim();
////                    Log.i("test", "p =====> " + s.length() + " text ---> " + s);
//                }

//                if (tag.tagName().equalsIgnoreCase("img")) {
//                    Log.i("test", "img =====> " + tag.attr("src"));
//                }

                if (tag.tagName().equalsIgnoreCase("p")) {
//                    sb.append("<p>" + tag.html() + "<br /></p>");
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
