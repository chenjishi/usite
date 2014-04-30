package com.chenjishi.u148.activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.PrefsUtil;
import com.nineoldandroids.view.ViewHelper;

import static com.chenjishi.u148.util.Constants.MODE_NIGHT;
import static com.chenjishi.u148.util.IntentUtils.KEY_PREVIEW_IMAGE;

/**
 * Created by chenjishi on 14-4-25.
 */
public class SlidingActivity extends BaseActivity implements SlidingLayout.SlideListener {
    private static final float MIN_SCALE = 0.85f;

    private float mInitOffset;
    private View mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSliding = true;
    }

    @SuppressLint("NewApi")
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.base_sliding_layout);

        final int screenWidth = getResources().getDisplayMetrics().widthPixels;
        mInitOffset = (1 - MIN_SCALE) * screenWidth / 2.f;

        mPreview = findViewById(R.id.iv_preview);
        FrameLayout contentView = (FrameLayout) findViewById(R.id.content_view);
        contentView.setBackgroundResource(MODE_NIGHT == PrefsUtil.getThemeMode() ?
                R.color.background_night : R.color.background);

        if (!mHideTitle) {
            int resId = mTitleResId == 0 ? R.layout.base_title_layout : mTitleResId;
            mInflater.inflate(resId, contentView);
        }

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, Gravity.BOTTOM);
        final int marginTop = mHideTitle ? 0 : dp2px(48);
        layoutParams.setMargins(0, marginTop, 0, 0);
        contentView.addView(mInflater.inflate(layoutResID, null), layoutParams);

        SlidingLayout slideLayout = (SlidingLayout) findViewById(R.id.slide_layout);
        slideLayout.setShadowResource(R.drawable.sliding_back_shadow);
        slideLayout.setSliderFadeColor(0x00000000);
        slideLayout.setPanelSlideListener(this);

        byte[] byteArray = getIntent().getByteArrayExtra(KEY_PREVIEW_IMAGE);
        if (null != byteArray) {
            Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            if (null != bmp) {
                ((ImageView) mPreview).setImageBitmap(bmp);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mPreview.setScaleX(MIN_SCALE);
                    mPreview.setScaleY(MIN_SCALE);
                } else {
                    ViewHelper.setScaleX(mPreview, MIN_SCALE);
                    ViewHelper.setScaleY(mPreview, MIN_SCALE);
                }
            } else {
                /** preview image captured fail, disable the slide back */
                slideLayout.setSlideable(false);
            }
        } else {
            /** preview image captured fail, disable the slide back */
            slideLayout.setSlideable(false);
        }
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
        final int sdkInt = Build.VERSION.SDK_INT;

        if (slideOffset <= 0) {
            if (sdkInt >= Build.VERSION_CODES.HONEYCOMB) {
                mPreview.setScaleX(MIN_SCALE);
                mPreview.setScaleY(MIN_SCALE);
            } else {
                ViewHelper.setScaleX(mPreview, MIN_SCALE);
                ViewHelper.setScaleY(mPreview, MIN_SCALE);
            }
        } else if (slideOffset < 1) {
            // Scale the page down (between MIN_SCALE and 1)
            float scaleFactor = MIN_SCALE + Math.abs(slideOffset) * (1 - MIN_SCALE);

            if (sdkInt >= Build.VERSION_CODES.HONEYCOMB) {
                mPreview.setAlpha(slideOffset);
                mPreview.setTranslationX(mInitOffset * (1 - slideOffset));
                mPreview.setScaleX(scaleFactor);
                mPreview.setScaleY(scaleFactor);
            } else {
                ViewHelper.setAlpha(mPreview, slideOffset);
                ViewHelper.setTranslationX(mPreview, mInitOffset * (1 - slideOffset));
                ViewHelper.setScaleX(mPreview, scaleFactor);
                ViewHelper.setScaleY(mPreview, scaleFactor);
            }
        } else {
            if (sdkInt >= Build.VERSION_CODES.HONEYCOMB) {
                mPreview.setScaleX(1);
                mPreview.setScaleY(1);
                mPreview.setAlpha(1);
                mPreview.setTranslationX(0);
            } else {
                ViewHelper.setScaleX(mPreview, 1);
                ViewHelper.setScaleY(mPreview, 1);
                ViewHelper.setAlpha(mPreview, 1);
                ViewHelper.setTranslationX(mPreview, 0);
            }
            finish();
            overridePendingTransition(0, 0);
        }
    }

    @Override
    public void onPanelOpened(View panel) {

    }

    @Override
    public void onPanelClosed(View panel) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /** recycle the bitmap */
        Drawable drawable = ((ImageView) mPreview).getDrawable();
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            bitmap.recycle();
            System.gc();
        }
    }
}
