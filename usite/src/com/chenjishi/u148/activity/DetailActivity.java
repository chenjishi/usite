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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.*;
import com.baidu.mobads.InterstitialAd;
import com.baidu.mobads.InterstitialAdListener;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.DBHelper;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.model.Article;
import com.chenjishi.u148.model.FeedItem;
import com.chenjishi.u148.model.UserInfo;
import com.chenjishi.u148.service.MusicPlayListener;
import com.chenjishi.u148.service.MusicService;
import com.chenjishi.u148.sina.RequestListener;
import com.chenjishi.u148.util.Constants;
import com.chenjishi.u148.util.HttpUtils;
import com.chenjishi.u148.util.ShareUtils;
import com.chenjishi.u148.util.Utils;
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
    private View mEmptyView;

    private TextView favoriteBtn;

    private MusicService mMusicService;

    private Article mArticle;
    private FeedItem mFeed;

    private DBHelper mDatabase;

    private boolean mBounded = false;

    private ShareDialog mShareDialog;

    private boolean isFavorite = false;
    private boolean favorited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

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

        mEmptyView = findViewById(R.id.empty_layout);

        mDatabase = DBHelper.getInstance(this);
        String title = categoryMap.get(String.valueOf(mFeed.category));
        if (null == mFeed.usr) {
            title = "返回";
        }
        setTitle(title);

        favoriteBtn = (TextView) findViewById(R.id.btn_favorite);

        isFavorite = favorited = mDatabase.exist(mFeed.id);

        mWebView = (ArticleWebView) findViewById(R.id.webview_content);

        mJsBridge = new JavascriptBridge(this);
        mWebView.addJavascriptInterface(mJsBridge, "U148");

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

    private View mMusicPanel;
    private TextView mSongText;
    private TextView mArtistText;
    private ProgressBar mMusicProgress;
    private ImageButton mPlayBtn;

    @Override
    public void onMusicStartParse() {
        if (null == mMusicPanel) {
            mMusicPanel = LayoutInflater.from(this).inflate(R.layout.music_pane_layout, null);

            mSongText = (TextView) mMusicPanel.findViewById(R.id.tv_song_title);
            mArtistText = (TextView) mMusicPanel.findViewById(R.id.tv_artist);
            mMusicProgress = (ProgressBar) mMusicPanel.findViewById(R.id.pb_music_loading);
            mPlayBtn = (ImageButton) mMusicPanel.findViewById(R.id.btn_play);
        }

        mSongText.setText(getString(R.string.content_loading));
        mArtistText.setVisibility(View.GONE);
        mPlayBtn.setVisibility(View.GONE);
        mMusicProgress.setVisibility(View.VISIBLE);
        mMusicPanel.setVisibility(View.VISIBLE);

        if (null != mMusicPanel.getParent()) return;

        final FrameLayout view = (FrameLayout) findViewById(android.R.id.content);
        final float density = getResources().getDisplayMetrics().density;
        final RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (density * 60. + 0.5));
        view.addView(mMusicPanel, lp);
    }

    @Override
    public void onMusicPrepared(String song, String artist) {
        mSongText.setText(song);
        mArtistText.setText(artist);
        mMusicProgress.setVisibility(View.GONE);

        mArtistText.setVisibility(View.VISIBLE);
        mPlayBtn.setImageResource(R.drawable.ic_pause);
        mPlayBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMusicCompleted() {
        if (null == mMusicPanel) return;

        final FrameLayout view = (FrameLayout) findViewById(android.R.id.content);
        view.removeView(mMusicPanel);
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
    protected void onStop() {
        super.onStop();
        if (mBounded) {
            unbindService(mConnection);
            mBounded = false;

            if (null != mMusicPanel && mMusicPanel.getParent() != null) {
                ((ViewGroup) mMusicPanel.getParent()).removeView(mMusicPanel);
            }
        }
        favorite();
    }

    void favorite() {
        if (isFavorite == favorited) return;

        String url;
        if (isFavorite) {
            url = "http://www.u148.net/json/favourite";
            mDatabase.insert(mFeed);
        } else {
            url = "http://www.u148.net/json/del_favourite";
            mDatabase.delete(mFeed.id);
        }

        final UserInfo user = PrefsUtil.getUser();
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", mFeed.id);
        params.put("token", user.token);
        HttpUtils.post(url, params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
            }
        }, this);
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

    public void onCommentClicked(View v) {
        Intent intent = new Intent(this, CommentActivity.class);
        intent.putExtra("article_id", mFeed.id);
        startActivity(intent);
    }

    public void onFavoriteClicked(View v) {
        final UserInfo user = PrefsUtil.getUser();

        if (null == user || TextUtils.isEmpty(user.token)) {
            Utils.showToast("请先登录再收藏");
            return;
        }

        if (isFavorite) {
            favoriteBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite, 0, 0, 0);
            Utils.showToast(R.string.favorite_cancel);
            isFavorite = false;
        } else {
            favoriteBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_full, 0, 0, 0);
            Utils.showToast(R.string.favorite_success);
            isFavorite = true;
        }
    }

    public void onShareClicked(View v) {
        if (null == mShareDialog) {
            mShareDialog = new ShareDialog(this, this);
        }
        mShareDialog.show();
    }

    public void onButtonClicked(View v) {
        switch (v.getId()) {
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
        Utils.setErrorView(mEmptyView, "网络错误");
    }

    @Override
    public void onResponse(Article response) {
        if (null != response && !TextUtils.isEmpty(response.content)) {
            mArticle = response;
            renderPage();
            findViewById(R.id.article_layout).setVisibility(View.VISIBLE);
        } else {
            Utils.setErrorView(mEmptyView, R.string.parse_error);
        }
    }

    private void renderPage() {
        String template = Utils.readFromAssets(this, "usite.html");
        template = template.replace("{TITLE}", mFeed.title);

        final UserInfo usr = mFeed.usr;
        if (null != usr) {
            long t = mFeed.create_time * 1000L;
            Date date = new Date(t);
            Format format = new SimpleDateFormat("yyyy-MM-dd");
            String pubTime = String.format(getString(R.string.pub_time),
                    mFeed.usr.nickname, format.format(date));
            template = template.replace("{U_AUTHOR}", pubTime);
            String reviews = String.format(getString(R.string.pub_reviews), mFeed.count_browse,
                    mFeed.count_review);
            template = template.replace("{U_COMMENT}", reviews);
        } else {
            template = template.replace("{U_AUTHOR}", "");
            template = template.replace("{U_COMMENT}", "");
        }
        template = template.replace("{CONTENT}", mArticle.content);

        final int mode = PrefsUtil.getThemeMode();
        if (Constants.MODE_NIGHT == mode) {
            template = template.replace("{SCREEN_MODE}", "night");
        } else {
            template = template.replace("{SCREEN_MODE}", "");
        }

        mWebView.loadDataWithBaseURL(null, template, "text/html", "UTF-8", null);
        mWebView.setWebChromeClient(new MyWebChromeClient());
    }

    class MyWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                boolean adShowed = PrefsUtil.isAdShowed();
                if (!adShowed) initAd();
            }
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
//            Toast.makeText(DetailActivity.this, message, Toast.LENGTH_SHORT).show();
//            result.cancel();
            return true;
        }
    }

    @Override
    public void onShare(final int type) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(Constants.PARAM_TITLE, mFeed.title);
        FlurryAgent.logEvent(Constants.EVENT_ARTICLE_SHARE, params);

        final String title = String.format(getString(R.string.share_title), mFeed.title);
        final String desc = mFeed.summary;

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
                        ShareUtils.shareWebpage(DetailActivity.this, url, type, title, desc, response);
                    } else {
                        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
                        ShareUtils.shareWebpage(DetailActivity.this, url, type, title, desc, icon);
                    }
                }
            }, 0, 0, null, null);

            HttpUtils.getRequestQueue().add(request);
        } else {
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
            ShareUtils.shareWebpage(this, url, type, title, desc, icon);
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

    class JavascriptBridge {
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
            if (TextUtils.isEmpty(src)) return;

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

        final View splitBottom = findViewById(R.id.split_h1);
        final View BottomLayout = findViewById(R.id.bottom_layout);
        final View split1 = findViewById(R.id.split_v_1);
        final View split2 = findViewById(R.id.split_v_2);
        final TextView commentBtn = (TextView) findViewById(R.id.btn_comment);
        final TextView shareBtn = (TextView) findViewById(R.id.btn_share);

        if (Constants.MODE_NIGHT == theme) {
            splitBottom.setBackgroundColor(0xFF303030);
            BottomLayout.setBackgroundColor(0xFF1C1C1C);

            split1.setBackgroundColor(0xFF303030);
            split2.setBackgroundColor(0xFF303030);
            commentBtn.setTextColor(getResources().getColor(R.color.text_color_summary));
            commentBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_comment_night, 0, 0, 0);
            final int resId = isFavorite ? R.drawable.ic_favorite_full : R.drawable.ic_favorite_night;
            favoriteBtn.setTextColor(getResources().getColor(R.color.text_color_summary));
            favoriteBtn.setCompoundDrawablesWithIntrinsicBounds(resId, 0, 0, 0);
            shareBtn.setTextColor(getResources().getColor(R.color.text_color_summary));
            shareBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_share_night, 0, 0, 0);
        } else {
            splitBottom.setBackgroundColor(0xFFEEEEEE);
            BottomLayout.setBackgroundColor(0xFFF9F9F9);

            split1.setBackgroundColor(0xFFEEEEEE);
            split2.setBackgroundColor(0xFFEEEEEE);
            commentBtn.setTextColor(getResources().getColor(R.color.gray_2));
            commentBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_comment, 0, 0, 0);
            favoriteBtn.setTextColor(getResources().getColor(R.color.gray_2));
            final int resId = isFavorite ? R.drawable.ic_favorite_full : R.drawable.ic_favorite;
            favoriteBtn.setCompoundDrawablesWithIntrinsicBounds(resId, 0, 0, 0);
            shareBtn.setTextColor(getResources().getColor(R.color.gray_2));
            shareBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_social_share, 0, 0, 0);
        }
    }
}
