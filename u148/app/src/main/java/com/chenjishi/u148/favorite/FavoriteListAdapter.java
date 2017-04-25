package com.chenjishi.u148.favorite;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chenjishi.u148.Config;
import com.chenjishi.u148.R;
import com.chenjishi.u148.article.DetailsActivity;
import com.chenjishi.u148.home.Feed;
import com.chenjishi.u148.utils.Constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by jishichen on 2017/4/25.
 */
public class FavoriteListAdapter extends RecyclerView.Adapter<FavoriteViewHolder> {
    private final List<Favorite> mDataList = new ArrayList<>();

    private Context mContext;

    private final DateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private final Date mDate = new Date();

    private View.OnLongClickListener mOnLongClickListener;

    public FavoriteListAdapter(Context context, View.OnLongClickListener listener) {
        mContext = context;
        mOnLongClickListener = listener;
    }

    public void deleteItem(Favorite item) {
        mDataList.remove(item);
        notifyDataSetChanged();
    }

    public void addData(List<Favorite> dataList) {
        mDataList.addAll(dataList);
        notifyDataSetChanged();
    }

    @Override
    public FavoriteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.favorite_item, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FavoriteViewHolder holder, int position) {
        Favorite item = mDataList.get(position);
        boolean isNight = Config.getThemeMode(mContext) == Constants.MODE_NIGHT;

        holder.titleText.setTextColor(isNight ? 0xFF999999 : 0xFF333333);
        holder.timeText.setTextColor(isNight ? 0xFF666666 : 0xFF999999);
        holder.titleText.setText(item.title);
        mDate.setTime(item.create_time * 1000);
        holder.timeText.setText(mDateFormat.format(mDate));
        holder.itemView.setTag(item);
        holder.itemView.setOnClickListener(mOnClickListener);
        holder.itemView.setOnLongClickListener(mOnLongClickListener);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (null == v.getTag()) return;

            Favorite item = (Favorite) v.getTag();
            Feed feed = new Feed();
            feed.id = item.aid;
            feed.title = item.title;
            feed.create_time = item.create_time;
            feed.category = item.category;

            Intent intent = new Intent(mContext, DetailsActivity.class);
            intent.putExtra("feed", feed);
            mContext.startActivity(intent);
        }
    };
}
