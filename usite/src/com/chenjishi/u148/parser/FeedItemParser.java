package com.chenjishi.u148.parser;

import com.chenjishi.u148.entity.FeedItem;
import io.vov.vitamio.utils.Log;
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

    public static ArrayList<FeedItem> parseFeedList(String html) {
        return parse(Jsoup.parse(html));
    }

    public static ArrayList<FeedItem> getFeedList(String url) {
        Log.i("test", "url " + url);
        ArrayList<FeedItem> feedItems = null;

        try {
            Document doc = Jsoup.connect(url).get();
            feedItems = parse(doc);
        } catch (IOException e) {
            Log.e(FeedItemParser.class.getName(), "parse error " + e);
        }

        return feedItems;
    }

    private static ArrayList<FeedItem> parse(Document doc) {
        if (null == doc) return null;

        ArrayList<FeedItem> feedItems = new ArrayList<FeedItem>();
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

            feedItems.add(item);
        }

        return feedItems;
    }
}
