package com.chenjishi.u148.service;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Process;
import com.chenjishi.u148.base.App;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-11-4
 * Time: 下午10:54
 * To change this template use File | Settings | File Templates.
 */
public class DownloadAPKThread extends Thread {
    private String mDownloadURL;
    private String mDirectory;
    private String mFileName;
    private String mTempAPKFilePath;
    private int mBytesSoFar = 0;
    private int mTotalBytes;
    private long mTimeLastNotification;
    public static final long MIN_PROGRESS_TIME = 1500;
    private DownloadNotification mNotifier;
    private Context mContext;

    public DownloadAPKThread(String downloadURL, String directory, String fileName) {
        this.mContext = App.getInstance().getApplicationContext();
        this.mDownloadURL = downloadURL;
        this.mDirectory = directory;
        this.mFileName = fileName;
        mTempAPKFilePath = directory + System.currentTimeMillis();
    }

    public FileOutputStream getTempFileOutputStream() throws IOException {
        File cacheFile = new File(mTempAPKFilePath);
        if (!cacheFile.exists()) {
            cacheFile.createNewFile();
        }
        return new FileOutputStream(cacheFile);
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        FileOutputStream fos=null;
        InputStream is=null;
        try {
            reportProgress();

            HttpURLConnection conn = (HttpURLConnection) new URL(mDownloadURL).openConnection();
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");

            mTotalBytes = conn.getContentLength();
            is = conn.getInputStream();
            fos = getTempFileOutputStream();
            byte[] buff = new byte[4096];
            int n = -1;
            while ((n = is.read(buff)) != -1) {
                fos.write(buff, 0, n);
                mBytesSoFar = mBytesSoFar + n;
                reportProgress();
            }
            reportComplete();

        } catch (Exception e) {
            deleteTempFile();
            reportComplete();
            return;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }

        File tempFile = new File(mTempAPKFilePath);
        File apkFile = new File(mDirectory + mFileName);
        if (tempFile.exists()) {
            if (tempFile.renameTo(apkFile)) {
                installApk(apkFile, mContext);
            }
        }
    }

    private void reportProgress() {
        if (mNotifier == null) {
            mNotifier = new DownloadNotification(mContext);
        }
        long now = System.currentTimeMillis();
        if (now - mTimeLastNotification > MIN_PROGRESS_TIME) {
            mTimeLastNotification = now;
            mNotifier.updateNotification(mBytesSoFar, mTotalBytes);
        }
    }

    private void reportComplete(){
        if (mNotifier == null) {
            mNotifier = new DownloadNotification(mContext);
        }
        mNotifier.removeNotification();
    }

    private void deleteTempFile() {
        File cacheFile = new File(mTempAPKFilePath);
        if (cacheFile.exists()) {
            cacheFile.delete();
        }
    }

    private void installApk(File apkFile, Context context) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}
