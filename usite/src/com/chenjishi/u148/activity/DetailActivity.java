package com.chenjishi.u148.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.*;
import com.baidu.mobads.InterstitialAd;
import com.baidu.mobads.InterstitialAdListener;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.model.Article;
import com.chenjishi.u148.model.Feed;
import com.chenjishi.u148.service.MusicPlayListener;
import com.chenjishi.u148.service.MusicService;
import com.chenjishi.u148.sina.RequestListener;
import com.chenjishi.u148.util.CommonUtil;
import com.chenjishi.u148.util.Constants;
import com.chenjishi.u148.util.HttpUtils;
import com.chenjishi.u148.util.ShareUtils;
import com.chenjishi.u148.view.ArticleWebView;
import com.chenjishi.u148.view.ShareDialog;
import com.chenjishi.u148.volley.Response;
import com.chenjishi.u148.volley.VolleyError;
import com.chenjishi.u148.volley.toolbox.ImageRequest;
import com.flurry.android.FlurryAgent;
import com.sina.weibo.sdk.exception.WeiboException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-4
 * Time: 下午7:56
 * To change this template use File | Settings | File Templates.
 */
public class DetailActivity extends BaseActivity implements MusicPlayListener, ShareDialog.OnShareListener,
        Response.Listener<Article>, Response.ErrorListener, InterstitialAdListener {
    private final static String REQUEST_URL = "http://www.u148.net/json/article/%1$s";
    private ArticleWebView mWebView;
    private JavascriptBridge mJsBridge;

    private MusicService mMusicService;

    private Article mArticle;

    private Feed mFeed;

    private RelativeLayout mMusicPanel;
    private TextView mSongText;
    private TextView mArtistText;
    private ProgressBar mMusicProgress;
    private ImageButton mPlayBtn;

    private boolean mBounded = false;

    private ShareDialog mShareDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail, R.layout.article_title_layout);

        Bundle bundle = getIntent().getExtras();

        if (null != bundle) {
            mFeed = bundle.getParcelable("feed");
        } else {
            finish();
        }

        Map<String, String> categoryMap;
        categoryMap = new HashMap<String, String>();
        int[] ids = getResources().getIntArray(R.array.category_id);
        String[] names = getResources().getStringArray(R.array.category_name);
        for (int i = 0; i < ids.length; i++) {
            categoryMap.put(String.valueOf(ids[i]), names[i]);
        }

        String title = categoryMap.get(String.valueOf(mFeed.category));
        setTitle(title);

        mSongText = (TextView) findViewById(R.id.tv_song_title);
        mArtistText = (TextView) findViewById(R.id.tv_artist);
        mMusicProgress = (ProgressBar) findViewById(R.id.pb_music_loading);
        mPlayBtn = (ImageButton) findViewById(R.id.btn_play);
        mMusicPanel = (RelativeLayout) findViewById(R.id.panel_music);

        mWebView = (ArticleWebView) findViewById(R.id.webview_content);

        mJsBridge = new JavascriptBridge(this);
        mWebView.addJavascriptInterface(mJsBridge, "U148");

        renderPage(getString(R.string.content_loading));

        //for debug javascript only
