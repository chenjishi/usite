package com.chenjishi.u148.parser;

import com.chenjishi.u148.entity.FeedItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-3
 * Time: 下午7:02
 * To change this template use File | Settings | File Templates.
 */
public class FeedItemParser {
    //http://www.u148.net/article/66142.html

    public static ArrayList<FeedItem> getMainList(String url) {
        ArrayList<FeedItem> mainLists = new ArrayList<FeedItem>();

        try {
            Document doc = Jsoup.connect(url).get();

            Elements content = doc.getElementsByClass("mainlist");
            for (Element el : content) {
                FeedItem item = new FeedItem();

                item.imageUrl = el.getElementsByTag("img").get(0).attr("src");

                Elements titles = el.getElementsByTag("h1");
                Elements links = titles.get(0).getElementsByTag("a");

                item.category = links.get(0).text();
                item.title = links.get(1).text();
                item.link = links.get(1).attr("href");

                item.time = el.getElementsByTag("span").get(0).ownText();

                Element _el = el.getElementsByClass("summary").get(0);

                Element _link = _el.getElementsByTag("a").get(0);
                item.author = _link.ownText();
                item.summary = _el.ownText();

                mainLists.add(item);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mainLists;
    }

}
