package com.chenjishi.u148.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import com.chenjishi.u148.base.AppApplication;
import com.chenjishi.u148.base.FileCache;

import java.io.*;
import java.text.DecimalFormat;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-18
 * Time: 下午4:58
 * To change this template use File | Settings | File Templates.
 */
public class FileUtils {

    public static Object unserializeObject(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        ObjectInputStream ois = null;
        Object o = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(path));
            o = ois.readObject();
        } catch (Exception e) {
            file.delete();
            return null;
        } finally {
            if (null != ois) {
                try {
                    ois.close();
                } catch (IOException e) {
                }
            }
        }
        return o;
    }

    public static boolean serializeObject(String path, Object o) {
        ObjectOutputStream oos = null;

        try {
            oos = new ObjectOutputStream(new FileOutputStream(path));
            oos.writeObject(o);
        } catch (IOException e) {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                file.delete();
            }
            return false;
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
            } catch (IOException e) {
                return false;
            }
        }

        return true;
    }

    public static void deleteFile(String path) {
        if (TextUtils.isEmpty(path)) return;

        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
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

    public static String getImageCacheSize() {
        String cachePath = FileCache.getImageCacheDirectory(AppApplication.getInstance());
        return getCurrentCacheSize(cachePath);
    }

    private static String getCurrentCacheSize(String path) {
        StringBuilder sb=new StringBuilder(6);
        double size=getFileSize(new File(path));
        DecimalFormat df = new DecimalFormat();
        df.applyPattern( "0.00");
        if(size<(1024*1024)){
            sb.append(df.format(size/(1024))).append("KB");
        }else if(size<(1024*1024*1024)){
            sb.append(df.format(size/(1024*1024))).append("MB");
        }else{
            sb.append(df.format(size/(1024*1024*1024))).append("GB");
        }
        return sb.toString();
    }

    public static void clearCache() {
        String imageCachePath = FileCache.getImageCacheDirectory(AppApplication.getInstance());

        File[] cachedFiles = new File(imageCachePath).listFiles();
        if (cachedFiles == null) {
            return;
        }
        for (File f : cachedFiles) {
            f.delete();
        }
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

    public static void writeToFile(String fileName, String data) {
        BufferedWriter writer = null;

        try {
            File outFile = new File(fileName);

            writer = new BufferedWriter(new FileWriter(outFile));
            writer.write(data);
        } catch (IOException e) {

        } finally {
            try {
                if (null != writer) {
                    writer.close();
                }
            } catch (IOException e) {
            }
        }
    }

    public static String readFromFile(String filePath) {
        File inFile = new File(filePath);
        if (!inFile.exists())
            return null;

        StringBuilder sb = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(inFile));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
        } catch (IOException e) {
        }

        return sb.toString();
    }
}
