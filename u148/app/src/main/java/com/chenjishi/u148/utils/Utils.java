package com.chenjishi.u148.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.cache.disk.FileCache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jishichen on 2017/4/14.
 */
public class Utils {

    private Utils() {
    }

    public static int dp2px(Context context, float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (density * dp + .5f);
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
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

    public static String getImageCacheSize(Context ctx) {
        String cachePath = FileUtils.getRootDir(ctx);
        return getCurrentCacheSize(cachePath);
    }

    private static String getCurrentCacheSize(String path) {
        StringBuilder sb = new StringBuilder(6);
        double size = getFileSize(new File(path));
        DecimalFormat df = new DecimalFormat();
        df.applyPattern("0.00");
        if (size < (1024 * 1024)) {
            sb.append(df.format(size / (1024))).append("KB");
        } else if (size < (1024 * 1024 * 1024)) {
            sb.append(df.format(size / (1024 * 1024))).append("MB");
        } else {
            sb.append(df.format(size / (1024 * 1024 * 1024))).append("GB");
        }
        return sb.toString();
    }


    public static long getFileSize(File f) {
        long size = 0;
        File flist[] = f.listFiles();
        if (flist == null) {
            return 0;
        }
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSize(flist[i]);
            } else {
                size = size + flist[i].length();
            }
        }
        return size;
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

    public static void clearCache(Context ctx) {
        String dataCacheDir = FileUtils.getDataCacheDir(ctx);
        File[] cachedFiles = new File(dataCacheDir).listFiles();
        if (cachedFiles == null) return;
        for (File f : cachedFiles) f.delete();
    }
}
