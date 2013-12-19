package com.chenjishi.u148.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.chenjishi.u148.R;
import com.chenjishi.u148.util.Constants;
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
public abstract class BaseActivity extends FragmentActivity implements SlidingPaneLayout.PanelSlideListener {
    private ImageView mMenuIcon2;
    private SlidingPaneLayout slidingPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_layout);

        LinearLayout contentView = (LinearLayout) findViewById(R.id.content_view);
        LayoutInflater.from(this).inflate(getLayoutId(), contentView, true);

        AdView adView = new AdView(this, AdSize.FIT_SCREEN);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        ((LinearLayout) findViewById(R.id.ad_view)).addView(adView, layoutParams);

        slidingPane = (SlidingPaneLayout) findViewById(R.id.slide_panel);
        slidingPane.setShadowResource(R.drawable.sliding_back_shadow);
        slidingPane.setSliderFadeColor(0x00000000);
        slidingPane.setPanelSlideListener(this);

        mMenuIcon2 = (ImageView) findViewById(R.id.icon_menu2);
    }

    @Override
    public void onPanelSlide(View view, float v) {
        if (v >= 0.9) finish();
    }

    @Override
    public void onPanelOpened(View view) {

    }

    @Override
    public void onPanelClosed(View view) {

    }

    protected void setActionBarHide(boolean b) {
        findViewById(R.id.view_action_bar).setVisibility(b ? View.GONE : View.VISIBLE);
    }

    protected void setMenuIcon2Visibility(boolean b) {

        if (b) {
            mMenuIcon2.setVisibility(View.VISIBLE);
        } else {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            findViewById(R.id.content_share).setLayoutParams(layoutParams);
            mMenuIcon2.setVisibility(View.GONE);
        }
        mMenuIcon2.setVisibility(b ? View.VISIBLE : View.INVISIBLE);
    }

    protected void setMenuIcon3Visibility(boolean b) {
        findViewById(R.id.content_share).setVisibility(b ? View.VISIBLE : View.GONE);
    }

    protected void setTitleText(String s) {
        TextView textView = (TextView) findViewById(R.id.title);
        textView.setText(s);
        textView.setVisibility(View.VISIBLE);
    }

    protected void setTitleText(int resId) {
        setTitleText(getString(resId));
    }

    protected abstract int getLayoutId();

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, Constants.FLURRY_APP_ID);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FlurryAgent.onEndSession(this);
    }
}
