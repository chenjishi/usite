package com.chenjishi.u148.comment;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chenjishi.u148.Config;
import com.chenjishi.u148.R;
import com.chenjishi.u148.home.UserInfo;
import com.chenjishi.u148.utils.FootViewHolder;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.chenjishi.u148.utils.Constants.MODE_NIGHT;

/**
 * Created by jishichen on 2017/4/25.
 */
public class CommentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int ITEM_COMMENT = 0;
    private static final int ITEM_FOOTER = 1;

    private final List<Comment> mCommentList = new ArrayList<>();
    private final Format mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final Date mDate = new Date();

    private Context mContext;

    private View.OnClickListener mOnClickListener;

    public CommentListAdapter(Context context, View.OnClickListener listener) {
        mContext = context;
        mOnClickListener = listener;
    }

    public void addData(List<Comment> commentItems) {
        mCommentList.addAll(commentItems);
        notifyDataSetChanged();
    }

    public void clear() {
        mCommentList.clear();
        notifyDataSetChanged();
    }

    public void showFoot() {
        mCommentList.add(null);
        notifyItemInserted(getItemCount() - 1);
    }

    public void hideFoot() {
        if (mCommentList.size() <= 0) return;

        mCommentList.remove(getItemCount() - 1);
        notifyItemRemoved(getItemCount());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        boolean isFoot = viewType == ITEM_FOOTER;
        View v = LayoutInflater.from(mContext).inflate(
                isFoot ? R.layout.load_more : R.layout.comment_item,
                parent, false);
        return isFoot ? new FootViewHolder(v) : new CommentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        boolean isNight = Config.getThemeMode(mContext) == MODE_NIGHT;

        if (getItemViewType(position) == ITEM_COMMENT) {
            CommentViewHolder viewHolder = (CommentViewHolder) holder;

            viewHolder.userText.setTextColor(isNight ? 0xFFB26B00 : 0xFFFF9900);
            viewHolder.contentText.setTextColor(isNight ? 0xFF999999 : 0xFF333333);
            viewHolder.replyText.setTextColor(isNight ? 0xFF666666 : 0xFF999999);
            viewHolder.splitLine.setBackgroundColor(isNight ? 0xFF666666 : 0xFFCACACA);

            Comment cmt = mCommentList.get(position);
            UserInfo user = cmt.usr;
            mDate.setTime(cmt.create_time * 1000);

            viewHolder.imageView.setImageURI(Uri.parse(user.icon));

            String formattedString = user.nickname + " " + (isNight
                    ? "<font color='#666666'>" : "<font color='#999999'>") + mDateFormat.format(mDate) + "</font>";
            viewHolder.userText.setText(Html.fromHtml(formattedString));

            String content = cmt.contents;
            if (content.contains("blockquote")) {
                Pattern pattern = Pattern.compile("(.*?)<blockquote>(.*?)<\\/blockquote>");
                Matcher matcher = pattern.matcher(content);

                String reply = "";
                while (matcher.find()) {
                    content = matcher.group(1);
                    reply = matcher.group(2);
                }

                if (reply.length() > 0) {
                    viewHolder.replyText.setText(Html.fromHtml(reply));
                    viewHolder.replyLayout.setVisibility(View.VISIBLE);
                }
            } else {
                viewHolder.replyLayout.setVisibility(View.GONE);
            }

            viewHolder.contentText.setText(Html.fromHtml(content));
            viewHolder.itemView.setTag(cmt);
            viewHolder.itemView.setOnClickListener(mOnClickListener);
        } else {
            FootViewHolder viewHolder = (FootViewHolder) holder;
            viewHolder.mFootText.setTextColor(isNight ? 0xFF666666 : 0xFF333333);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return null == mCommentList.get(position) ? ITEM_FOOTER : ITEM_COMMENT;
    }

    @Override
    public int getItemCount() {
        return mCommentList.size();
    }
}
