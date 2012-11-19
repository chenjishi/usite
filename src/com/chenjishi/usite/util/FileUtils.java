package com.chenjishi.usite.util;

import android.text.TextUtils;

import java.io.*;

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
}
