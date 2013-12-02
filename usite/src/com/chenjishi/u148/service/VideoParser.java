package com.chenjishi.u148.service;

import com.chenjishi.u148.entity.Video;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-11-29
 * Time: 下午3:23
 * To change this template use File | Settings | File Templates.
 */
public class VideoParser {

    public static ArrayList<Video> parseVideoList(String html) {
        ArrayList<Video> videoList;
        Pattern p = Pattern.compile("id_(\\w+).html");

        Document doc = Jsoup.parse(html);
        Elements elements = doc.getElementsByClass("v");
        if (null == elements || elements.size() == 0) return null;

        videoList = new ArrayList<Video>();
        for (Element element : elements) {
            Video video = new Video();
            Elements thumb = element.getElementsByClass("v-thumb");
            if (null != thumb && thumb.size() > 0) {
                video.thumbUrl = thumb.get(0).select("img").get(0).attr("src");
            }

            Elements link = element.getElementsByClass("v-link");
            if (null != link && link.size() > 0) {
                Element vLink = link.get(0).select("a").get(0);
                String href = vLink.attr("href");
                Matcher m = p.matcher(href);
                while (m.find()) {
                    video.id = m.group(1);
                }
                video.title = vLink.attr("title");
            }

            videoList.add(video);
        }
        return videoList;
    }
}
