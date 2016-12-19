package com.chenjishi.u148.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import com.chenjishi.u148.R;
import com.chenjishi.u148.model.Feed;
import com.chenjishi.u148.model.FeedDoc;
import com.chenjishi.u148.util.Constants;
import com.chenjishi.u148.util.ErrorListener;
import com.chenjishi.u148.util.Listener;
import com.chenjishi.u148.util.NetworkRequest;
import com.chenjishi.u148.view.DividerItemDecoration;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by chenjishi on 15/2/3.
 */
public class SearchActivity extends SlidingActivity implements Listener<FeedDoc>, ErrorListener, OnPageEndListener {
    private EditText mEditText;

    private FeedListAdapter mListAdapter;

    private int mPage = 1;
    private String mKeyword;

    private OnListScrollListener mScrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_search);
        setTitle(R.string.search);

        mEditText = (EditText) findViewById(R.id.edit_search);
        mListAdapter = new FeedListAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mScrollListener = new OnListScrollListener(layoutManager);
        mScrollListener.setOnPageEndListener(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.search_list_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mListAdapter);
        recyclerView.addOnScrollListener(mScrollListener);
    }

    @Override
    public void onPageEnd() {
        mPage += 1;
        request();
    }

    public void onSearchClicked(View view) {
        if (mScrollListener.getIsLoading()) return;

        String text = mEditText.getText().toString();
        if (TextUtils.isEmpty(text)) return;

        String keyword;
        try {
            keyword = URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            keyword = "";
        }

        if (TextUtils.isEmpty(keyword)) return;

        mKeyword = keyword;
        mListAdapter.clear();
        request();
    }

    private void request() {
        mScrollListener.setIsLoading(true);

        String url = String.format(Constants.API_SEARCH, mPage, mKeyword);
        NetworkRequest.getInstance().get(url, FeedDoc.class, this, this);
    }

    @Override
    public void onErrorResponse() {
//        Utils.setErrorView(mEmptyView, getString(R.string.net_error));
        mScrollListener.setIsLoading(false);
    }

    @Override
    public void onResponse(FeedDoc response) {
        if (null != response && null != response.data) {
            final List<Feed> feedList = response.data.data;
            if (null != feedList && feedList.size() > 0) {
                mListAdapter.addData(feedList);
            }
        } else {
//            Utils.setErrorView(mEmptyView, getString(R.string.parse_error));
        }
        mScrollListener.setIsLoading(false);
    }
}
