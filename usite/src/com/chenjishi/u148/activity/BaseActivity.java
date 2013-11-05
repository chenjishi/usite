package com.chenjishi.u148.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chenjishi.u148.R;
import com.chenjishi.u148.util.ApiUtils;
import com.flurry.android.FlurryAgent;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-8
 * Time: 下午10:30
 * To change this template use File | Settings | File Templates.
 */
public abstract class BaseActivity extends Activity {
    private TextView mTitleText;
    private ImageView mMenuIcon;
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

        mTitleText = (TextView) findViewById(R.id.tag_actionbar_title);
        mMenuIcon = (ImageView) findViewById(R.id.icon_menu);
        mHomeIcon = (LinearLayout) findViewById(R.id.home_up);
        mMenuIcon2 = (ImageView) findViewById(R.id.icon_menu2);
        mHomeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backIconClicked();
            }
        });

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
    }

    protected  void setMenuIconVisibility(boolean b) {
        mMenuIcon.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    protected void setActionBarHide(boolean b) {
        findViewById(R.id.view_action_bar).setVisibility(b ? View.GONE : View.VISIBLE);
    }

    protected void setMenuIcon2Visibility(boolean b) {
        mMenuIcon2.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    protected void setMenuIcon(int resId) {
        mMenuIcon.setImageResource(resId);
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
        FlurryAgent.onStartSession(this, ApiUtils.CODE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FlurryAgent.onEndSession(this);
    }
}
