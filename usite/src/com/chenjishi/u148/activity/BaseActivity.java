package com.chenjishi.u148.activity;

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
public class BaseActivity extends FragmentActivity implements SlidingLayout.PanelSlideListener {
    private FrameLayout rootView;
    private SlidingLayout slidingPane;

    private boolean hideTitle = false;
    private int titleResId = -1;
    protected int theme;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.base_layout);

        rootView = (FrameLayout) findViewById(R.id.content_view);
        rootView.setBackgroundColor(getResources().getColor(Constants.MODE_NIGHT == PrefsUtil.getThemeMode()
                ? R.color.background_night : R.color.background));

        if (!hideTitle) {
            int resId = -1 == titleResId ? R.layout.base_title_layout : titleResId;
            LayoutInflater.from(this).inflate(resId, rootView);
        }

        View contentView = LayoutInflater.from(this).inflate(layoutResID, null);
        contentView.setId(R.id.content_layout);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, Gravity.BOTTOM);
        int marginTop = hideTitle ? 0 : (int) getResources().getDimension(R.dimen.action_bar_height);
        lp.setMargins(0, marginTop, 0, 0);
        rootView.addView(contentView, lp);

        slidingPane = (SlidingLayout) findViewById(R.id.slide_panel);
        slidingPane.setShadowResource(R.drawable.sliding_back_shadow);
        slidingPane.setSliderFadeColor(0x00000000);
        slidingPane.setPanelSlideListener(this);
    }

    protected void setContentView(int layoutResID, int titleResId) {
        this.titleResId = titleResId;
        setContentView(layoutResID);
    }

    protected void setContentView(int layoutResID, boolean hideTitle) {
        this.hideTitle = hideTitle;
        setContentView(layoutResID);
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
        if (!hideTitle) {
            final RelativeLayout titleView = (RelativeLayout) findViewById(R.id.title_bar);
            final TextView titleText = (TextView) findViewById(R.id.tv_title);
            final View divider = findViewById(R.id.split_h);
            final ImageView backBtn = (ImageView) findViewById(R.id.ic_arrow);

            if (Constants.MODE_NIGHT == theme) {
                titleView.setBackgroundColor(0xFF1C1C1C);
                titleText.setTextColor(0xFF999999);
                divider.setBackgroundColor(0xFF303030);
                backBtn.setImageResource(R.drawable.ic_back_night);
            } else {
                titleView.setBackgroundColor(0xFFE5E5E5);
                titleText.setTextColor(0xFF666666);
                divider.setBackgroundColor(0xFFCACACA);
                backBtn.setImageResource(R.drawable.ic_back);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        theme = PrefsUtil.getThemeMode();
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
}
