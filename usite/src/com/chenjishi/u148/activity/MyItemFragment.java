package com.chenjishi.u148.activity;

import android.os.Bundle;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.model.User;

/**
 * Created by chenjishi on 13-12-21.
 */
public class MyItemFragment extends ItemFragment {
    private final static String USER_ARTICLE_LIST = "http://www.u148.net/u/%1$s/%2$d.html";
    private User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = PrefsUtil.getUser();
    }

    @Override
    protected String getUrl() {
        return String.format(USER_ARTICLE_LIST, user.id, currentPage);
    }
}