//        mWebView.setWebChromeClient(new MyWebChromeClient());
        HttpUtils.ArticleRequest(String.format(REQUEST_URL, mFeed.id), this, this);
    }

    private InterstitialAd interstitialAd;
    private void initAd() {
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setListener(this);
        interstitialAd.loadAd();
    }

    @Override
    public void onAdReady() {
        interstitialAd.showAd(this);
        PrefsUtil.setAdShowed(true);
    }

    @Override
    public void onAdPresent() {

    }

    @Override
    public void onAdClick(InterstitialAd interstitialAd) {

    }

    @Override
    public void onAdDismissed() {

    }

    @Override
    public void onAdFailed(String s) {

    }

    private void initMusicPanel() {
        mSongText.setText("正在加载...");
        mArtistText.setVisibility(View.GONE);
        mMusicProgress.setVisibility(View.VISIBLE);
        mPlayBtn.setVisibility(View.GONE);
        mMusicPanel.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMusicStartParse() {
        initMusicPanel();
    }

    @Override
    public void onMusicPrepared(String song, String artist) {
        mSongText.setText(song);
        mArtistText.setText(artist);
        mArtistText.setVisibility(View.VISIBLE);

        mMusicProgress.setVisibility(View.GONE);
        mPlayBtn.setImageResource(R.drawable.ic_pause);
        mPlayBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMusicCompleted() {
        mMusicPanel.setVisibility(View.GONE);
    }

    @Override
    public void onMusicParseError() {
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mBounded) {
            bindService(new Intent(this, MusicService.class), mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBounded) {
            unbindService(mConnection);
            mBounded = false;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMusicService = ((MusicService.MusicBinder) service).getService();
            mMusicService.registerListener(DetailActivity.this);
            mBounded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMusicService.unRegisterListener();
            mMusicService = null;
            mBounded = false;
        }
    };

    public void onButtonClicked(View v) {
        switch (v.getId()) {
            case R.id.ic_share:
                if (null == mShareDialog) {
                    mShareDialog = new ShareDialog(this, this);
                }
                mShareDialog.show();
                break;
            case R.id.ic_comment:
                Intent intent = new Intent(this, CommentActivity.class);
                intent.putExtra("article_id", mFeed.id);
                startActivity(intent);
                break;
            case R.id.btn_play:
                if (mMusicService != null) {
                    mPlayBtn.setImageResource(mMusicService.isPlaying()
                            ? R.drawable.ic_play : R.drawable.ic_pause);
                    mMusicService.togglePlayer();
                }
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        CommonUtil.showToast(R.string.connection_error);
    }

    @Override
    public void onResponse(Article response) {
        if (null != response && !TextUtils.isEmpty(response.content)) {
            mArticle = response;
            renderPage(mArticle.content);
            boolean adShowed = PrefsUtil.isAdShowed();
            if (!adShowed) initAd();
        } else {
            CommonUtil.showToast(R.string.parse_error);
            finish();
        }
    }

    private void renderPage(String content) {
        String template = CommonUtil.readFromAssets(this, "usite.html");
        template = template.replace("{TITLE}", mFeed.title);

        long t = mFeed.createTime * 1000L;
        Date date = new Date(t);
        Format format = new SimpleDateFormat("yyyy-MM-dd");

        String pubTime = String.format(getString(R.string.pub_time),
                mFeed.user.nickname, format.format(date));
        template = template.replace("{U_AUTHOR}", pubTime);
        String reviews = String.format(getString(R.string.pub_reviews), mFeed.countBrowse, mFeed.countReview);
        template = template.replace("{U_COMMENT}", reviews);

        template = template.replace("{CONTENT}", content);

        mWebView.loadDataWithBaseURL(null, template, "text/html", "UTF-8", null);
    }

    class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Toast.makeText(DetailActivity.this, message, Toast.LENGTH_SHORT).show();
            result.cancel();
            return true;
        }
    }

    @Override
    public void onShare(final int type) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(Constants.PARAM_TITLE, mFeed.title);
        FlurryAgent.logEvent(Constants.EVENT_ARTICLE_SHARE, params);

        final String title = String.format(getString(R.string.share_title), mFeed.title);

        if (type == ShareUtils.SHARE_WEIBO) {
            shareToWeibo(title);
            mShareDialog.dismiss();
            return;
        }

        final ArrayList<String> imageList = mArticle.imageList;
        final String url = "http://www.u148.net/article/" + mFeed.id + ".html";
        if (null != imageList && imageList.size() > 0) {
            ImageRequest request = new ImageRequest(imageList.get(0), new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {

                    if (null != response) {
                        ShareUtils.shareWebpage(DetailActivity.this, url, type, title, response);
                    } else {
                        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
                        ShareUtils.shareWebpage(DetailActivity.this, url, type, title, icon);
                    }
                }
            }, 0, 0, null, null);

            HttpUtils.getRequestQueue().add(request);
        } else {
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
            ShareUtils.shareWebpage(this, url, type, title, icon);
        }

        mShareDialog.dismiss();
    }

    private void shareToWeibo(String title) {
        final ArrayList<String> imageList = mArticle.imageList;
        String imageUrl = null != imageList && imageList.size() > 0 ? imageList.get(0) : "no picture";
        ShareUtils.shareToWeibo(this, title + "http://www.u148.net/article/" + mFeed.id + ".html", null, imageUrl, new RequestListener() {
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

    private class JavascriptBridge {
        private Context mContext;

        public JavascriptBridge(Context context) {
            mContext = context;
        }

        @JavascriptInterface
        public void initTheme() {
            int mode = PrefsUtil.getThemeMode();
            if (mode == Constants.MODE_NIGHT) {
                mWebView.loadUrl("javascript:setScreenMode('night')");
            } else {
                mWebView.loadUrl("javascript:setScreenMode('day')");
            }
        }

        @JavascriptInterface
        public void onImageClick(String src) {
            Intent intent = new Intent(mContext, ImageActivity.class);
            intent.putExtra("imgsrc", src);
            intent.putStringArrayListExtra("images", mArticle.imageList);
            mContext.startActivity(intent);
        }

        @JavascriptInterface
        public void onVideoClick(String src) {
            Intent intent = new Intent();
            if (src.contains("xiami")) {
                intent.setClass(mContext, MusicService.class);
                intent.putExtra("url", src);
                startService(intent);
            } else {
                intent.setClass(mContext, VideoActivity.class);
                intent.putExtra("url", src);
                mContext.startActivity(intent);
            }
        }

        @JavascriptInterface
        public void onCommentClicked() {
            Intent intent = new Intent(mContext, CommentActivity.class);
            intent.putExtra("article_id", mFeed.id);
            mContext.startActivity(intent);
        }
    }

    @Override
    protected void applyTheme() {
        super.applyTheme();
        final View split1 = findViewById(R.id.split_v_1);
        final View split2 = findViewById(R.id.split_v_2);
        final ImageView commentBtn = (ImageView) findViewById(R.id.ic_comment);
        final ImageView shareBtn = (ImageView) findViewById(R.id.ic_share);

        if (Constants.MODE_NIGHT == theme) {
            split1.setBackgroundColor(0xFF303030);
            split2.setBackgroundColor(0xFF303030);
            commentBtn.setImageResource(R.drawable.ic_comment_night);
            shareBtn.setImageResource(R.drawable.ic_share_night);
        } else {
            split1.setBackgroundColor(0xFFCACACA);
            split2.setBackgroundColor(0xFFCACACA);
            commentBtn.setImageResource(R.drawable.ic_comment);
            shareBtn.setImageResource(R.drawable.ic_social_share);
        }
    }
}
