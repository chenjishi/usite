package com.chenjishi.u148.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.model.Feed;
import com.chenjishi.u148.model.FeedDoc;
import com.chenjishi.u148.util.Constants;
import com.chenjishi.u148.util.HttpUtils;
import com.chenjishi.u148.util.Utils;
import com.chenjishi.u148.volley.Response;
import com.chenjishi.u148.volley.VolleyError;
import com.flurry.android.FlurryAgent;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenjishi on 15/2/3.
 */
public class SearchActivity extends BaseActivity implements Response.Listener<FeedDoc>, AbsListView.OnScrollListener,
        Response.ErrorListener, AdapterView.OnItemClickListener {
    private ListView mListView;
    private EditText mEditText;
    private View mFootView;
    private View mEmptyView;

    private FeedListAdapter mListAdapter;

    private int mPage = 1;
    private String mKeyword;
    private int mLastItemIndex;
    private boolean mIsDataLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_search);
        setTitle(R.string.search);

        mEditText = (EditText) findViewById(R.id.edit_search);
        mListAdapter = new FeedListAdapter(this);

        mEmptyView = LayoutInflater.from(this).inflate(R.layout.empty_view, null);
        mFootView = LayoutInflater.from(this).inflate(R.layout.load_more, null);
        TextView footLabel = (TextView) mFootView.findViewById(R.id.loading_text);
        mFootView.setVisibility(View.GONE);

        mListView = (ListView) findViewById(R.id.search_list);
        mListView.addFooterView(mFootView, null, false);
        ((ViewGroup) mListView.getParent()).addView(mEmptyView);
        mListView.setEmptyView(mEmptyView);

        if (Constants.MODE_NIGHT == PrefsUtil.getThemeMode()) {
            mListView.setDivider(getResources().getDrawable(R.drawable.split_color_night));
            footLabel.setTextColor(getResources().getColor(R.color.text_color_summary));
        } else {
            mListView.setDivider(getResources().getDrawable(R.drawable.split_color));
            footLabel.setTextColor(getResources().getColor(R.color.text_color_regular));
        }

        mListView.setDividerHeight(1);

        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(this);
        mListView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
    }

    public void onSearchClicked(View view) {
        if (mIsDataLoading) return;

        String text = mEditText.getText().toString();
        if (TextUtils.isEmpty(text)) return;

        if (mListView.getVisibility() == View.GONE) {
            mListView.setVisibility(View.VISIBLE);
        }

        String keyword;
        try {
            keyword = URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            keyword = "";
        }

        if (TextUtils.isEmpty(keyword)) return;

        mKeyword = keyword;
        mListAdapter.clearData();
        mFootView.setVisibility(View.GONE);
        request();
    }

    private void request() {
        mIsDataLoading = true;
        if (mPage > 1) mFootView.setVisibility(View.VISIBLE);

        String url = String.format(Constants.API_SEARCH, mPage, mKeyword);
        Log.i("test", "#url  " + url);
        HttpUtils.get(url, FeedDoc.class, this, this);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE && mLastItemIndex == mListAdapter.getCount()) {
            if (!mIsDataLoading) {
                mPage++;
                request();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mLastItemIndex = firstVisibleItem + visibleItemCount - 1;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Utils.setErrorView(mEmptyView, getString(R.string.net_error));
        mFootView.setVisibility(View.GONE);
        mIsDataLoading = false;
        mFootView.setVisibility(View.GONE);
    }

    @Override
    public void onResponse(FeedDoc response) {
        if (null != response && null != response.data) {
            final List<Feed> feedList = response.data.data;
            if (null != feedList && feedList.size() > 0) {
                mListAdapter.addData(feedList);
            }
        } else {
            Utils.setErrorView(mEmptyView, getString(R.string.parse_error));
        }

        mFootView.setVisibility(View.GONE);
        mIsDataLoading = false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Feed feed = mListAdapter.getItem(position);

        Map<String, String> params = new HashMap<String, String>();
        params.put("author", feed.usr.nickname);
        params.put("title", feed.title);
        FlurryAgent.logEvent("read_article", params);

        final Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(Constants.KEY_FEED, feed);
        startActivity(intent);
    }
}