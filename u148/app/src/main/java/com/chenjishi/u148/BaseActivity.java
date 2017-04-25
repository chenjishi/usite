package com.chenjishi.u148;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import com.chenjishi.u148.utils.Constants;
import com.chenjishi.u148.utils.Utils;
import com.chenjishi.u148.widget.LoadingView;
import com.flurry.android.FlurryAgent;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by jishichen on 2017/4/14.
 */
public class BaseActivity extends AppCompatActivity {
    protected boolean mHideTitle;
    protected int mTitleResId = -1;

    protected LayoutInflater mInflater;
    protected FrameLayout mRootView;

    protected int mTheme;

    protected boolean mSliding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRootView = (FrameLayout) findViewById(android.R.id.content);
        mRootView.setBackgroundColor(getResources().getColor(Constants.MODE_NIGHT == Config.getThemeMode(this)
                ? R.color.background_night : R.color.background));
        mInflater = LayoutInflater.from(this);
        setStatusViewColor(0xFFE5E5E5);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTheme = Config.getThemeMode(this);
        applyTheme();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        if (mSliding) {
            super.setContentView(R.layout.base_sliding_layout);
        } else {
            super.setContentView(R.layout.base_layout);

            if (!mHideTitle) {
                int resId = mTitleResId == -1 ? R.layout.title_base : mTitleResId;
                mInflater.inflate(resId, mRootView);
            }

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(MATCH_PARENT,
                    MATCH_PARENT, Gravity.BOTTOM);
            lp.topMargin = mHideTitle ? 0 : dp2px(48);
            mRootView.addView(mInflater.inflate(layoutResID, null), lp);
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

    protected void onRightIconClicked() {

    }

    public void onBackClicked(View v) {
        finish();
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

    protected void setRightButtonIcon(int resId) {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.right_view);
        layout.removeAllViews();

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(dp2px(48), MATCH_PARENT);
        ImageButton button = getImageButton(resId);
        button.setBackgroundResource(R.drawable.home_button_bkg);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRightIconClicked();
            }
        });
        layout.addView(button, lp);
    }

    protected ImageButton getImageButton(int resId) {
        ImageButton button = new ImageButton(this);
        button.setBackgroundResource(R.drawable.home_up_bg);
        button.setImageResource(resId);

        return button;
    }

    private LoadingView mLoadingView;

    protected void showLoadingView() {
        if (null == mLoadingView) {
            mLoadingView = new LoadingView(this);
            mLoadingView.setBackgroundColor(getResources().getColor(Constants.MODE_NIGHT == Config.getThemeMode(this)
                    ? R.color.background_night : R.color.background));
        }

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        lp.topMargin = dp2px(48);
        lp.gravity = Gravity.BOTTOM;

        ViewParent viewParent = mLoadingView.getParent();
        if (null != viewParent) {
            ((ViewGroup) viewParent).removeView(mLoadingView);
        }

        mRootView.addView(mLoadingView, lp);
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
                backBtn.setImageResource(R.mipmap.ic_back_night);
            } else {
                titleView.setBackgroundColor(0xFFE5E5E5);
                titleText.setTextColor(0xFF666666);
                divider.setBackgroundColor(0xFFCACACA);
                backBtn.setImageResource(R.mipmap.back_arrow);
            }
        }
    }

    protected int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    protected void setStatusViewColor(@ColorInt int color) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(color);
    }
}
