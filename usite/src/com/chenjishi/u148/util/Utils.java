package com.chenjishi.u148.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.App;
import com.chenjishi.u148.base.PrefsUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-18
 * Time: 上午11:06
 * To change this template use File | Settings | File Templates.
 */
public class Utils {
    public static int dp2px(Context context, float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (density * dp + .5f);
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
        if (TextUtils.isEmpty(s))
            return "";

        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static void setErrorView(View errorView, int resId) {
        setErrorView(errorView, App.getInstance().getString(resId));
    }

    public static void setErrorView(View errorView, String msg) {
        final TextView errorText = (TextView) errorView.findViewById(R.id.tv_empty_tip);
        errorView.findViewById(R.id.progress_bar).setVisibility(View.GONE);
        errorText.setText(msg);
        final int mode = PrefsUtil.getThemeMode();
        errorText.setTextColor(mode == Constants.MODE_NIGHT ? 0xFF999999: 0xFF333333);
        errorText.setVisibility(View.VISIBLE);
    }

    public static void showToast(String msg) {
        Toast.makeText(App.getInstance(), msg, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(int resId) {
        showToast(App.getInstance().getString(resId));
    }

    public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidht = ((float) w / width);
        float scaleHeight = ((float) h / height);
        matrix.postScale(scaleWidht, scaleHeight);
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height,
                matrix, true);
        return newbmp;
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
        if (null == context) return null;

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

    public static boolean isLogin() {
        return null != PrefsUtil.getUser();
    }

    public static <T> Collection<T> nullSafe(Collection<T> c) {
        return (null == c) ? Collections.<T>emptyList() : c;
    }

    public static Bitmap circleToBitmap(Bitmap bitmap) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setFilterBitmap(false);
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        int r = Math.min(w / 2, h / 2);
        Canvas canvas = new Canvas(bmp);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        paint.setXfermode(null);
        bitmap = null;

        return bmp;
    }
}
