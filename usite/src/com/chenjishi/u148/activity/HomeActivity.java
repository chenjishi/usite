package com.chenjishi.u148.activity;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.FileCache;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.model.UserInfo;
import com.chenjishi.u148.service.DownloadAPKThread;
import com.chenjishi.u148.util.Constants;
import com.chenjishi.u148.util.HttpUtils;
import com.chenjishi.u148.util.IntentUtils;
import com.chenjishi.u148.util.Utils;
import com.chenjishi.u148.view.AboutDialog;
import com.chenjishi.u148.view.ExitDialog;
import com.chenjishi.u148.view.FireworksView;
import com.chenjishi.u148.view.LoginDialog;
import com.chenjishi.u148.volley.Response;
import com.chenjishi.u148.volley.VolleyError;
import com.chenjishi.u148.volley.toolbox.ImageLoader;
import com.flurry.android.FlurryAgent;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-3
 * Time: 下午4:05
 * To change this template use File | Settings | File Templates.
 */
public class HomeActivity extends FragmentActivity implements RadioGroup.OnCheckedChangeListener,
        ViewPager.OnPageChangeListener, DrawerLayout.DrawerListener, LoginDialog.OnLoginListener,
        Response.Listener<String>, Response.ErrorListener {
    public static final int REQUEST_CODE_REGISTER = 101;
    public static final int RESULT_CODE_REGISTER = 102;
    private ViewPager mViewPager;
    private RadioGroup mRadioGroup;
    private TabsAdapter mTabAdapter;
    private MenuAdapter mMenuAdapter;

    private DrawerLayout drawerLayout;
    private TextView mDrawerIcon;

    private int maxIconIndent;
    private float density;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        getWindow().setBackgroundDrawable(null);
        /** we want to show Ad in detail page, so make it false */
        PrefsUtil.setAdShowed(false);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mRadioGroup = (RadioGroup) findViewById(R.id.radio_group);
        mRadioGroup.setOnCheckedChangeListener(this);

        mDrawerIcon = (TextView) findViewById(R.id.ic_drawer);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        drawerLayout.setDrawerListener(this);

        //maximum 8dp for the indent of drawer icon
        density = getResources().getDisplayMetrics().density;
        maxIconIndent = (int) (density * 8.0f);

        initMenuList();

        mTabAdapter = new TabsAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mTabAdapter);
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setCurrentItem(0);

        mRadioGroup.check(R.id.radio_home);
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
        if (lastCheckTime == -1 || System.currentTimeMillis() >= lastCheckTime) {
            HttpUtils.get("http://www.u148.net/json/version", this, this);
            PrefsUtil.saveLongPreference(PrefsUtil.KEY_CHECK_UPDATE_TIME, System.currentTimeMillis() + 24 * 60 * 60 * 1000L);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {
        if (TextUtils.isEmpty(response)) return;

        try {
            JSONObject jObj = new JSONObject(response);
            final JSONObject dataObj = jObj.getJSONObject("data");

            final int versionCode = dataObj.optInt("versionCode", -1);
            final int currentCode = Utils.getVersionCode(HomeActivity.this);

            if (versionCode > currentCode) {
                downloadApk(dataObj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void downloadApk(final JSONObject dataObj) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.new_version_tip))
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String apkUrl = dataObj.optString("url", "");
                        final String path = FileCache.getTempCacheDir();
                        if (!new File(path).exists()) FileCache.mkDirs(path);

                        new DownloadAPKThread(apkUrl, FileCache.getTempCacheDir(), "u148.apk").start();
                    }
                })
                .setNegativeButton(R.string.cancel, null);
        builder.show();
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
    public void onPageScrolled(int i, float v, int i2) {
    }

    @Override
    public void onPageSelected(int i) {
        int id = -1;
        switch (i) {
            case 0:
                id = R.id.radio_home;
                break;
            case 1:
                id = R.id.radio_video;
                break;
            case 2:
                id = R.id.radio_image;
                break;
            case 3:
                id = R.id.radio_audio;
                break;
            case 4:
                id = R.id.radio_text;
                break;
            case 5:
                id = R.id.radio_miscell;
                break;
        }

        mRadioGroup.check(id);
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int index = 1;
        switch (checkedId) {
            case R.id.radio_home:
                index = 0;
                break;
            case R.id.radio_video:
                index = 1;
                break;
            case R.id.radio_image:
                index = 2;
                break;
            case R.id.radio_audio:
                index = 3;
                break;
            case R.id.radio_text:
                index = 4;
                break;
            case R.id.radio_miscell:
                index = 5;
                break;
        }
        mViewPager.setCurrentItem(index);
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
        super.onDestroy();
        if (null != mPlayer) {
            mPlayer.release();
            mPlayer = null;
        }
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
                            (int)(32. * density));
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
        private int[] categoryIds;

        public TabsAdapter(FragmentManager fm) {
            super(fm);
            categoryIds = getResources().getIntArray(R.array.category_id);
        }

        @Override
        public Fragment getItem(int i) {
            Bundle bundle = new Bundle();
            bundle.putInt("category", categoryIds[i]);
            return Fragment.instantiate(HomeActivity.this, FeedListFragment.class.getName(), bundle);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return 6;
        }
    }

    private void applyTheme(int theme) {
        final FrameLayout rootView = (FrameLayout) findViewById(android.R.id.content);
        final RelativeLayout titleView = (RelativeLayout) findViewById(R.id.title_bar);
        final TextView leftBtn = (TextView) findViewById(R.id.ic_drawer);
        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        final RadioButton rb1 = (RadioButton) findViewById(R.id.radio_home);
        final RadioButton rb2 = (RadioButton) findViewById(R.id.radio_video);
        final RadioButton rb3 = (RadioButton) findViewById(R.id.radio_image);
        final RadioButton rb4 = (RadioButton) findViewById(R.id.radio_audio);
        final RadioButton rb5 = (RadioButton) findViewById(R.id.radio_text);
        final RadioButton rb6 = (RadioButton) findViewById(R.id.radio_miscell);
        final View split = findViewById(R.id.split_h);

        if (Constants.MODE_NIGHT == theme) {
            rootView.setBackgroundColor(0xFF222222);
            titleView.setBackgroundColor(getResources().getColor(R.color.action_bar_bg_night));
            leftBtn.setTextColor(0xFFBBBBBB);
            leftBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_navigation_drawer_night, 0, 0, 0);
            radioGroup.setBackgroundColor(0xFF1C1C1C);
            rb1.setTextColor(getResources().getColorStateList(R.color.tab_text_color_night));
            rb2.setTextColor(getResources().getColorStateList(R.color.tab_text_color_night));
            rb3.setTextColor(getResources().getColorStateList(R.color.tab_text_color_night));
            rb4.setTextColor(getResources().getColorStateList(R.color.tab_text_color_night));
            rb5.setTextColor(getResources().getColorStateList(R.color.tab_text_color_night));
            rb6.setTextColor(getResources().getColorStateList(R.color.tab_text_color_night));

            rb1.setBackgroundResource(R.drawable.tab_indicator_night);
            rb2.setBackgroundResource(R.drawable.tab_indicator_night);
            rb3.setBackgroundResource(R.drawable.tab_indicator_night);
            rb4.setBackgroundResource(R.drawable.tab_indicator_night);
            rb5.setBackgroundResource(R.drawable.tab_indicator_night);
            rb6.setBackgroundResource(R.drawable.tab_indicator_night);

            split.setBackgroundColor(getResources().getColor(R.color.text_color_regular));
        } else {
            rootView.setBackgroundColor(getResources().getColor(R.color.background));
            titleView.setBackgroundColor(0xFFff9900);
            leftBtn.setTextColor(0xFFFFFFFF);
            leftBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_navigation_drawer, 0, 0, 0);
            radioGroup.setBackgroundColor(0xFFE5E5E5);
            rb1.setTextColor(getResources().getColorStateList(R.color.tab_text_color));
            rb2.setTextColor(getResources().getColorStateList(R.color.tab_text_color));
            rb3.setTextColor(getResources().getColorStateList(R.color.tab_text_color));
            rb4.setTextColor(getResources().getColorStateList(R.color.tab_text_color));
            rb5.setTextColor(getResources().getColorStateList(R.color.tab_text_color));
            rb6.setTextColor(getResources().getColorStateList(R.color.tab_text_color));

            rb1.setBackgroundResource(R.drawable.abs__tab_indicator_ab_holo);
            rb2.setBackgroundResource(R.drawable.abs__tab_indicator_ab_holo);
            rb3.setBackgroundResource(R.drawable.abs__tab_indicator_ab_holo);
            rb4.setBackgroundResource(R.drawable.abs__tab_indicator_ab_holo);
            rb5.setBackgroundResource(R.drawable.abs__tab_indicator_ab_holo);
            rb6.setBackgroundResource(R.drawable.abs__tab_indicator_ab_holo);

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
