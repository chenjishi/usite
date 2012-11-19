package com.chenjishi.usite.service;

import android.support.v4.util.LruCache;
import com.chenjishi.usite.entity.FeedItem;
import com.chenjishi.usite.parser.FeedItemParser;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-18
 * Time: 下午5:09
 * To change this template use File | Settings | File Templates.
 */
public class FeedItemDataService {
    private LruCache<String, ArrayList<FeedItem>> caches = new LruCache<String, ArrayList<FeedItem>>(10);

    public ArrayList<FeedItem> getFeedItemList(String url) {
        if (null != caches.get(url)) {
            return caches.get(url);
        } else {
            ArrayList<FeedItem> newItems = FeedItemParser.getMainList(url);
            if (null != newItems) {
                caches.put(url, newItems);
                return newItems;
            }

        }
        return null;
    }

    public void clearCaches() {
        caches.evictAll();
    }

    public void addToCaches(String url, ArrayList<FeedItem> items) {
        caches.put(url, items);
    }
}
