package com.chenjishi.u148.activity;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.model.Feed;
import com.chenjishi.u148.util.Constants;
import com.chenjishi.u148.util.HttpUtils;
import com.chenjishi.u148.view.GifMovieView;
import com.chenjishi.u148.volley.toolbox.ImageLoader;
import com.chenjishi.u148.volley.toolbox.NetworkImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenjishi on 15/2/4.
 */
public class FeedListAdapter extends BaseAdapter {
    private List<Feed> mFeedList = new ArrayList<Feed>();
    private LayoutInflater mInflater;
    private Resources mResources;
    private SparseArray<String> mCategoryArray;
    private float mDensity;
    private final ImageLoader mImageLoader = HttpUtils.getImageLoader();

    public FeedListAdapter(Context context) {
        mResources = context.getResources();
        mInflater = LayoutInflater.from(context);
        mDensity = mResources.getDisplayMetrics().density;

        int[] ids = mResources.getIntArray(R.array.category_id);
        String[] names = mResources.getStringArray(R.array.category_name);

        int len = ids.length;
        mCategoryArray = new SparseArray<String>(len);

        for (int i = 0; i < len; i++) {
            mCategoryArray.put(ids[i], names[i]);
        }
    }

    public void clearData() {
        if (mFeedList.size() > 0) mFeedList.clear();
        notifyDataSetChanged();
    }

    public void addData(List<Feed> dataList) {
        mFeedList.addAll(dataList);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mFeedList.size();
    }

    @Override
    public Feed getItem(int position) {
        return mFeedList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        final int theme = PrefsUtil.getThemeMode();
        String color = "#FF9900";
        if (null == convertView) {
            convertView = mInflater.inflate(R.layout.feed_list_item, parent, false);
            holder = new ViewHolder();

            holder.cellLayout = (RelativeLayout) convertView.findViewById(R.id.cell_layout);
            holder.thumb = (NetworkImageView) convertView.findViewById(R.id.feed_image);
            holder.gifView = (GifMovieView) convertView.findViewById(R.id.gif_view);
            holder.title = (TextView) convertView.findViewById(R.id.feed_title);
            holder.commentText = (TextView) convertView.findViewById(R.id.tv_comment);
            holder.content = (TextView) convertView.findViewById(R.id.feed_content);

            if (Constants.MODE_NIGHT == theme) {
                color = "#B26B00";
                holder.title.setTextColor(mResources.getColor(R.color.text_color_weak));
                holder.content.setTextColor(mResources.getColor(R.color.text_color_summary));
                holder.commentText.setTextColor(mResources.getColor(R.color.text_color_summary));
            } else {
                holder.title.setTextColor(mResources.getColor(R.color.text_color_regular));
                holder.content.setTextColor(mResources.getColor(R.color.text_color_weak));
                holder.commentText.setTextColor(mResources.getColor(R.color.text_color_weak));
            }

            convertView.setTag(holder);
        }

        holder = (ViewHolder) convertView.getTag();

        /**
         * here we make the first cell's top padding larger
         */
        int paddingTop = 12;
        int paddingLeft = (int) (8 * mDensity);
        int padingBottom = (int) (12 * mDensity);
        if (0 == position) {
            paddingTop = 20;
        }

        holder.cellLayout.setPadding(paddingLeft, (int) (paddingTop * mDensity), paddingLeft, padingBottom);
        final Feed feed = getItem(position);

        String imageUrl = feed.pic_mid;
        if (!TextUtils.isEmpty(imageUrl)) {
            if (imageUrl.endsWith("gif") || imageUrl.endsWith("GIF") ||
                    imageUrl.endsWith("Gif")) {
                holder.thumb.setVisibility(View.GONE);

                holder.gifView.setImageUrl(imageUrl, (int) (mDensity * 90));
                holder.gifView.setVisibility(View.VISIBLE);
            } else {
                holder.gifView.setVisibility(View.GONE);

                holder.thumb.setImageUrl(feed.pic_mid, mImageLoader);
                holder.thumb.setDefaultImageResId(R.drawable.pictrue_bg);
                holder.thumb.setVisibility(View.VISIBLE);
            }

        } else {
            holder.gifView.setVisibility(View.GONE);

            holder.thumb.setImageResource(R.drawable.pictrue_bg);
            holder.thumb.setVisibility(View.VISIBLE);
        }

        String title = "<font color=\"" + color + "\">[" + mCategoryArray.get(feed.category) + "]</font> " + feed.title;
        holder.title.setText(Html.fromHtml(title));
        holder.commentText.setText(mResources.getString(R.string.views, feed.count_browse, feed.count_review));
        holder.content.setText(feed.summary);

        return convertView;
    }

    private static class ViewHolder {
        public RelativeLayout cellLayout;
        public NetworkImageView thumb;
        public GifMovieView gifView;
        public TextView title;
        public TextView commentText;
        public TextView content;
    }
}
