package com.chenjishi.u148.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.chenjishi.u148.util.Constants.API_FEED_LIST;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-11-28
 * Time: 下午3:20
 * To change this template use File | Settings | File Templates.
 */
public class FeedListFragment extends Fragment implements AdapterView.OnItemClickListener,
        Response.Listener<FeedDoc>, Response.ErrorListener, SwipeRefreshLayout.OnRefreshListener,
        AbsListView.OnScrollListener {
    private SwipeRefreshLayout swipeRefreshLayout;
    private FeedListAdapter listAdapter;
    private View footView;
    private View emptyView;

    protected int page = 1;
    private int category;
    private boolean dataLoaded;

    private int mLastItemIndex;
    private boolean mIsDataLoading;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        category = bundle != null ? bundle.getInt("category") : 0;
        dataLoaded = false;

        listAdapter = new FeedListAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        ListView listView = (ListView) view.findViewById(R.id.list_feed);

        emptyView = view.findViewById(R.id.empty_view);
        footView = inflater.inflate(R.layout.load_more, null);
        TextView footLabel = (TextView) footView.findViewById(R.id.loading_text);

        listView.addFooterView(footView, null, false);
        footView.setVisibility(View.GONE);

        listView.setEmptyView(emptyView);

        if (Constants.MODE_NIGHT == PrefsUtil.getThemeMode()) {
            view.setBackgroundColor(getResources().getColor(R.color.background_night));
            listView.setDivider(getResources().getDrawable(R.drawable.split_color_night));
            footLabel.setTextColor(getResources().getColor(R.color.text_color_summary));
        } else {
            view.setBackgroundColor(getResources().getColor(R.color.background));
            listView.setDivider(getResources().getDrawable(R.drawable.split_color));
            footLabel.setTextColor(getResources().getColor(R.color.text_color_regular));
        }

        listView.setDividerHeight(1);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(this);
        swipeRefreshLayout.setOnRefreshListener(this);

        return view;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE && mLastItemIndex == listAdapter.getCount()) {
            if (!mIsDataLoading) {
                page++;
                request();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mLastItemIndex = firstVisibleItem + visibleItemCount - 1;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Feed feed = listAdapter.getItem(position);

        Map<String, String> params = new HashMap<String, String>();
        params.put("author", feed.usr.nickname);
        params.put("title", feed.title);
        FlurryAgent.logEvent("read_article", params);

        final Intent intent = new Intent(getActivity(), DetailsActivity.class);
        intent.putExtra(Constants.KEY_FEED, feed);
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        page = 1;
        request();
    }

    private void request() {
        mIsDataLoading = true;

        if (page > 1) footView.setVisibility(View.VISIBLE);

        String url = String.format(API_FEED_LIST, category, page);
        HttpUtils.get(url, FeedDoc.class, this, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!dataLoaded) {
            swipeRefreshLayout.setRefreshing(true);
            footView.setVisibility(View.GONE);
            request();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Utils.setErrorView(emptyView, getString(R.string.net_error));

        swipeRefreshLayout.setRefreshing(false);
        mIsDataLoading = false;
        footView.setVisibility(View.GONE);
    }

    @Override
    public void onResponse(FeedDoc response) {
        if (null != response && null != response.data) {
            if (1 == page) listAdapter.clearData();

            dataLoaded = true;

            final List<Feed> feedList = response.data.data;
            if (null != feedList && feedList.size() > 0) {
                listAdapter.addData(feedList);
            }
        } else {
            Utils.setErrorView(emptyView, getString(R.string.parse_error));
        }

        footView.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
        mIsDataLoading = false;
    }

}
