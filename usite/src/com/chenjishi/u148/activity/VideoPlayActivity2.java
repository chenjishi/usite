package com.chenjishi.u148.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import com.chenjishi.u148.R;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-11-21
 * Time: 下午11:44
 * To change this template use File | Settings | File Templates.
 */
public class VideoPlayActivity2 extends Activity {
    private VideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(this)) return;
        setContentView(R.layout.videoview);

        String url = getIntent().getStringExtra("url");
        play(url);
    }

    private void play(String url) {
        if (!TextUtils.isEmpty(url)) {
            mVideoView = (VideoView) findViewById(R.id.surface_view);
            mVideoView.setVideoPath(url);
            mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);
            mVideoView.setMediaController(new MediaController(this));
        } else {
            Toast.makeText(this, "无法播放", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mVideoView)
            mVideoView.stopPlayback();
    }
}
