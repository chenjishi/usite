package com.chenjishi.u148.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.*;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.DBHelper;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.model.Article;
import com.chenjishi.u148.model.Feed;
import com.chenjishi.u148.model.UserInfo;
import com.chenjishi.u148.service.MusicPlayListener;
import com.chenjishi.u148.service.MusicService;
import com.chenjishi.u148.util.*;
import com.chenjishi.u148.view.ArticleWebView;
import com.chenjishi.u148.view.CircleView;
import com.chenjishi.u148.view.ShareDialog;
import com.flurry.android.FlurryAgent;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.chenjishi.u148.util.Constants.API_ADD_FAVORITE;
import static com.chenjishi.u148.util.Constants.API_ARTICLE;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-4
 * Time: 下午7:56
 * To change this template use File | Settings | File Templates.
 */
public class DetailsActivity extends SlidingActivity implements MusicPlayListener, Listener<String>,
        ErrorListener, JSCallback, View.OnClickListener {
    private ArticleWebView mWebView;

    private ImageButton favoriteBtn;

    private MusicService mMusicService;

    private Article mArticle;
    private Feed mFeed;

    private DBHelper mDatabase;

    private boolean mBounded = false;

    private ShareDialog mShareDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_detail, R.layout.details_title_layout);

        mFeed = getIntent().getExtras().getParcelable(Constants.KEY_FEED);

        Map<String, String> categoryMap;
        categoryMap = new HashMap();
        int[] ids = getResources().getIntArray(R.array.category_id);
        String[] names = getResources().getStringArray(R.array.category_name);
        for (int i = 0; i < ids.length; i++) {
            categoryMap.put(String.valueOf(ids[i]), names[i]);
        }

        mDatabase = DBHelper.getInstance(this);
        String title = categoryMap.get(String.valueOf(mFeed.category));
        if (null == mFeed.usr) {
            title = "返回";
        }
        setTitle(title);

        favoriteBtn = (ImageButton) findViewById(R.id.btn_favorite);

        mWebView = (ArticleWebView) findViewById(R.id.webview);
        mWebView.addJavascriptInterface(new JavaScriptBridge(this), "U148");

        findViewById(R.id.title_bar).setOnClickListener(this);

        showLoadingView();
        NetworkRequest.getInstance().get(String.format(API_ARTICLE, mFeed.id), this, this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_bar) {
            mWebView.scrollTo(0, 0);
        }
    }

    private View mMusicPanel;
    private TextView mSongText;
    private TextView mArtistText;
    private ProgressBar mMusicProgress;
    private ImageButton mPlayBtn;

    @Override
    public void onMusicStartParse() {
        setupMusicPanel();
    }

    void setupMusicPanel() {
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
        mDatabase.updateArticleOffset(mFeed.id, mWebView.getScrollY());
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMusicService = ((MusicService.MusicBinder) service).getService();
            mMusicService.registerListener(DetailsActivity.this);
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
        startCommentActivity();
    }

    private void startCommentActivity() {
        Intent intent = new Intent(this, CommentActivity.class);
        intent.putExtra("article_id", mFeed.id);
        IntentUtils.getInstance().startActivity(this, intent);
    }

    public void onFavoriteClicked(View v) {
        final UserInfo user = PrefsUtil.getUser();

        if (null == user || TextUtils.isEmpty(user.token)) {
            Utils.showToast("请先登录再收藏");
            return;
        }

        Feed feed = mDatabase.getFavoriteById(mFeed.id);
        if (null != feed && !TextUtils.isEmpty(feed.id)) {
            Utils.showToast("已收藏");
            return;
        }

        String requestUrl = String.format(API_ADD_FAVORITE, mFeed.id, user.token);
        NetworkRequest.getInstance().get(requestUrl, new Listener<String>() {
            @Override
            public void onResponse(String response) {
                favoriteBtn.setImageResource(R.drawable.ic_favorite_full);
                mDatabase.insert(mFeed);
                Utils.showToast(R.string.favorite_success);
            }
        }, this);
    }

    public void onShareClicked(View v) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(Constants.PARAM_TITLE, mFeed.title);
        FlurryAgent.logEvent(Constants.EVENT_ARTICLE_SHARE, params);

        if (null == mShareDialog) {
            mShareDialog = new ShareDialog(this);
        }

        final ArrayList<String> imageList = mArticle.imageList;
        if (null == imageList || imageList.size() == 0) {
            imageList.add(mFeed.pic_mid);
        }

        mShareDialog.setShareFeed(mFeed);
        mShareDialog.setImageList(imageList);
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
    public void onResponse(String response) {
        if (TextUtils.isEmpty(response)) {
            setError(getString(R.string.parse_error));
            return;
        }

        hideLoadingView();

        String json = new String(response);

        try {
            JSONObject jObj = new JSONObject(json);
            JSONObject dataObj = jObj.getJSONObject("data");

            String result = dataObj.getString("content");
            mArticle = parseU148Content(result);
            renderPage();
        } catch (JSONException e) {
        }
    }

    @Override
    public void onErrorResponse() {
        setError();
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

        int commentNum = mFeed.count_review;
        if (commentNum > 0) {
            if (commentNum >= 100) commentNum = 99;

            CircleView view = (CircleView) findViewById(R.id.comment_count);
            view.setNumber(commentNum);
            view.setVisibility(View.VISIBLE);
        }
    }

    class MyWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                final int offset = mDatabase.getOffsetById(mFeed.id);
                if (offset > 0) {
                    mWebView.scrollTo(0, offset);
                }
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
    public void onImageClicked(String url) {
        Intent intent = new Intent(this, ImageActivity.class);
        intent.putExtra("imgsrc", url);
        intent.putStringArrayListExtra("images", mArticle.imageList);
        startActivity(intent);
    }

    @Override
    public void onMusicClicked(String url) {
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("url", url);
        startService(intent);
    }

    @Override
    public void onVideoClicked(String url) {
    }

    @Override
    public void onThemeChange() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int mode = PrefsUtil.getThemeMode();
                if (mode == Constants.MODE_NIGHT) {
                    mWebView.loadUrl("javascript:setScreenMode('night')");
                } else {
                    mWebView.loadUrl("javascript:setScreenMode('day')");
                }
            }
        });
    }

    private Article parseU148Content(String html) {
        Document doc = Jsoup.parse(html);
        if (null == doc) return null;

        Article article = new Article();

        Elements images = doc.select("img");
        for (Element image : images) {
            article.imageList.add(image.attr("src"));
        }

        Elements videos = doc.select("embed");
        for (Element video : videos) {
            String videoUrl = video.attr("src");
            if (videoUrl.contains("xiami")) {
                video.parent().html("<img src=\"file:///android_asset/audio.png\" title=\"" + videoUrl + "\" />");
            } else {
                video.parent().html("<img src=\"file:///android_asset/video.png\" title=\"" + videoUrl + "\" />");
            }
        }
        article.content = doc.html();
        return article;
    }

    @Override
    protected void applyTheme() {
        super.applyTheme();

        ImageView commentBtn = (ImageView) findViewById(R.id.ic_comment);
        ImageButton shareBtn = (ImageButton) findViewById(R.id.btn_share);

        if (Constants.MODE_NIGHT == mTheme) {
            commentBtn.setImageResource(R.drawable.ic_comment_night);
            shareBtn.setImageResource(R.drawable.ic_share_night);
            favoriteBtn.setImageResource(R.drawable.ic_favorite_night);
        } else {
            commentBtn.setImageResource(R.drawable.ic_comment);
            shareBtn.setImageResource(R.drawable.ic_social_share);
            favoriteBtn.setImageResource(R.drawable.ic_favorite);
        }

        Feed feed = mDatabase.getFavoriteById(mFeed.id);
        if (null != feed && !TextUtils.isEmpty(feed.id)) {
            favoriteBtn.setImageResource(R.drawable.ic_favorite_full);
        }
    }
}
