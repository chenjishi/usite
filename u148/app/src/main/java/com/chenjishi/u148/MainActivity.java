package com.chenjishi.u148;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.*;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chenjishi.u148.home.FeedListFragment;
import com.chenjishi.u148.home.MenuLayout;
import com.chenjishi.u148.home.SearchActivity;
import com.chenjishi.u148.model.UpdateInfo;
import com.chenjishi.u148.utils.*;
import com.chenjishi.u148.widget.TabPageIndicator;

import static com.chenjishi.u148.utils.Constants.API_UPGRADE;
import static com.chenjishi.u148.utils.Constants.MODE_DAY;

public class MainActivity extends BaseActivity implements DrawerLayout.DrawerListener, MenuLayout.MenuListener {
    private TabsAdapter mTabAdapter;
    private TabPageIndicator mTabIndicator;

    private DrawerLayout drawerLayout;

    private int maxIconIndent;

    private long mDownloadId;
    private boolean mDownloadReceiverRegistered;
    private LinearLayout mLeftView;

    private BroadcastReceiver mDownloadCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
            if (id != mDownloadId) return;

            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(id);
            Cursor cursor = downloadManager.query(query);

            if (!cursor.moveToFirst()) return;

            int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            if (DownloadManager.STATUS_SUCCESSFUL != cursor.getInt(statusIndex)) {
                return;
            }

            int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
            String apkUriString = cursor.getString(uriIndex);

            installApk(apkUriString);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setBackgroundDrawable(null);

        mLeftView = (LinearLayout) findViewById(R.id.left_view);
        mLeftView.setPadding(0, 0, 0, 0);

        //maximum 8dp for the indent of drawer icon
        maxIconIndent = dp2px(8);

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        mTabIndicator = (TabPageIndicator) findViewById(R.id.pager_tab_strip);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerListener(this);

        MenuLayout menuLayout = (MenuLayout) findViewById(R.id.menu_layout);
        menuLayout.setMenuListener(this);

        mTabAdapter = new TabsAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mTabAdapter);
        viewPager.setCurrentItem(0);

        mTabIndicator.setViewPager(viewPager);
        checkUpdate();
    }

    @Override
    public void onBackClicked(View v) {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            drawerLayout.openDrawer(Gravity.LEFT);
        }
    }

    @Override
    public void onThemeChanged() {
        applyTheme();
    }

    @Override
    public void onPanelClose() {
        drawerLayout.closeDrawers();
    }

    @Override
    public void onRightButtonClicked(View v) {
        Intent intent = new Intent(this, SearchActivity.class);
        IntentUtils.getInstance().startActivity(this, intent);
    }

    private void checkUpdate() {
        long lastCheckTime = Config.getLong(this, Config.KEY_CHECK_UPDATE_TIME, -1L);
        long currentTime = System.currentTimeMillis();
        if (lastCheckTime == -1 || currentTime >= lastCheckTime) {
            NetworkRequest.getInstance().get(API_UPGRADE, UpdateInfo.class, new Listener<UpdateInfo>() {
                @Override
                public void onResponse(UpdateInfo response) {
                    if (null == response || null == response.data) return;

                    UpdateInfo.UpdateData data = response.data;

                    int currentCode = Utils.getVersionCode(MainActivity.this);
                    if (data.versionCode > currentCode) {
                        downloadApk(data.url);
                    }
                }
            }, new ErrorListener() {
                @Override
                public void onErrorResponse() {

                }
            });

            Config.putLong(this, Config.KEY_CHECK_UPDATE_TIME, currentTime + 24 * 60 * 60 * 1000L);
        }
    }

    private void downloadApk(final String url) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.new_version_tip))
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startDownload(url);
                    }
                })
                .setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void startDownload(String url) {
        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(mDownloadCompleteReceiver, intentFilter);
        mDownloadReceiverRegistered = true;

        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.updating_app))
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "u148.apk");
        mDownloadId = downloadManager.enqueue(request);
    }

    private void installApk(String uri) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse(uri), "application/vnd.android.package-archive");
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        int indent = (int) (slideOffset * maxIconIndent);
        mLeftView.setPadding(-indent, 0, dp2px(8), 0);
    }

    @Override
    public void onDrawerOpened(View drawerView) {

    }

    @Override
    public void onDrawerClosed(View drawerView) {

    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }

    private long lastBackPressTime;
    private int backPressCount = 0;

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            if (backPressCount == 0) {
                Utils.showToast(this, R.string.exit_tips);
                backPressCount += 1;
                lastBackPressTime = System.currentTimeMillis();
            } else {
                if (System.currentTimeMillis() - lastBackPressTime >= 1000L) {
                    Utils.showToast(this, R.string.exit_tips);
                    lastBackPressTime = System.currentTimeMillis();
                } else {
                    IntentUtils.getInstance().clear();
                    finish();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDownloadReceiverRegistered) {
            unregisterReceiver(mDownloadCompleteReceiver);
        }
    }

    private class TabsAdapter extends FragmentPagerAdapter {
        private int[] ids = getResources().getIntArray(R.array.category_id);
        private String[] titles = getResources().getStringArray(R.array.category_name);

        public TabsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Bundle bundle = new Bundle();
            bundle.putInt("category", ids[i]);
            return Fragment.instantiate(MainActivity.this, FeedListFragment.class.getName(), bundle);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return ids.length;
        }
    }

    @Override
    protected void applyTheme() {
        Resources res = getResources();
        int theme = Config.getThemeMode(this);
        boolean day = theme == MODE_DAY;
        mRootView.setBackgroundColor(res.getColor(day ? R.color.background : R.color.background_night));
        findViewById(R.id.title_bar).setBackgroundColor(res.getColor(day ?
                R.color.action_bar_bg : R.color.action_bar_bg_night));

        TextView titleText = (TextView) findViewById(R.id.tv_title);
        titleText.setTextColor(day ? Color.WHITE : 0xFFBBBBBB);
        titleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18.f);
        titleText.setTypeface(null);
        ((ImageView) findViewById(R.id.ic_arrow)).setImageResource(day ?
        R.mipmap.ic_navigation_drawer : R.mipmap.ic_navigation_drawer_night);

        ImageButton rightBtn = (ImageButton) findViewById(R.id.btn_right);
        rightBtn.setImageResource(day ? R.mipmap.ic_action_search : R.mipmap.ic_action_search_night);
        rightBtn.setVisibility(View.VISIBLE);
        findViewById(R.id.split_h).setBackgroundColor(day ? 0xFFE6E6E6 :
                res.getColor(R.color.text_color_regular));

        mTabIndicator.setTheme(theme);
        mTabAdapter.notifyDataSetChanged();
    }
}
