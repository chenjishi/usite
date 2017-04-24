package com.chenjishi.u148.utils;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by chenjishi on 16/2/3.
 */
public class OnListScrollListener extends RecyclerView.OnScrollListener {

    private boolean isLoading;

    private LinearLayoutManager layoutManager;

    private OnPageEndListener listener;

    public OnListScrollListener(LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    public void setIsLoading(boolean b) {
        isLoading = b;
    }

    public void setOnPageEndListener(OnPageEndListener listener) {
        this.listener = listener;
    }

    public boolean getIsLoading() {
        return isLoading;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (dy <= 0 || isLoading) return;

        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

        if (visibleItemCount + firstVisibleItem >= totalItemCount) {
            if (null != listener) listener.onPageEnd();
        }
    }
}
