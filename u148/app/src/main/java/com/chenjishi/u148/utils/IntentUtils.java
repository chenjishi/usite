package com.chenjishi.u148.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import com.chenjishi.u148.model.BitmapItem;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by chenjishi on 14-3-19.
 */
public class IntentUtils {

    private static final IntentUtils INSTANCE = new IntentUtils();

    private LinkedHashMap<String, BitmapItem> mCachedBitmaps;

    private IntentUtils() {
        mCachedBitmaps = new LinkedHashMap<String, BitmapItem>(0, 0.75f, true);
    }

    public void clear() {
        for (Map.Entry<String, BitmapItem> entry : mCachedBitmaps.entrySet()) {
            entry.getValue().clear();
        }
        mCachedBitmaps.clear();
    }

    public void setIsDisplayed(String id, boolean isDisplayed) {
        BitmapItem item = mCachedBitmaps.get(id);
        if (null != item) {
            item.setIsDisplayed(isDisplayed);
        }
    }

    private BitmapItem getBitmapItem(int width, int height) {
        int size = mCachedBitmaps.size();

        if (size > 0) {
            BitmapItem reuseItem = null;
            for (Map.Entry<String, BitmapItem> entry : mCachedBitmaps.entrySet()) {
                BitmapItem item = entry.getValue();
                if (item.getReferenceCount() <= 0) {
                    reuseItem = item;
                }
            }

            if (null != reuseItem) {
                return reuseItem;
            } else {
                return crateItem(width, height);
            }
        } else {
            return crateItem(width, height);
        }
    }

    private BitmapItem crateItem(int width, int height) {
        BitmapItem item = BitmapItem.create(width, height);
        String id = "id_" + System.currentTimeMillis();
        item.setId(id);
        mCachedBitmaps.put(id, item);
        return item;
    }

    public static IntentUtils getInstance() {
        return INSTANCE;
    }

    public Bitmap getBitmap(String id) {
        return mCachedBitmaps.get(id).getBitmap();
    }

    public void startActivity(final Context context, final Intent intent) {
        final View v = ((Activity) context).findViewById(android.R.id.content);

        BitmapItem item = getBitmapItem(v.getWidth(), v.getHeight());
        final Bitmap bitmap = item.getBitmap();
        intent.putExtra("bitmap_id", item.getId());

        v.postDelayed(new Runnable() {
            @Override
            public void run() {
                v.draw(new Canvas(bitmap));
                context.startActivity(intent);
            }
        }, 100);
    }
}
