package com.chenjishi.u148.activity;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.*;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.*;
import android.widget.*;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.model.UpdateInfo;
import com.chenjishi.u148.model.UserInfo;
import com.chenjishi.u148.util.Constants;
import com.chenjishi.u148.util.HttpUtils;
import com.chenjishi.u148.util.IntentUtils;
import com.chenjishi.u148.util.Utils;
import com.chenjishi.u148.view.*;
import com.chenjishi.u148.volley.Response;
import com.chenjishi.u148.volley.VolleyError;
import com.chenjishi.u148.volley.toolbox.ImageLoader;
import com.flurry.android.FlurryAgent;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-3
 * Time: 下午4:05
 * To change this template use File | Settings | File Templates.
 */
public class HomeActivity extends FragmentActivity implements DrawerLayout.DrawerListener,
        LoginDialog.OnLoginListener {
    public static final int REQUEST_CODE_REGISTER = 101;
    public static final int RESULT_CODE_REGISTER = 102;
    private TabsAdapter mTabAdapter;
    private MenuAdapter mMenuAdapter;
    private TabPageIndicator mTabIndicator;

    private DrawerLayout drawerLayout;
    private TextView mDrawerIcon;

    private int maxIconIndent;
    private float density;

    private long mDownloadId;
    private boolean mDownloadReceiverRegistered = false;

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
        setContentView(R.layout.home);
        getWindow().setBackgroundDrawable(null);
        /** we want to show Ad in detail page, so make it false */
        PrefsUtil.setAdShowed(false);

        //maximum 8dp for the indent of drawer icon
        density = getResources().getDisplayMetrics().density;
        maxIconIndent = (int) (density * 8.0f);

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        mTabIndicator = (TabPageIndicator) findViewById(R.id.pager_tab_strip);

        mDrawerIcon = (TextView) findViewById(R.id.ic_drawer);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        drawerLayout.setDrawerListener(this);

        initMenuList();

        mTabAdapter = new TabsAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mTabAdapter);
        viewPager.setCurrentItem(0);

        mTabIndicator.setViewPager(viewPager);


        applyTheme(PrefsUtil.getThemeMode());

        ImageButton button = (ImageButton) findViewById(R.id.btn_avatar);

        float density = getResources().getDisplayMetrics().density;

        int reqWidth = (int) (32 * density * 0.88);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_avatar);
        Bitmap scaledBitmap = ThumbnailUtils.extractThumbnail(bitmap, reqWidth, reqWidth);

        Bitmap circleBitmap = Utils.circleToBitmap(scaledBitmap);

        button.setImageBitmap(circleBitmap);

        setUserIcon();
        checkUpdate();
    }

    private void checkUpdate() {
        if (!Utils.isWifiConnected(this)) return;

        long lastCheckTime = PrefsUtil.getLongPreferences(PrefsUtil.KEY_CHECK_UPDATE_TIME, -1L);
        long currentTime = System.currentTimeMillis();
        if (lastCheckTime == -1 || currentTime >= lastCheckTime) {
            HttpUtils.get("http://www.u148.net/json/version", UpdateInfo.class, new Response.Listener<UpdateInfo>() {
                @Override
                public void onResponse(UpdateInfo response) {
                    if (null == response || null == response.data) return;

                    UpdateInfo.UpdateData data = response.data;

                    int currentCode = Utils.getVersionCode(HomeActivity.this);
                    if (data.versionCode > currentCode) {
                        downloadApk(data.url);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });

            PrefsUtil.saveLongPreference(PrefsUtil.KEY_CHECK_UPDATE_TIME, currentTime + 24 * 60 * 60 * 1000L);
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

    private void setUserIcon() {
        final ImageButton button = (ImageButton) findViewById(R.id.btn_avatar);
        final float density = getResources().getDisplayMetrics().density;
        final int reqWidth = (int) (32 * density * 0.88);

        if (Utils.isLogin()) {
            UserInfo user = PrefsUtil.getUser();

            HttpUtils.getImageLoader().get(user.icon, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    Bitmap bitmap = response.getBitmap();

                    if (null != bitmap) {
                        button.setImageBitmap(bitmap);
                    } else {
                        button.setImageBitmap(getDefaultIcon(reqWidth));
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    button.setImageBitmap(getDefaultIcon(reqWidth));

                }
            }, reqWidth, reqWidth, true);
        } else {
            button.setImageBitmap(getDefaultIcon((int) (32 * density)));
        }
    }

    @Override
    public void onLoginSuccess() {
        setUserIcon();
        mMenuAdapter.notifyDataSetChanged();
        Utils.showToast(getString(R.string.login_success));
    }

    @Override
    public void onLoginError() {
        Utils.showToast(getString(R.string.login_fail));
    }

    private Bitmap getDefaultIcon(int w) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_avatar);
        Bitmap scaledBitmap = ThumbnailUtils.extractThumbnail(bitmap, w, w);

        return Utils.circleToBitmap(scaledBitmap);
    }

    @Override
    public void onDrawerSlide(View view, float v) {
        int indent = (int) (v * maxIconIndent);
        mDrawerIcon.setPadding(-indent, 0, (int) (density * 8.0f), 0);
    }

    @Override
    public void onDrawerOpened(View view) {

    }

    @Override
    public void onDrawerClosed(View view) {

    }

    @Override
    public void onDrawerStateChanged(int i) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, "YYHS4STVXPMH6Y9GJ8WD");
    }

    @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    private long lastBackPressTime;
    private int backPressCount = 0;

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            if (backPressCount == 0) {
                Utils.showToast("再按一次退出");
                backPressCount += 1;
                lastBackPressTime = System.currentTimeMillis();
            } else {
                if (System.currentTimeMillis() - lastBackPressTime >= 1000L) {
                    Utils.showToast("再按一次退出");
                    lastBackPressTime = System.currentTimeMillis();
                } else {
                    PrefsUtil.setAdShowed(false);
                    finish();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mDownloadReceiverRegistered) {
            unregisterReceiver(mDownloadCompleteReceiver);
        }
        if (null != mPlayer) {
            mPlayer.release();
            mPlayer = null;
        }
        super.onDestroy();
    }

    public void onDrawerButtonClicked(View v) {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            drawerLayout.openDrawer(Gravity.LEFT);
        }
    }

    public void onLoginButtonClicked(View v) {
        if (Utils.isLogin()) {
            final ExitDialog dialog = new ExitDialog(this, new ExitDialog.OnLogoutListener() {
                @Override
                public void logout() {
                    PrefsUtil.setUser(null);
                    setUserIcon();
                    Utils.showToast(R.string.logout_success);
                    mMenuAdapter.notifyDataSetChanged();
                }
            });
            dialog.show();
        } else {
            new LoginDialog(this, this).show();
        }
    }

    private MediaPlayer mPlayer;

    private void easterEgg() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        FrameLayout rootView = (FrameLayout) findViewById(android.R.id.content);
        final FireworksView fireworksView = new FireworksView(this);
        rootView.addView(fireworksView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mPlayer = MediaPlayer.create(this, R.raw.fireworks);
        mPlayer.setLooping(true);
        mPlayer.start();
    }

    private void initMenuList() {
        final ListView listView = (ListView) findViewById(R.id.list_menu);
        mMenuAdapter = new MenuAdapter();
        listView.setAdapter(mMenuAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                switch (position) {
                    case 0:
                        if (!Utils.isLogin()) {
                            intent = new Intent(HomeActivity.this, RegisterActivity.class);
                            startActivityForResult(intent, REQUEST_CODE_REGISTER);
                        } else {
                            Utils.showToast("您已经登录");
                            drawerLayout.closeDrawer(Gravity.LEFT);
                        }
                        break;
                    case 1:
                        intent = new Intent(HomeActivity.this, SettingsActivity.class);
                        IntentUtils.startPreviewActivity(HomeActivity.this, intent);
                        break;
                    case 2:
                        PrefsUtil.setThemeMode(PrefsUtil.getThemeMode() == Constants.MODE_DAY
                                ? Constants.MODE_NIGHT : Constants.MODE_DAY);
                        mMenuAdapter.notifyDataSetChanged();
                        applyTheme(PrefsUtil.getThemeMode());
                        drawerLayout.closeDrawer(Gravity.LEFT);
                        break;
                    case 3:
                        if (Utils.isLogin()) {
                            intent = new Intent(HomeActivity.this, FavoriteActivity.class);
                            IntentUtils.startPreviewActivity(HomeActivity.this, intent);
                        } else {
                            new LoginDialog(HomeActivity.this, HomeActivity.this).show();
                            drawerLayout.closeDrawer(Gravity.LEFT);
                        }
                        break;
                    case 4:
                        Uri uri = Uri.parse("market://details?id=" + getPackageName());
                        intent = new Intent(Intent.ACTION_VIEW, uri);
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Utils.showToast(R.string.google_play_unavailable);
                        }
                        drawerLayout.closeDrawer(Gravity.LEFT);
                        break;
                    case 5:
                        AboutDialog dialog = new AboutDialog(HomeActivity.this, new AboutDialog.AboutDialogListener() {
                            @Override
                            public void onVersionClicked() {
                                easterEgg();
                            }
                        });
                        dialog.show();
                        drawerLayout.closeDrawer(Gravity.LEFT);
                        break;
                }
            }
        });
    }

    private class MenuAdapter extends BaseAdapter {
        private String[] menuItems;
        private int[] iconIds;

        public MenuAdapter() {
            menuItems = getResources().getStringArray(R.array.menu_item);
            iconIds = new int[]{R.drawable.user_default,
                    R.drawable.ic_settings,
                    R.drawable.ic_bulb,
                    R.drawable.ic_favorite_menu,
                    R.drawable.ic_star,
                    R.drawable.ic_info};
        }

        @Override
        public int getCount() {
            return menuItems.length;
        }

        @Override
        public String getItem(int position) {
            return menuItems[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (null == convertView) {
                convertView = LayoutInflater.from(HomeActivity.this).inflate(R.layout.menu_cell, parent, false);
                holder = new ViewHolder();

                holder.iconImage = (ImageView) convertView.findViewById(R.id.iv_icon);
                holder.titleText = (TextView) convertView.findViewById(R.id.tv_title);

                convertView.setTag(holder);
            }

            holder = (ViewHolder) convertView.getTag();

            if (0 == position) {
                final boolean isLogin = Utils.isLogin();
                RelativeLayout.LayoutParams layoutParams;
                if (isLogin) {
                    final UserInfo userInfo = PrefsUtil.getUser();
                    final ImageLoader imageLoader = HttpUtils.getImageLoader();
                    layoutParams = new RelativeLayout.LayoutParams((int) (32. * density),
                            (int) (32. * density));
                    layoutParams.setMargins(0, 0, (int) (8. * density), 0);

                    holder.titleText.setText(userInfo.nickname);
                    holder.iconImage.setLayoutParams(layoutParams);
                    imageLoader.get(userInfo.icon, ImageLoader.getImageListener(holder.iconImage, R.drawable.ic_avatar_2,
                            R.drawable.ic_avatar_2));
                } else {
                    layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(0, 0, (int) (density * 8.), 0);
                    holder.titleText.setText(getItem(position));
                    holder.iconImage.setLayoutParams(layoutParams);
                    holder.iconImage.setImageResource(R.drawable.ic_avatar_2);
                }
            } else if (2 == position) {
                final int mode = PrefsUtil.getThemeMode();
                holder.titleText.setText(mode == Constants.MODE_DAY ? "夜间" : "日间");
                holder.iconImage.setImageResource(iconIds[position]);
            } else {
                holder.titleText.setText(getItem(position));
                holder.iconImage.setImageResource(iconIds[position]);
            }

            return convertView;
        }
    }

    private static class ViewHolder {
        ImageView iconImage;
        TextView titleText;
    }

    private class TabsAdapter extends FragmentPagerAdapter {
        private int[] categoryIds = {0, 3, 6, 7, 5, 8, 9};
        private int[] mTitleIds = {
                R.string.app_home,
                R.string.app_image,
                R.string.app_text,
                R.string.app_miscell,
                R.string.app_audio,
                R.string.app_bottle,
                R.string.app_market
        };

        public TabsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Bundle bundle = new Bundle();
            bundle.putInt("category", categoryIds[i]);
            return Fragment.instantiate(HomeActivity.this, FeedListFragment.class.getName(), bundle);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getResources().getString(mTitleIds[position]);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return categoryIds.length;
        }
    }

    private void applyTheme(int theme) {
        final FrameLayout rootView = (FrameLayout) findViewById(android.R.id.content);
        final RelativeLayout titleView = (RelativeLayout) findViewById(R.id.title_bar);
        final TextView leftBtn = (TextView) findViewById(R.id.ic_drawer);

        mTabIndicator.setTheme(theme);
        final View split = findViewById(R.id.split_h);

        if (Constants.MODE_NIGHT == theme) {
            rootView.setBackgroundColor(0xFF222222);
            titleView.setBackgroundColor(getResources().getColor(R.color.action_bar_bg_night));
            leftBtn.setTextColor(0xFFBBBBBB);
            leftBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_navigation_drawer_night, 0, 0, 0);
            split.setBackgroundColor(getResources().getColor(R.color.text_color_regular));
        } else {
            rootView.setBackgroundColor(getResources().getColor(R.color.background));
            titleView.setBackgroundColor(0xFFff9900);
            leftBtn.setTextColor(0xFFFFFFFF);
            leftBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_navigation_drawer, 0, 0, 0);
            split.setBackgroundColor(0xFFE6E6E6);
        }

        mTabAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_REGISTER) {
            /**
             * register success, update the user icon in menu
             */
            if (resultCode == RESULT_CODE_REGISTER) {
                mMenuAdapter.notifyDataSetChanged();
                setUserIcon();
            }
        }
    }
}
