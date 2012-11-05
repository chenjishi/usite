package com.chenjishi.usite.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import com.chenjishi.usite.R;
import com.chenjishi.usite.entity.FeedItem;
import com.chenjishi.usite.image.IImageCallback;
import com.chenjishi.usite.image.ImagePool;
import com.chenjishi.usite.parser.FeedItemParser;
import com.chenjishi.usite.util.ApiUtils;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-3
 * Time: 下午4:05
 * To change this template use File | Settings | File Templates.
 */
public class HomeActivity extends Activity implements View.OnClickListener {
    private static final int MODE_NORMAL = 0;
    private static final int MODE_MENU = 1;

    private LinearLayout mMenuView;
    private FrameLayout mViewContainer;
    private LinearLayout mRootView;

    private int mViewMode = MODE_NORMAL;

    private ListView mListView;
    private View mEmptyView;
    private View mLoadingBar;

    private int mCurrentPage = 1;

    private FeedListAdapter mFeedListAdapter;


    private List<FeedItem> mFeedItems = new ArrayList<FeedItem>();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (1 == msg.what) {
                HeaderViewListAdapter headerAdapter = (HeaderViewListAdapter) mListView.getAdapter();
                ((BaseAdapter) headerAdapter.getWrappedAdapter()).notifyDataSetChanged();
                mLoadingBar.setVisibility(View.GONE);
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

        findViewById(R.id.button_menu).setOnClickListener(this);

        mEmptyView = findViewById(R.id.main_list_empty);
        mListView = (ListView) findViewById(R.id.feed_list);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLoadingBar = layoutInflater.inflate(R.layout.bottom_loading_bar, null);
        mLoadingBar.setVisibility(View.GONE);

        mListView.addFooterView(mLoadingBar, null, false);
        mListView.setEmptyView(mEmptyView);

        mFeedListAdapter = new FeedListAdapter(this, mListView);
        requestData(false);


    }

    private void requestData(final boolean isPage) {
        new Thread() {
            @Override
            public void run() {
                String requestUrl = isPage ? getNextPageUrl() : getFirstPage();
                Log.i("test", "requestUrl ---> " + requestUrl);
                List<FeedItem> feedItems = FeedItemParser.getMainList(requestUrl);
                mFeedItems.addAll(feedItems);
                mHandler.sendEmptyMessage(1);

            }
        }.start();
    }

    private String getNextPageUrl() {
        String url = getUrl("/list/");
        return url;
    }


    private String getUrl(String str) {
        return ApiUtils.BASE_URL + str + mCurrentPage + ".html";

    }

    private String getFirstPage() {
        return ApiUtils.BASE_URL;
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

                holder.ivImage = (ImageView) view.findViewById(R.id.iv_image);
                holder.tvCate = (TextView) view.findViewById(R.id.tv_cate);
                holder.tvTitle = (TextView) view.findViewById(R.id.tv_title);
                holder.tvAuthor = (TextView) view.findViewById(R.id.tv_author);
                holder.tvTime = (TextView) view.findViewById(R.id.tv_time_line);
                holder.tvContent = (TextView) view.findViewById(R.id.tv_summary);

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
            Intent intent = new Intent(HomeActivity.this, DetailActivity.class);
            intent.putExtra("link", mFeedItems.get(i).link);
            startActivity(intent);
        }

        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {
            if (i == SCROLL_STATE_IDLE && absListView.getLastVisiblePosition() == absListView.getCount() - 1) {
                mLoadingBar.setVisibility(View.VISIBLE);
                mCurrentPage++;
                requestData(true);
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
}
