package com.chenjishi.u148.activity;

import android.content.Intent;
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
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.AppApplication;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.service.DownloadAPKThread;
import com.chenjishi.u148.service.MusicService;
import com.chenjishi.u148.util.CommonUtil;
import com.chenjishi.u148.util.HttpUtils;
import com.chenjishi.u148.volley.Response;
import com.chenjishi.u148.volley.VolleyError;
import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-3
 * Time: 下午4:05
 * To change this template use File | Settings | File Templates.
 */
public class HomeActivity extends BaseActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener,
        Response.Listener<String>, Response.ErrorListener, ViewPager.OnPageChangeListener {

    private ViewPager mViewPager;
    private RadioGroup mRadioGroup;

    private DrawerLayout drawerLayout;

    private String[] categories;

    private String cacheFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitleText(R.string.app_home);

        ((ImageView) findViewById(R.id.actionbar_icon)).setImageResource(R.drawable.ic_drawer_home_pressed);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mRadioGroup = (RadioGroup) findViewById(R.id.radio_group);
        mRadioGroup.setOnCheckedChangeListener(this);

        categories = getResources().getStringArray(R.array.menu_category);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        initMenuList();

        AdView adView = new AdView(this, AdSize.FIT_SCREEN);
        LinearLayout adLayout = (LinearLayout) findViewById(R.id.adLayout);
        adLayout.addView(adView);

        mViewPager.setAdapter(new TabsAdapter(getSupportFragmentManager()));
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setCurrentItem(0);

        mRadioGroup.check(R.id.radio_home);

//        checkUpdate();
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
    protected void onStop() {
        super.onStop();
        stopService(new Intent(this, MusicService.class));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.home;
    }

    @Override
    protected void backIconClicked() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            drawerLayout.openDrawer(Gravity.LEFT);
        }
    }

//    private void initData(String path) {
//        if (null == path) return;
//
//        Object o = FileUtils.unserializeObject(path);
//        if (null != o) {
//            mFeedItems = (ArrayList<FeedItem>) o;
//            mAdapter.notifyDataSetChanged();
//        }
//    }

    //http://www.u148.net/list/2.html
    //http://www.u148.net/video/2.html

//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        long t = UsiteConfig.getUpdateTime(this);
//        if (System.currentTimeMillis() - t >= UsiteConfig.FOUR_HOURS) {
//            FileUtils.deleteFile(cacheFilePath);
//        }
//        finish();
//        return true;
//    }

    @Override
    public void onClick(View v) {
        drawerLayout.closeDrawer(Gravity.LEFT);
        Integer index = (Integer) v.getTag();
        if (null == index) return;

        Intent intent = new Intent();
        switch (index) {
            case 0:
                intent.setClass(this, VideoListActivity.class);
                break;
            case 1:
                intent.setClass(this, ArticleListActivity.class);
                break;
            case 2:
                intent.setClass(this, AboutActivity.class);
                break;
        }

        startActivity(intent);
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

    private class TabsAdapter extends FragmentPagerAdapter {

        public TabsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Bundle bundle = new Bundle();
            bundle.putInt("category", i);
            return Fragment.instantiate(HomeActivity.this, ItemFragment.class.getName(), bundle);
        }

        @Override
        public int getCount() {
            return 6;
        }
    }
}
