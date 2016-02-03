package com.chenjishi.u148.util;

import android.text.TextUtils;
import android.webkit.JavascriptInterface;

/**
 * Created by chenjishi on 14-4-25.
 */
public class JavaScriptBridge {
    private JSCallback mCallback;

    public JavaScriptBridge(JSCallback callback) {
        mCallback = callback;
    }

    @JavascriptInterface
    public void initTheme() {
        if (null == mCallback) return;

        mCallback.onThemeChange();
    }

    @JavascriptInterface
    public void onImageClick(String url) {
        if (TextUtils.isEmpty(url) || null == mCallback) return;

        mCallback.onImageClicked(url);
    }

    @JavascriptInterface
    public void onVideoClick(String url) {
        if (TextUtils.isEmpty(url) || null == mCallback) return;

        if (url.contains("xiami")) {
            mCallback.onMusicClicked(url);
        } else {
            mCallback.onVideoClicked(url);
        }
    }
}
