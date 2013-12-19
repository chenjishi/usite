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
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.*;
import com.chenjishi.u148.R;
import com.chenjishi.u148.model.Article;
import com.chenjishi.u148.service.MusicPlayListener;
import com.chenjishi.u148.service.MusicService;
import com.chenjishi.u148.sina.RequestListener;
import com.chenjishi.u148.util.CommonUtil;
import com.chenjishi.u148.util.Constants;
import com.chenjishi.u148.util.HttpUtils;
import com.chenjishi.u148.util.ShareUtils;
import com.chenjishi.u148.view.ShareDialog;
import com.chenjishi.u148.volley.Response;
import com.chenjishi.u148.volley.VolleyError;
import com.chenjishi.u148.volley.toolbox.ImageRequest;
import com.flurry.android.FlurryAgent;
import com.sina.weibo.sdk.exception.WeiboException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.chenjishi.u148.util.Constants.BASE_URL;
import static com.chenjishi.u148.util.Constants.SOURCE_U148;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-4
 * Time: 下午7:56
 * To change this template use File | Settings | File Templates.
 */
public class DetailActivity extends BaseActivity implements MusicPlayListener, ShareDialog.OnShareListener,
        Response.Listener<Article>, Response.ErrorListener {
    private WebView mWebView;
    private JavascriptBridge mJsBridge;

    private MusicService mMusicService;

    private String mTitle;
    private Article mArticle;

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
        setMenuIcon3Visibility(true);

        Bundle bundle = getIntent().getExtras();

        mTitle = bundle.getString("title");
        int source = bundle.getInt("source");
        String link = bundle.getString("link");
        String url;
        if (SOURCE_U148 == source) {
            url = BASE_URL + link;
            setMenuIcon2Visibility(true);
        } else {
            url = link;
            setMenuIcon2Visibility(false);
        }

        mSongText = (TextView) findViewById(R.id.tv_song_title);
        mArtistText = (TextView) findViewById(R.id.tv_artist);
        mMusicProgress = (ProgressBar) findViewById(R.id.pb_music_loading);
        mPlayBtn = (ImageButton) findViewById(R.id.btn_play);
        mMusicPanel = (RelativeLayout) findViewById(R.id.panel_music);

        mWebView = (WebView) findViewById(R.id.webview_content);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        mJsBridge = new JavascriptBridge(this);

        mWebView.addJavascriptInterface(mJsBridge, "U148");

        //for debug javascript only
//        mWebView.setWebChromeClient(new MyWebChromeClient());
        HttpUtils.get(url, source, this, this);
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

    public void onButtonClicked(View view) {

        switch (view.getId()) {
            case R.id.icon_menu2:
                FlurryAgent.logEvent(Constants.EVENT_COMMENT_CLICK);
                if (mArticle.source == SOURCE_U148) {
                    if (null == mArticle.comment) return;

                    Intent intent = new Intent(this, CommentActivity.class);
                    intent.putExtra("floors", mArticle.comment);
                    startActivity(intent);
                } else {
                    //todo
                }

                break;
            case R.id.content_share:
                if (null == mShareDialog) {
                    mShareDialog = new ShareDialog(this, this);
                }
                mShareDialog.show();
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
    protected int getLayoutId() {
        return R.layout.detail;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        CommonUtil.showToast(R.string.connection_error);
    }

    @Override
    public void onResponse(Article response) {
        if (null != response && !TextUtils.isEmpty(response.content)) {
            mArticle = response;
            renderPage();
        } else {
            CommonUtil.showToast(R.string.parse_error);
            finish();
        }
    }

    private void renderPage() {
        String template = CommonUtil.readFromAssets(this, "usite.html");
        template = template.replace("{TITLE}", mTitle);
        template = template.replace("{CONTENT}", mArticle.content);

        mWebView.loadDataWithBaseURL(null, template, "text/html", "UTF-8", null);
        mWebView.setVisibility(View.VISIBLE);
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
        params.put(Constants.PARAM_TITLE, mTitle);
        FlurryAgent.logEvent(Constants.EVENT_ARTICLE_SHARE, params);

        final String title = String.format(getString(R.string.share_title), mTitle);

        if (type == ShareUtils.SHARE_WEIBO) {
            shareToWeibo(title);
            mShareDialog.dismiss();
            return;
        }

        final ArrayList<String> imageList = mArticle.imageList;
        final String url = mArticle.url;
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
        ShareUtils.shareToWeibo(this, title + mArticle.url, null, imageUrl, new RequestListener() {
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

        public void onImageClick(String src) {
            Intent intent = new Intent(mContext, ImageActivity.class);
            intent.putExtra("imgsrc", src);
            intent.putStringArrayListExtra("images", mArticle.imageList);
            mContext.startActivity(intent);
        }

        public void onVideoClick(String src) {
            Intent intent = new Intent();
            if (src.contains("xiami")) {
                intent.setClass(mContext, MusicService.class);
                intent.putExtra("url", src);
                startService(intent);
            } else {
                intent.setClass(mContext, VideoPlayerActivity.class);
                intent.putExtra("url", src);
                mContext.startActivity(intent);
            }
        }
    }
}
