package com.chenjishi.u148.article;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import com.chenjishi.u148.BaseActivity;
import com.chenjishi.u148.Config;
import com.chenjishi.u148.R;
import com.chenjishi.u148.comment.CommentActivity;
import com.chenjishi.u148.home.Feed;
import com.chenjishi.u148.home.UserInfo;
import com.chenjishi.u148.utils.*;
import com.chenjishi.u148.widget.CircleView;
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
import java.util.Locale;

import static android.text.TextUtils.isEmpty;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.chenjishi.u148.utils.Constants.*;
import static com.chenjishi.u148.utils.Utils.showToast;

/**
 * Created by jishichen on 2017/4/14.
 */
public class DetailsActivity extends BaseActivity implements Listener<String>, ErrorListener,
        JSCallback, View.OnClickListener {
    private static final int TAG_SHARE = 233;
    private static final int TAG_FAVORITE = 234;
    private static final int TAG_COMMENT = 235;

    private WebView mWebView;

    private Article mArticle;
    private Feed mFeed;

    private DBHelper mDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mDatabase = DBHelper.getInstance(this);
        mFeed = getIntent().getExtras().getParcelable(Constants.KEY_FEED);

        SparseArray<String> array = new SparseArray<>();
        int[] ids = getResources().getIntArray(R.array.category_id);
        String[] names = getResources().getStringArray(R.array.category_name);
        for (int i = 0; i < ids.length; i++) {
            array.put(ids[i], names[i]);
        }

        String title = array.get(mFeed.category);
        if (null == mFeed.usr) title = getString(R.string.back);
        setTitle(title);

        mWebView = (WebView) findViewById(R.id.web_view);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new JavaScriptBridge(this), "U148");

        findViewById(R.id.title_bar).setOnClickListener(this);

        showLoadingView();
        NetworkRequest.getInstance().get(String.format(API_ARTICLE, mFeed.id), this, this);
    }

    private void addToFavorite() {
        UserInfo user = Config.getUser(this);
        if (null == user || isEmpty(user.token)) {
            showToast(this, R.string.login_to_favorite);
            return;
        }

        if (mDatabase.isFavorite(mFeed.id)) {
            showToast(this, R.string.favorite_already);
            return;
        }

        String url = String.format(API_ADD_FAVORITE, mFeed.id, user.token);
        NetworkRequest.getInstance().get(url, new Listener<String>() {
            @Override
            public void onResponse(String response) {
                mDatabase.insert(mFeed);
                applyTheme();
                showToast(DetailsActivity.this, R.string.favorite_success);
            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse() {

            }
        });
    }

    private void share() {
//        Map<String, String> params = new HashMap<>();
//        params.put(PARAM_TITLE, mFeed.title);
//        FlurryAgent.logEvent(EVENT_ARTICLE_SHARE, params);
//
//        ShareDialog dialog = new ShareDialog(this);
//        ArrayList<String> imageList = mArticle.imageList;
//        if (null == imageList || imageList.size() == 0) {
//            imageList.add(mFeed.pic_mid);
//        }
//
//        dialog.setShareFeed(mFeed);
//        dialog.setImageList(imageList);
//        dialog.show();
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

        UserInfo usr = mFeed.usr;
        String author = "", review = "";
        if (null != usr) {
            Date date = new Date(mFeed.create_time * 1000);
            Format format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            author = String.format(getString(R.string.pub_time), mFeed.usr.nickname,
                    format.format(date));
            review = String.format(getString(R.string.pub_reviews), mFeed.count_browse,
                    mFeed.count_review);
        }
        template = template.replace("{U_AUTHOR}", author);
        template = template.replace("{U_COMMENT}", review);
        template = template.replace("{CONTENT}", mArticle.content);

        int mode = Config.getThemeMode(this);
        template = template.replace("{SCREEN_MODE}", mode == MODE_DAY ? "" : "night");

        mWebView.loadDataWithBaseURL(null, template, "text/html", "UTF-8", null);
        mWebView.setWebChromeClient(new MyWebChromeClient());
    }

    private class MyWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                int offset = mDatabase.getOffsetById(mFeed.id);
                if (offset > 0) mWebView.scrollTo(0, offset);
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
        article.imageList = new ArrayList<>();

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
        Intent intent = new Intent(this, ImageBrowseActivity.class);
        intent.putExtra("imgsrc", url);
        intent.putStringArrayListExtra("images", mArticle.imageList);
        startActivity(intent);
    }

    @Override
    public void onThemeChange() {
        final int mode = Config.getThemeMode(this);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:setScreenMode('" +
                        (mode == MODE_DAY ? "day" : "night") + "')");
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_bar) {
            mWebView.scrollTo(0, 0);
            return;
        }

        if (null == v.getTag()) return;

        int idx = (Integer) v.getTag();
        switch (idx) {
            case TAG_COMMENT:
                Intent intent = new Intent(this, CommentActivity.class);
                intent.putExtra("article_id", mFeed.id);
                startActivity(intent);
                break;
            case TAG_FAVORITE:
                addToFavorite();
                break;
            case TAG_SHARE:
                share();
                break;
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
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.right_view);
        layout.removeAllViews();

        int[] icons1 = {R.drawable.ic_social_share, R.drawable.ic_favorite,
                R.drawable.ic_comment};
        int[] icons2 = {R.drawable.ic_share_night, R.drawable.ic_favorite_night,
                R.drawable.ic_comment_night};
        Feed feed = mDatabase.getFavoriteById(mFeed.id);
        if (null != feed && !isEmpty(feed.id)) {
            icons1[1] = R.drawable.ic_favorite_full;
            icons2[1] = R.drawable.ic_favorite_full;
        }

        int theme = Config.getThemeMode(this);
        for (int i = 0; i < icons1.length; i++) {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(dp2px(48), MATCH_PARENT);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            lp.rightMargin = i * dp2px(48);
            ImageButton button = getImageButton(theme == MODE_DAY ?
                    icons1[i] : icons2[i]);
            button.setTag(i + TAG_SHARE);
            button.setOnClickListener(this);
            layout.addView(button, lp);
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
