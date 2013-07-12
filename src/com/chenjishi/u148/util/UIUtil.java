package com.chenjishi.u148.util;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-4-9
 * Time: 下午1:09
 * To change this template use File | Settings | File Templates.
 */
public class UIUtil {
    public static void runWithoutMessage(final Runnable action, final Runnable postAction) {
        final Handler handler = new Handler() {
            public void handleMessage(Message message) {
                postAction.run();
            }
        };

        final Thread runner = new Thread(new Runnable() {
            public void run() {
                action.run();
                handler.sendEmptyMessage(0);
            }
        });
        runner.setPriority(Thread.MIN_PRIORITY);
        runner.start();
    }

    public static void showMsg(Context context, int resourceId){
        Toast.makeText(context, context.getResources().getString(resourceId), Toast.LENGTH_SHORT).show();
    }

    public static void showMsg(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
