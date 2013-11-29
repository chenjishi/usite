package com.chenjishi.u148.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chenjishi.u148.R;
import com.chenjishi.u148.activity.DetailActivity;
import com.chenjishi.u148.entity.FeedItem;
import com.chenjishi.u148.util.HttpUtils;
import com.chenjishi.u148.volley.toolbox.ImageLoader;
import com.flurry.android.FlurryAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-11-28
 * Time: 下午3:27
 * To change this template use File | Settings | File Templates.
 */
public class FeedListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
    private ArrayList<FeedItem> dataList;
    private Context context;
    private LayoutInflater inflater;

    public FeedListAdapter(Context context, ArrayList<FeedItem> dataList, ListView listView) {
        this.context = context;
        this.dataList = dataList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        listView.setOnItemClickListener(this);
        listView.setAdapter(this);
    }

    @Override
    public int getCount() {
        return null == dataList ? 0 : dataList.size();
    }

    @Override
    public FeedItem getItem(int position) {
        return null == dataList ? null : dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (null == convertView) {
            convertView = inflater.inflate(R.layout.feed_list_item, null);
            holder = new ViewHolder();

            holder.thumb = (ImageView) convertView.findViewById(R.id.feed_image);
            holder.category = (TextView) convertView.findViewById(R.id.feed_type);
            holder.title = (TextView) convertView.findViewById(R.id.feed_title);
            holder.author = (TextView) convertView.findViewById(R.id.feed_author);
            holder.time = (TextView) convertView.findViewById(R.id.feed_time);
            holder.content = (TextView) convertView.findViewById(R.id.feed_content);

            convertView.setTag(holder);
        }

        holder = (ViewHolder) convertView.getTag();

        FeedItem feed = getItem(position);

        holder.category.setText(feed.category);
        holder.title.setText(feed.title);
        holder.author.setText(feed.author);
        holder.time.setText(feed.time);
        holder.content.setText(feed.summary);

        HttpUtils.getImageLoader().get(feed.imageUrl, ImageLoader.getImageListener(holder.thumb,
                R.drawable.pictrue_bg, R.drawable.pictrue_bg));

        return convertView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FeedItem item = getItem(position - 1);
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra("title", item.title);
        intent.putExtra("link", item.link);

        Map<String, String> params = new HashMap<String, String>();
        params.put("article_title", item.title);
        params.put("article_author", item.author);
        FlurryAgent.logEvent("read_article", params);

        context.startActivity(intent);
    }

    private static class ViewHolder {
        ImageView thumb;
        TextView category;
        TextView title;
        TextView author;
        TextView time;
        TextView content;
    }
}
