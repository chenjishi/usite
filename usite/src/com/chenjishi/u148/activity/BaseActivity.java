package com.chenjishi.u148.activity;

import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.chenjishi.u148.R;
import com.chenjishi.u148.util.Constants;
import com.flurry.android.FlurryAgent;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-8
 * Time: 下午10:30
 * To change this template use File | Settings | File Templates.
 */
public class BaseActivity extends FragmentActivity implements SlidingLayout.PanelSlideListener {
    private ImageView mMenuIcon2;
    private FrameLayout rootView;
    private SlidingLayout slidingPane;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.base_layout);

        rootView = (FrameLayout) findViewById(R.id.content_view);
        rootView.setBackgroundColor(0xFFE6E6E6);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, Gravity.BOTTOM);
        lp.setMargins(0, (int) getResources().getDimension(R.dimen.action_bar_height), 0, 0);
        View contentView = LayoutInflater.from(this).inflate(layoutResID, null);
        contentView.setId(R.id.content_layout);
        rootView.addView(contentView, lp);

        slidingPane = (SlidingLayout) findViewById(R.id.slide_panel);
        slidingPane.setShadowResource(R.drawable.sliding_back_shadow);
        slidingPane.setSliderFadeColor(0x00000000);
        slidingPane.setPanelSlideListener(this);

        mMenuIcon2 = (ImageView) findViewById(R.id.icon_menu2);
    }

    protected void setTitleVisible(boolean b) {
        RelativeLayout titleBar = (RelativeLayout) findViewById(R.id.view_action_bar);

        if (null != titleBar) {
            int marginTop = b ? (int) getResources().getDimension(R.dimen.action_bar_height) : 0;
            titleBar.setVisibility(b ? View.VISIBLE : View.GONE);

            View contentView = rootView.findViewById(R.id.content_layout);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT, Gravity.BOTTOM);
            lp.setMargins(0, marginTop, 0, 0);
            contentView.setLayoutParams(lp);
        }

        if (null != titleBar) titleBar.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    protected void slideDisable(boolean b) {
        slidingPane.disableSlide(b);
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
}
