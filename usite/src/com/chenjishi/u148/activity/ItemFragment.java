package com.chenjishi.u148.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.FileCache;
import com.chenjishi.u148.entity.FeedItem;
import com.chenjishi.u148.parser.FeedItemParser;
import com.chenjishi.u148.pulltorefresh.PullToRefreshBase;
import com.chenjishi.u148.pulltorefresh.PullToRefreshListView;
import com.chenjishi.u148.service.DataCacheService;
import com.chenjishi.u148.util.ConstantUtils;
import com.chenjishi.u148.util.FileUtils;
import com.chenjishi.u148.util.HttpUtils;
import com.chenjishi.u148.volley.toolbox.ImageLoader;
import com.flurry.android.FlurryAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-11-28
 * Time: 下午3:20
 * To change this template use File | Settings | File Templates.
 */
public class ItemFragment extends Fragment implements AbsListView.OnScrollListener,
        PullToRefreshBase.OnRefreshListener, AdapterView.OnItemClickListener {
    private static final int MSG_LOAD_OK = 1;

    private PullToRefreshListView pullToRefresh;
    private FeedListAdapter listAdapter;
    private View footView;

    private ArrayList<FeedItem> feedItems = new ArrayList<FeedItem>();
    private String[] urls = {
            "/list/",
            "/video/",
            "/image/",
            "/audio/",
            "/text/",
            "/mix/"
    };

    private int lastItemIndex;
    private int currentPage = 1;
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

        pullToRefresh = (PullToRefreshListView) view.findViewById(R.id.lv_feeds);
        ListView listView = pullToRefresh.getRefreshableView();

        footView = inflater.inflate(R.layout.load_more, null);
        footView.setVisibility(View.GONE);
        listView.addFooterView(footView);

        listView.setAdapter(listAdapter);
        listView.setOnScrollListener(this);
        listView.setOnItemClickListener(this);

        pullToRefresh.setOnRefreshListener(this);

        return view;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (SCROLL_STATE_IDLE == scrollState && lastItemIndex > feedItems.size() - 1) {
            currentPage++;
            loadData();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        lastItemIndex = firstVisibleItem + visibleItemCount - 1;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FeedItem item = feedItems.get(position - 1);
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra("title", item.title);
        intent.putExtra("link", item.link);

        Map<String, String> params = new HashMap<String, String>();
        params.put("title", item.title);
        FlurryAgent.logEvent("read_article", params);

        startActivity(intent);
    }

    @Override
    public void onRefresh(PullToRefreshBase refreshView) {
        DataCacheService.getInstance().clearCaches();

        if (feedItems.size() > 0) {
            feedItems.clear();
            listAdapter.notifyDataSetChanged();
        }
        currentPage = 1;
        loadData();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!dataLoaded) {
            if (0 == category) {
                loadCacheData();
            } else {
                loadData();
            }
        }
    }

    private void loadCacheData() {
        String path = FileCache.getDataCacheDirectory(getActivity()) + ConstantUtils.CACHED_FILE_NAME;
        final String data = FileUtils.readFromFile(path);
        if (null != data) {
            new Thread(){
                @Override
                public void run() {
                    ArrayList<FeedItem> tmpList = FeedItemParser.parseFeedList(data);
                    if (null != tmpList && tmpList.size() > 0) {
                        feedItems.addAll(tmpList);
                    }
                    mHandler.sendEmptyMessage(MSG_LOAD_OK);
                }
            }.start();
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what != MSG_LOAD_OK) return;

            if (feedItems.size() > 0) {
                dataLoaded = true;
                listAdapter.notifyDataSetChanged();
            } else {
                //todo
            }

            footView.setVisibility(View.GONE);
            pullToRefresh.onRefreshComplete();
        }
    };

    public void loadData() {
        footView.setVisibility(View.VISIBLE);
        new Thread(){
            @Override
            public void run() {
                ArrayList<FeedItem> tmpList = DataCacheService.getInstance().getFeedItemList(getUrl());
                if (null != tmpList && tmpList.size() > 0) {
                    feedItems.addAll(tmpList);
                }
                mHandler.sendEmptyMessage(MSG_LOAD_OK);
            }
        }.start();
    }

    private String getUrl() {
        return ConstantUtils.BASE_URL + urls[category] + currentPage + ".html";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    private class FeedListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
        private LayoutInflater mInflater;

        public FeedListAdapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return null == feedItems ? 0 : feedItems.size();
        }

        @Override
        public FeedItem getItem(int position) {
            return null == feedItems ? null : feedItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.feed_list_item, null);
                holder = new ViewHolder();

                holder.thumb = (ImageView) convertView.findViewById(R.id.feed_image);
                holder.category = (TextView) convertView.findViewById(R.id.feed_type);
                holder.title = (TextView) convertView.findViewById(R.id.feed_title);
                holder.author = (TextView) convertView.findViewById(R.id.feed_author);
                holder.time = (TextView) convertView.findViewById(R.id.feed_time);
                holder.content = (TextView) convertView.findViewById(R.id.feed_content);

                convertView.setTag(holder);
            }

            holder = (ViewHolder) convertView.getTag();

            FeedItem feed = getItem(position);

            holder.category.setText(feed.category);
            holder.title.setText(feed.title);
            holder.author.setText(feed.author);
            holder.time.setText(feed.time);
            holder.content.setText(feed.summary);

            HttpUtils.getImageLoader().get(feed.imageUrl, ImageLoader.getImageListener(holder.thumb,
                    R.drawable.pictrue_bg, R.drawable.pictrue_bg));

            return convertView;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        }
    }

    private static class ViewHolder {
        ImageView thumb;
        TextView category;
        TextView title;
        TextView author;
        TextView time;
        TextView content;
    }
}
