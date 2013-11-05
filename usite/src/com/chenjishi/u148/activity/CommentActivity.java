package com.chenjishi.u148.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.chenjishi.u148.R;
import com.chenjishi.u148.entity.Comment;
import com.chenjishi.u148.volley.RequestQueue;
import com.chenjishi.u148.volley.VolleyError;
import com.chenjishi.u148.volley.toolbox.BitmapLruCache;
import com.chenjishi.u148.volley.toolbox.ImageLoader;
import com.chenjishi.u148.volley.toolbox.Volley;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-5-4
 * Time: 下午12:35
 * To change this template use File | Settings | File Templates.
 */
public class CommentActivity extends BaseActivity {
    private ImageLoader mImageLoader;
    private ArrayList<Comment> mDataList = new ArrayList<Comment>();
    private CommentAdapter mAdapter;

    private View mEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mEmptyView = LayoutInflater.from(this).inflate(R.layout.empty_view, null);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        mImageLoader = new ImageLoader(requestQueue, new BitmapLruCache(this));

        mAdapter = new CommentAdapter(this);
        ListView listView = (ListView) findViewById(R.id.list_comment);
        ((ViewGroup) listView.getParent()).addView(mEmptyView);
        listView.setEmptyView(mEmptyView);
        listView.setAdapter(mAdapter);

        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            mDataList = bundle.getParcelableArrayList("comments");
            if (mDataList.size() > 0) {
                mAdapter.notifyDataSetChanged();
            } else {
                mEmptyView.findViewById(R.id.progress_bar).setVisibility(View.GONE);
                ((TextView) mEmptyView.findViewById(R.id.tv_empty_tip)).setText("无评论");
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.comment;
    }

    @Override
    protected void backIconClicked() {
        finish();
    }

    private class CommentAdapter extends BaseAdapter {
        LayoutInflater inflater;

        public CommentAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (null == convertView) {
                convertView = inflater.inflate(R.layout.comment_item, null);

                holder = new ViewHolder();
                holder.iconAvatar = (ImageView) convertView.findViewById(R.id.icon_avatar);
                holder.tagUser = (TextView) convertView.findViewById(R.id.tag_user);
                holder.tagContent = (TextView) convertView.findViewById(R.id.tag_content);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Comment comment = mDataList.get(position);
            int idx = comment.time.indexOf('┆');
            String dateString = "";
            if (idx != -1) {
                dateString = comment.time.substring(0, idx);
            }

            final ImageView avatarImage = holder.iconAvatar;
            mImageLoader.get(comment.avatar, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    avatarImage.setImageBitmap(response.getBitmap());
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                }
            });
            String formattedString = comment.userName + " " + "<font color='#999999'>" + dateString + "</font>";
            holder.tagUser.setText(Html.fromHtml(formattedString));
            holder.tagContent.setText(comment.content);

            return convertView;
        }

        class ViewHolder {
            ImageView iconAvatar;
            TextView tagUser;
            TextView tagContent;
        }
    }
}
