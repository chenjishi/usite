package com.chenjishi.u148.model;

import java.util.List;

/**
 * Created by chenjishi on 14-8-28.
 */
public class FeedDoc {

    public FeedData data;

    public static class FeedData {

        public int pageNo;

        public int pageSize;

        public List<Feed> data;
    }
}
