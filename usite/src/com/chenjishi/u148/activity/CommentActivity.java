package com.chenjishi.u148.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.chenjishi.u148.R;
import com.chenjishi.u148.entity.Comment;
import com.chenjishi.u148.util.CommonUtil;
import com.chenjishi.u148.util.HttpUtils;
import com.chenjishi.u148.volley.toolbox.ImageLoader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-5-4
 * Time: 下午12:35
 * To change this template use File | Settings | File Templates.
 */
public class CommentActivity extends BaseActivity {
    private ArrayList<Comment> comments = new ArrayList<Comment>();
    private CommentAdapter mAdapter;

    private View mEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitleText(R.string.comment);

        mEmptyView = LayoutInflater.from(this).inflate(R.layout.empty_view, null);

        mAdapter = new CommentAdapter(this);
        ListView listView = (ListView) findViewById(R.id.list_comment);
        ((ViewGroup) listView.getParent()).addView(mEmptyView);
        listView.setEmptyView(mEmptyView);
        listView.setAdapter(mAdapter);

        loadData();
    }

    private void loadData() {
        Runnable action = new Runnable() {
            @Override
            public void run() {
                parseContent();
            }
        };

        Runnable postAction = new Runnable() {
            @Override
            public void run() {
                if (null != comments && comments.size() > 0) {
                    mAdapter.notifyDataSetChanged();
                } else {
                    mEmptyView.findViewById(R.id.progress_bar).setVisibility(View.GONE);
                    ((TextView) mEmptyView.findViewById(R.id.tv_empty_tip)).setText(getString(R.string.no_comment));
                }
            }
        };

        CommonUtil.runWithoutMessage(action, postAction);
    }

    private void parseContent() {
        String content = getIntent().getStringExtra("floors");
        if (null == content) finish();

        Document doc = Jsoup.parse(content);
        Elements items = doc.select("ul");
        for (Element e : items) {
            Comment comment = new Comment();
            Elements el = e.select("li");
            if (el.size() > 0) {
                Element _el = el.get(0);

                Element imgEl = _el.getElementsByClass("uhead").get(0);
                comment.avatar = imgEl.attr("src");

                Element userEl = _el.getElementsByClass("reply").get(0);
                Element userInfo = userEl.getElementsByClass("uinfo").get(0);
                comment.userName = userInfo.select("a").get(0).text();
                comment.time = userInfo.select("span").get(0).text();

                comment.content = userInfo.nextElementSibling().text();
            }

            comments.add(comment);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.comment;
    }

    @Override
    protected void backIconClicked() {
        finish();
    }

    private class CommentAdapter extends BaseAdapter {
        LayoutInflater inflater;

        public CommentAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return null == comments ? 0 : comments.size();
        }

        @Override
        public Object getItem(int position) {
            return null == comments ? null : comments.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (null == convertView) {
                convertView = inflater.inflate(R.layout.comment_item, null);

                holder = new ViewHolder();
                holder.iconAvatar = (ImageView) convertView.findViewById(R.id.icon_avatar);
                holder.tagUser = (TextView) convertView.findViewById(R.id.tag_user);
                holder.tagContent = (TextView) convertView.findViewById(R.id.tag_content);

                convertView.setTag(holder);
            }

            holder = (ViewHolder) convertView.getTag();

            Comment comment = comments.get(position);
            int idx = comment.time.indexOf('┆');
            String dateString = "";
            if (idx != -1) {
                dateString = comment.time.substring(0, idx);
            }

            ImageLoader imageLoader = HttpUtils.getImageLoader();
            imageLoader.get(comment.avatar, ImageLoader.getImageListener(holder.iconAvatar,
                    R.drawable.pictrue_bg, R.drawable.pictrue_bg));

            String formattedString = comment.userName + " " + "<font color='#999999'>" + dateString + "</font>";
            holder.tagUser.setText(Html.fromHtml(formattedString));
            holder.tagContent.setText(comment.content);

            return convertView;
        }

        class ViewHolder {
            ImageView iconAvatar;
            TextView tagUser;
            TextView tagContent;
        }
    }
}
