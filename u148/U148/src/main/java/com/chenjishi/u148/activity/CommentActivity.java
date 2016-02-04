package com.chenjishi.u148.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.chenjishi.u148.view.DividerItemDecoration;
import com.chenjishi.u148.view.LoginDialog;
import com.chenjishi.u148.volley.Response.ErrorListener;
import com.chenjishi.u148.volley.Response.Listener;
import com.chenjishi.u148.volley.VolleyError;
import com.facebook.drawee.view.SimpleDraweeView;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;
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
public class CommentActivity extends SlidingActivity implements Listener<Comment>, ErrorListener, OnPageEndListener,
        LoginDialog.OnLoginListener, SwipeRefreshLayout.OnRefreshListener {
    private String articleId;
    private String mContent;
    private String commentId;
    private long lastClickTime;

    private CommentListAdapter mListAdapter;

    private int mPage = 1;

    private EditText mEditText;
    private SwipeRefreshLayout mRefreshLayout;

    private OnListScrollListener mScrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment);
        setTitle(R.string.comment);

        articleId = getIntent().getStringExtra("article_id");

        mEditText = (EditText) findViewById(R.id.et_content);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mRefreshLayout.setOnRefreshListener(this);

        mListAdapter = new CommentListAdapter(this, mOnClickListener);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mScrollListener = new OnListScrollListener(layoutManager);
        mScrollListener.setOnPageEndListener(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.comment_list_view);
        recyclerView.addItemDecoration(new DividerItemDecoration(this));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mListAdapter);
        recyclerView.addOnScrollListener(mScrollListener);

        showLoadingView();
        loadData();
    }

    @Override
    public void onRefresh() {
        mPage = 1;
        mRefreshLayout.setRefreshing(true);
        loadData();
    }

    @Override
    public void onPageEnd() {
        mPage += 1;
        loadData();
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (null == v.getTag()) return;

            CommentItem comment = (CommentItem) v.getTag();
            mEditText.setHint("回复:" + comment.usr.nickname);
            mEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);
            commentId = comment.id;
        }
    };

    @Override
    public void onErrorResponse(VolleyError error) {
        setError();
        mRefreshLayout.setRefreshing(false);
        mScrollListener.setIsLoading(false);
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
        mScrollListener.setIsLoading(true);

        final String url = String.format(API_COMMENTS_GET, articleId, mPage);
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
                Utils.showToast(R.string.empty_comment_tip);
            }
        }
    }

    private void sendComment(final String content, String id) {
        if (content.equals(mContent)) return;

        final UserInfo user = PrefsUtil.getUser();
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.submit_comment));
        pd.show();

        Map<String, String> params = new HashMap<>();
        params.put("id", articleId);
        params.put("token", user.token);
        params.put("client", "android");
        params.put("content", content);
        if (!TextUtils.isEmpty(id)) {
            params.put("review_id", id);
        }

        HttpUtils.post(API_COMMENT_POST, params, new Listener<String>() {
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
                                    Utils.showToast(R.string.comment_success);
                                    loadData();
                                } else {
                                    Utils.showToast(R.string.comment_fail);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Utils.showToast(R.string.comment_fail);
                        }
                    }
                }, new ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pd.dismiss();
                        Utils.showToast(R.string.comment_fail);
                    }
                }
        );
    }

    @Override
    public void onResponse(Comment response) {
        if (null != response && null != response.data) {
            ArrayList<CommentItem> commentList = response.data.data;
            if (null != commentList && commentList.size() > 0) {
                if (mPage == 1) mListAdapter.clear();

                mListAdapter.addData(commentList);
                hideLoadingView();
            } else {
                if (1 == mPage) setError(getString(R.string.no_comment));
            }
        } else {
            if (1 == mPage) setError(getString(R.string.no_comment));
        }
        mRefreshLayout.setRefreshing(false);
        mScrollListener.setIsLoading(false);
    }

    private static final class ItemViewHolder extends RecyclerView.ViewHolder {

        public View itemView;

        public SimpleDraweeView imageView;

        private TextView userText;

        private TextView contentText;

        private TextView replyText;

        private LinearLayout replyLayout;

        private View splitLine;

        public ItemViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            imageView = (SimpleDraweeView) itemView.findViewById(R.id.image_view);
            userText = (TextView) itemView.findViewById(R.id.user_name);
            contentText = (TextView) itemView.findViewById(R.id.content);
            replyText = (TextView) itemView.findViewById(R.id.reply);
            replyLayout = (LinearLayout) itemView.findViewById(R.id.reply_layout);
            splitLine = itemView.findViewById(R.id.split_v);
        }
    }

    private static class CommentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int ITEM_TYPE_COMMENT = 0;
        private static final int ITEM_TYPE_FOOTER = 1;

        private final ArrayList<CommentItem> mCommentList = new ArrayList<>();
        private final Format mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        private final Date mDate = new Date(System.currentTimeMillis());

        private LayoutInflater mInflater;
        private Context mContext;

        private View.OnClickListener mOnClickListener;

        public CommentListAdapter(Context context, View.OnClickListener listener) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mOnClickListener = listener;
        }

        public void addData(List<CommentItem> commentItems) {
            mCommentList.addAll(commentItems);
            notifyDataSetChanged();
        }

        public void clear() {
            mCommentList.clear();
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == ITEM_TYPE_FOOTER) {
                View view = mInflater.inflate(R.layout.load_more, parent, false);
                return new FootViewHolder(view);
            } else {
                View view = mInflater.inflate(R.layout.comment_item, parent, false);
                return new ItemViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int theme = PrefsUtil.getThemeMode();
            Resources res = mContext.getResources();


            if (getItemViewType(position) == ITEM_TYPE_COMMENT) {
                ItemViewHolder viewHolder = (ItemViewHolder) holder;

                if (Constants.MODE_NIGHT == theme) {
                    viewHolder.userText.setTextColor(res.getColor(R.color.action_bar_bg_night));
                    viewHolder.contentText.setTextColor(res.getColor(R.color.text_color_weak));
                    viewHolder.replyText.setTextColor(res.getColor(R.color.text_color_summary));
                    viewHolder.splitLine.setBackgroundColor(res.getColor(R.color.text_color_summary));
                } else {
                    viewHolder.userText.setTextColor(res.getColor(R.color.action_bar_bg));
                    viewHolder.contentText.setTextColor(res.getColor(R.color.text_color_regular));
                    viewHolder.replyText.setTextColor(res.getColor(R.color.text_color_weak));
                    viewHolder.splitLine.setBackgroundColor(0xFFCACACA);
                }

                final CommentItem comment = mCommentList.get(position);
                final UserInfo user = comment.usr;
                mDate.setTime(comment.create_time * 1000L);

                viewHolder.imageView.setImageURI(Uri.parse(user.icon));

                String formattedString = user.nickname + " " + (Constants.MODE_NIGHT == theme
                        ? "<font color='#666666'>" : "<font color='#999999'>") + mDateFormat.format(mDate) + "</font>";
                viewHolder.userText.setText(Html.fromHtml(formattedString));

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
                        viewHolder.replyText.setText(Html.fromHtml(reply));
                        viewHolder.replyLayout.setVisibility(View.VISIBLE);
                    }
                } else {
                    viewHolder.replyLayout.setVisibility(View.GONE);
                }

                viewHolder.contentText.setText(Html.fromHtml(content));
                viewHolder.itemView.setTag(comment);
                viewHolder.itemView.setOnClickListener(mOnClickListener);
            } else {
                FootViewHolder viewHolder = (FootViewHolder) holder;
                if (theme == Constants.MODE_NIGHT) {
                    viewHolder.mFootText.setTextColor(res.getColor(R.color.text_color_summary));
                } else {
                    viewHolder.mFootText.setTextColor(res.getColor(R.color.text_color_regular));
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            return position == mCommentList.size() ? ITEM_TYPE_FOOTER : ITEM_TYPE_COMMENT;
        }

        @Override
        public int getItemCount() {
            return mCommentList.size() > 0 ? mCommentList.size() + 1 : 0;
        }
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
