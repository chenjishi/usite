package com.chenjishi.u148.util;

import android.app.ActivityManager;
import android.content.Context;
import com.chenjishi.u148.model.Article;
import com.chenjishi.u148.volley.*;
import com.chenjishi.u148.volley.toolbox.*;

import java.util.Map;

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

    public static RequestQueue getRequestQueue() {
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

    public static void ArticleRequest(String url,
                                      Response.Listener<Article> listener,
                                      Response.ErrorListener errorListener) {
        RequestQueue queue = getRequestQueue();
        ArticleRequest request = new ArticleRequest(url, listener, errorListener);
        request.setShouldCache(true);
        queue.add(new ArticleRequest(url, listener, errorListener));
    }

    public static void get(String url,
                           Response.Listener<String> listener,
                           Response.ErrorListener errorListener) {
        RequestQueue queue = getRequestQueue();
        queue.add(new StringRequest(url, listener, errorListener));
    }

    public static void post(String url,
                            final Map<String, String> params,
                            Response.Listener<String> listener,
                            Response.ErrorListener errorListener) {
        RequestQueue queue = getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.POST, url, listener, errorListener) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };
        /**
         * prevent retry by set retry number to 0
         */
        request.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1));
        queue.add(request);
    }

    public static <T> void get(String url,
                               Class<T> clazz,
                               Response.Listener<T> listener,
                               Response.ErrorListener errorListener) {
        RequestQueue queue = getRequestQueue();
        GsonRequest<T> request = new GsonRequest<T>(url, clazz, listener, errorListener);
        queue.add(request);
    }
}
