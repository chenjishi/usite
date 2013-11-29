package com.chenjishi.u148.service;

import com.chenjishi.u148.entity.Video;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-11-25
 * Time: 下午5:23
 * To change this template use File | Settings | File Templates.
 */
public class YouKuParser {

    public static ArrayList<Video> parseVideoList() {
        ArrayList<Video> videoList = new ArrayList<Video>();
        String s = "http://www.youku.com/v_showlist/c94.html";
        Pattern p = Pattern.compile("id_(\\w+).html");

        try {
            Document document = Jsoup.connect(s).get();

            Element list = document.getElementById("list");
            if (null != list) {
                Elements items = list.getElementsByTag("ul");
//                for (Element item : items) {
                for (int i = 0; i < 10; i++) {
                    Element item = items.get(i);
                    Video video = new Video();
                    Elements links = item.getElementsByClass("v_link");
                    if (null != links && links.size() > 0) {
                        Element link = links.get(0).select("a").first();
                        String href = link.attr("href");
                        Matcher m = p.matcher(href);
                        while (m.find()) {
                            String id = m.group(1);
                            video.id = id;
                        }
                        String title = link.attr("title");
                        video.title = title;
                    }

                    Elements thumbs = item.getElementsByClass("v_thumb");
                    if (null != thumbs && thumbs.size() > 0) {
                        Element thumb = thumbs.get(0).select("img").first();
                        String thumbUrl = thumb.attr("src");
                        video.thumbUrl = thumbUrl;
                    }

                    Elements times = item.getElementsByClass("v_time");
                    if (null != times && times.size() > 0) {
                        Element time = times.get(0).select("span").first();
                        String duration = time.text();
                        video.duration = duration;
                    }

                    videoList.add(video);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return videoList;
    }
}
