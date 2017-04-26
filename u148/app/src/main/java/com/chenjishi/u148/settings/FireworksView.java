package com.chenjishi.u148.settings;

import android.content.Context;
import android.graphics.*;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.chenjishi.u148.Config;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-11-15
 * Time: 下午7:41
 * To change this template use File | Settings | File Templates.
 */
public class FireworksView extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread thread;
    private float desity;
    private String mTitle;
    private String mDesc;

    enum AnimateState {
        asReady, asRunning, asPause
    }

    public FireworksView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FireworksView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context){
        setBackgroundColor(Color.BLACK);
        setZOrderOnTop(true);
        SurfaceHolder holder = getHolder();
        holder.setFormat(PixelFormat.TRANSLUCENT);
        holder.addCallback(this);
        setFocusable(true);

        desity = context.getResources().getDisplayMetrics().density;

        mTitle = Config.getSurpriseTitle(context);
        if (TextUtils.isEmpty(mTitle)) {
            mTitle = "Wow, You Find This!";
        }

        mDesc = Config.getSurpriseDesc(context);
        if (TextUtils.isEmpty(mDesc)) {
            mDesc = "Very Appreciated you like it";
        }
    }

    public void setTitle(String title, String desc) {
        mTitle = title;
        mDesc = desc;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread = new GameThread(holder);
        thread.setRunning(true);
        thread.doStart();
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        thread.setSurfaceSize(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread.setRunning(false);
        thread = null;
    }

    public void stopPlay() {
        if (thread != null){
            thread.pause();
        }
    }

    class GameThread extends Thread {
        private boolean mRun = false;

        private SurfaceHolder surfaceHolder;
        private AnimateState state;
        private Paint paint;
        private Paint textPaint;
        Fireworks fireworks;

        GameThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;

            fireworks = new Fireworks(getWidth(), getHeight(), desity);

            paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setMaskFilter(new BlurMaskFilter(2, BlurMaskFilter.Blur.NORMAL));

            textPaint = new Paint();
            textPaint.setColor(0xff666666);
            textPaint.setAntiAlias(true);
            textPaint.setTextSize(14 * desity);
            textPaint.setTextAlign(Paint.Align.CENTER);

        }

        public void doStart() {
            synchronized (surfaceHolder) {
                setState(AnimateState.asRunning);
            }
        }

        public void pause() {
            synchronized (surfaceHolder) {
                if (state == AnimateState.asRunning)
                    setState(AnimateState.asPause);
            }
        }

        public void unpause() {
            setState(AnimateState.asRunning);
        }

        @Override
        public void run() {
            while (mRun) {
                Canvas c = null;
                try {
                    c = surfaceHolder.lockCanvas(null);

                    synchronized (surfaceHolder) {
                        if (state == AnimateState.asRunning)
                            doDraw(c);
                    }
                } finally {
                    if (c != null) {
                        surfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }

        public void setRunning(boolean b) {
            mRun = b;
        }

        public void setState(AnimateState state) {
            synchronized (surfaceHolder) {
                this.state = state;
            }
        }

        public void doDraw(Canvas canvas) {
            if (null == canvas) return;

            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            int xPos = canvas.getWidth() / 2;
            int yPos = (int) ((canvas.getHeight() / 2) - (textPaint.descent() + textPaint.ascent()) / 2);
            canvas.drawText(mTitle, xPos, yPos, textPaint);
            canvas.drawText(mDesc, xPos, yPos + 46.0f, textPaint);
            fireworks.doDraw(canvas, paint);
        }

        public void setSurfaceSize(int width, int height) {
            synchronized (surfaceHolder) {
                fireworks.reshape(width, height);
            }
        }
    }
}
