package com.chenjishi.u148.util;

import android.app.ActivityManager;
import android.content.Context;
import com.chenjishi.u148.volley.RequestQueue;
import com.chenjishi.u148.volley.Response;
import com.chenjishi.u148.volley.toolbox.*;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-11-4
 * Time: 下午10:02
 * To change this template use File | Settings | File Templates.
 */
public class HttpUtils {

    private static RequestQueue mRequestQueue;
    private static ImageLoader mImageLoader;

    public static void init(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);

        int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                .getMemoryClass();
        int cacheSize = 1024 * 1024 * memClass / 8;
        mImageLoader = new ImageLoader(mRequestQueue, new BitmapLruCache((cacheSize)));
    }

    private static RequestQueue getRequestQueue() {
        if (null != mRequestQueue) {
            return mRequestQueue;
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }

    public static ImageLoader getImageLoader() {
        if (null != mImageLoader) {
            return mImageLoader;
        } else {
            throw new IllegalStateException("ImageLoader not initialized");
        }
    }

    public static void get(String url,
                           Response.Listener<String> listener,
                           Response.ErrorListener errorListener) {
        RequestQueue queue = getRequestQueue();
        queue.add(new StringRequest(url, listener, errorListener));
    }

    public static String getSync(String url) {

        RequestFuture<String> f = RequestFuture.newFuture();
        StringRequest req = new StringRequest(url, f, f);
        RequestQueue queue = getRequestQueue();
        queue.add(req);

        try {
            String response = f.get();
            return response;
        } catch (Exception e) {
            return "";
        }
    }
}
