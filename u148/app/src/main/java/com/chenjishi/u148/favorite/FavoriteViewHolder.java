package com.chenjishi.u148.favorite;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import com.chenjishi.u148.R;

/**
 * Created by jishichen on 2017/4/25.
 */
public class FavoriteViewHolder extends RecyclerView.ViewHolder {

    public TextView titleText;

    public TextView timeText;

    public FavoriteViewHolder(View itemView) {
        super(itemView);
        titleText = (TextView) itemView.findViewById(R.id.tv_title);
        timeText = (TextView) itemView.findViewById(R.id.tv_time);
    }
}
