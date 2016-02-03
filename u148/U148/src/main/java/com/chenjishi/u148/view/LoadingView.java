package com.chenjishi.u148.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.chenjishi.u148.R;

/**
 * Created by chenjishi on 14/11/12.
 */
public class LoadingView extends FrameLayout {

	public TextView textView;
	
    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        Context context = getContext();

        LayoutInflater.from(context).inflate(R.layout.empty_view, this);
    }

    public void setError(String text) {
        findViewById(R.id.progress_bar).setVisibility(GONE);
        textView = (TextView) findViewById(R.id.tv_empty_tip);
        textView.setText(text);
        textView.setVisibility(VISIBLE);
    }
}
