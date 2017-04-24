package com.chenjishi.u148.article;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import com.chenjishi.u148.BaseActivity;
import com.chenjishi.u148.Config;
import com.chenjishi.u148.R;
import com.chenjishi.u148.comment.CommentActivity;
import com.chenjishi.u148.model.Article;
import com.chenjishi.u148.model.Feed;
import com.chenjishi.u148.model.UserInfo;
import com.chenjishi.u148.utils.*;
import com.chenjishi.u148.widget.ArticleWebView;
import com.chenjishi.u148.widget.CircleView;
import com.chenjishi.u148.widget.ShareDialog;
import com.flurry.android.FlurryAgent;
import com.tencent.connect.avatar.ImageActivity;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

import static android.text.TextUtils.isEmpty;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.chenjishi.u148.utils.Constants.*;

/**
 * Created by jishichen on 2017/4/14.
 */
public class DetailsActivity extends BaseActivity implements Listener<String>, ErrorListener,
        JSCallback, View.OnClickListener {

    private ArticleWebView mWebView;

    private ImageButton favoriteBtn;

    private Article mArticle;
    private Feed mFeed;

    private DBHelper mDatabase;

    private ShareDialog mShareDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_details);

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

        mWebView = (ArticleWebView) findViewById(R.id.web_view);
        mWebView.addJavascriptInterface(new JavaScriptBridge(this), "U148");

        findViewById(R.id.title_bar).setOnClickListener(this);

        showLoadingView();
        NetworkRequest.getInstance().get(String.format(API_ARTICLE, mFeed.id), this, this);
    }

    public void onCommentClicked(View v) {
        startCommentActivity();
    }

    private void startCommentActivity() {
        Intent intent = new Intent(this, CommentActivity.class);
        intent.putExtra("article_id", mFeed.id);
        IntentUtils.getInstance().startActivity(this, intent);
    }

    public void onFavoriteClicked(View v) {
        UserInfo user = Config.getUser(this);

        if (null == user || TextUtils.isEmpty(user.token)) {
            Utils.showToast(this, "请先登录再收藏");
            return;
        }

        Feed feed = mDatabase.getFavoriteById(mFeed.id);
        if (null != feed && !TextUtils.isEmpty(feed.id)) {
            Utils.showToast(this, "已收藏");
            return;
        }

        String requestUrl = String.format(API_ADD_FAVORITE, mFeed.id, user.token);
        NetworkRequest.getInstance().get(requestUrl, new Listener<String>() {
            @Override
            public void onResponse(String response) {
                favoriteBtn.setImageResource(R.mipmap.ic_favorite_full);
                mDatabase.insert(mFeed);
                Utils.showToast(DetailsActivity.this, R.string.favorite_success);
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

    @Override
    public void onResponse(String response) {
        if (isEmpty(response)) {
            setError(getString(R.string.parse_error));
            return;
        }

        hideLoadingView();

        try {
            JSONObject jObj = new JSONObject(response);
            JSONObject dataObj = jObj.getJSONObject("data");
            mArticle = parseU148Content(dataObj.getString("content"));
            renderPage();
        } catch (JSONException e) {
        }
    }

    private void renderPage() {
        String template = Utils.readFromAssets(this, "usite.html");
        template = template.replace("{TITLE}", mFeed.title);

        final UserInfo usr = mFeed.usr;
        if (null != usr) {
            long t = mFeed.create_time * 1000L;
            Date date = new Date(t);
            Format format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
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

        final int mode = Config.getThemeMode(this);
        if (Constants.MODE_NIGHT == mode) {
            template = template.replace("{SCREEN_MODE}", "night");
        } else {
            template = template.replace("{SCREEN_MODE}", "");
        }

        mWebView.loadDataWithBaseURL(null, template, "text/html", "UTF-8", null);
        mWebView.setWebChromeClient(new MyWebChromeClient());
    }

    private class MyWebChromeClient extends WebChromeClient {

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
    public void onErrorResponse() {
        setError();
    }

    @Override
    public void onImageClicked(String url) {
        Intent intent = new Intent(this, ImageActivity.class);
        intent.putExtra("imgsrc", url);
        intent.putStringArrayListExtra("images", mArticle.imageList);
        startActivity(intent);
    }

    @Override
    public void onThemeChange() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int mode = Config.getThemeMode(DetailsActivity.this);
                if (mode == Constants.MODE_NIGHT) {
                    mWebView.loadUrl("javascript:setScreenMode('night')");
                } else {
                    mWebView.loadUrl("javascript:setScreenMode('day')");
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_bar) {
            mWebView.scrollTo(0, 0);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDatabase.updateArticleOffset(mFeed.id, mWebView.getScrollY());
    }

    @Override
    protected void applyTheme() {
        super.applyTheme();
        initButtons();
    }

    private void initButtons() {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.right_view);
        layout.removeAllViews();

        int[] icons1 = {R.mipmap.ic_social_share, R.mipmap.ic_favorite,
                R.mipmap.ic_comment};
        int[] icons2 = {R.mipmap.ic_share_night, R.mipmap.ic_favorite_night,
                R.mipmap.ic_comment_night};
        Feed feed = mDatabase.getFavoriteById(mFeed.id);
        if (null != feed && !isEmpty(feed.id)) {
            icons1[1] = R.mipmap.ic_favorite_full;
            icons2[1] = R.mipmap.ic_favorite_full;
        }

        int theme = Config.getThemeMode(this);
        for (int i = 0; i < icons1.length; i++) {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(dp2px(48), MATCH_PARENT);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            lp.rightMargin = i * dp2px(48);
            layout.addView(getImageButton(theme == MODE_DAY ?
                    icons1[i] : icons2[i]), lp);
        }

        if (null != mFeed && mFeed.count_review > 0) {
            int num = mFeed.count_review;
            if (num >= 100) num = 99;

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(dp2px(12),
                    dp2px(12));
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            lp.rightMargin = dp2px(48) * 2 + dp2px(4);
            lp.topMargin = dp2px(8);
            CircleView numView = new CircleView(this);
            numView.setNumber(num);
            layout.addView(numView, lp);
        }
    }
}
