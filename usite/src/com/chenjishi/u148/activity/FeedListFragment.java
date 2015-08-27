package com.chenjishi.u148.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
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
        Response.Listener<FeedDoc>, Response.ErrorListener, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout swipeRefreshLayout;
    private FeedListAdapter listAdapter;
    private View footView;
    private View emptyView;

    protected int page = 1;
    private int category;
    private boolean dataLoaded;

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
        Button loadBtn = (Button) footView.findViewById(R.id.btn_load);
        loadBtn.setOnClickListener(this);

        listView.addFooterView(footView);
        listView.setEmptyView(emptyView);

        if (Constants.MODE_NIGHT == PrefsUtil.getThemeMode()) {
            view.setBackgroundColor(getResources().getColor(R.color.background_night));
            listView.setDivider(getResources().getDrawable(R.drawable.split_color_night));
            loadBtn.setBackgroundResource(R.drawable.btn_gray_night);
            loadBtn.setTextColor(getResources().getColor(R.color.text_color_summary));
        } else {
            view.setBackgroundColor(getResources().getColor(R.color.background));
            listView.setDivider(getResources().getDrawable(R.drawable.split_color));
            loadBtn.setBackgroundResource(R.drawable.btn_gray);
            loadBtn.setTextColor(getResources().getColor(R.color.text_color_regular));
        }

        listView.setDividerHeight(1);

        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(this);
        swipeRefreshLayout.setOnRefreshListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_load) {
            footView.findViewById(R.id.btn_load).setVisibility(View.GONE);
            footView.findViewById(R.id.loading_layout).setVisibility(View.VISIBLE);
            page++;
            request();
        }
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

        footView.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onResponse(FeedDoc response) {
        if (null != response && null != response.data) {
            if (1 == page) listAdapter.clearData();

            dataLoaded = true;

            final List<Feed> feedList = response.data.data;
            if (null != feedList && feedList.size() > 0) {
                listAdapter.addData(feedList);

                int size = feedList.size();
                if (size < 12) {
                    footView.setVisibility(View.GONE);
                } else {
                    footView.findViewById(R.id.loading_layout).setVisibility(View.GONE);
                    footView.findViewById(R.id.btn_load).setVisibility(View.VISIBLE);
                    footView.setVisibility(View.VISIBLE);
                }
            } else {
                footView.setVisibility(View.GONE);
            }
        } else {
            Utils.setErrorView(emptyView, getString(R.string.parse_error));
            footView.setVisibility(View.GONE);
        }

        swipeRefreshLayout.setRefreshing(false);
    }
}
