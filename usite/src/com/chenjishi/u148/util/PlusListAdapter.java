package com.chenjishi.u148.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.BaseAdapter;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

/**
 * Created by chenjishi on 14-1-10.
 */
public abstract class PlusListAdapter extends BaseAdapter {
    protected static final long ANIM_DEFAULT_SPEED = 600L;
    protected Interpolator interpolator;

    protected SparseBooleanArray positionsMapper;
    protected int size, height, width, previousPostition;
    protected SpeedScrollListener scrollListener;
    protected double speed;
    protected long animDuration;
    protected View v;
    protected Context context;

    protected PlusListAdapter(Context context, SpeedScrollListener scrollListener) {
        this.context = context;
        this.scrollListener = scrollListener;

        previousPostition = -1;
        positionsMapper = new SparseBooleanArray(size);
        interpolator = new DecelerateInterpolator();

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        width = metrics.widthPixels;
        height = metrics.heightPixels;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getAnimatedView(position, convertView, parent);
    }

    protected View getAnimatedView(int position, View convertView, ViewGroup parent) {
        v = getRowView(position, convertView, parent);
        if (v != null && !positionsMapper.get(position) && position > previousPostition) {
            speed = scrollListener.getSpeed();

            animDuration = (((int) speed) == 0) ? ANIM_DEFAULT_SPEED : (long) (1 / speed * 15000);

            if (animDuration > ANIM_DEFAULT_SPEED)
                animDuration = ANIM_DEFAULT_SPEED;

            previousPostition = position;

            /**
             * compatible with pre API11, revised by chenjishi
             */
            ViewHelper.setTranslationX(v, 0.0F);
            ViewHelper.setTranslationY(v, height);
            ViewHelper.setRotationX(v, 45.0F);
            ViewHelper.setScaleX(v, 0.7F);
            ViewHelper.setScaleY(v, 0.55F);

            ViewPropertyAnimator localViewPropertyAnimator = ViewPropertyAnimator.animate(v).
                    rotationX(0.0F).rotationY(0.0F).translationX(0).translationY(0).setDuration(animDuration).scaleX(1.0F)
                    .scaleY(1.0F).setInterpolator(interpolator);
            localViewPropertyAnimator.setStartDelay(0).start();

            positionsMapper.put(position, true);
        }
        return v;
    }

    protected abstract View getRowView(int position, View convertView, ViewGroup parent);
}
