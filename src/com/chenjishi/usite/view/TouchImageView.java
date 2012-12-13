package com.chenjishi.usite.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.FloatMath;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-26
 * Time: 下午4:56
 * To change this template use File | Settings | File Templates.
 */
public class TouchImageView extends ImageView {
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    static final int BIGGER = 3;
    static final int SMALLER = 4;
    private int mode = NONE;

    private float beforeLenght;
    private float afterLenght;
    private float scale = 0.04f;

    private int screenW;
    private int screenH;
    private int originalW;
    private int originalH;

    private int start_x;
    private int start_y;
    private int stop_x;
    private int stop_y;

    private float first_x;
    private ImageListener touchListener;
    private int mPagingTouchSlop;
    private static final int DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout();
    private int mDoubleTapSlop;

    private MotionEvent mPreviousDownEvent;
    private MotionEvent mPreviousUpEvent;

    private TranslateAnimation trans;

    public TouchImageView(Context context, int w, int h, ImageListener touchListener) {
        super(context);
        setAdjustViewBounds(true);
        screenW = w;
        screenH = h;
        this.touchListener = touchListener;
        final ViewConfiguration conf = ViewConfiguration.get(context);
        mPagingTouchSlop = conf.getScaledTouchSlop() * 2;
        mDoubleTapSlop = conf.getScaledDoubleTapSlop();
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mPreviousDownEvent != null && mPreviousUpEvent != null && isConsideredDoubleTap(mPreviousDownEvent, mPreviousUpEvent, event)) {
                    touchListener.doubleClick();
                }

                if (mPreviousDownEvent != null) {
                    mPreviousDownEvent.recycle();
                }
                mPreviousDownEvent = MotionEvent.obtain(event);
                break;

