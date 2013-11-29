package com.chenjishi.u148.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.chenjishi.u148.base.DatabaseHelper;
import com.chenjishi.u148.base.FileCache;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.entity.Video;
import com.chenjishi.u148.util.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-11-25
 * Time: 下午4:03
 * To change this template use File | Settings | File Templates.
 */
public class DownloadService extends Service {
    private static final long DAY = 24 * 60 * 60 * 1000;

    private static final int SOCKET_READ_TIMEOUT = 5000;
    private static final int SOCKET_CONNECT_TIMEOUT = 5000;
    private static final int BUFFER_SIZE = 20 * 1024;

    private DatabaseHelper dataBase;

    private ArrayList<Video> videoList;
    private Video currentVideo;
    private boolean downloading = false;

    private boolean stopFlag;

    @Override
    public void onCreate() {
        super.onCreate();
        dataBase = DatabaseHelper.getInstance(this);
        stopFlag = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (CommonUtil.isWifiConnected(this) && !downloading) {
            new Thread() {
                @Override
                public void run() {
                    clearInvalidFile();
                    checkVideoUpdate();
                }
            }.start();
        }

        return START_STICKY;
    }

    private void clearInvalidFile() {
        String videoCachePath = FileCache.getVideoDirectory(this);
        File[] flists = new File(videoCachePath).listFiles();

        if (null == flists || flists.length == 0) return;

        //delete files not in database
        for (File f : flists) {
            String name = f.getName();
            if (!StringUtil.isEmpty(name)) {
                boolean isExistInDB = DatabaseHelper.getInstance(this).isExist(name,
                        DatabaseHelper.TB_NAME_VIDEOS);
                if (!isExistInDB) f.delete();
            }
        }

        //delete invalid database video
        ArrayList<Video> savedVideos = dataBase.loadAll(DatabaseHelper.TB_NAME_VIDEOS);
        if (null != savedVideos && savedVideos.size() > 0) {
            for (Video video : savedVideos) {
                String filePath = video.localPath;
                File file = new File(filePath);
                if (!file.exists()) dataBase.deleteVideo(video.id, DatabaseHelper.TB_NAME_VIDEOS);
            }
        }
    }

    private void checkVideoUpdate() {
        videoList = dataBase.loadAll(DatabaseHelper.TB_NAME_LINKS);
        for (Video v : videoList)
            Log.i("test", "id " + v.id );
        if (null == videoList || videoList.size() == 0) {
            videoList = YouKuParser.parseVideoList();
            Log.i("test", "size " + videoList.size());
            if (null != videoList && videoList.size() > 0) {
                HashMap<String, String> historyIds = dataBase.loadHistoryIds();
                ArrayList<Video> tmpList = new ArrayList<Video>();

                if (null != historyIds && historyIds.size() > 0) {
                    for (Video v : videoList) {
                        String key = v.id;
                        String value = historyIds.get(key);

                        if (!StringUtil.isEmpty(value)) tmpList.add(v);
                    }
                }

                if (tmpList.size() > 0) videoList.removeAll(tmpList);

                if (videoList.size() == 0) {
                    stopSelf();
                } else {
                    dataBase.insertAll(videoList, DatabaseHelper.TB_NAME_LINKS);
                }
            } else {
                stopSelf();
            }
        }

        if (null != videoList && videoList.size() > 0) {
            long lastTime = PrefsUtil.getLongPreferences(PrefsUtil.KEY_VIDEO_UPDATE_TIME);
            if (System.currentTimeMillis() >= lastTime) {
                ArrayList<Video> downloadList = dataBase.loadAll(DatabaseHelper.TB_NAME_VIDEOS);
                if (null != downloadList && downloadList.size() > 0) {
                    for (Video video : downloadList) {
                        dataBase.deleteVideo(video.id, DatabaseHelper.TB_NAME_VIDEOS);
                        FileUtils.deleteFile(video.localPath);
                        Log.i("test", "deleted " + video.localPath);
                    }
                }
                PrefsUtil.saveLongPreference(PrefsUtil.KEY_VIDEO_UPDATE_TIME, System.currentTimeMillis() + DAY);
            }

            initTask();
        }
    }

    private void initTask() {
        long size = FileUtils.getCachedVideoSize(this);
        if (size < 200 * 1024 * 1024) {
            videoList.clear();
            videoList = dataBase.loadAll(DatabaseHelper.TB_NAME_LINKS);
            if (null != videoList && videoList.size() > 0) {
                startDownload();
            } else {
                stopSelf();
            }
        } else {
            stopSelf();
        }
    }

    private void startDownload() {
        Log.i("test", "startDownload");
        downloading = true;
        currentVideo = getNext();
        while (null != currentVideo && !stopFlag) {
            String fileName = FileCache.getVideoDirectory(this) + currentVideo.id;
            File file = new File(fileName);

            try {
                ArrayList<String> videoList;
                videoList = VideoUrlParser.parserYoukuFlv(currentVideo.id);
                if (null != videoList && videoList.size() > 0) {
                    currentVideo.url = videoList.get(0);
                }
            } catch (Exception e) {
            }

            boolean state = downloadFile(currentVideo.url, file);
            if (state) {
                currentVideo.localPath = fileName;

                dataBase.insert(currentVideo, DatabaseHelper.TB_NAME_VIDEOS);
                dataBase.deleteVideo(currentVideo.id, DatabaseHelper.TB_NAME_LINKS);
                dataBase.insertVideoId(currentVideo.id);
                sendStatusBroadcast(ConstantUtils.MSG_DOWNLOAD_SUCCESS);
            } else {
                dataBase.deleteVideo(currentVideo.id, DatabaseHelper.TB_NAME_LINKS);
            }
            videoList.remove(0);
            currentVideo = getNext();
        }

        if (null == currentVideo) {
            stopSelf();
        }
    }

    private boolean downloadFile(String videoUrl, File file) {
        if (StringUtil.isEmpty(videoUrl)) return false;

        HttpURLConnection conn = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            URL url = new URL(videoUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(SOCKET_CONNECT_TIMEOUT);
            conn.setReadTimeout(SOCKET_READ_TIMEOUT);
            conn.connect();

            bis = new BufferedInputStream(conn.getInputStream());
            bos = new BufferedOutputStream(new FileOutputStream(file));

            byte buf[] = new byte[BUFFER_SIZE];
            int len;
            while ((len = bis.read(buf)) != -1) bos.write(buf, 0, len);
        } catch (IOException e) {
            if (file.exists()) file.delete();
            return false;
        } finally {
            try {
                if (null != bis) bis.close();
                if (null != bos) bos.close();
                if (null != conn) conn.disconnect();
            } catch (IOException e) {
            }
        }
        return true;
    }

    private Video getNext() {
        if (null != videoList && videoList.size() > 0) {
            return videoList.get(0);
        } else {
            return null;
        }
    }

    private void sendStatusBroadcast(int code) {
        Intent intent = new Intent(ConstantUtils.DOWNLOAD_STATUS_ACTION);
        intent.putExtra(ConstantUtils.KEY_VIDEO_SIZE, null == videoList ? 0 : videoList.size());
        intent.putExtra(ConstantUtils.KEY_MESSAGE_TYPE, code);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        Log.i("test", "onDestroy");
        super.onDestroy();
        stopFlag = true;
        downloading = false;
    }
}
