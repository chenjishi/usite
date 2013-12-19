package com.chenjishi.u148.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.chenjishi.u148.R;
import com.chenjishi.u148.util.CommonUtil;
import com.chenjishi.u148.util.Constants;
import com.flurry.android.FlurryAgent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-7-11
 * Time: 下午7:31
 * To change this template use File | Settings | File Templates.
 */
public class ArticleListActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private ArrayList<NewArticle> mDataList = new ArrayList<NewArticle>();
    private ArticleAdapter mAdapter;
    private View mEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitleText(R.string.app_latest);

        mEmptyView = LayoutInflater.from(this).inflate(R.layout.empty_view, null);
        ListView listView = (ListView) findViewById(R.id.list_article);
        ((ViewGroup) listView.getParent()).addView(mEmptyView);
        listView.setEmptyView(mEmptyView);

        mAdapter = new ArticleAdapter(this);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);

        loadData();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        NewArticle item = mDataList.get(position);
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("title", item.title);
        intent.putExtra("link", item.url);
        intent.putExtra("source", Constants.SOURCE_U148);

        Map<String, String> params = new HashMap<String, String>();
        params.put("article_title", item.title);
        FlurryAgent.logEvent("read_article", params);

        startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.article_list_layout;
    }

    private void loadData() {
        Runnable action = new Runnable() {
            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect("http://www.u148.net/").get();
                    Elements u148main = doc.getElementsByClass("u148main");
                    Elements content = u148main.get(0).getElementsByClass("u148content");
                    Elements articles = content.get(0).getElementsByClass("uarticles");
                    if (articles.size() > 0) {
                        Element list = articles.get(0).child(0);
                        Elements items = list.select("li");
                        for (Element e : items) {
                            NewArticle article = new NewArticle();
                            article.url = e.child(0).attr("href");
                            article.title = e.child(0).text();
                            article.author = e.child(1).child(0).text();

                            mDataList.add(article);
                        }
                    }
                } catch (IOException e) {
                }
            }
        };

        Runnable postAction = new Runnable() {
            @Override
            public void run() {
                if (mDataList.size() > 0) {
                    mAdapter.notifyDataSetChanged();
                } else {
                    mEmptyView.findViewById(R.id.progress_bar).setVisibility(View.GONE);
                    ((TextView) mEmptyView.findViewById(R.id.tv_empty_tip)).setText("网络连接错误");
                }
            }
        };
        CommonUtil.runWithoutMessage(action, postAction);
    }

    private class ArticleAdapter extends BaseAdapter {
        LayoutInflater inflater;

        public ArticleAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView;
            if (null == convertView) {
                itemView = inflater.inflate(R.layout.list_item_article, parent, false);
            } else {
                itemView = convertView;
            }

            NewArticle article = (NewArticle) getItem(position);
            String formattedString = String.format("%s <font color='#888888'>%s</font>", article.title, article.author);

            ((TextView) itemView.findViewById(R.id.tag_title)).setText(Html.fromHtml(formattedString));

            return itemView;
        }
    }

    class NewArticle {
        public String url;
        public String title;
        public String author;
    }
}
