package com.chenjishi.u148.favorite;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import com.chenjishi.u148.BaseActivity;
import com.chenjishi.u148.Config;
import com.chenjishi.u148.R;
import com.chenjishi.u148.home.UserInfo;
import com.chenjishi.u148.utils.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.chenjishi.u148.utils.Constants.API_DELETE_FAVORITE;
import static com.chenjishi.u148.utils.Constants.API_FAVORITE_GET;

/**
 * Created by jishichen on 2017/4/14.
 */
public class FavoriteActivity extends BaseActivity implements Listener<FavoriteDoc>, ErrorListener, OnPageEndListener {
    private int mPage = 1;

    private FavoriteListAdapter mListAdapter;

    private OnListScrollListener mScrollListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        setTitle(R.string.favorite);

        mListAdapter = new FavoriteListAdapter(this, mOnLongClickListener);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mScrollListener = new OnListScrollListener(layoutManager);
        mScrollListener.setOnPageEndListener(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this));
        recyclerView.setAdapter(mListAdapter);
        recyclerView.addOnScrollListener(mScrollListener);

        loadData();
    }

    private void loadData() {
        mScrollListener.setIsLoading(true);

        UserInfo user = Config.getUser(this);
        NetworkRequest.getInstance().get(String.format(API_FAVORITE_GET, mPage, user.token),
                FavoriteDoc.class, this, this);
    }

    @Override
    public void onResponse(FavoriteDoc response) {
        if (null != response) {
            List<Favorite> list = response.data.data;
            if (null != list && list.size() > 0) {
                mListAdapter.addData(list);
            }
        }
        mScrollListener.setIsLoading(false);
    }

    @Override
    public void onErrorResponse() {
        mScrollListener.setIsLoading(false);
    }

    @Override
    public void onPageEnd() {
        mPage += 1;
        loadData();
    }

    private void deleteFavorite(String id) {
        String url = String.format(API_DELETE_FAVORITE, id, Config.getUser(this).token);
        NetworkRequest.getInstance().get(url, new Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (TextUtils.isEmpty(response)) {
                    Utils.showToast(FavoriteActivity.this, R.string.favorite_delete_fail);
                    return;
                }
                try {
                    JSONObject jObj = new JSONObject(response);
                    int code = jObj.optInt("code", -1);
                    Utils.showToast(FavoriteActivity.this, code == 0 ? R.string.favorite_delete_success :
                            R.string.favorite_delete_fail);
                } catch (JSONException e) {
                }
            }
        }, this);
    }

    private final View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (null == v.getTag()) return false;

            DeletePopupWindow popupWindow = new DeletePopupWindow(FavoriteActivity.this);
            final Favorite favorite = (Favorite) v.getTag();
            popupWindow.setOnDeleteListener(new DeletePopupWindow.OnDeleteListener() {
                @Override
                public void onDelete() {
                    deleteFavorite(favorite.id);
                    mListAdapter.deleteItem(favorite);
                }
            });
            popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.NO_GRAVITY, 0, 0);

            return true;
        }
    };
}
