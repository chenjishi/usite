package com.chenjishi.usite.entity;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-4
 * Time: 下午8:21
 * To change this template use File | Settings | File Templates.
 */
public class Article {
    public String content;

    public String title;

    public Article() {};

    public Article(String title, String content) {
        this.content = content;

        this.title = title;

    }
}
