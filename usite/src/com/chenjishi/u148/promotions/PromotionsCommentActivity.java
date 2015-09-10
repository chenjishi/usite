package com.chenjishi.u148.promotions;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chenjishi.u148.R;
import com.chenjishi.u148.activity.BaseActivity;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.model.UserInfo;
import com.chenjishi.u148.util.Constants;
import com.chenjishi.u148.util.HttpUtils;
import com.chenjishi.u148.util.Utils;
import com.chenjishi.u148.volley.Response;
import com.chenjishi.u148.volley.VolleyError;
import com.chenjishi.u148.volley.toolbox.ImageLoader;
import com.chenjishi.u148.volley.toolbox.NetworkImageView;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by chenjishi on 15/9/9.
 */
public class PromotionsCommentActivity extends BaseActivity implements Response.Listener<ReplyDoc>, Response.ErrorListener,
        AbsListView.OnScrollListener, SwipeRefreshLayout.OnRefreshListener {
    public static final String KEY_TOPIC_ID = "topic_id";
    private static final int PAGE_SIZE = 20;

    private int mOffset;
    private int mLastItemIndex;
    private boolean mIsDataLoading;
    private String mTopicId;

    private SwipeRefreshLayout mRefreshLayout;
    private EditText mEditText;
    private View mFootView;

    private ReplyListAdapter mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment);
        setTitle(R.string.comment);

        mTopicId = getIntent().getExtras().getString(KEY_TOPIC_ID);

        mFootView = mInflater.inflate(R.layout.load_more, null);
        TextView footLabel = (TextView) mFootView.findViewById(R.id.loading_text);

        mEditText = (EditText) findViewById(R.id.et_content);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mRefreshLayout.setOnRefreshListener(this);

        mListAdapter = new ReplyListAdapter(this);

        ListView listView = (ListView) findViewById(R.id.list_comment);
        listView.addFooterView(mFootView, null, false);
        listView.setAdapter(mListAdapter);
        listView.setOnScrollListener(this);

        if (Constants.MODE_NIGHT == PrefsUtil.getThemeMode()) {
            listView.setDivider(getResources().getDrawable(R.drawable.split_color_night));
            footLabel.setTextColor(getResources().getColor(R.color.text_color_summary));
        } else {
            listView.setDivider(getResources().getDrawable(R.drawable.split_color));
            footLabel.setTextColor(getResources().getColor(R.color.text_color_regular));
        }
        listView.setDividerHeight(1);

        showLoadingView();
        request();
    }

    private void request() {
        mIsDataLoading = true;

        if (mOffset > 0) mFootView.setVisibility(View.VISIBLE);

        Map<String, String> params = new HashMap<String, String>();
        params.put("tid", mTopicId);
        params.put("offset", String.valueOf(mOffset));
        params.put("limit", String.valueOf(PAGE_SIZE));

        String url = "http://app.goudaifu.com/funclub/v1/funclubreplylist";
        HttpUtils.post(url, params, ReplyDoc.class, this, this);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        setError();
        mIsDataLoading = false;
        mFootView.setVisibility(View.GONE);
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onResponse(ReplyDoc response) {
        if (null != response && null != response.data) {
            List<Reply> replyList = response.data.replys;
            if (null != replyList && replyList.size() > 0) {
                mListAdapter.addData(replyList);
            }
            hideLoadingView();
        } else {
            setError();
        }

        mIsDataLoading = false;
        mFootView.setVisibility(View.GONE);
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        mRefreshLayout.setRefreshing(true);
        mOffset = 0;
        mListAdapter.clearData();
        request();
    }

    public void onSendButtonClicked(View view) {
        String content = mEditText.getText().toString();
        if (TextUtils.isEmpty(content)) {
            Utils.showToast(R.string.empty_comment_tip);
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.submit_comment));
        progressDialog.show();

        Map<String, String> params = new HashMap<String, String>();
        params.put("tid", mTopicId);
        if (Utils.isLogin()) {
            UserInfo userInfo = PrefsUtil.getUser();
            if (null != userInfo && !TextUtils.isEmpty(userInfo.nickname)) {
                params.put("name", userInfo.nickname);
                params.put("avatar", userInfo.icon);
            }
        } else {
            params.put("name", Utils.getDeviceName());

            Random rand = new Random();
            int avatarId = rand.nextInt((60 - 1) + 1) + 1;
            params.put("avatar", "http://img.u148.net/uhead/selecthead/sort1/" + avatarId + ".jpg");
        }

        params.put("content", content);

        String url = "http://app.goudaifu.com/funclub/v1/funclubreplyadd";
        HttpUtils.post(url, params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                if (!TextUtils.isEmpty(response)) {
                    try {
                        JSONObject jObj = new JSONObject(response);
                        int code = jObj.optInt("errNo", -1);
                        if (code == 0) {
                            mEditText.setText("");
                            Utils.showToast(R.string.comment_success);
                        } else {
                            Utils.showToast(jObj.optString("errstr"));
                        }
                    } catch (JSONException e) {
                        Utils.showToast(R.string.comment_fail);
                    }
                } else {
                    Utils.showToast(R.string.comment_fail);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Utils.showToast(R.string.comment_fail);
            }
        });
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE && mLastItemIndex == mListAdapter.getCount()) {
            if (!mIsDataLoading) {
                mOffset += PAGE_SIZE;
                request();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mLastItemIndex = firstVisibleItem + visibleItemCount - 1;
    }

    private static class ReplyListAdapter extends BaseAdapter {
        private final ArrayList<Reply> mReplyList = new ArrayList<Reply>();
        private LayoutInflater mInflater;
        private final ImageLoader mImageLoader = HttpUtils.getImageLoader();
        private final Format mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        private final Date mDate = new Date(System.currentTimeMillis());

        public ReplyListAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        public void addData(List<Reply> dataList) {
            mReplyList.addAll(dataList);
            notifyDataSetChanged();
        }

        public void clearData() {
            mReplyList.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mReplyList.size();
        }

        @Override
        public Reply getItem(int position) {
            return mReplyList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.comment_item, parent, false);
                holder = new ViewHolder();

                holder.iconView = (NetworkImageView) convertView.findViewById(R.id.avatar);
                holder.userText = (TextView) convertView.findViewById(R.id.user_name);
                holder.contentText = (TextView) convertView.findViewById(R.id.content);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Reply reply = getItem(position);
            mDate.setTime(reply.createTime * 1000L);

            holder.iconView.setImageUrl(reply.avatar, mImageLoader);
            holder.iconView.setDefaultImageResId(R.drawable.user_default);

            String formattedString = reply.name + " <font color='#999999'>" + mDateFormat.format(mDate) + "</font>";
            holder.userText.setText(Html.fromHtml(formattedString));
            holder.contentText.setText(reply.content);

            return convertView;
        }
    }

    private static class ViewHolder {

        public NetworkImageView iconView;

        private TextView userText;

        private TextView contentText;
    }
}
