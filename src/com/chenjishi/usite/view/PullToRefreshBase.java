package com.chenjishi.usite.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import com.chenjishi.usite.R;

public abstract class PullToRefreshBase<T extends View> extends LinearLayout {
    final class SmoothScrollRunnable implements Runnable {

        static final int ANIMATION_DURATION_MS = 190;
        static final int ANIMATION_FPS = 1000 / 60;

        private final Interpolator interpolator;
        private final int scrollToY;
        private final int scrollFromY;
        private final Handler handler;

        private boolean continueRunning = true;
        private long startTime = -1;
        private int currentY = -1;

        public SmoothScrollRunnable(Handler handler, int fromY, int toY) {
            this.handler = handler;
            this.scrollFromY = fromY;
            this.scrollToY = toY;
            this.interpolator = new AccelerateDecelerateInterpolator();
        }

        @Override
        public void run() {
            if (startTime == -1) {
                startTime = System.currentTimeMillis();
            } else {
                long normalizedTime = (1000 * (System.currentTimeMillis() - startTime)) / ANIMATION_DURATION_MS;
                normalizedTime = Math.max(Math.min(normalizedTime, 1000), 0);

                final int deltaY = Math.round((scrollFromY - scrollToY)
                        * interpolator.getInterpolation(normalizedTime / 1000f));
                this.currentY = scrollFromY - deltaY;
                setHeaderScroll(currentY);
            }

            if (continueRunning && scrollToY != currentY) {
                handler.postDelayed(this, ANIMATION_FPS);
            }
        }

        public void stop() {
            this.continueRunning = false;
            this.handler.removeCallbacks(this);
        }
    }

    static final float FRICTION = 2.0f;

    static final int PULL_TO_REFRESH = 0x0;
    static final int RELEASE_TO_REFRESH = 0x1;
    static final int REFRESHING = 0x2;
    static final int MANUAL_REFRESHING = 0x3;

    public static final int MODE_PULL_DOWN_TO_REFRESH = 0x1;
    public static final int MODE_PULL_UP_TO_REFRESH = 0x2;
    public static final int MODE_BOTH = 0x3;


    private int touchSlop;

    private float initialMotionY;
    private float lastMotionX;
    private float lastMotionY;
    private boolean isBeingDragged = false;

    private int state = PULL_TO_REFRESH;
    private int mode = MODE_PULL_DOWN_TO_REFRESH;
    private int currentMode;

    private boolean disableScrollingWhileRefreshing = true;

    T refreshableView;
    private boolean isPullToRefreshEnabled = true;

    private LoadingLayout headerLayout;
    private LoadingLayout footerLayout;
    private int headerHeight;

    private final Handler handler = new Handler();

    private OnRefreshListener onRefreshListener;

    private SmoothScrollRunnable currentSmoothScrollRunnable;


    public PullToRefreshBase(Context context) {
        super(context);
        init(context);
    }

    public PullToRefreshBase(Context context, int mode) {
        super(context);
        this.mode = mode;
        init(context);
    }

