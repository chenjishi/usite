package com.chenjishi.u148.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.AppApplication;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.model.User;
import com.chenjishi.u148.service.DataCacheService;
import com.chenjishi.u148.service.DownloadAPKThread;
import com.chenjishi.u148.service.MusicService;
import com.chenjishi.u148.util.CommonUtil;
import com.chenjishi.u148.util.Constants;
import com.chenjishi.u148.util.HttpUtils;
import com.chenjishi.u148.view.LoginDialog;
import com.chenjishi.u148.volley.Response;
import com.chenjishi.u148.volley.VolleyError;
import com.chenjishi.u148.volley.toolbox.ImageLoader;
import com.flurry.android.FlurryAgent;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-3
 * Time: 下午4:05
 * To change this template use File | Settings | File Templates.
 */
public class HomeActivity extends FragmentActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener,
        Response.Listener<String>, Response.ErrorListener, ViewPager.OnPageChangeListener, DrawerLayout.DrawerListener {

    private ViewPager mViewPager;
    private RadioGroup mRadioGroup;

    private DrawerLayout drawerLayout;
    private TextView mDrawerIcon;

    private int maxIconIndent;

    private String[] categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mRadioGroup = (RadioGroup) findViewById(R.id.radio_group);
        mRadioGroup.setOnCheckedChangeListener(this);

        categories = getResources().getStringArray(R.array.menu_category);

        mDrawerIcon = (TextView) findViewById(R.id.ic_drawer);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        drawerLayout.setDrawerListener(this);

        //maximum 8dp for the indent of drawer icon
        maxIconIndent = (int) (getResources().getDisplayMetrics().density * 8.0f);

        initMenuList();

        mViewPager.setAdapter(new TabsAdapter(getSupportFragmentManager()));
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setCurrentItem(0);

        mRadioGroup.check(R.id.radio_home);

        checkUpdate();

        //cookie will be expired after 30days, check it and login again;
        User user = PrefsUtil.getUser();
        if (null != user && !TextUtils.isEmpty(user.userName)) {
            long lastLoginTime = user.loginTime;
            if (lastLoginTime > 0) {
                long diff = System.currentTimeMillis() - lastLoginTime;
                long seconds = diff / 1000;
                if (seconds >= 30 * 24 * 60 * 60) {
                    login(user.userName, user.password);
                }
            }
        }
    }

    @Override
    public void onDrawerSlide(View view, float v) {
        int indent = (int) (v * maxIconIndent);
        mDrawerIcon.setPadding(-indent, 0, 0, 0);
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
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, "YYHS4STVXPMH6Y9GJ8WD");
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopService(new Intent(this, MusicService.class));
        FlurryAgent.onEndSession(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        TextView userText = (TextView) findViewById(R.id.user_name);
        if (null != userText) {
            userText.setText(getUserName());
        }
    }

    public void onDrawerButtonClicked(View v) {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            drawerLayout.openDrawer(Gravity.LEFT);
        }
    }

    @Override
    public void onClick(View v) {
        drawerLayout.closeDrawer(Gravity.LEFT);
        Integer index = (Integer) v.getTag();
        if (null == index) return;

        Intent intent = new Intent();
        switch (index) {
            case 0:
                User user = PrefsUtil.getUser();
                if (null != user && !TextUtils.isEmpty(user.cookie)) {
                    startActivity(new Intent(this, UserActivity.class));
                } else {
                    showLoginDialog();
                }
                break;
            case 1:
                intent.setClass(this, FunListActivity.class);
                intent.putExtra("source", Constants.SOURCE_JIANDAN);
                startActivity(intent);
                break;
            case 2:
                intent.setClass(this, FunListActivity.class);
                intent.putExtra("source", Constants.SOURCE_NEWS);
                startActivity(intent);
                break;
            case 3:
                intent.setClass(this, ArticleListActivity.class);
                startActivity(intent);
                break;
            case 4:
                intent.setClass(this, AboutActivity.class);
                startActivity(intent);
                break;
        }

    }

    private void showLoginDialog() {

        LoginDialog dialog = new LoginDialog(this, new LoginDialog.OnLoginListener() {
            @Override
            public void onConfirm(String userName, String password) {
                new LoginTask().execute(userName, password);
            }
        });
        dialog.show();
    }

    private String getUserName() {
        User user = PrefsUtil.getUser();
        String text;
        if (null != user && !TextUtils.isEmpty(user.userName)) {
            text = !TextUtils.isEmpty(user.nickName) ? user.nickName : user.userName;
        } else {
            text = categories[0];
        }

        return text;
    }

    private void initMenuList() {
        LinearLayout menuLayout = (LinearLayout) findViewById(R.id.layout_menu);

        View userView = LayoutInflater.from(this).inflate(R.layout.user_info, menuLayout, false);
        final ImageView userIcon = (ImageView) userView.findViewById(R.id.user_avatar);
        final TextView userText = (TextView) userView.findViewById(R.id.user_name);

        userText.setText(getUserName());
        User user = PrefsUtil.getUser();
        if (null != user && !TextUtils.isEmpty(user.avatar)) {
            ImageLoader imageLoader = HttpUtils.getImageLoader();
            imageLoader.get(user.avatar, ImageLoader.getImageListener(userIcon,
                    R.drawable.pictrue_bg, R.drawable.pictrue_bg));
            userIcon.setVisibility(View.VISIBLE);
        }

        userView.setTag(0);
        userView.setOnClickListener(this);
        menuLayout.addView(userView);

        for (int i = 1; i < categories.length; i++) {
            menuLayout.addView(getMenuItemView(i));
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
        itemView.setTextColor(0xFFDEDEDE);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataCacheService.getInstance().clearCaches();
    }

    private static final String REQUEST_URL = "http://www.u148.net/user/login.html";
    private static final String LOGING_URL = "http://www.u148.net/usr/ajax_login.u?usr.uname=%1$s&usr.password=%2$s&usr.exp=2592000&rand=%3$s";

    private class LoginTask extends AsyncTask<String, Void, Boolean> {

        private ProgressDialog progess;

        @Override
        protected void onPreExecute() {
            progess = new ProgressDialog(HomeActivity.this);
            progess.setMessage("登录中...");
            progess.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String userName = params[0];
            String email = params[1];

            return login(userName, email);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            Toast.makeText(HomeActivity.this, aBoolean ? "登陆成功!" : "登录失败!", Toast.LENGTH_SHORT).show();
            progess.dismiss();
            if (aBoolean) {
                User user = PrefsUtil.getUser();

                Map<String, String> params = new HashMap<String, String>();
                params.put("userName", user.userName);
                params.put("password", user.password);
                FlurryAgent.logEvent("login", params);

                startActivity(new Intent(HomeActivity.this, UserActivity.class));
            }
        }
    }

    private boolean login(String userName, String passWord) {
        boolean result = false;

        HttpURLConnection conn = null;
        URL url = null;
        try {
            url = new URL(REQUEST_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.connect();
            String headerName = null;
            String cookie = "";
            for (int i = 1; (headerName = conn.getHeaderFieldKey(i)) != null; i++) {
                if (headerName.equals("Set-Cookie")) {
                    cookie = conn.getHeaderField(i);
                    break;
                }
            }

            double rand = Math.random();
            String loginUrl = String.format(LOGING_URL, userName, passWord, rand + "");

            url = new URL(loginUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Cookie", cookie);
            conn.connect();

            for (int i = 1; (headerName = conn.getHeaderFieldKey(i)) != null; i++) {
                if (headerName.equals("Set-Cookie")) {
                    cookie = conn.getHeaderField(i);
                    break;
                }
            }

            InputStreamReader in = new InputStreamReader((InputStream) conn.getContent());
            BufferedReader buff = new BufferedReader(in);
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = buff.readLine()) != null) {
                sb.append(line);
            }
            buff.close();

            String json = sb.toString();
            if (!TextUtils.isEmpty(json)) {
                JSONObject jsonObj = new JSONObject(json);
                int code = jsonObj.optInt("status", -1);
                if (code == 1) {
                    User user = new User();
                    user.userName = userName;
                    user.password = passWord;
                    user.cookie = cookie;
                    user.loginTime = System.currentTimeMillis();
                    PrefsUtil.setUser(user);
                    result = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {

        } finally {
            if (null != conn) {
                conn.disconnect();
            }
        }

        return result;
    }
}
