package com.chenjishi.u148.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.DBHelper;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.model.FeedItem;
import com.chenjishi.u148.model.UserInfo;
import com.chenjishi.u148.service.MusicPlayListener;
import com.chenjishi.u148.service.MusicService;
import com.chenjishi.u148.sina.RequestListener;
import com.chenjishi.u148.util.*;
import com.chenjishi.u148.view.DepthPageTransformer;
import com.chenjishi.u148.view.ShareDialog;
import com.chenjishi.u148.volley.Response;
import com.chenjishi.u148.volley.VolleyError;
import com.chenjishi.u148.volley.toolbox.ImageRequest;
import com.flurry.android.FlurryAgent;
import com.sina.weibo.sdk.exception.WeiboException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.chenjishi.u148.util.Constants.*;

/**
 * Created by chenjishi on 14-4-25.
 */
public class DetailsActivity extends BaseActivity implements OnMusicClickListener, ViewPager.OnPageChangeListener,
        ShareDialog.OnShareListener, MusicPlayListener {
    private final static String TAG = "DetailActivity";

    private ArrayList<FeedItem> mFeedList;

    private ViewPager mViewPager;
    private FeedPagerAdapter mPagerAdapter;

    private ImageButton favoriteBtn;
    private boolean isFavorite;
    private boolean favorited;

    private DBHelper mDatabase;

    private int mCurrentIndex;

    private Map<Integer, String> mCategoryMap;

    private MusicService mMusicService;
    private boolean mBounded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_details, R.layout.details_title_layout);

        Bundle bundle = getIntent().getExtras();
        mFeedList = bundle.getParcelableArrayList(KEY_FEED_LIST);
        mCurrentIndex = bundle.getInt(KEY_FEED_INDEX, 0);

        mDatabase = DBHelper.getInstance(this);

        favoriteBtn = (ImageButton) findViewById(R.id.btn_favorite);

        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mPagerAdapter = new FeedPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setCurrentItem(mCurrentIndex);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setPageTransformer(true, new DepthPageTransformer());

        mCategoryMap = new HashMap<Integer, String>();
        int[] ids = getResources().getIntArray(R.array.category_id);
        String[] names = getResources().getStringArray(R.array.category_name);
        for (int i = 0; i < ids.length; i++) {
            mCategoryMap.put(ids[i], names[i]);
        }

        final FeedItem feed = mFeedList.get(mCurrentIndex);
        String title = mCategoryMap.get(feed.category);

        int resId;
        int theme = PrefsUtil.getThemeMode();
        isFavorite = favorited = mDatabase.exist(feed.id);
        if (theme == Constants.MODE_DAY) {
            resId = isFavorite ? R.drawable.ic_favorite_full : R.drawable.ic_favorite;
        } else {
            resId = isFavorite ? R.drawable.ic_favorite_full : R.drawable.ic_favorite_night;
        }
        favoriteBtn.setImageResource(resId);

        if (null == feed.usr) {
            title = "返回";
        }
        setTitle(title);
    }

    public void onCommentClicked(View v) {
        final FeedItem feed = mFeedList.get(mCurrentIndex);
        Intent intent = new Intent(this, CommentActivity.class);
        intent.putExtra("article_id", feed.id);

        IntentUtils.startPreviewActivity(this, intent);
    }

    private ShareDialog mShareDialog;

    public void onShareClicked(View v) {
        if (null == mShareDialog) {
            mShareDialog = new ShareDialog(this, this);
        }
        mShareDialog.show();
    }

    @Override
    public void onShare(final int type) {
        final FeedItem feed = mFeedList.get(mCurrentIndex);

        HashMap<String, String> params = new HashMap<String, String>();
        params.put(Constants.PARAM_TITLE, feed.title);
        FlurryAgent.logEvent(Constants.EVENT_ARTICLE_SHARE, params);

        final String title = String.format(getString(R.string.share_title), feed.title);
        final String desc = feed.summary;
        final String url = "http://www.u148.net/article/" + feed.id + ".html";

        if (type == ShareUtils.SHARE_WEIBO) {
            shareToWeibo(title + url);
            mShareDialog.dismiss();
            return;
        }

        final ArrayList<String> imageList = getImageList();
        if (null != imageList && imageList.size() > 0) {
            ImageRequest request = new ImageRequest(imageList.get(0), new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {

                    if (null != response) {
                        ShareUtils.shareWebpage(DetailsActivity.this, url, type, title, desc, response);
                    } else {
                        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
                        ShareUtils.shareWebpage(DetailsActivity.this, url, type, title, desc, icon);
                    }
                }
            }, 0, 0, null, null);

            HttpUtils.getRequestQueue().add(request);
        } else {
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
            ShareUtils.shareWebpage(this, url, type, title, desc, icon);
        }

        mShareDialog.dismiss();
    }

    private void shareToWeibo(String content) {
        final ArrayList<String> imageList = getImageList();
        String imageUrl = null != imageList && imageList.size() > 0 ? imageList.get(0) : "no picture";
        ShareUtils.shareToWeibo(this, content, null, imageUrl, new RequestListener() {
            @Override
            public void onComplete(String response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showToast(R.string.share_success);
                    }
                });
            }

            @Override
            public void onComplete4binary(ByteArrayOutputStream responseOS) {

            }

            @Override
            public void onIOException(IOException e) {

            }

            @Override
            public void onError(WeiboException e) {

            }
        });
    }

    private ArrayList<String> getImageList() {
        final int index = mViewPager.getCurrentItem();
        final DetailsFragment fragment = mPagerAdapter.getActiveFragment(mViewPager, index);
        return fragment.getImageList();
    }

    @Override
    public void onMusicClicked(String url) {
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("url", url);
        startService(intent);
    }

    private View mMusicPanel;
    private TextView mSongText;
    private TextView mArtistText;
    private ProgressBar mMusicProgress;
    private ImageButton mPlayBtn;

    private void setupMusicPanel() {
        if (null == mMusicPanel) {
            mMusicPanel = LayoutInflater.from(this).inflate(R.layout.music_pane_layout, null);

            mSongText = (TextView) mMusicPanel.findViewById(R.id.tv_song_title);
            mArtistText = (TextView) mMusicPanel.findViewById(R.id.tv_artist);
            mMusicProgress = (ProgressBar) mMusicPanel.findViewById(R.id.pb_music_loading);
            mPlayBtn = (ImageButton) mMusicPanel.findViewById(R.id.btn_play);
        }

        mSongText.setText(getString(R.string.content_loading));
        mArtistText.setVisibility(View.GONE);
        mPlayBtn.setVisibility(View.GONE);
        mMusicProgress.setVisibility(View.VISIBLE);
        mMusicPanel.setVisibility(View.VISIBLE);

        if (null != mMusicPanel.getParent()) return;

        final FrameLayout view = (FrameLayout) findViewById(android.R.id.content);
        final float density = getResources().getDisplayMetrics().density;
        final RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (density * 60. + 0.5));
        view.addView(mMusicPanel, lp);
    }

    @Override
    public void onMusicStartParse() {
        setupMusicPanel();
    }

    @Override
    public void onMusicPrepared(String song, String artist) {
        mSongText.setText(song);
        mArtistText.setText(artist);
        mMusicProgress.setVisibility(View.GONE);

        mArtistText.setVisibility(View.VISIBLE);
        mPlayBtn.setImageResource(R.drawable.ic_pause);
        mPlayBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMusicCompleted() {
        if (null == mMusicPanel) return;

        final FrameLayout view = (FrameLayout) findViewById(android.R.id.content);
        view.removeView(mMusicPanel);
    }

    @Override
    public void onMusicParseError() {

    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {
        mCurrentIndex = i;

        final FeedItem feed = mFeedList.get(mCurrentIndex);
        if (null != mCategoryMap) {
            setTitle(mCategoryMap.get(feed.category));
        }

        final boolean isFavorite = mDatabase.exist(feed.id);
        favoriteBtn.setImageResource(isFavorite ? R.drawable.ic_favorite_full : R.drawable.ic_favorite);
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    private class FeedPagerAdapter extends FragmentStatePagerAdapter {
        public FeedPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            final FeedItem feed = mFeedList.get(i);
            Bundle bundle = new Bundle();
            bundle.putParcelable(KEY_FEED, feed);
            return Fragment.instantiate(DetailsActivity.this, DetailsFragment.class.getName(), bundle);
        }

        @Override
        public int getCount() {
            return null != mFeedList ? mFeedList.size() : 0;
        }

        public DetailsFragment getActiveFragment(ViewPager container, int position) {
            return (DetailsFragment) instantiateItem(container, position);
        }
    }

    public void onFavoriteClicked(View v) {
        final UserInfo user = PrefsUtil.getUser();

        if (null == user || TextUtils.isEmpty(user.token)) {
            Utils.showToast("请先登录再收藏");
            return;
        }

        if (isFavorite) {
            favoriteBtn.setImageResource(R.drawable.ic_favorite);
            Utils.showToast(R.string.favorite_cancel);
            isFavorite = false;
        } else {
            favoriteBtn.setImageResource(R.drawable.ic_favorite_full);
            Utils.showToast(R.string.favorite_success);
            isFavorite = true;
        }

        favorite();
    }

    private void favorite() {
        if (isFavorite == favorited) return;

        final FeedItem feed = mFeedList.get(mCurrentIndex);

        String url;
        if (isFavorite) {
            url = "http://www.u148.net/json/favourite";
            mDatabase.insert(feed);
        } else {
            url = "http://www.u148.net/json/del_favourite";
            mDatabase.delete(feed.id);
        }

        final UserInfo user = PrefsUtil.getUser();
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", feed.id);
        params.put("token", user.token);
        HttpUtils.post(url, params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    @Override
    protected void applyTheme() {
        super.applyTheme();

        ImageView commentBtn = (ImageView) findViewById(R.id.ic_comment);
        ImageButton shareBtn = (ImageButton) findViewById(R.id.btn_share);

        if (Constants.MODE_NIGHT == mTheme) {
            commentBtn.setImageResource(R.drawable.ic_comment_night);
            shareBtn.setImageResource(R.drawable.ic_share_night);
            favoriteBtn.setImageResource(R.drawable.ic_favorite_night);
        } else {
            commentBtn.setImageResource(R.drawable.ic_comment);
            shareBtn.setImageResource(R.drawable.ic_social_share);
            favoriteBtn.setImageResource(R.drawable.ic_favorite);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mBounded) {
            bindService(new Intent(this, MusicService.class), mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBounded) {
            unbindService(mConnection);
            mBounded = false;

            if (null != mMusicPanel && mMusicPanel.getParent() != null) {
                ((ViewGroup) mMusicPanel.getParent()).removeView(mMusicPanel);
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMusicService = ((MusicService.MusicBinder) service).getService();
            mMusicService.registerListener(DetailsActivity.this);
            mBounded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMusicService.unRegisterListener();
            mMusicService = null;
            mBounded = false;
        }
    };

    public void onButtonClicked(View v) {
        switch (v.getId()) {
            case R.id.btn_play:
                if (mMusicService != null) {
                    mPlayBtn.setImageResource(mMusicService.isPlaying()
                            ? R.drawable.ic_play : R.drawable.ic_pause);
                    mMusicService.togglePlayer();
                }
                break;
        }
    }
}