package com.chenjishi.u148.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;
import com.chenjishi.u148.R;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-10-27
 * Time: 下午5:39
 * To change this template use File | Settings | File Templates.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    private static final int NOTIFY_ID = 10010;
    private static final String NOTIFICATION_CLOSE_ACTION = "NotificationCloseBroadCast";
    private MediaPlayer mPlayer;
    private String mUrl;

    private String songName;
    private String artistName;
    private String coverUrl;

    private String mCurrentUrl;

    private MusicPlayListener mListener;

    private final MusicBinder mBinder = new MusicBinder();

    private RemoteViews mRemoteViews;
    private NotificationManager mNotificationMgr;
    private NotificationCloseBroadCast mBroadCast;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mRemoteViews = new RemoteViews(getPackageName(), R.layout.notification_view);
        mNotificationMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mPlayer = new MediaPlayer();
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mBroadCast = new NotificationCloseBroadCast();
        registerReceiver(mBroadCast, new IntentFilter(NOTIFICATION_CLOSE_ACTION));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null == intent) return START_STICKY;

        final Bundle bundle = intent.getExtras();
        if (null == bundle) return START_STICKY;

        final String url = bundle.getString("url");
        if (TextUtils.isEmpty(url)) return START_STICKY;

        if (!url.equals(mCurrentUrl)) {
            mCurrentUrl = url;
            if (null != mPlayer) {
                mPlayer.reset();
            }
            if (null != mListener) mListener.onMusicStartParse();
            parseMp3Url(url);
        } else {
            togglePlayer();
        }

        return START_STICKY;
    }

    public void stopMusic() {
        if (null != mPlayer && mPlayer.isPlaying()) {
            mPlayer.stop();
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mPlayer;
    }

    public void togglePlayer() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.start();
        }
    }

    public boolean isPlaying() {
        if (null != mPlayer) {
            return mPlayer.isPlaying();
        } else {
            return false;
        }
    }

    public void registerListener(MusicPlayListener listener) {
        mListener = listener;
    }

    public void unRegisterListener() {
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mPlayer) {
            mPlayer.stop();
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }
        unregisterReceiver(mBroadCast);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mListener.onMusicCompleted();
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (null != mListener) mListener.onMusicPrepared(songName, artistName);
        showNotify();
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    private void parseMp3Url(final String src) {

        new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL(src);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setInstanceFollowRedirects(false);
                    URL secondUrl = new URL(conn.getHeaderField("Location"));
                    String queryString = secondUrl.getQuery();
                    if (!TextUtils.isEmpty(queryString)) {
                        String[] params = queryString.split("=");
                        Document doc = Jsoup.connect(params[1]).get();

                        Elements cover = doc.getElementsByTag("album_cover");
                        if (null != cover && cover.size() > 0) {
                            coverUrl = cover.get(0).text();
                        }

                        Elements song = doc.getElementsByTag("song_name");
                        if (null != song && song.size() > 0) {
                            songName = song.get(0).text();
                        }

                        Elements artist = doc.getElementsByTag("artist_name");
                        if (artist.size() > 0) {
                            artistName = artist.get(0).text();
                        }

                        Elements elements = doc.getElementsByTag("location");
                        if (elements.size() > 0) {
                            String location = elements.get(0).text();
                            mUrl = getLink(location);
                        }

                        play();
                    }
                } catch (IOException e) {
                }
            }
        }.start();
    }

    void play() {
        if (TextUtils.isEmpty(mUrl)) {
            mListener.onMusicParseError();
            return;
        }

        try {
            mPlayer.setDataSource(mUrl);

            mPlayer.prepare();
            mPlayer.start();

        } catch (IOException e) {
        }
    }

    //get xiami's real mp3 path
    String getLink(String location) {
        int loc_2 = Integer.valueOf(location.substring(0, 1));
        String loc_3 = location.substring(1);
        int loc_4 = (int) Math.floor(Double.valueOf(loc_3.length()) / loc_2);
        int loc_5 = loc_3.length() % loc_2;
        ArrayList<String> loc_6 = new ArrayList<String>();
        int loc_7 = 0;
        StringBuilder loc_8 = new StringBuilder();
        String loc_9 = "";
        int loc_10;

        while (loc_7 < loc_5) {
            int start = (loc_4 + 1) * loc_7;
            int end = start + loc_4 + 1;
            String str = loc_3.substring(start, end);
            loc_6.add(str);
            loc_7++;
        }

        loc_7 = loc_5;
        while (loc_7 < loc_2) {
            int start = loc_4 * (loc_7 - loc_5) + (loc_4 + 1) * loc_5;
            int end = start + loc_4;
            String str = loc_3.substring(start, end);
            loc_6.add(str);
            loc_7++;
        }

        loc_7 = 0;
        while (loc_7 < loc_6.get(0).length()) {
            loc_10 = 0;
            while (loc_10 < loc_6.size()) {
                String str = loc_6.get(loc_10);
                int len = str.length();
                if (loc_7 < len) {
                    loc_8.append(str.toCharArray()[loc_7]);
                }
                loc_10++;
            }
            loc_7++;
        }

        try {
            loc_9 = URLDecoder.decode(loc_8.toString(), "UTF-8");
            loc_9 = loc_9.replace('^', '0');
        } catch (UnsupportedEncodingException e) {
        }

        return loc_9;
    }

    void showNotify() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

        mRemoteViews.setTextViewText(R.id.tv_name, songName);
        mRemoteViews.setTextViewText(R.id.tv_artist, artistName);
        mRemoteViews.setImageViewResource(R.id.iv_album, R.drawable.ic_notify_album);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                new Intent(NOTIFICATION_CLOSE_ACTION),
                0);
        mRemoteViews.setOnClickPendingIntent(R.id.btn_close, pendingIntent);

        mBuilder.setSmallIcon(R.drawable.ic_notify)
                .setContent(mRemoteViews)
                .setWhen(System.currentTimeMillis());

        final Notification notification = mBuilder.build();
        mNotificationMgr.notify(NOTIFY_ID, notification);
        startForeground(NOTIFY_ID, notification);
    }

    private class NotificationCloseBroadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mNotificationMgr.cancelAll();
            stopForeground(true);
            stopSelf();
        }
    }
}
