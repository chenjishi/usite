package com.chenjishi.usite.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.util.LruCache;
import android.text.TextUtils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-3
 * Time: 下午6:27
 * To change this template use File | Settings | File Templates.
 */
public class ImagePool {
    private ExecutorService mService;
    private LruCache<String, Drawable> cache = new LruCache<String, Drawable>(10);
    private Handler mHandler;

    public ImagePool() {
        this(5);
    }

    public ImagePool(int taskNum) {
        this(taskNum, new Handler());
    }

    public Drawable getCachedDrawable(String url) {
        return cache.get(url);
    }

    public ImagePool(int taskNum, Handler handler) {
        mService = Executors.newFixedThreadPool(taskNum);
        mHandler = handler;
    }

    public void requestImage(final String url, final IImageCallback callback) {
        requestImage(url, callback, mHandler);
    }

    public void requestImage(final String url, final IImageCallback callback, final Handler handler) {
        if (TextUtils.isEmpty(url)) return;

        if (null == callback) {
            throw new IllegalArgumentException("callback can't be null");
        }

        if (null == handler) {
            throw new IllegalArgumentException("handler can't be null");
        }

        Drawable oldDrawable = cache.get(url);
        if (null != oldDrawable) {
            callback.onImageResponse(oldDrawable);
        } else {
            mService.execute(new Runnable() {
                @Override
                public void run() {
                    final Drawable d = getDrawableFromUrl(url);
                    if (null == d) return;

                    cache.put(url, d);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onImageResponse(d);
                        }
                    });
                }
            });
        }
    }

    private Drawable getDrawableFromUrl(String url) {
        Bitmap bm = null;
        try {
            URL imageURl = new URL(url);
            InputStream is = imageURl.openStream();
            bm = BitmapFactory.decodeStream(new FlushedInputStream(is));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null != bm ? new BitmapDrawable(bm) : null;
    }

    static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream is) {
            super(is);
        }

        @Override
        public long skip(long count) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < count) {
                long bytesSkipped = in.skip(count - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int byteValue = read();
                    if (byteValue < 0) {
                        break;  // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }
}
