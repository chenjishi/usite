package com.chenjishi.u148.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-18
 * Time: 上午9:40
 * To change this template use File | Settings | File Templates.
 */
public class CustomDialog extends Dialog {
    public CustomDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    public void show() {
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();

        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.width = (int) (d.getWidth() * 0.8);

        getWindow().setAttributes(p);
        super.show();
    }
}
