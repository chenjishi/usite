package com.chenjishi.u148.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.model.Comment;
import com.chenjishi.u148.model.CommentItem;
import com.chenjishi.u148.model.UserInfo;
import com.chenjishi.u148.util.Constants;
import com.chenjishi.u148.util.HttpUtils;
import com.chenjishi.u148.util.Utils;
import com.chenjishi.u148.view.LoginDialog;
import com.chenjishi.u148.volley.Response;
import com.chenjishi.u148.volley.VolleyError;
import com.chenjishi.u148.volley.toolbox.ImageLoader;
import com.chenjishi.u148.volley.toolbox.NetworkImageView;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.chenjishi.u148.util.Constants.API_COMMENTS_GET;
import static com.chenjishi.u148.util.Constants.API_COMMENT_POST;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-5-4
 * Time: 下午12:35
 * To change this template use File | Settings | File Templates.
 */
public class CommentActivity extends SlidingActivity implements Response.Listener<Comment>,
        Response.ErrorListener, LoginDialog.OnLoginListener, AdapterView.OnItemClickListener, AbsListView.OnScrollListener {
    private String articleId;
    private String mContent;
    private String commentId;
    private long lastClickTime;

    private ArrayList<CommentItem> commentList = new ArrayList<CommentItem>();
    private CommentAdapter mAdapter;

    private View emptyView;
    private int currentPage = 1;
    private int mLastItemIndex;
    private boolean mIsDataLoading;
    private View footView;

    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment);
        setTitle(R.string.comment);

        articleId = getIntent().getStringExtra("article_id");

        emptyView = findViewById(R.id.empty_view);
        footView = LayoutInflater.from(this).inflate(R.layout.load_more, null);
        TextView footLabel = (TextView) footView.findViewById(R.id.loading_text);

        mEditText = (EditText) findViewById(R.id.et_content);

        mAdapter = new CommentAdapter(this);
        ListView listView = (ListView) findViewById(R.id.list_comment);
        listView.addFooterView(footView, null, false);
        footView.setVisibility(View.GONE);
        listView.setEmptyView(emptyView);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(this);

        if (Constants.MODE_NIGHT == PrefsUtil.getThemeMode()) {
            listView.setDivider(getResources().getDrawable(R.drawable.split_color_night));
            footLabel.setTextColor(getResources().getColor(R.color.text_color_summary));
        } else {
            listView.setDivider(getResources().getDrawable(R.drawable.split_color));
            footLabel.setTextColor(getResources().getColor(R.color.text_color_regular));
        }
        listView.setDividerHeight(1);

        loadData();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE && mLastItemIndex == mAdapter.getCount()) {
            if (!mIsDataLoading) {
                currentPage++;
                loadData();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mLastItemIndex = firstVisibleItem + visibleItemCount - 1;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final CommentItem comment = commentList.get(position);
        mEditText.setHint("回复:" + comment.usr.nickname);
        mEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);
        commentId = comment.id;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Utils.setErrorView(emptyView, R.string.net_error);
        mIsDataLoading = false;
        footView.setVisibility(View.GONE);
    }

    @Override
    public void onLoginSuccess() {
        String content = mEditText.getText().toString();
        if (!TextUtils.isEmpty(commentId)) {
            sendComment(content, commentId);
            commentId = "";
        } else {
            sendComment(content, "");
        }
    }

    @Override
    public void onLoginError() {
        Utils.showToast(getString(R.string.login_fail));
    }

    private void loadData() {
        mIsDataLoading = true;

        if (currentPage > 1) footView.setVisibility(View.VISIBLE);

        final String url = String.format(API_COMMENTS_GET, articleId, currentPage);
        HttpUtils.get(url, Comment.class, this, this);
    }

    public void onSendButtonClicked(View v) {
        long nowTime = System.currentTimeMillis();
        if (nowTime - lastClickTime > 2000L) {
            String content = mEditText.getText().toString();
            lastClickTime = nowTime;

            if (!TextUtils.isEmpty(content)) {
                if (Utils.isLogin()) {
                    if (!TextUtils.isEmpty(commentId)) {
                        sendComment(content, commentId);
                        commentId = "";
                    } else {
                        sendComment(content, "");
                    }
                } else {
                    new LoginDialog(this, this).show();
                }
            } else {
                Utils.showToast("评论不能为空");
            }
        }
    }

    private void sendComment(final String content, String id) {
        if (content.equals(mContent)) return;

        final UserInfo user = PrefsUtil.getUser();
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("评论提交中...");
        pd.show();

        Map<String, String> params = new HashMap<String, String>();
        params.put("id", articleId);
        params.put("token", user.token);
        params.put("client", "android");
        params.put("content", content);
        if (!TextUtils.isEmpty(id)) {
            params.put("review_id", id);
        }

        HttpUtils.post(API_COMMENT_POST, params, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pd.dismiss();
                        if (!TextUtils.isEmpty(response)) {
                            try {
                                JSONObject jObj = new JSONObject(response);
                                int code = jObj.optInt("code", -1);

                                if (0 == code) {
                                    mEditText.setText("");
                                    mEditText.setHint("");
                                    mEditText.clearFocus();
                                    mContent = content;
                                    Utils.showToast("评论成功");
                                    loadData();
                                } else {
                                    Utils.showToast("评论失败，请稍后再试");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Utils.showToast("评论失败，请稍后再试");
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pd.dismiss();
                        Utils.showToast("评论失败，请稍后再试");
                    }
                }
        );
    }

    @Override
    public void onResponse(Comment response) {
        if (null != response && response.code == 0) {
            final int count = response.data.data.size();

            if (count > 0) {
                if (1 == currentPage) commentList.clear();

                commentList.addAll(response.data.data);
                mAdapter.notifyDataSetChanged();
            } else {
                Utils.setErrorView(emptyView, R.string.no_comment);
            }
        } else {
            Utils.setErrorView(emptyView, R.string.no_comment);
        }

        mIsDataLoading = false;
        footView.setVisibility(View.GONE);
    }

    private class CommentAdapter extends BaseAdapter {
        private final ImageLoader mImageLoader = HttpUtils.getImageLoader();
        private LayoutInflater inflater;
        Format format;
        Date date;

        public CommentAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            format = new SimpleDateFormat("yyyy-MM-dd");
            date = new Date(System.currentTimeMillis());
        }

        @Override
        public int getCount() {
            return commentList.size();
        }

        @Override
        public CommentItem getItem(int position) {
            return commentList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (null == convertView) {
                convertView = inflater.inflate(R.layout.comment_item, parent, false);
                holder = new ViewHolder();

                holder.iconView = (NetworkImageView) convertView.findViewById(R.id.avatar);
                holder.userText = (TextView) convertView.findViewById(R.id.user_name);
                holder.contentText = (TextView) convertView.findViewById(R.id.content);
                holder.replyText = (TextView) convertView.findViewById(R.id.reply);
                holder.replyLayout = (LinearLayout) convertView.findViewById(R.id.reply_layout);
                holder.splitLine = convertView.findViewById(R.id.split_v);

                if (Constants.MODE_NIGHT == mTheme) {
                    holder.userText.setTextColor(getResources().getColor(R.color.action_bar_bg_night));
                    holder.contentText.setTextColor(getResources().getColor(R.color.text_color_weak));
                    holder.replyText.setTextColor(getResources().getColor(R.color.text_color_summary));
                    holder.splitLine.setBackgroundColor(getResources().getColor(R.color.text_color_summary));
                } else {
                    holder.userText.setTextColor(getResources().getColor(R.color.action_bar_bg));
                    holder.contentText.setTextColor(getResources().getColor(R.color.text_color_regular));
                    holder.replyText.setTextColor(getResources().getColor(R.color.text_color_weak));
                    holder.splitLine.setBackgroundColor(0xFFCACACA);
                }

                convertView.setTag(holder);
            }

            holder = (ViewHolder) convertView.getTag();

            final CommentItem comment = getItem(position);
            final long t = comment.create_time * 1000L;
            final UserInfo user = comment.usr;
            date.setTime(t);

            holder.iconView.setImageUrl(user.icon, mImageLoader);
            holder.iconView.setDefaultImageResId(R.drawable.user_default);

            String formattedString = user.nickname + " " + (Constants.MODE_NIGHT == mTheme
                    ? "<font color='#666666'>" : "<font color='#999999'>") + format.format(date) + "</font>";
            holder.userText.setText(Html.fromHtml(formattedString));

            String content = comment.contents;

            if (content.contains("blockquote")) {
                final Pattern pattern = Pattern.compile("(.*?)<blockquote>(.*?)<\\/blockquote>");
                Matcher matcher = pattern.matcher(content);

                String reply = "";
                while (matcher.find()) {
                    content = matcher.group(1);
                    reply = matcher.group(2);
                }

                if (reply.length() > 0) {
                    holder.replyText.setText(Html.fromHtml(reply));
                    holder.replyLayout.setVisibility(View.VISIBLE);
                }
            } else {
                holder.replyLayout.setVisibility(View.GONE);
            }

            holder.contentText.setText(Html.fromHtml(content));

            return convertView;
        }
    }

    private static class ViewHolder {
        public NetworkImageView iconView;
        private TextView userText;
        private TextView contentText;
        private TextView replyText;
        private LinearLayout replyLayout;
        private View splitLine;
    }

    @Override
    protected void applyTheme() {
        super.applyTheme();

        final Button sendBtn = (Button) findViewById(R.id.btn_send);
        final RelativeLayout replyView = (RelativeLayout) findViewById(R.id.comment_layout);
        final View split = findViewById(R.id.split_h_comment);

        if (Constants.MODE_NIGHT == mTheme) {
            sendBtn.setBackgroundResource(R.drawable.btn_gray_night);
            sendBtn.setTextColor(getResources().getColor(R.color.text_color_summary));
            replyView.setBackgroundColor(0xFF1C1C1C);
            split.setBackgroundColor(0xFF303030);
            mEditText.setTextColor(getResources().getColor(R.color.text_color_weak));
            mEditText.setHintTextColor(0xFF666666);
        } else {
            sendBtn.setBackgroundResource(R.drawable.btn_gray);
            sendBtn.setTextColor(getResources().getColor(R.color.text_color_regular));
            replyView.setBackgroundColor(0xFFF6F6F6);
            split.setBackgroundColor(0xFFFFFFFF);
            mEditText.setTextColor(getResources().getColor(R.color.text_color_regular));
            mEditText.setHintTextColor(0xFF9C9C9C);
        }
    }
}
