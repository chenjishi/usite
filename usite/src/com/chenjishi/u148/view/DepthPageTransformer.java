package com.chenjishi.u148.view;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by chenjishi on 14-3-6.
 */
public class DepthPageTransformer implements ViewPager.PageTransformer {
    private static final float MIN_SCALE = 0.75f;

    @SuppressWarnings("NewApi")
    @Override
    public void transformPage(View view, float v) {
        int pageWidth = view.getWidth();

        if (v < -1) {
            view.setAlpha(0);
        } else if (v <= 0) {
            view.setAlpha(1);
            view.setTranslationX(0);
            view.setScaleX(1);
            view.setScaleY(1);
        } else if (v <= 1) {
            view.setAlpha(1 - v);

            view.setTranslationX(pageWidth * -v);

            float scaleFactor = MIN_SCALE
                    + (1 - MIN_SCALE) * (1 - Math.abs(v));
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);
        } else {
            view.setAlpha(0);
        }
    }
}