            case MotionEvent.ACTION_UP:
                if (mPreviousUpEvent != null) {
                    mPreviousUpEvent.recycle();
                }
                mPreviousUpEvent = MotionEvent.obtain(event);
                break;
        }

        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mode = DRAG;
                stop_x = (int) event.getRawX();
                stop_y = (int) event.getRawY();
                start_x = (int) event.getX();
                start_y = stop_y - this.getTop();
                first_x = event.getRawX();
                if (event.getPointerCount() == 2)
                    beforeLenght = spacing(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (spacing(event) > 10f) {
                    mode = ZOOM;
                    beforeLenght = spacing(event);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs(first_x - event.getRawX()) < mPagingTouchSlop) {
                    touchListener.singleClick();
                    break;
                }
                if (mode != DRAG) {
                    mode = NONE;
                    break;
                }
                float flipSpace = event.getRawX() - first_x;
                if ((getWidth() > screenW && getLeft() > screenW / 2) || (getWidth() <= screenW && flipSpace > mPagingTouchSlop)) {
                    if (touchListener.dragPrev()) {
                        break;
                    }
                }
                if ((getWidth() > screenW && getRight() < screenW / 2) || (getWidth() <= screenW && flipSpace < -mPagingTouchSlop)) {
                    if (touchListener.dragNext()) {
                        break;
                    }
                }
                adjustPosition();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (getWidth() < originalW || getHeight() < originalH) {
                    setOriginalScale();
                }
                mode = NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {

                    if (Math.abs(stop_x - start_x - getLeft()) < 88 && Math.abs(stop_y - start_y - getTop()) < 85) {
                        this.setPosition(stop_x - start_x, stop_y - start_y, stop_x + this.getWidth() - start_x, stop_y - start_y + this.getHeight());
                        if (getWidth() > screenW) {
                            stop_x = (int) event.getRawX();
                        }
                        if (getHeight() > screenH) {
                            stop_y = (int) event.getRawY();
                        }
                    }
                }
                else if (mode == ZOOM) {
                    if (spacing(event) > 10f) {
                        afterLenght = spacing(event);
                        float gapLenght = afterLenght - beforeLenght;
                        if (gapLenght == 0) {
                            break;
                        } else if (Math.abs(gapLenght) > 5f) {
                            if (gapLenght > 0) {
                                this.setScale(scale, BIGGER);
                            } else {
                                this.setScale(scale, SMALLER);
                            }
                            beforeLenght = afterLenght;
                        }
                    }
                }
                break;
        }
        return true;
    }

    private void adjustPosition() {
        int disX = 0;
        int disY = 0;

        if (getHeight() > screenH) {
            if (this.getTop() > 0) {
                int dis = getTop();
                this.layout(this.getLeft(), 0, this.getRight(), this.getHeight());
                disY = dis - getTop();
            } else if (this.getBottom() < screenH) {
                disY = getHeight() - screenH + getTop();
                this.layout(this.getLeft(), screenH - getHeight(), this.getRight(), screenH);
            }
        } else {
            disY = getTop() - (screenH - this.getHeight()) / 2;
            this.layout(this.getLeft(), (screenH - this.getHeight()) / 2, this.getRight(), (screenH + this.getHeight()) / 2);
        }

        if (getWidth() > screenW) {
            if (this.getLeft() > 0) {
                disX = getLeft();
                this.layout(0, this.getTop(), getWidth(), this.getBottom());
            } else if (this.getRight() < screenW) {
                disX = getWidth() - screenW + getLeft();
                this.layout(screenW - getWidth(), this.getTop(), screenW, this.getBottom());
            }
        } else {
            disX = getLeft() - (screenW - this.getWidth()) / 2;
            this.layout((screenW - this.getWidth()) / 2, this.getTop(), (screenW + this.getWidth()) / 2, this.getBottom());
        }

        if (disX != 0 || disY != 0) {
            trans = new TranslateAnimation(disX, 0, disY, 0);
            trans.setDuration(500);
            this.startAnimation(trans);
        }
    }

    private void setScale(float temp, int flag) {

        if (flag == BIGGER) {
            this.setFrame(this.getLeft() - (int) (temp * this.getWidth()),
                    this.getTop() - (int) (temp * this.getHeight()),
                    this.getRight() + (int) (temp * this.getWidth()),
                    this.getBottom() + (int) (temp * this.getHeight()));
        } else if (flag == SMALLER) {
            this.setFrame(this.getLeft() + (int) (temp * this.getWidth()),
                    this.getTop() + (int) (temp * this.getHeight()),
                    this.getRight() - (int) (temp * this.getWidth()),
                    this.getBottom() - (int) (temp * this.getHeight()));
        }
        adjustPosition();
    }

    private void setOriginalScale() {
        this.setFrame((screenW - originalW) / 2, (screenH - originalH) / 2, (screenW + originalW) / 2, (screenH + originalH) / 2);
    }

    public void setBigScale() {
        float rate = 0.2f * screenW / getWidth();
        setScale(rate, BIGGER);
    }

    public void setSmallScale() {
        float rate = 0.2f * screenW / getWidth();
        setScale(rate, SMALLER);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if (bm.getHeight() < screenH && bm.getWidth() < screenW) {
            originalW = bm.getWidth();
            originalH = bm.getHeight();
        } else if ((float) bm.getHeight() / bm.getWidth() > (float) screenH / screenW) {
            originalW = bm.getWidth() * screenH / bm.getHeight();
            originalH = screenH;
        } else {
            originalW = screenW;
            originalH = bm.getHeight() * screenW / bm.getWidth();
        }
        setLayoutParams(new FrameLayout.LayoutParams(originalW, originalH, Gravity.CENTER));
        super.setImageBitmap(bm);
    }

    private void setPosition(int left, int top, int right, int bottom) {
        this.layout(left, top, right, bottom);
    }

    private boolean isConsideredDoubleTap(MotionEvent firstDown, MotionEvent firstUp, MotionEvent secondDown) {
        if (secondDown.getEventTime() - firstUp.getEventTime() > DOUBLE_TAP_TIMEOUT) {
            return false;
        }
        int deltaX = (int) firstDown.getX() - (int) secondDown.getX();
        int deltaY = (int) firstDown.getY() - (int) secondDown.getY();
        return (Math.abs(deltaX) < mDoubleTapSlop && Math.abs(deltaY) < mDoubleTapSlop);
    }

    public interface ImageListener {
        public boolean dragNext();

        public boolean dragPrev();

        public void singleClick();

        public boolean doubleClick();

        public void loadingSuccess();
    }
}
