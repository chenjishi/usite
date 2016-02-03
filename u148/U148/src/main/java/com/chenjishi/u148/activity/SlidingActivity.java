package com.chenjishi.u148.activity;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.util.Constants;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by chenjishi on 14-3-17.
 */
public class SlidingActivity extends BaseActivity implements SlidingLayout.SlidingListener {
    private View mPreview;

    private float mInitOffset;
    private String mBitmapId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSliding = true;
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.base_sliding_layout);

        mRootContentView.setBackgroundColor(getResources().getColor(Constants.MODE_NIGHT == PrefsUtil.getThemeMode()
                ? R.color.background_night : R.color.background));

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mInitOffset = -(1.f / 3) * metrics.widthPixels;

        mPreview = findViewById(R.id.iv_preview);
        FrameLayout contentView = (FrameLayout) findViewById(R.id.content_view);

        if (!mHideTitle) {
            int resId = -1 == mTitleResId ? R.layout.base_title_layout : mTitleResId;
            mInflater.inflate(resId, contentView);
        }

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(MATCH_PARENT,
                MATCH_PARENT, Gravity.BOTTOM);
        final int marginTop = mHideTitle ? 0 : dp2px(48);
        lp.setMargins(0, marginTop, 0, 0);
        contentView.addView(mInflater.inflate(layoutResID, null), lp);

        final SlidingLayout slideLayout = (SlidingLayout) findViewById(R.id.slide_layout);
        slideLayout.setShadowResource(R.drawable.sliding_back_shadow);
        slideLayout.setSlidingListener(this);
        slideLayout.setEdgeSize((int) (metrics.density * 20));

        mBitmapId = getIntent().getExtras().getString("bitmap_id");
        Bitmap bitmap = IntentUtils.getInstance().getBitmap(mBitmapId);
        if (null != bitmap) {
            if (Build.VERSION.SDK_INT >= 16) {
                mPreview.setBackground(new BitmapDrawable(bitmap));
            } else {
                mPreview.setBackgroundDrawable(new BitmapDrawable(bitmap));
            }

            IntentUtils.getInstance().setIsDisplayed(mBitmapId, true);
        } else {
            slideLayout.setSlideable(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        IntentUtils.getInstance().setIsDisplayed(mBitmapId, false);
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
        if (slideOffset <= 0) {
        } else if (slideOffset < 1) {
            mPreview.setTranslationX(mInitOffset * (1 - slideOffset));
        } else {
            mPreview.setTranslationX(0);
            finish();
            overridePendingTransition(0, 0);
        }
    }

    protected void setContentView(int layoutResID, int titleResId) {
        mTitleResId = titleResId;
        setContentView(layoutResID);
    }

    protected void setContentView(int layoutResID, boolean hideTitle) {
        mHideTitle = hideTitle;
        setContentView(layoutResID);
    }
}
