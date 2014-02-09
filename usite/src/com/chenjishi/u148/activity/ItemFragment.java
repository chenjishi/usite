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
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.model.Feed;
import com.chenjishi.u148.parser.JsonParser;
import com.chenjishi.u148.pulltorefresh.PullToRefreshBase;
import com.chenjishi.u148.pulltorefresh.PullToRefreshListView;
import com.chenjishi.u148.util.*;
import com.chenjishi.u148.volley.Response;
import com.chenjishi.u148.volley.VolleyError;
import com.chenjishi.u148.volley.toolbox.ImageLoader;
import com.flurry.android.FlurryAgent;

import java.io.File;
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
public class ItemFragment extends Fragment implements PullToRefreshBase.OnRefreshListener, AdapterView.OnItemClickListener,
        Response.Listener<ArrayList<Feed>>, Response.ErrorListener, View.OnClickListener {
    private static final String REQUEST_URL = "http://www.u148.net/json/%1$d/%2$d";
    private static final int MSG_LOAD_OK = 1;

    private PullToRefreshListView pullToRefresh;
    private FeedListAdapter listAdapter;
    private View footView;
    private View emptyView;

    private ArrayList<Feed> feedList = new ArrayList<Feed>();

    protected int currentPage = 1;
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

        emptyView = view.findViewById(R.id.empty_view);
        footView = inflater.inflate(R.layout.load_more, null);
        Button loadBtn = (Button) footView.findViewById(R.id.btn_load);
        loadBtn.setOnClickListener(this);

        listView.addFooterView(footView);
        listView.setEmptyView(emptyView);

        if (Constants.MODE_NIGHT == PrefsUtil.getThemeMode()) {
            listView.setDivider(getResources().getDrawable(R.drawable.split_color_night));
            loadBtn.setBackgroundResource(R.drawable.btn_gray_night);
            loadBtn.setTextColor(getResources().getColor(R.color.text_color_summary));
        } else {
            listView.setDivider(getResources().getDrawable(R.drawable.split_color));
            loadBtn.setBackgroundResource(R.drawable.btn_gray);
            loadBtn.setTextColor(getResources().getColor(R.color.text_color_regular));
        }

        listView.setDividerHeight(1);

        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(this);

        pullToRefresh.setOnRefreshListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_load) {
            footView.findViewById(R.id.btn_load).setVisibility(View.GONE);
            footView.findViewById(R.id.loading_layout).setVisibility(View.VISIBLE);
            currentPage++;
            loadData();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Feed feed = feedList.get(position - 1);

        Map<String, String> params = new HashMap<String, String>();
        params.put("author", feed.user.nickname);
        params.put("title", feed.title);
        FlurryAgent.logEvent("read_article", params);

        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra("feed", feed);

        startActivity(intent);
    }

    @Override
    public void onRefresh(PullToRefreshBase refreshView) {
        currentPage = 1;
        loadData();
    }

    private void loadData() {
        if (!CommonUtil.didNetworkConnected(getActivity())) return;

        String url = String.format(REQUEST_URL, category, currentPage);
        HttpUtils.feedRequest(url, this, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!dataLoaded) {
            footView.setVisibility(View.GONE);
            loadData();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        loadCacheData();
        setErrorView(R.string.net_error);

        footView.setVisibility(View.GONE);
        pullToRefresh.onRefreshComplete();
    }

    @Override
    public void onResponse(ArrayList<Feed> response) {
        if (null != response && response.size() > 0) {
            if (currentPage == 1) feedList.clear();

            feedList.addAll(response);
            dataLoaded = true;
            listAdapter.notifyDataSetChanged();

            footView.findViewById(R.id.loading_layout).setVisibility(View.GONE);
            footView.findViewById(R.id.btn_load).setVisibility(View.VISIBLE);
            footView.setVisibility(View.VISIBLE);
        } else {
            setErrorView(R.string.parse_error);
            footView.setVisibility(View.GONE);
        }
        pullToRefresh.onRefreshComplete();
    }

    private void setErrorView(int resId) {
        emptyView.findViewById(R.id.progress_bar).setVisibility(View.GONE);
        ((TextView) emptyView.findViewById(R.id.tv_empty_tip)).setText(getString(resId));
    }

    protected void loadCacheData() {
        String dir = FileCache.getDataCacheDir(getActivity());
        String url = String.format(REQUEST_URL, category, currentPage);
        String path = dir + StringUtil.getMD5Str(url);
        File cacheFile = new File(path);

        if (cacheFile.exists()) {
            final String data = FileUtils.readFromFile(path);
            if (null != data) {
                new Thread(){
                    @Override
                    public void run() {
                        ArrayList<Feed> tmpList = JsonParser.getFeedList(data);
                        if (null != tmpList && tmpList.size() > 0) {
                            feedList.addAll(tmpList);
                        }
                        mHandler.sendEmptyMessage(MSG_LOAD_OK);
                    }
                }.start();
            }
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what != MSG_LOAD_OK) return;

            if (feedList.size() > 0) {
                dataLoaded = true;
                listAdapter.notifyDataSetChanged();
            } else {
                //todo
            }

            footView.setVisibility(View.GONE);
            pullToRefresh.onRefreshComplete();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    private class FeedListAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private Map<String, String> categoryMap;
        private float density;

        public FeedListAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            categoryMap = new HashMap<String, String>();
            feedList = new ArrayList<Feed>();
            density = getResources().getDisplayMetrics().density;

            int[] ids = context.getResources().getIntArray(R.array.category_id);
            String[] names = context.getResources().getStringArray(R.array.category_name);
            for (int i = 0; i < ids.length; i++) {
                categoryMap.put(String.valueOf(ids[i]), names[i]);
            }
        }

        @Override
        public int getCount() {
            return feedList.size();
        }

        @Override
        public Feed getItem(int position) {
            return feedList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (null == convertView) {
                convertView = inflater.inflate(R.layout.feed_list_item, parent, false);
                holder = new ViewHolder();

                holder.cellLayout = (RelativeLayout) convertView.findViewById(R.id.cell_layout);
                holder.thumb = (ImageView) convertView.findViewById(R.id.feed_image);
                holder.category = (TextView) convertView.findViewById(R.id.feed_type);
                holder.title = (TextView) convertView.findViewById(R.id.feed_title);
                holder.viewsText = (TextView) convertView.findViewById(R.id.tv_views);
                holder.commentText = (TextView) convertView.findViewById(R.id.tv_comment);
                holder.content = (TextView) convertView.findViewById(R.id.feed_content);

                final int theme = PrefsUtil.getThemeMode();
                if (Constants.MODE_NIGHT == theme) {
                    holder.category.setTextColor(getResources().getColor(R.color.action_bar_bg_night));
                    holder.title.setTextColor(getResources().getColor(R.color.text_color_weak));
                    holder.content.setTextColor(getResources().getColor(R.color.text_color_summary));
                    holder.viewsText.setTextColor(getResources().getColor(R.color.text_color_summary));
                    holder.commentText.setTextColor(getResources().getColor(R.color.text_color_summary));
                } else {
                    holder.category.setTextColor(getResources().getColor(R.color.action_bar_bg));
                    holder.title.setTextColor(getResources().getColor(R.color.text_color_regular));
                    holder.content.setTextColor(getResources().getColor(R.color.text_color_weak));
                    holder.viewsText.setTextColor(getResources().getColor(R.color.text_color_weak));
                    holder.commentText.setTextColor(getResources().getColor(R.color.text_color_weak));
                }

                convertView.setTag(holder);
            }

            holder = (ViewHolder) convertView.getTag();

            /**
             * here we make the first cell's top padding larger
             */
            int paddingTop = 12;
            int paddingLeft = (int) (8 * density);
            int padingBottom = (int) (12 * density);
            if (0 == position) {
                paddingTop = 20;
            }

            holder.cellLayout.setPadding(paddingLeft, (int) (paddingTop * density), paddingLeft, padingBottom);

            Feed feed = getItem(position);

            holder.category.setText("[" + categoryMap.get(feed.category + "") + "]");
            holder.title.setText(feed.title);
            holder.viewsText.setText(String.format(getString(R.string.views), feed.countBrowse));
            holder.commentText.setText(String.format(getString(R.string.comment_count), feed.countReview));
            holder.content.setText(feed.summary);

            HttpUtils.getImageLoader().get(feed.picMid, ImageLoader.getImageListener(holder.thumb,
                    R.drawable.pictrue_bg, R.drawable.pictrue_bg));

            return convertView;
        }
    }

    private static class ViewHolder {
        RelativeLayout cellLayout;
        ImageView thumb;
        TextView category;
        TextView title;
        TextView viewsText;
        TextView commentText;
        TextView content;
    }
}
