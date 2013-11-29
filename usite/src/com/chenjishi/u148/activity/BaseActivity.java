package com.chenjishi.u148.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chenjishi.u148.R;
import com.chenjishi.u148.util.ConstantUtils;
import com.flurry.android.FlurryAgent;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-8
 * Time: 下午10:30
 * To change this template use File | Settings | File Templates.
 */
public abstract class BaseActivity extends FragmentActivity {
    private TextView mTitleText;
    private ImageView mMenuIcon2;
    private LinearLayout mHomeIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout rootView = new LinearLayout(this);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        rootView.setOrientation(LinearLayout.VERTICAL);

        View contentView = LayoutInflater.from(this).inflate(getLayoutId(), null);
        LayoutInflater.from(this).inflate(R.layout.action_bar_layout, rootView);
        rootView.addView(contentView, layoutParams);

        ((FrameLayout) findViewById(android.R.id.content)).addView(rootView, layoutParams);

        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = metrics.widthPixels;
        lp.height = metrics.heightPixels - statusBarHeight;
        lp.gravity = Gravity.BOTTOM;
        getWindow().setAttributes(lp);

        mTitleText = (TextView) findViewById(R.id.tag_actionbar_title);
        mHomeIcon = (LinearLayout) findViewById(R.id.home_up);
        mMenuIcon2 = (ImageView) findViewById(R.id.icon_menu2);
        mHomeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backIconClicked();
            }
        });
    }

    protected void setActionBarHide(boolean b) {
        findViewById(R.id.view_action_bar).setVisibility(b ? View.GONE : View.VISIBLE);
    }

    protected void setMenuIcon2Visibility(boolean b) {
        mMenuIcon2.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    protected void setMenuIcon3Visibility(boolean b) {
        findViewById(R.id.content_share).setVisibility(b ? View.VISIBLE : View.GONE);
    }

    protected void setTitleText(String s) {
        mTitleText.setText(s);
    }

    protected void setTitleText(int res) {
        mTitleText.setText(getString(res));
    }

    protected abstract int getLayoutId();

    protected abstract void backIconClicked();

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, ConstantUtils.FLURRY_APP_ID);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FlurryAgent.onEndSession(this);
    }
}
