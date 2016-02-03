package com.chenjishi.u148.view;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * Created by chenjishi on 14-1-11.
 */
public class ArticleWebView extends WebView {

    public ArticleWebView(Context context, AttributeSet attrs) {
        super(context, attrs);

        /**
         *  fix webview white background bug
         */
        setBackgroundColor(0x00000000);
        setHorizontalScrollBarEnabled(false);
        setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (canvas.isHardwareAccelerated()) {
                setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
        }
        super.onDraw(canvas);
    }
}
