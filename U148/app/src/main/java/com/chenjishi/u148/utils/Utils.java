package com.chenjishi.u148.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jishichen on 2017/4/14.
 */
public class Utils {

    private Utils() {
    }

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    public static int generateViewId() {
        for (; ; ) {
            final int result = sNextGeneratedId.get();
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF)
                newValue = 1;
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
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

    public static void showToast(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context ctx, int resId) {
        showToast(ctx, ctx.getString(resId));
    }

    public static boolean isNetworkConnected(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null != cm) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (null != info) {
                return info.isConnected();
            }
        }

        return false;
    }

    public static void deleteFile(String path) {
        if (TextUtils.isEmpty(path)) return;

        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    //delete all files include directory
    public static boolean delete(File dir) {
        if (dir.isDirectory()) {
            File[] listFiles = dir.listFiles();
            int i = 0;
            while (i < listFiles.length && delete(listFiles[i])) {
                i++;
            }
        }
        return dir.delete();
    }

    public static TextView generateTextView(Context context, int resId, int color, float size) {
        return generateTextView(context, context.getString(resId), color, size);
    }

    public static TextView generateTextView(Context context, String text, int color, float size) {
        TextView view = new TextView(context);
        view.setTextColor(color);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        view.setText(text);

        return view;
    }
}
