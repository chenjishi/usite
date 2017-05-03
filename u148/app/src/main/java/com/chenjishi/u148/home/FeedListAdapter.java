package com.chenjishi.u148.home;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.chenjishi.u148.Config;
import com.chenjishi.u148.R;
import com.chenjishi.u148.article.DetailsActivity;
import com.chenjishi.u148.utils.Constants;
import com.chenjishi.u148.utils.FootViewHolder;
import com.chenjishi.u148.utils.Utils;
import com.chenjishi.u148.widget.GifMovieView;
import com.flurry.android.FlurryAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenjishi on 16/2/2.
 */
public class FeedListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_TYPE_FEED = 0;

    private static final int ITEM_TYPE_FOOTER = 1;

    private final ArrayList<Feed> mDataList = new ArrayList<>();

    private LayoutInflater mInflater;

    private Context mContext;

    private SparseArray<String> mCategoryArray;

    public FeedListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);

        int[] ids = mContext.getResources().getIntArray(R.array.category_id);
        String[] names = mContext.getResources().getStringArray(R.array.category_name);

        int len = ids.length;
        mCategoryArray = new SparseArray<>(len);

        for (int i = 0; i < len; i++) {
            mCategoryArray.put(ids[i], names[i]);
        }
    }

    public void addData(List<Feed> dataList) {
        mDataList.addAll(dataList);
        notifyDataSetChanged();
    }

    public void clear() {
        mDataList.clear();
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (i == ITEM_TYPE_FOOTER) {
            View view = mInflater.inflate(R.layout.load_more, viewGroup, false);
            return new FootViewHolder(view);
        } else {
            View view = mInflater.inflate(R.layout.feed_list_item, viewGroup, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        final int theme = Config.getThemeMode(mContext);
        final Resources res = mContext.getResources();

        if (getItemViewType(i) == ITEM_TYPE_FEED) {
            Feed feed = mDataList.get(i);
            ItemViewHolder holder = (ItemViewHolder) viewHolder;

            String color = "#FF9900";
            if (Constants.MODE_NIGHT == theme) {
                color = "#B26B00";

                holder.titleText.setTextColor(res.getColor(R.color.text_color_weak));
                holder.descText.setTextColor(res.getColor(R.color.text_color_summary));
                holder.numText.setTextColor(res.getColor(R.color.text_color_summary));
            } else {
                holder.titleText.setTextColor(res.getColor(R.color.text_color_regular));
                holder.descText.setTextColor(res.getColor(R.color.text_color_weak));
                holder.numText.setTextColor(res.getColor(R.color.text_color_weak));
            }

            String url = feed.pic_mid;
            if (!TextUtils.isEmpty(url)) {
                if (url.endsWith("gif") || url.endsWith("GIF") ||
                        url.endsWith("Gif")) {
                    holder.imageView.setVisibility(View.GONE);
                    holder.gifView.setImageUrl(url, Utils.dp2px(mContext, 90));
                    holder.gifView.setVisibility(View.VISIBLE);
                } else {
                    holder.gifView.setVisibility(View.GONE);
                    Glide.with(mContext).load(url).into(holder.imageView);
                    holder.imageView.setVisibility(View.VISIBLE);
                }
            }

            String type = mCategoryArray.get(feed.category);
            if (feed.category == -1) type = feed.uid;

            String title = "<font color='" + color + "'>[" + type + "]</font> " + feed.title;
            holder.titleText.setText(Html.fromHtml(title));
            holder.numText.setText(res.getString(R.string.views, feed.count_browse, feed.count_review));
            holder.descText.setText(feed.summary);
            holder.itemView.setTag(i);
            holder.itemView.setOnClickListener(mOnClickListener);
        } else {
            FootViewHolder holder = (FootViewHolder) viewHolder;
            if (theme == Constants.MODE_NIGHT) {
                holder.mFootText.setTextColor(res.getColor(R.color.text_color_summary));
            } else {
                holder.mFootText.setTextColor(res.getColor(R.color.text_color_regular));
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == mDataList.size() ? ITEM_TYPE_FOOTER : ITEM_TYPE_FEED;
    }

    @Override
    public int getItemCount() {
        return mDataList.size() > 0 ? mDataList.size() + 1 : 0;
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (null == v.getTag()) return;

            int index = (Integer) v.getTag();

            Feed feed = mDataList.get(index);
            Intent intent = new Intent(mContext, DetailsActivity.class);
            intent.putExtra(Constants.KEY_FEED, feed);

            Map<String, String> params = new HashMap<>();
            params.put("author", feed.usr.nickname);
            params.put("title", feed.title);
            FlurryAgent.logEvent("read_article", params);

            mContext.startActivity(intent);
        }
    };

    private static final class ItemViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;

        public GifMovieView gifView;

        public TextView titleText;

        public TextView numText;

        public TextView descText;

        public ItemViewHolder(View itemView) {
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.image_view);
            gifView = (GifMovieView) itemView.findViewById(R.id.gif_view);
            titleText = (TextView) itemView.findViewById(R.id.feed_title);
            numText = (TextView) itemView.findViewById(R.id.tv_comment);
            descText = (TextView) itemView.findViewById(R.id.feed_content);
        }
    }
}
