package com.chenjishi.usite.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import com.chenjishi.usite.R;
import com.chenjishi.usite.base.BaseActivity;
import com.chenjishi.usite.entity.FeedItem;
import com.chenjishi.usite.image.IImageCallback;
import com.chenjishi.usite.image.ImagePool;
import com.chenjishi.usite.service.FeedItemDataService;
import com.chenjishi.usite.util.ApiUtils;
import com.chenjishi.usite.util.CommonUtil;
import com.chenjishi.usite.util.FileUtils;
import com.chenjishi.usite.util.UsiteConfig;
import com.chenjishi.usite.view.ExitDialog;
import com.chenjishi.usite.view.PullToRefreshBase;
import com.chenjishi.usite.view.PullToRefreshListView;
import com.flurry.android.FlurryAgent;

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
public class HomeActivity extends BaseActivity implements View.OnClickListener {
    private static final int MODE_NORMAL = 0;
    private static final int MODE_MENU = 1;

    private LinearLayout mMenuView;
    private FrameLayout mViewContainer;
    private LinearLayout mRootView;

    private int mViewMode = MODE_NORMAL;

    private PullToRefreshListView mListView;
    private ListView actualListView;
    private View mEmptyView;
    private View mLoadingBar;

    private int mCurrentPage = 1;
    private boolean isRefresh = false;

    private String[] categories;

    private int currentCategory = 0;

    private TextView cateText;

