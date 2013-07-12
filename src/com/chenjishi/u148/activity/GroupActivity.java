package com.chenjishi.u148.activity;

import android.os.Bundle;
import com.chenjishi.u148.R;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-17
 * Time: 下午11:21
 * To change this template use File | Settings | File Templates.
 */
public class GroupActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected int getLayoutId() {
        return R.layout.group;
    }

    @Override
    protected void backIconClicked() {
        finish();
    }
}
