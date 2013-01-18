package com.chenjishi.usite.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-4
 * Time: 下午8:21
 * To change this template use File | Settings | File Templates.
 */
public class Article {
    private String content;
    private String title;
    private List<String> imgUrls = new ArrayList<String>();

    public Article() {};

    public Article(String title, String content) {
        this.content = content;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public List<String> getImgUrls() {
        return imgUrls;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setImgUrls(List<String> urlList) {
        this.imgUrls = urlList;
    }

}
