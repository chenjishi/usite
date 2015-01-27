package com.chenjishi.u148.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.util.Constants;
import com.flurry.android.FlurryAgent;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-8
 * Time: 下午10:30
 * To change this template use File | Settings | File Templates.
 */
public class BaseActivity extends FragmentActivity {

    protected boolean mHideTitle;
    protected int mTitleResId;

    protected float mDensity;
    protected LayoutInflater mInflater;

    protected int mTheme;

    protected boolean mSliding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDensity = getResources().getDisplayMetrics().density;
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @SuppressLint("NewApi")
    @Override
    public void setContentView(int layoutResID) {
        if (mSliding) {
            super.setContentView(layoutResID);
        } else {
            super.setContentView(R.layout.base_layout);

            FrameLayout rootView = (FrameLayout) findViewById(android.R.id.content);
            rootView.setBackgroundColor(getResources().getColor(Constants.MODE_NIGHT == PrefsUtil.getThemeMode()
                    ? R.color.background_night : R.color.background));

            if (!mHideTitle) {
                int resId = mTitleResId == 0 ? R.layout.base_title_layout : mTitleResId;
                mInflater.inflate(resId, rootView);
            }

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT, Gravity.BOTTOM);
            final int marginTop = mHideTitle ? 0 : dp2px(48);
            layoutParams.setMargins(0, marginTop, 0, 0);
            rootView.addView(mInflater.inflate(layoutResID, null), layoutParams);
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

    public void onBackClicked(View v) {
        finish();
    }

    protected void setRightButtonIcon(int resId) {
        final ImageButton button = (ImageButton) findViewById(R.id.btn_right);
        button.setImageResource(resId);
        button.setVisibility(View.VISIBLE);
    }

    @Override
    public void setTitle(CharSequence title) {
        final TextView textView = (TextView) findViewById(R.id.tv_title);
        textView.setText(title);
        textView.setVisibility(View.VISIBLE);
    }

    @Override
    public void setTitle(int titleId) {
        setTitle(getString(titleId));
    }

    protected void applyTheme() {
        if (!mHideTitle) {
            final RelativeLayout titleView = (RelativeLayout) findViewById(R.id.title_bar);
            final TextView titleText = (TextView) findViewById(R.id.tv_title);
            final View divider = findViewById(R.id.split_h);
            final ImageView backBtn = (ImageView) findViewById(R.id.ic_arrow);

            if (Constants.MODE_NIGHT == mTheme) {
                titleView.setBackgroundColor(0xFF1C1C1C);
                titleText.setTextColor(0xFF999999);
                divider.setBackgroundColor(0xFF303030);
                backBtn.setImageResource(R.drawable.ic_back_night);
            } else {
                titleView.setBackgroundColor(0xFFE5E5E5);
                titleText.setTextColor(0xFF666666);
                divider.setBackgroundColor(0xFFCACACA);
                backBtn.setImageResource(R.drawable.ic_back_to);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTheme = PrefsUtil.getThemeMode();
        applyTheme();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, "YYHS4STVXPMH6Y9GJ8WD");
    }

    @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    protected int dp2px(int d) {
        return (int) (d * mDensity + .5f);
    }
}
