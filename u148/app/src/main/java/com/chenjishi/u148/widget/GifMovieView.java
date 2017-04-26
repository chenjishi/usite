package com.chenjishi.u148.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import com.chenjishi.u148.utils.ErrorListener;
import com.chenjishi.u148.utils.Listener;
import com.chenjishi.u148.utils.NetworkRequest;

/**
 * Created by chenjishi on 14/11/14.
 */
public class GifMovieView extends View implements Listener<byte[]>, ErrorListener {
    private static final int MOVE_VIEW_DURATION = 1000;

    private Movie mMovie;

    private long mMovieStart;

    private int mCurrentAnimationTime = 0;

    private int mRequestWidth, mRequestHeight;

    private float mScaleX, mScaleY;

    private volatile boolean mPaused = false;

    private boolean mVisible = true;

    public GifMovieView(Context context) {
        this(context, null);
    }

    public GifMovieView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GifMovieView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        /**
         * Starting from HONEYCOMB have to turn off HW acceleration to draw
         * Movie on Canvas.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (null != mMovie) {
            int movieWidth = mMovie.width();
            int movieHeight = mMovie.height();

            if (movieWidth == 0 || movieHeight == 0) {
                mRequestHeight = mRequestWidth;

                mScaleX = 1;
                mScaleY = 1;
            } else {
                mRequestHeight = mRequestWidth * movieHeight / movieWidth;

                mScaleX = mRequestWidth * 1.f / movieWidth;
                mScaleY = mRequestHeight * 1.f / movieHeight;
            }

            setMeasuredDimension(mRequestWidth, mRequestHeight);
        } else {
            setMeasuredDimension(getSuggestedMinimumWidth(), getSuggestedMinimumHeight());
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mVisible = getVisibility() == VISIBLE;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (null == mMovie) return;

        if (!mPaused) {
            updateAnimationTime();
            drawMovieFrame(canvas);
            invalidateView();
        } else {
            drawMovieFrame(canvas);
        }
    }

    @SuppressLint("NewApi")
    private void invalidateView() {
        if (mVisible) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                postInvalidateOnAnimation();
            } else {
                invalidate();
            }
        }
    }

    /**
     * Calculate current animation time
     */
    private void updateAnimationTime() {
        long now = android.os.SystemClock.uptimeMillis();

        if (mMovieStart == 0) {
            mMovieStart = now;
        }

        int dur = mMovie.duration();

        if (dur == 0) {
            dur = MOVE_VIEW_DURATION;
        }

        mCurrentAnimationTime = (int) ((now - mMovieStart) % dur);
    }

    private void drawMovieFrame(Canvas canvas) {
        mMovie.setTime(mCurrentAnimationTime);

        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.scale(mScaleX, mScaleY);
        mMovie.draw(canvas, 0, 0);
        canvas.restore();
    }

    @SuppressLint("NewApi")
    @Override
    public void onScreenStateChanged(int screenState) {
        super.onScreenStateChanged(screenState);
        mVisible = screenState == SCREEN_STATE_ON;
        invalidateView();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        mVisible = visibility == View.VISIBLE;
        invalidateView();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mVisible = visibility == View.VISIBLE;
        invalidateView();
    }

    @Override
    public void onErrorResponse() {

    }

    @Override
    public void onResponse(byte[] response) {
        if (null != response && response.length > 0) {
            mMovie = Movie.decodeByteArray(response, 0, response.length);

            post(new Runnable() {
                @Override
                public void run() {
                    requestLayout();
                }
            });
        }
    }

    public void setImageResId(int resId, int width) {
        mRequestWidth = width;

        mMovie = Movie.decodeStream(getResources().openRawResource(resId));
        requestLayout();
    }

    public void setImageUrl(String url, int width) {
        NetworkRequest.getInstance().getBytes(url, this, this);
        mRequestWidth = width;
    }
}
