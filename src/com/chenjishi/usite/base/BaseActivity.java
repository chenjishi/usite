package com.chenjishi.usite.base;

import android.app.Activity;
import com.chenjishi.usite.util.ApiUtils;
import com.flurry.android.FlurryAgent;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-8
 * Time: 下午10:30
 * To change this template use File | Settings | File Templates.
 */
public class BaseActivity extends Activity {
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