    private String cacheFilePath;


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

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (1 == msg.what) {
                dataChangeNotify();
                if (isRefresh) {
                    mListView.onRefreshComplete();
                    FileUtils.deleteFile(cacheFilePath);
                    isRefresh = false;
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        mMenuView = (LinearLayout) findViewById(R.id.home_menu);
        mViewContainer = (FrameLayout) findViewById(R.id.view_container);
        mRootView = (LinearLayout) findViewById(R.id.home_root);

        categories = getResources().getStringArray(R.array.menu_category);

        findViewById(R.id.button_menu).setOnClickListener(this);
        cateText = (TextView) findViewById(R.id.category_text);

        ListView menuListView = (ListView) findViewById(R.id.my_menu_list);
        new MenuListAdapter(this, menuListView);

        mEmptyView = findViewById(R.id.main_list_empty);
        mListView = (PullToRefreshListView) findViewById(R.id.feed_list);

        actualListView = mListView.getRefreshableView();

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLoadingBar = layoutInflater.inflate(R.layout.bottom_loading_bar, null);
        mLoadingBar.setVisibility(View.GONE);

        actualListView.addFooterView(mLoadingBar, null, false);
        actualListView.setEmptyView(mEmptyView);


        new FeedListAdapter(this, actualListView);

        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (CommonUtil.didNetworkConnected(HomeActivity.this)) {
                    isRefresh = true;
                    dataService.clearCaches();
                    if (null != mFeedItems) {
                        mFeedItems.clear();
                    }
                    requestData();
                }
            }
        });

        dataService = new FeedItemDataService();

        setCateText();

        cacheFilePath = getIntent().getExtras().getString("file_path");

        initData(cacheFilePath);
    }

    private void initData(String path) {
        if (null == path) return;

        Object o = FileUtils.unserializeObject(path);
        if (null != o) {
            mFeedItems = (ArrayList<FeedItem>) o;
//            dataService.addToCaches(ApiUtils.BASE_URL + "/list/1.html", mFeedItems);
            dataChangeNotify();
        }
    }

    private void dataChangeNotify() {
        HeaderViewListAdapter headerAdapter = (HeaderViewListAdapter) actualListView.getAdapter();
        ((BaseAdapter) headerAdapter.getWrappedAdapter()).notifyDataSetChanged();
        mLoadingBar.setVisibility(View.GONE);
    }

    private void requestData() {
        new Thread() {
            @Override
            public void run() {
                Log.i("test", "getUrl " + getUrl());
                ArrayList<FeedItem> feedItems = dataService.getFeedItemList(getUrl());
                mFeedItems.addAll(feedItems);
                mHandler.sendEmptyMessage(1);

            }
        }.start();
    }

    //http://www.u148.net/list/2.html
    //http://www.u148.net/video/2.html

    private String getUrl() {
        return ApiUtils.BASE_URL + urls[currentCategory] + mCurrentPage + ".html";

    }

    private void showMenu() {
        mMenuView.setVisibility(View.VISIBLE);
        findViewById(R.id.menu_space).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL
                        || motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    hiddenMenu();
                }
                return true;
            }
        });

        int offset = findViewById(R.id.menu_list).getWidth();
        Animation animation = new TranslateAnimation(0, offset, 0, 0);
        animation.setDuration(300);
        animation.setFillAfter(true);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mViewContainer.bringChildToFront(mMenuView);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mRootView.startAnimation(animation);
        mViewMode = MODE_MENU;
    }

    private void hiddenMenu() {
        mViewContainer.bringChildToFront(mRootView);
        final int offset = mMenuView.findViewById(R.id.menu_list).getWidth();
        Animation animation = new TranslateAnimation(offset, 0, 0, 0);
        animation.setDuration(300);
        animation.setFillAfter(true);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mMenuView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mRootView.startAnimation(animation);
        mViewMode = MODE_NORMAL;

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_menu:
                if (MODE_NORMAL == mViewMode) {
                    showMenu();
                } else {
                    hiddenMenu();
                }
                break;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == event.getKeyCode()) {
            if (MODE_MENU == mViewMode) {
                hiddenMenu();
                mViewMode = MODE_NORMAL;
            } else {
                ExitDialog dialog = new ExitDialog(this, R.style.FullHeightDialog);
                dialog.setCallback(new ExitDialog.IAppExitCallback() {
                    @Override
                    public void onAppExitCallback() {
                        long t = UsiteConfig.getUpdateTime(HomeActivity.this);
                        if (System.currentTimeMillis() - t >= UsiteConfig.FOUR_HOURS) {
                            FileUtils.deleteFile(cacheFilePath);
                        }
                        finish();
                    }
                });
                dialog.show();
            }
        } else if (KeyEvent.KEYCODE_MENU == event.getKeyCode()) {
            if (MODE_MENU == mViewMode) {
                hiddenMenu();
                mViewMode = MODE_NORMAL;
            } else {
                showMenu();
                mViewMode = MODE_MENU;
            }
        }
        return true;
    }

    class FeedListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, AbsListView.OnScrollListener {
        ListView listView;
        LayoutInflater inflater;
        ImagePool imagePool;

        public FeedListAdapter(Context context, ListView listView) {
            this.listView = listView;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            imagePool = new ImagePool();

            this.listView.setOnItemClickListener(this);
            this.listView.setOnScrollListener(this);
            this.listView.setAdapter(this);
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

            final ImageView iv = holder.ivImage;
            imagePool.requestImage(mainList.imageUrl, new IImageCallback() {
                public void onImageResponse(Drawable d) {
                    iv.setImageDrawable(d);
                }
            });


            return view;
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            FeedItem item = mFeedItems.get(i);
            Intent intent = new Intent(HomeActivity.this, DetailActivity.class);
            intent.putExtra("link", item.link);

            Map<String, String> params = new HashMap<String, String>();
            params.put("article_title", item.title);
            params.put("article_author", item.author);
            FlurryAgent.logEvent("read_article", params);

            startActivity(intent);
        }

        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {
            if (i == SCROLL_STATE_IDLE && absListView.getLastVisiblePosition() == absListView.getCount() - 1) {
                mLoadingBar.setVisibility(View.VISIBLE);
                mCurrentPage++;
                requestData();
            }
        }

        @Override
        public void onScroll(AbsListView absListView, int i, int i1, int i2) {
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
            TextView v;
            if (null == view) {
                v = (TextView) inflater.inflate(R.layout.menu_item, null);
            } else {
                v = (TextView) view;
            }

            v.setText(categories[i]);

            return v;
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            hiddenMenu();
            mViewMode = MODE_NORMAL;
            switch (i) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    mCurrentPage = 1;
                    currentCategory = i;
                    setCateText();
                    if (mFeedItems != null) {
                        mFeedItems.clear();
                    }
                    dataChangeNotify();
                    requestData();
                    break;
                case 6:
                    startActivity(new Intent(HomeActivity.this, PuziActivity.class));
                    break;
                case 7:
                    startActivity(new Intent(HomeActivity.this, GroupActivity.class));
                    break;
                case 8:
                    startActivity(new Intent(HomeActivity.this, AboutActivity.class));
                    break;
            }

        }
    }

    private void setCateText() {
        cateText.setText(categories[currentCategory]);
    }
}
