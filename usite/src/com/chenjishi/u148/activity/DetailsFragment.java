package com.chenjishi.u148.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.model.Article;
import com.chenjishi.u148.model.FeedItem;
import com.chenjishi.u148.model.UserInfo;
import com.chenjishi.u148.util.*;
import com.chenjishi.u148.view.ArticleWebView;
import com.chenjishi.u148.volley.Response;
import com.chenjishi.u148.volley.VolleyError;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.chenjishi.u148.util.Constants.KEY_FEED;

/**
 * Created by chenjishi on 14-4-25.
 */
public class DetailsFragment extends Fragment implements Response.Listener<Article>,
        Response.ErrorListener, JSCallback {
    private final static String REQUEST_URL = "http://www.u148.net/json/article/%1$s";
    private FeedItem mFeed;
    private Article mArticle;

    private ArrayList<String> mImageUrls;

    private ArticleWebView mWebView;
    private View mEmptyView;

    private JavaScriptBridge mJsBridge;

    private OnMusicClickListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (OnMusicClickListener) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        mFeed = bundle.getParcelable(KEY_FEED);
        mJsBridge = new JavaScriptBridge(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        view.setBackgroundResource(Constants.MODE_NIGHT == PrefsUtil.getThemeMode() ?
        R.color.background_night : R.color.background);

        mEmptyView = view.findViewById(R.id.empty_view);

        mWebView = (ArticleWebView) view.findViewById(R.id.webview);
        mWebView.addJavascriptInterface(mJsBridge, "U148");
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        HttpUtils.ArticleRequest(String.format(REQUEST_URL, mFeed.id), this, this);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Utils.setErrorView(mEmptyView, R.string.netop_network_error);
    }

    @Override
    public void onResponse(Article response) {
        if (null != response && !TextUtils.isEmpty(response.content)) {
            mArticle = response;
            mImageUrls = mArticle.imageList;

            renderPage();
        } else {
            Utils.setErrorView(mEmptyView, R.string.netop_network_error);
        }
    }

    public ArrayList<String> getImageList() {
        return mImageUrls;
    }

    private void renderPage() {
        String template = Utils.readFromAssets(getActivity(), "usite.html");

        if (TextUtils.isEmpty(template)) return;

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

        mEmptyView.setVisibility(View.GONE);
        mWebView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onImageClicked(String url) {
        Intent intent = new Intent(getActivity(), ImageActivity.class);
        intent.putExtra("imgsrc", url);
        intent.putStringArrayListExtra("images", mArticle.imageList);
        startActivity(intent);
    }

    @Override
    public void onMusicClicked(String url) {
        if (null == mListener) return;

        mListener.onMusicClicked(url);
    }

    @Override
    public void onVideoClicked(String url) {
    }

    @Override
    public void onThemeChange() {
        getActivity().runOnUiThread(new Runnable() {
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
}
