package com.chenjishi.u148.model;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-5-4
 * Time: 下午1:01
 * To change this template use File | Settings | File Templates.
 */
public class Comment {
    public CommentData data;
    public int code;
    public String msg;

    public static class CommentData {
        public int total;

        public ArrayList<CommentItem> data;
    }
}
