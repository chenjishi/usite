package com.chenjishi.u148.parser;

import android.util.Log;
import com.chenjishi.u148.model.Feed;
import com.chenjishi.u148.model.FeedItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import static com.chenjishi.u148.util.Constants.*;

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

            Elements summary = el.select("div.summary");
            if (null != summary && summary.size() > 0) {
                Element sum = summary.get(0);

                item.summary = sum.ownText();
                item.author = sum.select("a").get(0).text();
                item.commentCount = sum.select("a").get(1).text();
            }

            Elements views = el.select("div.viewnum");
            if (null != views && views.size() > 0) {
                item.readCount = views.get(0).text();
            }

            feedItems.add(item);
        }

        return feedItems;
    }

    public static ArrayList<Feed> parseJianDanFeed2(String html) {
        Document doc = Jsoup.parse(html);
        if (null == doc) return null;

        ArrayList<Feed> feedList;

        Element content = doc.getElementById("content");

        if (null == content) return null;

        feedList = new ArrayList<Feed>();

        Elements posts = content.getElementsByClass("post");
        for (Element el : posts) {
            Feed feed = new Feed();

            Element thumb = el.child(0);
            if (null != thumb) {
                Elements links = thumb.getElementsByTag("a");
                if (null != links && links.size() > 0) {
                    Element link = links.get(0);

                    feed.url = link.attr("href");
                    Elements linkImage = link.getElementsByTag("img");
                    if (null != linkImage && linkImage.size() > 0) {
                        feed.thumbUrl = linkImage.get(0).attr("src");
                        feed.title = linkImage.get(0).attr("title");
                    }
                }
            }

            Element time = el.child(1);
            if (null != time) {
                Elements links = time.getElementsByTag("a");
                if (null != links && links.size() > 1) {
                    feed.author = links.get(0).text();
                    feed.category = links.get(1).text();
                    String t = links.get(0).parent().text();
                    int idx = t.indexOf("/");
                    if (idx != -1) {
                        feed.time = t.substring(idx + 1);
                    }
                }
            }

            feedList.add(feed);
        }

        return feedList;
    }

    public static ArrayList<Feed> parseNews(String html) {
        Document doc = Jsoup.parse(html);
        if (null == doc) return null;

        ArrayList<Feed> feedList = null;
        Element content = doc.getElementById("news_list");
        if (null != content) {
            feedList = new ArrayList<Feed>();

            Elements posts = content.getElementsByClass("news_block");
            for (Element el : posts) {
                Feed feed = new Feed();

                Elements title = el.getElementsByClass("news_entry");
                if (null != title && title.size() > 0) {
                    Element titleLink = title.get(0).select("a").first();
                    feed.title = titleLink.text();
                    feed.url = BASE_NEWS_URL + titleLink.attr("href");
                }

                Elements summary = el.getElementsByClass("entry_summary");
                if (null != summary && summary.size() > 0) {
                    Element summaryEl = summary.get(0);
                    feed.description = summaryEl.text();

                    Elements images = summaryEl.select("img");
                    if (null != images && images.size() > 0) {
                        feed.thumbUrl = BASE_NEWS_URL + images.first().attr("src");
                    }
                }
                feedList.add(feed);
            }
        }

        return feedList;
    }

    public static ArrayList<Feed> parseJianDanFeed(String html) {
        Document doc = Jsoup.parse(html);
        if (null == doc) return null;
        ArrayList<Feed> feedList = null;
        Element content = doc.getElementById("content");

        if (null != content) {
            feedList = new ArrayList<Feed>();

            Elements posts = content.getElementsByClass("post");
            for (Element el : posts) {
                Feed feed = new Feed();
                feed.source = 101;

                Element thumbEl = el.child(0);
                if (null != thumbEl) {
                    Element thumbLink = thumbEl.select("a").first();
                    if (null != thumbLink) {
                        feed.thumbUrl = thumbLink.select("img").first().attr("src");
                    }
                }

                Element indexEl = el.child(1);
                if (null != indexEl) {
                    feed.description = indexEl.ownText();

                    Elements links = indexEl.getElementsByTag("a");
                    if (links.size() > 3) {
                        feed.title = links.get(1).text();
                        feed.url = links.get(1).attr("href");
                        feed.author = links.get(2).text();
                        feed.category = links.get(3).text();

                        String t = links.get(2).parent().text();
                        int idx = t.indexOf(",");
                        if (idx != -1) {
                            feed.time = t.substring(idx + 1);
                        }
                    }
                }
                feedList.add(feed);
            }
        }

        return feedList;
    }
}
