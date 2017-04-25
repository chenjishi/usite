package com.chenjishi.u148.utils;

import android.text.TextUtils;

/**
 * Created by chenjishi on 14-8-28.
 */
public class QQAuthToken {

    public String access_token;

    public String open_id;

    public long expires_in;

    public boolean invalid() {
        return !TextUtils.isEmpty(access_token)
                && (System.currentTimeMillis() - expires_in < 0);
    }
}
