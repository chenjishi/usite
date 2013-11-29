package com.chenjishi.u148.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import com.chenjishi.u148.base.AppApplication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-18
 * Time: 上午11:06
 * To change this template use File | Settings | File Templates.
 */
public class CommonUtil {
    public static void showToast(String msg) {
        Toast.makeText(AppApplication.getInstance(), msg, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(int resId) {
        showToast(AppApplication.getInstance().getString(resId));
    }

    public static synchronized boolean didNetworkConnected(Context context) {
        ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null != conn) {
            NetworkInfo info = conn.getActiveNetworkInfo();
            if (null != info) return info.isConnected();
        }
        return false;
    }

    public static synchronized boolean isWifiConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager != null) {
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                int networkInfoType = networkInfo.getType();
                if (networkInfoType == ConnectivityManager.TYPE_WIFI || networkInfoType == ConnectivityManager.TYPE_ETHERNET) {
                    return networkInfo.isConnected();
                }
            }
        }
        return false;
    }

    public static String getVersionName(Context context) {
        String versionName = "";
        Context appContext = context.getApplicationContext();

        try {
            PackageManager pm = appContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(appContext.getPackageName(), 0);
            versionName = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionName;
    }

    public static int getVersionCode(Context context) {
        int versionCode = 0;
        Context appContext = context.getApplicationContext();

        try {
            PackageManager pm = appContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(appContext.getPackageName(), 0);
            versionCode = pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionCode;
    }

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

    public static String readFromAssets(Context context, String name) {
        InputStream is;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            is = context.getAssets().open(name);
            byte buf[] = new byte[1024];
            int len;
            while ((len = is.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
            baos.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toString();
    }
}
