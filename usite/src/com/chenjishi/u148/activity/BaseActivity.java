package com.chenjishi.u148.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.*;
import com.chenjishi.u148.R;
import com.chenjishi.u148.util.ConstantUtils;
import com.flurry.android.FlurryAgent;
import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;

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
        rootView.setOrientation(LinearLayout.VERTICAL);
        rootView.setBackgroundColor(0xFFE6E6E6);

        LayoutInflater.from(this).inflate(R.layout.action_bar_layout, rootView);

        View contentView = LayoutInflater.from(this).inflate(getLayoutId(), null);
        rootView.addView(contentView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                0, 1.0f));

        AdView adView = new AdView(this, AdSize.FIT_SCREEN);
        rootView.addView(adView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        ((FrameLayout) findViewById(android.R.id.content)).addView(rootView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

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

    protected void setTitleText2(String s) {
        TextView textView = (TextView) findViewById(R.id.title);
        textView.setText(s);
        textView.setVisibility(View.VISIBLE);
    }

    protected void setTitleText2(int resId) {
        setTitleText2(getString(resId));
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
