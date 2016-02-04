package com.chenjishi.u148.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import com.chenjishi.u148.R;
import com.chenjishi.u148.model.Feed;
import com.chenjishi.u148.model.FeedDoc;
import com.chenjishi.u148.util.HttpUtils;
import com.chenjishi.u148.view.DividerItemDecoration;
import com.chenjishi.u148.view.LoadingView;
import com.chenjishi.u148.volley.Response.ErrorListener;
import com.chenjishi.u148.volley.Response.Listener;
import com.chenjishi.u148.volley.VolleyError;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.chenjishi.u148.util.Constants.API_FEED_LIST;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-11-28
 * Time: 下午3:20
 * To change this template use File | Settings | File Templates.
 */
public class FeedListFragment extends Fragment implements Listener<FeedDoc>, ErrorListener, SwipeRefreshLayout.OnRefreshListener, OnPageEndListener {
    private SwipeRefreshLayout swipeRefreshLayout;

    private FrameLayout mContentView;

    protected int mPage = 1;
    private int category;
    private boolean dataLoaded;

    private LoadingView mLoadingView;

    private FeedListAdapter mListAdapter;

    private OnListScrollListener mScrollListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        category = bundle != null ? bundle.getInt("category") : 0;
        dataLoaded = false;

        mListAdapter = new FeedListAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed_list, container, false);
        mContentView = (FrameLayout) view.findViewById(R.id.content_layout);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mScrollListener = new OnListScrollListener(layoutManager);
        mScrollListener.setOnPageEndListener(this);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.feed_list_view);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mListAdapter);
        recyclerView.addOnScrollListener(mScrollListener);

        swipeRefreshLayout.setOnRefreshListener(this);

        return view;
    }

    @Override
    public void onPageEnd() {
        mPage += 1;
        request();
    }

    @Override
    public void onRefresh() {
        mPage = 1;
        request();
    }

    private void request() {
        mScrollListener.setIsLoading(true);
        HttpUtils.get(String.format(API_FEED_LIST, category, mPage), FeedDoc.class, this, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!dataLoaded) {
            swipeRefreshLayout.setRefreshing(true);
            showLoading();
            request();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if (mListAdapter.getItemCount() > 1) {
            hideLoading();
        } else {
            mLoadingView.setError(getString(R.string.net_error));
        }

        swipeRefreshLayout.setRefreshing(false);
        mScrollListener.setIsLoading(false);
    }

    @Override
    public void onResponse(FeedDoc response) {
        if (null != response && null != response.data) {
            if (1 == mPage) mListAdapter.clear();

            dataLoaded = true;

            List<Feed> feedList = new ArrayList<Feed>();
            final List<Feed> tempList = response.data.data;
            if (null != tempList && tempList.size() > 0) {
                feedList.addAll(tempList);
                mListAdapter.addData(feedList);
            }
        }

        swipeRefreshLayout.setRefreshing(false);
        mScrollListener.setIsLoading(false);
        hideLoading();
    }

    private void showLoading() {
        if (null == mLoadingView) {
            mLoadingView = new LoadingView(getContext());
        }

        if (null != mLoadingView.getParent()) {
            ((ViewGroup) mLoadingView.getParent()).removeView(mLoadingView);
        }

        mContentView.addView(mLoadingView, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
    }

    private void hideLoading() {
        if (null == mLoadingView) return;

        mContentView.removeView(mLoadingView);
    }
}
