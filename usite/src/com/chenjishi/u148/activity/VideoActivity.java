package com.chenjishi.u148.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import com.chenjishi.u148.R;
import com.chenjishi.u148.model.Video;
import com.chenjishi.u148.sina.RequestListener;
import com.chenjishi.u148.util.*;
import com.chenjishi.u148.view.ShareDialog;
import com.chenjishi.u148.view.VideoController;
import com.chenjishi.u148.volley.Response;
import com.chenjishi.u148.volley.toolbox.ImageRequest;
import com.flurry.android.FlurryAgent;
import com.sina.weibo.sdk.exception.WeiboException;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.VideoView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-2-2
 * Time: 上午11:03
 * To change this template use File | Settings | File Templates.
 */
public class VideoActivity extends Activity implements View.OnClickListener, ShareDialog.OnShareListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    private VideoView mVideoView;
    private VideoController mMediaController;

    private String mVideoUrl;

    private Video mVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Vitamio.isInitialized(this)) {
            new InitLibsTask().execute();
        } else {
            initView();
        }
    }

    private void initView() {
        setContentView(R.layout.videoview);

        mMediaController = new VideoController(this);
        mVideoView = (VideoView) findViewById(R.id.surface_view);
        mVideoView.setMediaController(mMediaController);

        mVideoUrl = getIntent().getStringExtra("url");
        if (!TextUtils.isEmpty(mVideoUrl)) {
            new VideoLoadTask().execute();
        } else {
            errorHandle();
        }
    }

    private class InitLibsTask extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(VideoActivity.this);
            progress.setCancelable(false);
            progress.setMessage("解码器初始化...");
            progress.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return Vitamio.initialize(VideoActivity.this, R.raw.libarm);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            progress.dismiss();
            initView();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mediacontroller_share:
                mShareDialog = new ShareDialog(this, this);
                mShareDialog.show();
                break;
        }
    }

    private ShareDialog mShareDialog;

    //http://www.56.com/u48/v_OTY2NTkyMzQ.html
//    http://v.youku.com/v_show/id_XMzE5MDA1ODQ0.html
    @Override
    public void onShare(final int type) {
        if (null == mVideo) return;

        HashMap<String, String> params = new HashMap<String, String>();
        params.put(Constants.PARAM_TITLE, mVideo.title);
        FlurryAgent.logEvent(Constants.EVENT_VIDEO_SHARE, params);

        final String videoUrl;
        if (mVideoUrl.contains("youku")) {
            videoUrl = "http://v.youku.com/v_show/id_" + mVideo.id + ".html";
        } else {
            videoUrl = "http://www.56.com/u48/v_" + mVideo.id + ".html";
        }

        if (type == ShareUtils.SHARE_WEIBO) {
            String content = String.format(getString(R.string.video_share), mVideo.title, videoUrl);
            shareToWeibo(content, mVideo.thumbUrl);
        } else {
            String thumbUrl = mVideo.thumbUrl;
            if (!TextUtils.isEmpty(thumbUrl)) {
                ImageRequest request = new ImageRequest(thumbUrl, new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {

                        if (null != response) {
                            ShareUtils.shareVideo(VideoActivity.this, videoUrl, type, mVideo.title, response);
                        } else {
                            //todo
                        }
                    }
                }, 0, 0, null, null);

                HttpUtils.getRequestQueue().add(request);
            } else {
                //todo
            }
        }
        mShareDialog.dismiss();
    }

    private void shareToWeibo(String content, String imageUrl) {
        ShareUtils.shareToWeibo(this, content, null, imageUrl, new RequestListener() {
            @Override
            public void onComplete(String response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showToast(R.string.share_success);
                    }
                });
            }

            @Override
            public void onComplete4binary(ByteArrayOutputStream responseOS) {

            }

            @Override
            public void onIOException(IOException e) {

            }

            @Override
            public void onError(WeiboException e) {

            }
        });
    }

    private class VideoLoadTask extends AsyncTask<Void, Void, String> {
        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(VideoActivity.this);
            progress.setMessage(getString(R.string.video_loading));
            progress.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = "";

            if (mVideoUrl.contains("youku")) {
                String vId = null;
                String regex = "sid\\/(\\w+)\\/";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(mVideoUrl);

                if (matcher.find()) vId = matcher.group(1);

                String m3u8Url = "http://v.youku.com/player/getRealM3U8/vid/%1$s/type/video.m3u8";

                mVideo = new Video();
                mVideo.id = vId;
                mVideo.url = String.format(m3u8Url, vId);
            } else if (mVideoUrl.contains("sina")) {
                result = VideoUrlParser.getSinaUrl(mVideoUrl);
            } else if (mVideoUrl.contains("tudou")) {
                mVideo = VideoUrlParser.getTudouUrl(mVideoUrl);
            } else if (mVideoUrl.contains("qiyi.com")) {
                mVideo = VideoUrlParser.getQiyiVideo(mVideoUrl);
            } else if (mVideoUrl.contains("56.com")) {
                mVideo = VideoUrlParser.get56VideoPath(mVideoUrl);
            } else {
                mVideo = null;
            }

            if (TextUtils.isEmpty(result)) {
                result = mVideo == null ? "" : mVideo.url;
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            progress.dismiss();
            if (!TextUtils.isEmpty(s)) {
               startPlay(s);
            } else {
                errorHandle();
            }
        }
    }

    private void startPlay(String url) {
        mVideoView.setVideoPath(url);
        mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnCompletionListener(this);

        mMediaController.findViewById(R.id.mediacontroller_share).setOnClickListener(this);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mMediaController.setFileName(mVideo.title);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (-1L != lastTimeWatched && null != mVideoView) {
            mVideoView.resume();
            mVideoView.seekTo(lastTimeWatched);
        }
    }

    private long lastTimeWatched = -1L;

    @Override
    protected void onPause() {
        super.onPause();

        lastTimeWatched = mVideoView.getCurrentPosition();
        mVideoView.pause();
    }

    private void errorHandle() {
        Utils.showToast(R.string.video_play_failed);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mVideoView)
            mVideoView.stopPlayback();
    }
}
