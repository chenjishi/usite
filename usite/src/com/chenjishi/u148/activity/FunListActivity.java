package com.chenjishi.u148.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.FileCache;
import com.chenjishi.u148.model.Feed;
import com.chenjishi.u148.parser.FeedItemParser;
import com.chenjishi.u148.pulltorefresh.PullToRefreshBase;
import com.chenjishi.u148.pulltorefresh.PullToRefreshListView;
import com.chenjishi.u148.util.FileUtils;
import com.chenjishi.u148.util.HttpUtils;
import com.chenjishi.u148.volley.Response;
import com.chenjishi.u148.volley.VolleyError;
import com.chenjishi.u148.volley.toolbox.ImageLoader;
import com.flurry.android.FlurryAgent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.chenjishi.u148.util.Constants.*;

/**
 * Created by chenjishi on 13-12-7.
 */
public class FunListActivity extends BaseActivity implements Response.Listener<ArrayList<Feed>>,
        Response.ErrorListener, AbsListView.OnScrollListener,
        PullToRefreshBase.OnRefreshListener, AdapterView.OnItemClickListener {
    private static final String JIAN_DAN = "http://jandan.net/page/%1$d";
    private static final String NEWS = "http://news.cnblogs.com/n/page/%1$d/";

    private ArrayList<Feed> mFeedList = new ArrayList<Feed>();

    private FunAdapter mAdapter;

    private PullToRefreshListView mRefreshView;

    private View mEmptyView;
    private View mFootView;

    private int lastItemIndex;
    private int currentPage = 1;

    private int mSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSource = getIntent().getIntExtra("source", SOURCE_JIANDAN);
        setTitleText(mSource == SOURCE_JIANDAN ? R.string.jian_dan : R.string.news);

        mEmptyView = LayoutInflater.from(this).inflate(R.layout.empty_view, null);
        mRefreshView = (PullToRefreshListView) findViewById(R.id.list_fun);
        ListView listView = mRefreshView.getRefreshableView();
        ((ViewGroup) listView.getParent()).addView(mEmptyView);
        listView.setEmptyView(mEmptyView);

        mFootView = LayoutInflater.from(this).inflate(R.layout.load_more, null);
        mFootView.setVisibility(View.GONE);
        listView.addFooterView(mFootView);

        mAdapter = new FunAdapter(this);
        listView.setAdapter(mAdapter);
        listView.setOnScrollListener(this);
        listView.setOnItemClickListener(this);

        mRefreshView.setOnRefreshListener(this);

        String path = FileCache.getDataCacheDirectory(FunListActivity.this) +
                (mSource == SOURCE_NEWS ? CACHED_NEWS : CACHED_JIANDAN);
        File file = new File(path);
        if (file.exists()) {
            loadCacheData(path);
        } else {
            loadData();
        }
    }

    private String getUrl() {
        return String.format(mSource == SOURCE_NEWS ? NEWS : JIAN_DAN, currentPage);
    }

    private void loadCacheData(final String path) {
        new Thread() {
            @Override
            public void run() {
                String html = FileUtils.readFromFile(path);
                ArrayList<Feed> tmpList = null;
                if (mSource == SOURCE_JIANDAN) {
                    tmpList = FeedItemParser.parseJianDanFeed(html);
                }

                if (mSource == SOURCE_NEWS) {
                    tmpList = FeedItemParser.parseNews(html);
                }

                if (null == tmpList || tmpList.size() == 0) return;

                for (Feed feed : tmpList) {
                    if (null != feed.title) {
                        mFeedList.add(feed);
                    }
                }

                Handler mainThread = new Handler(Looper.getMainLooper());
                mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }.start();
    }

    private void loadData() {
        mFootView.setVisibility(View.VISIBLE);
        HttpUtils.getFeed(mSource, getUrl(), this, this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Feed item = mFeedList.get(position - 1);
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("title", item.title);
        intent.putExtra("link", item.url);
        intent.putExtra("source", mSource);

        Map<String, String> params = new HashMap<String, String>();
        params.put("title", item.title);
        FlurryAgent.logEvent("read_article", params);

        startActivity(intent);
    }

    @Override
    public void onRefresh(PullToRefreshBase refreshView) {
        if (mFeedList.size() > 0) {
            mFeedList.clear();
            mAdapter.notifyDataSetChanged();
        }

        currentPage = 1;
        loadData();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fun_list_activity;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (SCROLL_STATE_IDLE == scrollState && lastItemIndex > mFeedList.size() - 1) {
            currentPage++;
            loadData();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        lastItemIndex = firstVisibleItem + visibleItemCount - 1;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mEmptyView.findViewById(R.id.progress_bar).setVisibility(View.GONE);
        ((TextView) mEmptyView.findViewById(R.id.tv_empty_tip)).setText("访问出错了哦~");
    }

    @Override
    public void onResponse(ArrayList<Feed> response) {
        if (null == response || response.size() == 0) return;

        for (Feed feed : response) {
            if (null != feed.title) {
                mFeedList.add(feed);
            }
        }

        mFootView.setVisibility(View.GONE);
        mRefreshView.onRefreshComplete();
        mAdapter.notifyDataSetChanged();
    }

    private class FunAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        public FunAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mFeedList.size();
        }

        @Override
        public Feed getItem(int position) {
            return mFeedList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (null == convertView) {
                convertView = inflater.inflate(R.layout.fun_list_cell, parent, false);
                holder = new ViewHolder();

                holder.feedImage = (ImageView) convertView.findViewById(R.id.feed_image);
                holder.feedTitle = (TextView) convertView.findViewById(R.id.feed_title);
                holder.feedDesc = (TextView) convertView.findViewById(R.id.feed_desc);

                convertView.setTag(holder);
            }

            holder = (ViewHolder) convertView.getTag();

            Feed feed = getItem(position);

            holder.feedTitle.setTextColor(mSource == SOURCE_JIANDAN ? 0xFFDD1111 : 0xFF075DB3);

            final ImageLoader imageLoader = HttpUtils.getImageLoader();
            int placeHolder = mSource == SOURCE_JIANDAN ? R.drawable.logo_eggs : R.drawable.news_logo;
            imageLoader.get(feed.thumbUrl, ImageLoader.getImageListener(holder.feedImage,
                    placeHolder, placeHolder));

            holder.feedTitle.setText(feed.title);
            holder.feedDesc.setText(feed.description);

            return convertView;
        }
    }

    private static class ViewHolder {
        public ImageView feedImage;
        public TextView feedTitle;
        public TextView feedDesc;
    }
}
