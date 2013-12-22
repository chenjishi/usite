package com.chenjishi.u148.activity;

import android.support.v4.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.widget.Toast;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.model.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by chenjishi on 13-12-20.
 */
public class UserActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitleText("我发表的文章");

        User user = PrefsUtil.getUser();
        if (TextUtils.isEmpty(user.url)) {
            new LoadTask(this).execute();
        } else {
            setupView();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.user_activity;
    }

    private void setupView() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putInt("category", -1);
        MyItemFragment fragment = (MyItemFragment) Fragment.instantiate(this, MyItemFragment.class.getName(), bundle);

        ft.add(R.id.fragment_container, fragment);
        ft.commitAllowingStateLoss();
    }

    private class LoadTask extends AsyncTask<String, Void, Boolean> {
        private ProgressDialog progress;
        private Context context;

        public LoadTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(UserActivity.this);
            progress.setMessage("Loading...");
            progress.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean result = false;

            HttpURLConnection conn = null;
            try {
                String userHomeUrl = "http://www.u148.net/home/";

                URL url = new URL(userHomeUrl);
                conn = (HttpURLConnection) url.openConnection();
                User user = PrefsUtil.getUser();
                conn.setConnectTimeout(5000);
                conn.setRequestProperty("Cookie", user.cookie);
                conn.connect();

                String html = inputToStream(conn.getInputStream());
                Document doc = Jsoup.parse(html);
                if (null != doc) {
                    Elements elements = doc.getElementsByClass("home_url");
                    if (null != elements && elements.size() > 0) {
                        Elements links = elements.get(0).select("a");
                        if (null != links && links.size() > 0) {
                            user.url = links.get(0).attr("href");
                            result = true;

                            url = new URL(user.url);
                            conn = (HttpURLConnection) url.openConnection();
                            conn.setConnectTimeout(5000);
                            conn.setRequestProperty("Cookie", user.cookie);
                            conn.connect();

                            String html2 = inputToStream(conn.getInputStream());

                            Document doc2 = Jsoup.parse(html2);
                            if (null != doc2) {
                                Elements authors = doc2.getElementsByClass("author");
                                if (authors != null && authors.size() > 0) {
                                    Elements imgs = authors.get(0).select("img");
                                    if (null != imgs && imgs.size() > 0) {
                                        user.avatar = imgs.attr("src");
                                    }
                                }

                                Elements sidebars = doc2.getElementsByClass("u148sidebar");
                                if (null != sidebars && sidebars.size() > 0) {
                                    Elements names = sidebars.get(0).select("strong");
                                    if (null != names && names.size() > 0) {
                                        user.nickName = names.get(0).text();
                                    }
                                }
                            }

                            PrefsUtil.setUser(user);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (null != conn)
                    conn.disconnect();
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progress.dismiss();
            if (aBoolean) {
                setupView();
            } else {
                Toast.makeText(context, "获取资源失败，请稍后重试!", Toast.LENGTH_SHORT).show();
            }
        }

        private String inputToStream(InputStream is) throws IOException {
            InputStreamReader in = new InputStreamReader(is);
            BufferedReader buff = new BufferedReader(in);
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = buff.readLine()) != null) {
                sb.append(line);
            }

            buff.close();
            is.close();

            return sb.toString();
        }
    }
}
