package com.chenjishi.u148.comment;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chenjishi.u148.R;
import com.facebook.drawee.view.SimpleDraweeView;

/**
 * Created by jishichen on 2017/4/25.
 */
public class CommentViewHolder extends RecyclerView.ViewHolder {

    public SimpleDraweeView imageView;

    public TextView userText;

    public TextView contentText;

    public TextView replyText;

    public LinearLayout replyLayout;

    public View splitLine;

    public CommentViewHolder(View itemView) {
        super(itemView);
        imageView = (SimpleDraweeView) itemView.findViewById(R.id.image_view);
        userText = (TextView) itemView.findViewById(R.id.user_name);
        contentText = (TextView) itemView.findViewById(R.id.content);
        replyText = (TextView) itemView.findViewById(R.id.reply);
        replyLayout = (LinearLayout) itemView.findViewById(R.id.reply_layout);
        splitLine = itemView.findViewById(R.id.split_v);
    }
}
