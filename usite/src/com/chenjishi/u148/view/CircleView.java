package com.chenjishi.u148.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by chenjishi on 14-5-6.
 */
public class CircleView extends View {
    private int mNumber;
    private float mDensity;
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public CircleView(Context context) {
        this(context, null);
    }

    public CircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void setNumber(int n) {
        mNumber = n;
        invalidate();
    }

    private void init(Context context) {
        mDensity = context.getResources().getDisplayMetrics().density;

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(0xFFFFFFFF);

        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(mDensity * 8f);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mNumber == 0) return;

        mPaint.setColor(0xFF999999);

        int width = getWidth();
        float r = width / 2f;
        canvas.drawCircle(r, r, r, mPaint);

        float x = r;
        float y = r - (mTextPaint.descent() + mTextPaint.ascent()) / 2;
        canvas.drawText(String.valueOf(mNumber), x, y, mTextPaint);
    }
}
