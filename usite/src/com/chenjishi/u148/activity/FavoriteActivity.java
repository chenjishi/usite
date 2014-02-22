package com.chenjishi.u148.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.model.Favorite;
import com.chenjishi.u148.model.FavoriteItem;
import com.chenjishi.u148.model.Feed;
import com.chenjishi.u148.model.User;
import com.chenjishi.u148.util.Constants;
import com.chenjishi.u148.util.HttpUtils;
import com.chenjishi.u148.volley.Response;
import com.chenjishi.u148.volley.VolleyError;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by chenjishi on 14-2-22.
 */
public class FavoriteActivity extends BaseActivity implements Response.Listener<Favorite>,
        Response.ErrorListener, View.OnClickListener, AdapterView.OnItemClickListener {
    private FavoriteAdapter mAdapter;
    private ArrayList<FavoriteItem> favoriteList = new ArrayList<FavoriteItem>();

    private View emptyView;
    private View footView;

    private int currentPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        setTitle(R.string.favorite);

        footView = LayoutInflater.from(this).inflate(R.layout.load_more, null);
        Button button = (Button) footView.findViewById(R.id.btn_load);
        button.setOnClickListener(this);

        emptyView = findViewById(R.id.empty_layout);
        ListView listView = (ListView) findViewById(R.id.list_favorite);
        listView.addFooterView(footView);
        listView.setOnItemClickListener(this);
        footView.setVisibility(View.GONE);
        listView.setEmptyView(emptyView);
        mAdapter = new FavoriteAdapter(this);

        listView.setAdapter(mAdapter);

        if (Constants.MODE_NIGHT == PrefsUtil.getThemeMode()) {
            listView.setDivider(getResources().getDrawable(R.drawable.split_color_night));
            button.setBackgroundResource(R.drawable.btn_gray_night);
            button.setTextColor(getResources().getColor(R.color.text_color_summary));
        } else {
            listView.setDivider(getResources().getDrawable(R.drawable.split_color));
            button.setBackgroundResource(R.drawable.btn_gray);
            button.setTextColor(getResources().getColor(R.color.text_color_regular));
        }
        listView.setDividerHeight(1);

        loadData();
    }

    void loadData() {
        final String url = "http://www.u148.net/json/get_favourite/0/%1$d?token=%2$s";
        final User user = PrefsUtil.getUser();

        HttpUtils.get(String.format(url, currentPage, user.token), Favorite.class, this, this);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        setErrorTip("网络无连接，请检查网络");
        footView.setVisibility(View.GONE);
    }

    @Override
    public void onResponse(Favorite response) {
        if (null != response) {
            final ArrayList<FavoriteItem> favorites = response.data.data;
            if (null != favorites && favorites.size() > 0) {
                favoriteList.addAll(favorites);
                mAdapter.notifyDataSetChanged();

                if (favorites.size() >= 30) {
                    footView.findViewById(R.id.loading_layout).setVisibility(View.GONE);
                    footView.findViewById(R.id.btn_load).setVisibility(View.VISIBLE);
                    footView.setVisibility(View.VISIBLE);
                }
            } else {
                footView.setVisibility(View.GONE);
            }
        } else {
            setErrorTip("解析错误或者网络无返回，请稍后再试");
            footView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        footView.findViewById(R.id.btn_load).setVisibility(View.GONE);
        footView.findViewById(R.id.loading_layout).setVisibility(View.VISIBLE);
        currentPage++;
        loadData();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FavoriteItem favoriteItem = favoriteList.get(position);

        Feed feed = new Feed();
        feed.id = favoriteItem.aid;
        feed.title = favoriteItem.title;
        feed.createTime = favoriteItem.create_time;
        feed.category = favoriteItem.category;

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("feed", feed);
        startActivity(intent);
    }

    void setErrorTip(String s) {
        final View errorView = emptyView;
        errorView.findViewById(R.id.progress_bar).setVisibility(View.GONE);
        ((TextView) errorView.findViewById(R.id.tv_empty_tip)).setText(s);
    }

    class FavoriteAdapter extends BaseAdapter {
        LayoutInflater inflater;
        private SimpleDateFormat dateFormat;

        public FavoriteAdapter(Context context) {
            inflater = LayoutInflater.from(context);
            dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        }

        @Override
        public int getCount() {
            return favoriteList.size();
        }

        @Override
        public FavoriteItem getItem(int position) {
            return favoriteList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (null == convertView) {
                convertView = inflater.inflate(R.layout.favorite_list_item, parent, false);
                holder = new ViewHolder();

                holder.titleText = (TextView) convertView.findViewById(R.id.tv_title);
                holder.timeText = (TextView) convertView.findViewById(R.id.tv_time);

                if (PrefsUtil.getThemeMode() == Constants.MODE_NIGHT) {
                    holder.titleText.setTextColor(getResources().getColor(R.color.text_color_weak));
                    holder.timeText.setTextColor(getResources().getColor(R.color.text_color_summary));
                } else {
                    holder.titleText.setTextColor(getResources().getColor(R.color.text_color_regular));
                    holder.timeText.setTextColor(getResources().getColor(R.color.text_color_weak));
                }

                convertView.setTag(holder);
            }
            holder = (ViewHolder) convertView.getTag();

            final FavoriteItem favorite = getItem(position);
            holder.titleText.setText(favorite.title);

            final String formattedTime = dateFormat.format(new Date(favorite.create_time * 1000L));
            holder.timeText.setText(formattedTime);

            return convertView;
        }
    }

    static class ViewHolder {
        TextView titleText;
        TextView timeText;
    }
}
