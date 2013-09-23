package com.chenjishi.u148.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import com.chenjishi.u148.R;
import com.chenjishi.u148.util.VideoUrlParser;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-2-2
 * Time: 上午11:03
 * To change this template use File | Settings | File Templates.
 */
public class VideoPlayerActivity extends Activity {
    private VideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(this)) return;
        setContentView(R.layout.videoview);

        String videoUrl = getIntent().getExtras().getString("url", "");

        if (!TextUtils.isEmpty(videoUrl)) {
            new VideoLoadTask(this).execute(videoUrl);
        } else {
            finish();
        }
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
            String videoUrl = strings[0];
            String result = null;

            if (videoUrl.startsWith("http://player.56.com")) {
                result = VideoUrlParser.get56VideoPath(videoUrl);
            } else if (videoUrl.startsWith("http://you.video.sina.com.cn")) {
                result = VideoUrlParser.getSinaUrl(videoUrl);
            } else if (videoUrl.startsWith("http://player.youku.com")) {
                result = VideoUrlParser.getYoukuUrl(videoUrl);
            }

            return result;
        }


        @Override
        protected void onPostExecute(String s) {
            if (!TextUtils.isEmpty(s)) {
                mVideoView = (VideoView) findViewById(R.id.surface_view);
                mVideoView.setVideoPath(s);
                mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);
                mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mProgressDialog.dismiss();
                    }
                });

                mVideoView.setMediaController(new MediaController(VideoPlayerActivity.this));
            } else {
                Toast.makeText(mContext, mContext.getString(R.string.video_play_failed), Toast.LENGTH_SHORT).show();
                VideoPlayerActivity.this.finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mVideoView)
            mVideoView.stopPlayback();
    }
}
