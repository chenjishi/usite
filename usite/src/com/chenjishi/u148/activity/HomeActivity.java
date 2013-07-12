package com.chenjishi.u148.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chenjishi.u148.R;
import com.chenjishi.u148.entity.FeedItem;
import com.chenjishi.u148.pulltorefresh.PullToRefreshBase;
import com.chenjishi.u148.pulltorefresh.PullToRefreshListView;
import com.chenjishi.u148.service.FeedItemDataService;
import com.chenjishi.u148.util.ApiUtils;
import com.chenjishi.u148.util.FileUtils;
import com.chenjishi.u148.util.UIUtil;
import com.chenjishi.u148.util.UsiteConfig;
import com.chenjishi.u148.view.SlidingMenu;
import com.chenjishi.u148.volley.RequestQueue;
import com.chenjishi.u148.volley.VolleyError;
import com.chenjishi.u148.volley.toolbox.BitmapLruCache;
import com.chenjishi.u148.volley.toolbox.ImageLoader;
import com.chenjishi.u148.volley.toolbox.Volley;
import com.flurry.android.FlurryAgent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-3
 * Time: 下午4:05
 * To change this template use File | Settings | File Templates.
 */
public class HomeActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private SlidingMenu mSlideingMenu;
    private PullToRefreshListView mRefreshListView;
    private View mEmptyView;

    private int mCurrentPage = 1;

    private String[] categories;

    private int currentCategory = 0;
    private int menuIndex = 0;

    private String cacheFilePath;

    private FeedListAdapter mAdapter;

    private ImageLoader mImageLoader;

    private ArrayList<FeedItem> mFeedItems = new ArrayList<FeedItem>();
    private FeedItemDataService dataService;

    private String[] urls = {
            "/list/",
            "/video/",
            "/image/",
            "/audio/",
            "/text/",
            "/mix/"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSlideingMenu = new SlidingMenu(this);
        mSlideingMenu.setMode(SlidingMenu.LEFT);
        mSlideingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        mSlideingMenu.setBehindOffsetRes(R.dimen.slide_menu_offset);
        mSlideingMenu.setFadeDegree(1.0f);
        mSlideingMenu.setBehindScrollScale(0.5f);
        mSlideingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        mSlideingMenu.setMenu(R.layout.slide_menu);

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        mImageLoader = new ImageLoader(requestQueue, new BitmapLruCache(this));

        categories = getResources().getStringArray(R.array.menu_category);

        ListView menuListView = (ListView) findViewById(R.id.list_menu);
        new MenuListAdapter(this, menuListView);

        mEmptyView = LayoutInflater.from(this).inflate(R.layout.empty_view, null);
        mRefreshListView = (PullToRefreshListView) findViewById(R.id.feed_list);

        ListView actualListView = mRefreshListView.getRefreshableView();
        ((ViewGroup) actualListView.getParent()).addView(mEmptyView);
        actualListView.setEmptyView(mEmptyView);

        mAdapter = new FeedListAdapter(this);
        actualListView.setAdapter(mAdapter);
        actualListView.setOnItemClickListener(this);

        mRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                dataService.clearCaches();
                if (new File(cacheFilePath).exists()) {
                    FileUtils.deleteFile(cacheFilePath);
                }

                if (mFeedItems.size() > 0) {
                    mFeedItems.clear();
                }
                mCurrentPage = 1;
                loadData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                mCurrentPage++;
                loadData();
            }
        });

        setTitleText(categories[currentCategory]);
        dataService = new FeedItemDataService();

        cacheFilePath = getIntent().getExtras().getString("file_path");

        initData(cacheFilePath);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.home;
    }

    @Override
    protected void backIconClicked() {
        mSlideingMenu.toggle();
    }

    private void initData(String path) {
        if (null == path) return;

        Object o = FileUtils.unserializeObject(path);
        if (null != o) {
            mFeedItems = (ArrayList<FeedItem>) o;
            mAdapter.notifyDataSetChanged();
        }
    }

    private void loadData() {
        Runnable action = new Runnable() {
            @Override
            public void run() {
                ArrayList<FeedItem> tmpList = dataService.getFeedItemList(getUrl());
                if (null != tmpList && tmpList.size() > 0) {
                    mFeedItems.addAll(tmpList);
                }
            }
        };

        Runnable postAction = new Runnable() {
            @Override
            public void run() {
                if (mFeedItems.size() > 0) {
                    mAdapter.notifyDataSetChanged();
                } else {
                    mEmptyView.findViewById(R.id.progress_bar).setVisibility(View.GONE);
                    ((TextView) mEmptyView.findViewById(R.id.tv_empty_tip)).setText("网络连接错误");
                }
                mRefreshListView.onRefreshComplete();
            }
        };

        UIUtil.runWithoutMessage(action, postAction);
    }

    //http://www.u148.net/list/2.html
    //http://www.u148.net/video/2.html

    private String getUrl() {
        return ApiUtils.BASE_URL + urls[currentCategory] + mCurrentPage + ".html";

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FeedItem item = mFeedItems.get(position - 1);
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("title", item.title);
        intent.putExtra("link", item.link);

        Map<String, String> params = new HashMap<String, String>();
        params.put("article_title", item.title);
        params.put("article_author", item.author);
        FlurryAgent.logEvent("read_article", params);

        startActivity(intent);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        long t = UsiteConfig.getUpdateTime(this);
        if (System.currentTimeMillis() - t >= UsiteConfig.FOUR_HOURS) {
            FileUtils.deleteFile(cacheFilePath);
        }
        finish();
        return true;
    }

    class FeedListAdapter extends BaseAdapter {
        LayoutInflater inflater;

        public FeedListAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mFeedItems.size();
        }

        @Override
        public Object getItem(int i) {
            return mFeedItems.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;

            if (null == view) {
                view = inflater.inflate(R.layout.feed_list_item, null);
                holder = new ViewHolder();

                holder.ivImage = (ImageView) view.findViewById(R.id.feed_image);
                holder.tvCate = (TextView) view.findViewById(R.id.feed_type);
                holder.tvTitle = (TextView) view.findViewById(R.id.feed_title);
                holder.tvAuthor = (TextView) view.findViewById(R.id.feed_author);
                holder.tvTime = (TextView) view.findViewById(R.id.feed_time);
                holder.tvContent = (TextView) view.findViewById(R.id.feed_content);

                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            FeedItem mainList = mFeedItems.get(i);

            holder.tvCate.setText(mainList.category);
            holder.tvTitle.setText(mainList.title);
            holder.tvAuthor.setText(mainList.author);
            holder.tvTime.setText(mainList.time);
            holder.tvContent.setText(mainList.summary);

            final ImageView thumbImage = holder.ivImage;
            mImageLoader.get(mainList.imageUrl, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    thumbImage.setImageBitmap(response.getBitmap());
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                }
            });

            return view;
        }

        class ViewHolder {
            ImageView ivImage;
            TextView tvCate;
            TextView tvTitle;
            TextView tvAuthor;
            TextView tvTime;
            TextView tvContent;
        }
    }

    class MenuListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
        ListView listView;
        LayoutInflater inflater;

        public MenuListAdapter(Context context, ListView listView) {
            this.listView = listView;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            this.listView.setOnItemClickListener(this);
            this.listView.setAdapter(this);
        }

        @Override
        public int getCount() {
            return categories.length;
        }

        @Override
        public Object getItem(int i) {
            return categories[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View itemView;
            if (null == view) {
                itemView = inflater.inflate(R.layout.menu_item, viewGroup, false);
            } else {
                itemView = view;
            }

            TextView menuText = (TextView) itemView.findViewById(R.id.tag_menu);
            if (i == menuIndex) {
                itemView.setBackgroundResource(R.drawable.menu_selected);
            } else {
                itemView.setBackgroundResource(R.drawable.menu_highlight);
            }
            menuText.setText(categories[i]);

            return itemView;
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            menuIndex = i;
            notifyDataSetChanged();
            mSlideingMenu.toggle();
            switch (i) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    mCurrentPage = 1;
                    currentCategory = i;

                    if (mFeedItems != null) {
                        mFeedItems.clear();
                    }
                    mAdapter.notifyDataSetChanged();
                    setTitleText(categories[i]);
                    loadData();
                    break;
                case 6:
                    Intent intent = new Intent(HomeActivity.this, AboutActivity.class);
                    startActivity(intent);
                    break;
                case 7:
                    Intent intent1 = new Intent(HomeActivity.this, ArticleListActivity.class);
                    startActivity(intent1);
                    break;
            }
        }
    }
}
