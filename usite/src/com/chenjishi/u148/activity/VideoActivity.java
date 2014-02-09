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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
public class VideoActivity extends Activity implements MediaController.OnHiddenListener,
        ShareDialog.OnShareListener, MediaController.OnShownListener {
    private static final String CONVERT_URL = "http://dservice.wandoujia.com/convert?target=%1$s&f=phoenix2&v=3.44.1&u=d83dc65e84c34305a1afee4a95879a35943891e2&vc=4513&ch=wandoujia_pc_baidu_pt&type=VIDEO";
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

        mTopLayout = (RelativeLayout) findViewById(R.id.title_bar);
        mMediaController = new MediaController(this);
        mMediaController.setOnHiddenListener(this);
        mMediaController.setOnShownListener(this);
        mVideoView = (VideoView) findViewById(R.id.surface_view);
        mVideoView.setMediaController(mMediaController);

        mVideoUrl = getIntent().getStringExtra("url");
        if (!TextUtils.isEmpty(mVideoUrl)) {
            new VideoLoadTask().execute();
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
            String result;

            if (mVideoUrl.contains("youku")) {
                mVideo = VideoUrlParser.getYoukuUrl(mVideoUrl);
                result = mVideo.url;
            } else if (mVideoUrl.contains("sina")) {
                result = VideoUrlParser.getSinaUrl(mVideoUrl);
            } else if (mVideoUrl.contains("tudou")) {
                mVideo = VideoUrlParser.getTudouUrl(mVideoUrl);
                result = mVideo.url;
            } else if (mVideoUrl.contains("qiyi.com")) {
                mVideo = getQiyiVideo(mVideoUrl);
                result = null != mVideo ? mVideo.url : "";
            } else {
                mVideo = VideoUrlParser.get56VideoPath(mVideoUrl);
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

    private Video getQiyiVideo(String url) {
        Video video = null;
        Pattern pattern = Pattern.compile("/(\\w+)\\.swf");
        Matcher matcher = pattern.matcher(url);

        String videoId = "";
        while (matcher.find()) {
            videoId = matcher.group(1);
        }
        if (TextUtils.isEmpty(videoId)) return null;

        String targetUrl = "http://www.iqiyi.com/" + videoId + ".html";
        String result = HttpUtils.getSync(String.format(CONVERT_URL, targetUrl));
        if (TextUtils.isEmpty(result)) return null;

        try {
            JSONObject jObj = new JSONObject(result);
            JSONArray jArray = jObj.getJSONArray("result");
            if (null != jArray && jArray.length() > 0) {
                video = new Video();

                video.originalUrl = targetUrl;
                JSONObject obj = jArray.getJSONObject(0);

                String redirectUrl = obj.getString("url");
                String result2 = HttpUtils.getSync(redirectUrl);

                pattern = Pattern.compile("l\":\"(.*?)\"");
                matcher = pattern.matcher(result2);
                while (matcher.find()) {
                    video.url = matcher.group(1);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return video;
    }

//    private Video getQiyiVideo(String url) {
//        Pattern pattern = Pattern.compile("qiyi\\.com/(\\w+)/");
//        Matcher matcher = pattern.matcher(url);
//
//        String videoId = "";
//        while (matcher.find()) {
//            videoId = matcher.group(1);
//        }
//
//        if (TextUtils.isEmpty(videoId)) return null;
//
//        Video video = null;
//        HttpURLConnection conn = null;
//        try {
//            Document doc = Jsoup.connect("http://cache.video.qiyi.com/v/" + videoId).get();
//            if (null == doc) return null;
//
//            Elements fileUrls = doc.getElementsByTag("file");
//            if (null != fileUrls && fileUrls.size() > 0) {
//                String fileUrl = fileUrls.get(0).text();
//
//                pattern = Pattern.compile("/(\\w+)\\.f4v");
//                matcher = pattern.matcher(fileUrl);
//                String videoId2 = "";
//                while (matcher.find()) {
//                    videoId2 = matcher.group(1);
//                }
//
//                if (TextUtils.isEmpty(videoId2)) return null;
//
//                conn = (HttpURLConnection) (new URL("http://data.video.qiyi.com/" + videoId2 + ".ts").openConnection());
//                conn.setInstanceFollowRedirects(false);
//                conn.connect();
//                String location = conn.getHeaderField("Location");
//
//                if (TextUtils.isEmpty(location)) return null;
//
//                pattern = Pattern.compile("key=(\\w+)");
//                matcher = pattern.matcher(location);
//                String key = "";
//                while (matcher.find()) {
//                    key = matcher.group(1);
//                }
//
//                if (TextUtils.isEmpty(key)) return null;
//
//                video = new Video();
//                video.url = fileUrl + "?key=" + key;
//
//                String title = doc.getElementsByTag("title").get(0).ownText();
//                title = title.replace("<![CDATA[", "");
//                title = title.replace("]]>", "");
//                video.title = title;
//                video.thumbUrl = doc.getElementsByTag("pic").get(0).ownText();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (null != conn) {
//                conn.disconnect();
//            }
//        }
//
//        return video;
//    }

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
