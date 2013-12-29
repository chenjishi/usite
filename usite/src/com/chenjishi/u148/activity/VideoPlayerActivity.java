package com.chenjishi.u148.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.chenjishi.u148.R;
import com.chenjishi.u148.model.Video;
import com.chenjishi.u148.sina.RequestListener;
import com.chenjishi.u148.util.*;
import com.chenjishi.u148.view.ShareDialog;
import com.chenjishi.u148.volley.Response;
import com.chenjishi.u148.volley.toolbox.ImageRequest;
import com.flurry.android.FlurryAgent;
import com.sina.weibo.sdk.exception.WeiboException;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-2-2
 * Time: 上午11:03
 * To change this template use File | Settings | File Templates.
 */
public class VideoPlayerActivity extends Activity implements MediaController.OnHiddenListener,
        ShareDialog.OnShareListener, MediaController.OnShownListener {
    private VideoView mVideoView;
    private MediaController mMediaController;
    private RelativeLayout mTopLayout;

    private String mVideoUrl;

    private Video mVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(this)) return;
        setContentView(R.layout.videoview);

        mTopLayout = (RelativeLayout) findViewById(R.id.title);
        mMediaController = new MediaController(this);
        mMediaController.setOnHiddenListener(this);
        mMediaController.setOnShownListener(this);
        mVideoView = (VideoView) findViewById(R.id.surface_view);
        mVideoView.setMediaController(mMediaController);

        mVideoUrl = getIntent().getStringExtra("url");
        if (!TextUtils.isEmpty(mVideoUrl)) {
            new VideoLoadTask().execute(mVideoUrl);
        } else {
            errorHandle();
        }
    }

    @Override
    public void onHidden() {
        mTopLayout.setVisibility(View.GONE);
    }

    @Override
    public void onShown() {
        mTopLayout.setVisibility(View.VISIBLE);
    }

    private ShareDialog mShareDialog;
    public void onShareButtonClicked(View view) {
        mShareDialog = new ShareDialog(this, this);
        mShareDialog.show();
    }

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
                            ShareUtils.shareVideo(VideoPlayerActivity.this, videoUrl, type, mVideo.title, response);
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
                        CommonUtil.showToast(R.string.share_success);
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

    class VideoLoadTask extends AsyncTask<String, Void, String> {
        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(VideoPlayerActivity.this);
            progress.setMessage(getString(R.string.video_loading));
            progress.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String videoUrl = strings[0];
            String result = null;

            if (videoUrl.startsWith("http://player.56.com")) {
                mVideo = VideoUrlParser.get56VideoPath(videoUrl);
                result = mVideo.url;
            } else if (videoUrl.startsWith("http://you.video.sina.com.cn")) {
                result = VideoUrlParser.getSinaUrl(videoUrl);
            } else if (videoUrl.startsWith("http://player.youku.com")) {
                mVideo = VideoUrlParser.getYoukuUrl(videoUrl);
                result = mVideo.url;
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            if (!TextUtils.isEmpty(s)) {
                mVideoView.setVideoPath(s);
                mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);
                mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mMediaController.setFileName("");
                        ((TextView) findViewById(R.id.video_title)).setText(mVideo.title);
                        progress.dismiss();
                    }
                });
            } else {
                errorHandle();
            }
        }
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
        Toast.makeText(this, getString(R.string.video_play_failed), Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mVideoView)
            mVideoView.stopPlayback();
    }
}
