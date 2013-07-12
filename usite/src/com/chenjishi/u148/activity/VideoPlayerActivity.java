package com.chenjishi.u148.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.chenjishi.u148.R;
import com.chenjishi.u148.util.VideoUrlParser;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-2-2
 * Time: 上午11:03
 * To change this template use File | Settings | File Templates.
 */
public class VideoPlayerActivity extends Activity {
    public static final int TYPE_56 = 1;
    public static final int TYPE_TUDOU = 2;
    public static final int TYPE_SINA = 3;
    public static final int TYPE_YOUKU = 4;

    private int currentType = -1;

    String videoUrl;

    private VideoView mVideoView;
    private MediaController mMediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(this))
            return;

        setContentView(R.layout.videoview);

        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            videoUrl = bundle.getString("url");
            Log.i("test", videoUrl);
        }

        String videoId = "";

        //56 http://player.56.com/v_ODI4NjUwNjk.swf/1030_zc5991335.swf
        //sina http://you.video.sina.com.cn/api/sinawebApi/outplayrefer.php
        // /vid=6657677_1211498717_ahqwSyFuBmTK+l1lHz2stqkP7KQNt6nki2u1v1usIg9eQ0/XM5GfZNgA5i7RFokbpWFLQp04c/kh/s.swf


        //youku http://player.youku.com/player.php/sid/XNTA3ODkxNTky/v.swf

        if (videoUrl.startsWith("http://player.56.com")) {
            currentType = TYPE_56;
            String regex = "v_(\\w+)\\.swf";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(videoUrl);

           if (matcher.find()) {
               videoId = matcher.group(1);
           }
        } else if (videoUrl.startsWith("http://you.video.sina.com.cn")) {
            currentType = TYPE_SINA;
            String regex = "vid=(\\d+)_\\d+";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(videoUrl);

            if (matcher.find()) {
                videoId = matcher.group(1);
            }
        } else if (videoUrl.startsWith("http://player.youku.com")) {
            currentType = TYPE_YOUKU;
            String regex = "sid\\/(\\w+)\\/";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(videoUrl);

            if (matcher.find()) {
                videoId = matcher.group(1);
            }
        }

        if (videoId.length() > 0) {
            new VideoLoadTask(this).execute(videoId);
        } else {
            Toast.makeText(this, getString(R.string.video_play_failed), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (mVideoView != null)
            mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE, 0);
        super.onConfigurationChanged(newConfig);
    }

    class VideoLoadTask extends AsyncTask<String, Void, String> {
        Context mContext;
        ProgressDialog mProgressDialog;

        public VideoLoadTask(Context context) {
            mContext = context;
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage("Video Loading...");
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            ArrayList<String> urlList = new ArrayList<String>();
            switch (currentType) {
                case TYPE_56:
                    urlList = VideoUrlParser.parse56Video(strings[0]);
                    break;
                case TYPE_TUDOU:
                    break;
                case TYPE_SINA:
                    try {
                        urlList = VideoUrlParser.getSinaflv(strings[0]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case TYPE_YOUKU:
                    try {
                        urlList = VideoUrlParser.ParserYoukuFlv(strings[0]);
                    } catch (Exception e) {

                    }
                    break;
                default:break;
            }

            if (urlList.size() > 0) {
                return urlList.get(0);
            } else {
                return null;
            }
        }



        @Override
        protected void onPostExecute(String s) {
            if (null == s) {
                Toast.makeText(mContext, mContext.getString(R.string.video_play_failed), Toast.LENGTH_SHORT).show();
                return;
            }

            mVideoView = (VideoView) findViewById(R.id.surface_view);
            mVideoView.setVideoPath(s);
            mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);
            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mProgressDialog.dismiss();
                }
            });

            mMediaController = new MediaController(VideoPlayerActivity.this);
            mVideoView.setMediaController(mMediaController);
        }
    }
}
