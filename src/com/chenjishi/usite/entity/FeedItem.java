package com.chenjishi.usite.entity;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-3
 * Time: 下午6:26
 * To change this template use File | Settings | File Templates.
 */
public class FeedItem implements Serializable {
    private static final long serialVersionUID = 2L;

    public String imageUrl;
    public String title;
    public String summary;
    public String time;
    public String author;
    public String category;
    public int level;
    public String link;
    public String comment;

    public boolean isRead;
}
