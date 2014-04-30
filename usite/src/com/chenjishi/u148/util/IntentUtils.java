package com.chenjishi.u148.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by chenjishi on 14-3-19.
 */
public class IntentUtils {
    public static String KEY_PREVIEW_IMAGE = "preview_image";

    /**
     * start screen capture with no delay
     *
     * @param context
     * @param intent
     */
    public static void startPreviewActivity(final Context context, final Intent intent) {
        final float f = (float) (Runtime.getRuntime().totalMemory() + Debug.getNativeHeapAllocatedSize());
        if (f / (float) Runtime.getRuntime().maxMemory() > .8f) {
            context.startActivity(intent);
        } else {
            final Handler mainThread = new Handler(Looper.getMainLooper());
            final Runnable postAction = new Runnable() {
                @Override
                public void run() {
                    context.startActivity(intent);
                }
            };

            /** process screen capture on background thread */
            Runnable action = new Runnable() {
                @Override
                public void run() {
                    /**
                     * activity's root layout id, you can change the android.R.id.content to your root
                     * layout id
                     */
                    final View contentView = ((Activity) context).findViewById(android.R.id.content);

                    ByteArrayOutputStream baos = null;
                    Bitmap bitmap = null;

                    try {
                        bitmap = Bitmap.createBitmap(contentView.getWidth(),
                                contentView.getHeight(), Bitmap.Config.ARGB_8888);
                        contentView.draw(new Canvas(bitmap));

                        baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
                        intent.putExtra(KEY_PREVIEW_IMAGE, baos.toByteArray());
                    } finally {
                        try {
                            /** no need to close, actually do nothing */
                            if (null != baos) baos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (null != bitmap && !bitmap.isRecycled()) {
                            bitmap.recycle();
                            bitmap = null;
                        }
                    }
                    mainThread.post(postAction);
                }
            };

            action.run();
        }
    }
}