    public PullToRefreshBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }


    public final T getAdapterView() {
        return refreshableView;
    }

    public final T getRefreshableView() {
        return refreshableView;
    }

    public final boolean isPullToRefreshEnabled() {
        return isPullToRefreshEnabled;
    }

    public final boolean isDisableScrollingWhileRefreshing() {
        return disableScrollingWhileRefreshing;
    }

    public final boolean isRefreshing() {
        return state == REFRESHING || state == MANUAL_REFRESHING;
    }

    public final void setDisableScrollingWhileRefreshing(boolean disableScrollingWhileRefreshing) {
        this.disableScrollingWhileRefreshing = disableScrollingWhileRefreshing;
    }

    public final void onRefreshComplete() {
        if (state != PULL_TO_REFRESH) {
            resetHeader();
        }
    }

    public final void setOnRefreshListener(OnRefreshListener listener) {
        onRefreshListener = listener;
    }

    public final void setPullToRefreshEnabled(boolean enable) {
        this.isPullToRefreshEnabled = enable;
    }

    public void setReleaseLabel(String releaseLabel) {
        if (null != headerLayout) {
            headerLayout.setReleaseLabel(releaseLabel);
        }
        if (null != footerLayout) {
            footerLayout.setReleaseLabel(releaseLabel);
        }
    }


    public void setPullLabel(String pullLabel) {
        if (null != headerLayout) {
            headerLayout.setPullLabel(pullLabel);
        }
        if (null != footerLayout) {
            footerLayout.setPullLabel(pullLabel);
        }
    }


    public void setRefreshingLabel(String refreshingLabel) {
        if (null != headerLayout) {
            headerLayout.setRefreshingLabel(refreshingLabel);
        }
        if (null != footerLayout) {
            footerLayout.setRefreshingLabel(refreshingLabel);
        }
    }

    public final void setRefreshing() {
        this.setRefreshing(true);
    }


    public final void setRefreshing(boolean doScroll) {
        if (!isRefreshing()) {
            setRefreshingInternal(doScroll);
            state = MANUAL_REFRESHING;
        }
    }

    public final boolean hasPullFromTop() {
        return currentMode != MODE_PULL_UP_TO_REFRESH;
    }



    @Override
    public final boolean onTouchEvent(MotionEvent event) {
        if (!isPullToRefreshEnabled) {
            return false;
        }

        if (isRefreshing() && disableScrollingWhileRefreshing) {
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN && event.getEdgeFlags() != 0) {
            return false;
        }

        switch (event.getAction()) {

            case MotionEvent.ACTION_MOVE: {
                if (isBeingDragged) {
                    lastMotionY = event.getY();
                    this.pullEvent();
                    return true;
                }
                break;
            }

            case MotionEvent.ACTION_DOWN: {
                if (isReadyForPull()) {
                    lastMotionY = initialMotionY = event.getY();
                    return true;
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (isBeingDragged) {
                    isBeingDragged = false;

                    if (state == RELEASE_TO_REFRESH && null != onRefreshListener) {
                        setRefreshingInternal(true);
                        onRefreshListener.onRefresh();
                    } else {
                        smoothScrollTo(0);
                    }
                    return true;
                }
                break;
            }
        }

        return false;
    }

    @Override
    public final boolean onInterceptTouchEvent(MotionEvent event) {

        if (!isPullToRefreshEnabled) {
            return false;
        }

        if (isRefreshing() && disableScrollingWhileRefreshing) {
            return true;
        }

        final int action = event.getAction();

        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            isBeingDragged = false;
            return false;
        }

        if (action != MotionEvent.ACTION_DOWN && isBeingDragged) {
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                if (isReadyForPull()) {

                    final float y = event.getY();
                    final float dy = y - lastMotionY;
                    final float yDiff = Math.abs(dy);
                    final float xDiff = Math.abs(event.getX() - lastMotionX);

                    if (yDiff > touchSlop && yDiff > xDiff) {
                        if ((mode == MODE_PULL_DOWN_TO_REFRESH || mode == MODE_BOTH) && dy >= 0.0001f
                                && isReadyForPullDown()) {
                            lastMotionY = y;
                            isBeingDragged = true;
                            if (mode == MODE_BOTH) {
                                currentMode = MODE_PULL_DOWN_TO_REFRESH;
                            }
                        } else if ((mode == MODE_PULL_UP_TO_REFRESH || mode == MODE_BOTH) && dy <= 0.0001f
                                && isReadyForPullUp()) {
                            lastMotionY = y;
                            isBeingDragged = true;
                            if (mode == MODE_BOTH) {
                                currentMode = MODE_PULL_UP_TO_REFRESH;
                            }
                        }
                    }
                }
                break;
            }
            case MotionEvent.ACTION_DOWN: {
                if (isReadyForPull()) {
                    lastMotionY = initialMotionY = event.getY();
                    lastMotionX = event.getX();
                    isBeingDragged = false;
                }
                break;
            }
        }

        return isBeingDragged;
    }

    protected void addRefreshableView(Context context, T refreshableView) {
        addView(refreshableView, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 0, 1.0f));
    }


    protected abstract T createRefreshableView(Context context, AttributeSet attrs);

    protected final int getCurrentMode() {
        return currentMode;
    }

    protected final LoadingLayout getFooterLayout() {
        return footerLayout;
    }

    protected final LoadingLayout getHeaderLayout() {
        return headerLayout;
    }

    protected final int getHeaderHeight() {
        return headerHeight;
    }

    protected final int getMode() {
        return mode;
    }

    protected abstract boolean isReadyForPullDown();

    protected abstract boolean isReadyForPullUp();

    protected void resetHeader() {
        state = PULL_TO_REFRESH;
        isBeingDragged = false;

        if (null != headerLayout) {
            headerLayout.reset();
        }
        if (null != footerLayout) {
            footerLayout.reset();
        }

        smoothScrollTo(0);
    }

    protected void setRefreshingInternal(boolean doScroll) {
        state = REFRESHING;

        if (null != headerLayout) {
            headerLayout.refreshing();
        }
        if (null != footerLayout) {
            footerLayout.refreshing();
        }

        if (doScroll) {
            smoothScrollTo(currentMode == MODE_PULL_DOWN_TO_REFRESH ? -headerHeight : headerHeight);
        }
    }

    protected final void setHeaderScroll(int y) {
        scrollTo(0, y);
    }

    protected final void smoothScrollTo(int y) {
        if (null != currentSmoothScrollRunnable) {
            currentSmoothScrollRunnable.stop();
        }

        if (this.getScrollY() != y) {
            this.currentSmoothScrollRunnable = new SmoothScrollRunnable(handler, getScrollY(), y);
            handler.post(currentSmoothScrollRunnable);
        }
    }

    private void init(Context context) {

        setOrientation(LinearLayout.VERTICAL);

        touchSlop = ViewConfiguration.getTouchSlop();

        refreshableView = this.createRefreshableView(context, null);
        this.addRefreshableView(context, refreshableView);

        String pullLabel = context.getString(R.string.pull_to_refresh_pull_label);
        String refreshingLabel = context.getString(R.string.pull_to_refresh_refreshing_label);
        String releaseLabel = context.getString(R.string.pull_to_refresh_release_label);

        if (mode == MODE_PULL_DOWN_TO_REFRESH || mode == MODE_BOTH) {
            headerLayout = new LoadingLayout(context, MODE_PULL_DOWN_TO_REFRESH, releaseLabel, pullLabel,
                    refreshingLabel);
            addView(headerLayout, 0, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            measureView(headerLayout);
            headerHeight = headerLayout.getMeasuredHeight();
        }
        if (mode == MODE_PULL_UP_TO_REFRESH || mode == MODE_BOTH) {
            footerLayout = new LoadingLayout(context, MODE_PULL_UP_TO_REFRESH, releaseLabel, pullLabel, refreshingLabel);
            addView(footerLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            measureView(footerLayout);
            headerHeight = footerLayout.getMeasuredHeight();
        }

        switch (mode) {
            case MODE_BOTH:
                setPadding(0, -headerHeight, 0, -headerHeight);
                break;
            case MODE_PULL_UP_TO_REFRESH:
                setPadding(0, 0, 0, -headerHeight);
                break;
            case MODE_PULL_DOWN_TO_REFRESH:
            default:
                setPadding(0, -headerHeight, 0, 0);
                break;
        }

        if (mode != MODE_BOTH) {
            currentMode = mode;
        }
    }

    private void init(Context context, AttributeSet attrs) {

        setOrientation(LinearLayout.VERTICAL);

        touchSlop = ViewConfiguration.getTouchSlop();

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PullToRefresh);
        mode = a.getInteger(R.styleable.PullToRefresh_mode, MODE_PULL_DOWN_TO_REFRESH);

        refreshableView = this.createRefreshableView(context, attrs);
        this.addRefreshableView(context, refreshableView);

        String pullLabel = context.getString(R.string.pull_to_refresh_pull_label);
        String refreshingLabel = context.getString(R.string.pull_to_refresh_refreshing_label);
        String releaseLabel = context.getString(R.string.pull_to_refresh_release_label);

        if (mode == MODE_PULL_DOWN_TO_REFRESH || mode == MODE_BOTH) {
            headerLayout = new LoadingLayout(context, MODE_PULL_DOWN_TO_REFRESH, releaseLabel, pullLabel,
                    refreshingLabel);
            addView(headerLayout, 0, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            measureView(headerLayout);
            headerHeight = headerLayout.getMeasuredHeight();
        }
        if (mode == MODE_PULL_UP_TO_REFRESH || mode == MODE_BOTH) {
            footerLayout = new LoadingLayout(context, MODE_PULL_UP_TO_REFRESH, releaseLabel, pullLabel, refreshingLabel);
            addView(footerLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            measureView(footerLayout);
            headerHeight = footerLayout.getMeasuredHeight();
        }

        if (a.hasValue(R.styleable.PullToRefresh_headerTextColor)) {
            final int color = a.getColor(R.styleable.PullToRefresh_headerTextColor, Color.BLACK);
            if (null != headerLayout) {
                headerLayout.setTextColor(color);
            }
            if (null != footerLayout) {
                footerLayout.setTextColor(color);
            }
        }
        if (a.hasValue(R.styleable.PullToRefresh_headerBackground)) {
            this.setBackgroundResource(a.getResourceId(R.styleable.PullToRefresh_headerBackground, Color.WHITE));
        }
        if (a.hasValue(R.styleable.PullToRefresh_adapterViewBackground)) {
            refreshableView.setBackgroundResource(a.getResourceId(R.styleable.PullToRefresh_adapterViewBackground,
                    Color.WHITE));
        }
        a.recycle();

        switch (mode) {
            case MODE_BOTH:
                setPadding(0, -headerHeight, 0, -headerHeight);
                break;
            case MODE_PULL_UP_TO_REFRESH:
                setPadding(0, 0, 0, -headerHeight);
                break;
            case MODE_PULL_DOWN_TO_REFRESH:
            default:
                setPadding(0, -headerHeight, 0, 0);
                break;
        }

        if (mode != MODE_BOTH) {
            currentMode = mode;
        }
    }

    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    private boolean pullEvent() {

        final int newHeight;
        final int oldHeight = this.getScrollY();

        switch (currentMode) {
            case MODE_PULL_UP_TO_REFRESH:
                newHeight = Math.round(Math.max(initialMotionY - lastMotionY, 0) / FRICTION);
                break;
            case MODE_PULL_DOWN_TO_REFRESH:
            default:
                newHeight = Math.round(Math.min(initialMotionY - lastMotionY, 0) / FRICTION);
                break;
        }

        setHeaderScroll(newHeight);

        if (newHeight != 0) {
            if (state == PULL_TO_REFRESH && headerHeight < Math.abs(newHeight)) {
                state = RELEASE_TO_REFRESH;

                switch (currentMode) {
                    case MODE_PULL_UP_TO_REFRESH:
                        footerLayout.releaseToRefresh();
                        break;
                    case MODE_PULL_DOWN_TO_REFRESH:
                        headerLayout.releaseToRefresh();
                        break;
                }

                return true;

            } else if (state == RELEASE_TO_REFRESH && headerHeight >= Math.abs(newHeight)) {
                state = PULL_TO_REFRESH;

                switch (currentMode) {
                    case MODE_PULL_UP_TO_REFRESH:
                        footerLayout.pullToRefresh();
                        break;
                    case MODE_PULL_DOWN_TO_REFRESH:
                        headerLayout.pullToRefresh();
                        break;
                }

                return true;
            }
        }

        return oldHeight != newHeight;
    }

    private boolean isReadyForPull() {
        switch (mode) {
            case MODE_PULL_DOWN_TO_REFRESH:
                return isReadyForPullDown();
            case MODE_PULL_UP_TO_REFRESH:
                return isReadyForPullUp();
            case MODE_BOTH:
                return isReadyForPullUp() || isReadyForPullDown();
        }
        return false;
    }

    public static interface OnRefreshListener {

        public void onRefresh();

    }

    public static interface OnLastItemVisibleListener {

        public void onLastItemVisible();

    }

    @Override
    public void setLongClickable(boolean longClickable) {
        getRefreshableView().setLongClickable(longClickable);
    }
}
