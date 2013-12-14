package com.chenjishi.u148.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.chenjishi.u148.R;
import com.chenjishi.u148.sina.RequestListener;
import com.chenjishi.u148.util.CommonUtil;
import com.chenjishi.u148.util.Constants;
import com.chenjishi.u148.util.HttpUtils;
import com.chenjishi.u148.util.ShareUtils;
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
 * Date: 13-11-21
 * Time: 下午11:44
 * To change this template use File | Settings | File Templates.
 */
public class VideoPlayActivity2 extends Activity implements MediaController.OnHiddenListener,
        ShareDialog.OnShareListener, MediaController.OnShownListener {
    private VideoView mVideoView;
    private MediaController mMediaController;
    private RelativeLayout mTopLayout;

    private String mVideoId;
    private String mVideoPath;
    private String mThumbUrl;
    private String mVideoTitle;

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

        mVideoPath = getIntent().getStringExtra("local_path");
        mVideoId = getIntent().getStringExtra("id");
        mThumbUrl = getIntent().getStringExtra("thumb");
        mVideoTitle = getIntent().getStringExtra("title");

        play();
    }

    private void play() {
        if (!TextUtils.isEmpty(mVideoPath)) {
            mVideoView.setVideoPath(mVideoPath);
            mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);
            mVideoView.setMediaController(mMediaController);
        } else {
            Toast.makeText(this, "无法播放", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onHidden() {
        mTopLayout.setVisibility(View.GONE);
    }

    @Override
    public void onShare(final int type) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(Constants.PARAM_TITLE, mVideoTitle);
        FlurryAgent.logEvent(Constants.EVENT_VIDEO_SHARE, params);

        final String videoUrl = "http://v.youku.com/v_show/id_" + mVideoId + ".html";

        if (type == ShareUtils.SHARE_WEIBO) {
            String content = String.format(getString(R.string.video_share), mVideoTitle, videoUrl);
            shareToWeibo(content, mThumbUrl);
        } else {
            String thumbUrl = mThumbUrl;
            if (!TextUtils.isEmpty(thumbUrl)) {
                ImageRequest request = new ImageRequest(thumbUrl, new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {

                        if (null != response) {
                            ShareUtils.shareVideo(VideoPlayActivity2.this, videoUrl, type, mVideoTitle, response);
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


    @Override
    public void onShown() {
        mTopLayout.setVisibility(View.VISIBLE);
    }

    private ShareDialog mShareDialog;
    public void onShareButtonClicked(View view) {
        mShareDialog = new ShareDialog(this, this);
        mShareDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mVideoView)
            mVideoView.stopPlayback();
    }
}
