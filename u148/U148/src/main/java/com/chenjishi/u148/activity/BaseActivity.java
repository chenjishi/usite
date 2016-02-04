package com.chenjishi.u148.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.util.Constants;
import com.chenjishi.u148.util.Utils;
import com.chenjishi.u148.view.LoadingView;
import com.flurry.android.FlurryAgent;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-8
 * Time: 下午10:30
 * To change this template use File | Settings | File Templates.
 */
public class BaseActivity extends FragmentActivity {

    protected boolean mHideTitle;
    protected int mTitleResId = -1;

    protected LayoutInflater mInflater;
    protected FrameLayout mRootContentView;

    protected int mTheme;

    protected boolean mSliding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRootContentView = (FrameLayout) findViewById(android.R.id.content);
        mInflater = LayoutInflater.from(this);
    }

    @SuppressLint("NewApi")
    @Override
    public void setContentView(int layoutResID) {
        if (mSliding) {
            super.setContentView(R.layout.base_sliding_layout);
        } else {
            super.setContentView(R.layout.base_layout);

            mRootContentView.setBackgroundColor(getResources().getColor(Constants.MODE_NIGHT == PrefsUtil.getThemeMode()
                    ? R.color.background_night : R.color.background));

            if (!mHideTitle) {
                int resId = mTitleResId == -1 ? R.layout.base_title_layout : mTitleResId;
                mInflater.inflate(resId, mRootContentView);
            }

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(MATCH_PARENT,
                    MATCH_PARENT, Gravity.BOTTOM);
            lp.topMargin = mHideTitle ? 0 : dp2px(48);
            mRootContentView.addView(mInflater.inflate(layoutResID, null), lp);
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

    public void onRightButtonClicked(View v) {

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

    private LoadingView mLoadingView;

    protected void showLoadingView() {
        if (null == mLoadingView) {
            mLoadingView = new LoadingView(this);
            mLoadingView.setBackgroundColor(getResources().getColor(Constants.MODE_NIGHT == PrefsUtil.getThemeMode()
                    ? R.color.background_night : R.color.background));
        }

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        lp.topMargin = dp2px(48);
        lp.gravity = Gravity.BOTTOM;

        ViewParent viewParent = mLoadingView.getParent();
        if (null != viewParent) {
            ((ViewGroup) viewParent).removeView(mLoadingView);
        }

        mRootContentView.addView(mLoadingView, lp);
    }

    protected void hideLoadingView() {
        if (null != mLoadingView) {
            ViewParent viewParent = mLoadingView.getParent();
            if (null != viewParent) {
                ((ViewGroup) viewParent).removeView(mLoadingView);
            }
            mLoadingView = null;
        }
    }

    protected void setError(String tips) {
        if (null != mLoadingView) {
            mLoadingView.setError(tips);
        }
    }

    protected void setError() {
        if (null != mLoadingView) {
            int resId = R.string.network_invalid;
            if (Utils.isNetworkConnected(this)) {
                resId = R.string.server_error;
            }

            mLoadingView.setError(getString(resId));
        }
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
                backBtn.setImageResource(R.drawable.back_arrow);
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
        FlurryAgent.onStartSession(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    protected int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
