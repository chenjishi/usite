package com.chenjishi.u148.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.AppApplication;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.entity.FeedItem;
import com.chenjishi.u148.pulltorefresh.PullToRefreshBase;
import com.chenjishi.u148.pulltorefresh.PullToRefreshListView;
import com.chenjishi.u148.service.DownloadAPKThread;
import com.chenjishi.u148.service.FeedItemDataService;
import com.chenjishi.u148.service.MusicService;
import com.chenjishi.u148.util.*;
import com.chenjishi.u148.volley.Response;
import com.chenjishi.u148.volley.VolleyError;
import com.chenjishi.u148.volley.toolbox.ImageLoader;
import com.flurry.android.FlurryAgent;
import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;
import org.json.JSONException;
import org.json.JSONObject;

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
public class HomeActivity extends BaseActivity implements AdapterView.OnItemClickListener, View.OnClickListener,
        PullToRefreshBase.OnRefreshListener, AbsListView.OnScrollListener,
        Response.Listener<String>, Response.ErrorListener {

    private DrawerLayout drawerLayout;
    private ListView actualListView;
    private View mEmptyView;
    private PullToRefreshListView mPullToRefresh;

    private int mCurrentPage = 1;
    private int lastItemIndex;

    private String[] categories;

    private int currentCategory = 0;

    private String cacheFilePath;
    private View mFootView;

    private FeedListAdapter mAdapter;

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

        categories = getResources().getStringArray(R.array.menu_category);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        mPullToRefresh = (PullToRefreshListView) findViewById(R.id.lv_feeds);
        actualListView = mPullToRefresh.getRefreshableView();

        mFootView = LayoutInflater.from(this).inflate(R.layout.load_more, null);
        mFootView.setVisibility(View.GONE);
        mEmptyView = LayoutInflater.from(this).inflate(R.layout.empty_view, null);
        actualListView.addFooterView(mFootView);
        ((ViewGroup) actualListView.getParent()).addView(mEmptyView);
        actualListView.setEmptyView(mEmptyView);

        mAdapter = new FeedListAdapter();
        actualListView.setAdapter(mAdapter);
        actualListView.setOnItemClickListener(this);
        actualListView.setOnScrollListener(this);

        mPullToRefresh.setOnRefreshListener(this);
        initMenuList();

        AdView adView = new AdView(this, AdSize.FIT_SCREEN);
        LinearLayout adLayout = (LinearLayout) findViewById(R.id.adLayout);
        adLayout.addView(adView);

        setTitleText(categories[currentCategory]);
        dataService = new FeedItemDataService();

        cacheFilePath = getIntent().getExtras().getString("file_path");

        initData(cacheFilePath);

        checkUpdate();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
    }

    @Override
    public void onResponse(String response) {
        try {
            JSONObject dataObj = new JSONObject(response);

            String fileUrl = "http://121.199.31.3:8086/ChangeBa/upload/usite.apk";
            int versionCode = dataObj.optInt("version_code", 0);
            String apkUrl = dataObj.optString("url", fileUrl);
            if (CommonUtil.getVersionCode(this) < versionCode) {
                String path;

                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                    path = Environment.getExternalStorageDirectory() + "/";
                } else {
                    path = AppApplication.getInstance().getCacheDir() + "/";
                }

                DownloadAPKThread apkThread = new DownloadAPKThread(apkUrl, path, "u148.apk");
                apkThread.start();
            }
        } catch (JSONException e) {
        }
    }

    private void checkUpdate() {
        long lastCheckTime = PrefsUtil.getCheckVersionTime();
        if (lastCheckTime == -1L || System.currentTimeMillis() > lastCheckTime) {
            PrefsUtil.saveCheckVersionTime(System.currentTimeMillis());
            String url = "http://121.199.31.3:8086/ChangeBa/upload/version.txt";
            HttpUtils.get(url, this, this);
        }
    }

    @Override
    public void onRefresh(PullToRefreshBase refreshView) {
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
    protected void onStop() {
        super.onStop();
        stopService(new Intent(this, MusicService.class));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.home;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (SCROLL_STATE_IDLE == scrollState && lastItemIndex > mFeedItems.size() - 1) {
            mCurrentPage++;
            loadData();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        lastItemIndex = firstVisibleItem + visibleItemCount - 1;
    }

    @Override
    protected void backIconClicked() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            drawerLayout.openDrawer(Gravity.LEFT);
        }
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
        mFootView.setVisibility(View.VISIBLE);
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
                mFootView.setVisibility(View.GONE);
                mPullToRefresh.onRefreshComplete();
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
        private ImageLoader imageLoader;

        public FeedListAdapter() {
            imageLoader = HttpUtils.getImageLoader();
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
                view = getLayoutInflater().inflate(R.layout.feed_list_item, null);
                holder = new ViewHolder();

                holder.ivImage = (ImageView) view.findViewById(R.id.feed_image);
                holder.tvCate = (TextView) view.findViewById(R.id.feed_type);
                holder.tvTitle = (TextView) view.findViewById(R.id.feed_title);
                holder.tvAuthor = (TextView) view.findViewById(R.id.feed_author);
                holder.tvTime = (TextView) view.findViewById(R.id.feed_time);
                holder.tvContent = (TextView) view.findViewById(R.id.feed_content);

                view.setTag(holder);
            }

            holder = (ViewHolder) view.getTag();

            FeedItem mainList = mFeedItems.get(i);

            holder.tvCate.setText(mainList.category);
            holder.tvTitle.setText(mainList.title);
            holder.tvAuthor.setText(mainList.author);
            holder.tvTime.setText(mainList.time);
            holder.tvContent.setText(mainList.summary);

            final ImageView thumbImage = holder.ivImage;
            imageLoader.get(mainList.imageUrl, new ImageLoader.ImageListener() {
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

    @Override
    public void onClick(View v) {
        Integer index = (Integer) v.getTag();
        if (null == index) return;

        if (index == 6) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (index == 7) {
            startActivity(new Intent(this, ArticleListActivity.class));
        } else {
            mCurrentPage = 1;
            currentCategory = index;

            if (null != mFeedItems) mFeedItems.clear();

            mAdapter.notifyDataSetChanged();
            setTitleText(categories[index]);
            loadData();
        }
        drawerLayout.closeDrawer(Gravity.LEFT);
    }

    private void initMenuList() {
        LinearLayout menuLayout = (LinearLayout) findViewById(R.id.layout_menu);
        for (int i = 0; i < categories.length; i++) {
            menuLayout.addView(getMenuItemView(i));
            ImageView divider = new ImageView(this);
            divider.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(0XFF6C6C6C);
            menuLayout.addView(divider);
        }
    }

    private TextView getMenuItemView(int position) {
        TextView itemView = new TextView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) getResources().getDimension(R.dimen.action_bar_height));
        itemView.setLayoutParams(layoutParams);
        itemView.setGravity(Gravity.CENTER_VERTICAL);
        itemView.setTag(position);

        itemView.setBackgroundResource(R.drawable.highlight_bg);
        itemView.setTextColor(0xFF000000);
        itemView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.0f);
        itemView.setPadding((int) getResources().getDimension(R.dimen.padding_left), 0, 0, 0);

        itemView.setText(categories[position]);
        itemView.setOnClickListener(this);

        return itemView;
    }
}
