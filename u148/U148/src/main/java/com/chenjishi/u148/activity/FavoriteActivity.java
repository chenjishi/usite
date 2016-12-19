package com.chenjishi.u148.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.model.Favorite;
import com.chenjishi.u148.model.FavoriteItem;
import com.chenjishi.u148.model.Feed;
import com.chenjishi.u148.model.UserInfo;
import com.chenjishi.u148.util.*;
import com.chenjishi.u148.view.DeletePopupWindow;
import com.chenjishi.u148.view.DividerItemDecoration;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.chenjishi.u148.util.Constants.API_DELETE_FAVORITE;
import static com.chenjishi.u148.util.Constants.API_FAVORITE_GET;

/**
 * Created by chenjishi on 14-2-22.
 */
public class FavoriteActivity extends SlidingActivity implements Listener<Favorite>,
        ErrorListener, OnPageEndListener {

    private int mPage = 1;

    private FavoriteListAdapter mListAdapter;

    private OnListScrollListener mScrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        setTitle(R.string.favorite);

        mListAdapter = new FavoriteListAdapter(this, mOnLongClickListener);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mScrollListener = new OnListScrollListener(layoutManager);
        mScrollListener.setOnPageEndListener(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.favorite_list_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this));
        recyclerView.setAdapter(mListAdapter);
        recyclerView.addOnScrollListener(mScrollListener);

        loadData();
    }

    void loadData() {
        mScrollListener.setIsLoading(true);

        final UserInfo user = PrefsUtil.getUser();
        NetworkRequest.getInstance().get(String.format(API_FAVORITE_GET, mPage, user.token), Favorite.class, this, this);
    }

    @Override
    public void onPageEnd() {
        mPage += 1;
        loadData();
    }

    @Override
    public void onErrorResponse() {
        mScrollListener.setIsLoading(false);
    }

    @Override
    public void onResponse(Favorite response) {
        if (null != response) {
            final ArrayList<FavoriteItem> favorites = response.data.data;
            if (null != favorites && favorites.size() > 0) {
                mListAdapter.addData(favorites);
            }
        }
        mScrollListener.setIsLoading(false);
    }

    private DeletePopupWindow mPopupWindow;

    private void deleteFavorite(String id) {
        final UserInfo user = PrefsUtil.getUser();
        String url = String.format(API_DELETE_FAVORITE, id, user.token);
        NetworkRequest.getInstance().get(url, new Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!TextUtils.isEmpty(response)) {
                    try {
                        JSONObject jObj = new JSONObject(response);
                        int code = jObj.optInt("code", -1);
                        Utils.showToast(code == 0 ? R.string.favorite_delete_success :
                                R.string.favorite_delete_fail);
                    } catch (JSONException e) {
                    }
                } else {
                    Utils.showToast(R.string.favorite_delete_fail);
                }
            }
        }, this);
    }

    private final View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (null == v.getTag()) return false;

            if (null == mPopupWindow) {
                mPopupWindow = new DeletePopupWindow(FavoriteActivity.this);
            }

            final FavoriteItem favorite = (FavoriteItem) v.getTag();
            mPopupWindow.setOnDeleteListener(new DeletePopupWindow.OnDeleteListener() {
                @Override
                public void onDelete() {
                    deleteFavorite(favorite.id);
                    mListAdapter.deleteItem(favorite);
                }
            });
            mPopupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.NO_GRAVITY, 0, 0);

            return true;
        }
    };

    private static class FavoriteListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final List<FavoriteItem> mDataList = new ArrayList<>();

        private Context mContext;

        private LayoutInflater mInflater;

        private final DateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        private final Date mDate = new Date();

        private View.OnLongClickListener mOnLongClickListener;

        public FavoriteListAdapter(Context context, View.OnLongClickListener listener) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mOnLongClickListener = listener;
        }

        public void deleteItem(FavoriteItem item) {
            mDataList.remove(item);
            notifyDataSetChanged();
        }

        public void addData(List<FavoriteItem> dataList) {
            mDataList.addAll(dataList);
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.favorite_list_item, parent, false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            FavoriteItem item = mDataList.get(position);
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

            final Resources res = mContext.getResources();
            if (PrefsUtil.getThemeMode() == Constants.MODE_NIGHT) {
                itemViewHolder.titleText.setTextColor(res.getColor(R.color.text_color_weak));
                itemViewHolder.timeText.setTextColor(res.getColor(R.color.text_color_summary));
            } else {
                itemViewHolder.titleText.setTextColor(res.getColor(R.color.text_color_regular));
                itemViewHolder.timeText.setTextColor(res.getColor(R.color.text_color_weak));
            }

            itemViewHolder.titleText.setText(item.title);
            mDate.setTime(item.create_time * 1000);
            itemViewHolder.timeText.setText(mDateFormat.format(mDate));
            itemViewHolder.itemLayout.setTag(item);
            itemViewHolder.itemLayout.setOnClickListener(mOnClickListener);
            itemViewHolder.itemLayout.setOnLongClickListener(mOnLongClickListener);
        }

        @Override
        public int getItemCount() {
            return mDataList.size();
        }

        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null == v.getTag()) return;

                FavoriteItem item = (FavoriteItem) v.getTag();
                Feed feed = new Feed();
                feed.id = item.aid;
                feed.title = item.title;
                feed.create_time = item.create_time;
                feed.category = item.category;

                Intent intent = new Intent(mContext, DetailsActivity.class);
                intent.putExtra("feed", feed);
                IntentUtils.getInstance().startActivity(mContext, intent);
            }
        };
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout itemLayout;

        public TextView titleText;

        public TextView timeText;

        public ItemViewHolder(View itemView) {
            super(itemView);
            itemLayout = (RelativeLayout) itemView.findViewById(R.id.item_layout);
            titleText = (TextView) itemView.findViewById(R.id.tv_title);
            timeText = (TextView) itemView.findViewById(R.id.tv_time);
        }
    }
}
