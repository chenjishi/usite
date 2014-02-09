package com.chenjishi.u148.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.model.Comment;
import com.chenjishi.u148.model.User;
import com.chenjishi.u148.util.CommonUtil;
import com.chenjishi.u148.util.Constants;
import com.chenjishi.u148.util.HttpUtils;
import com.chenjishi.u148.view.LoginDialog;
import com.chenjishi.u148.volley.Response;
import com.chenjishi.u148.volley.VolleyError;
import com.chenjishi.u148.volley.toolbox.ImageLoader;
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

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-5-4
 * Time: 下午12:35
 * To change this template use File | Settings | File Templates.
 */
public class CommentActivity extends BaseActivity implements Response.Listener<ArrayList<Comment>>,
        Response.ErrorListener, View.OnClickListener, LoginDialog.OnLoginListener, AdapterView.OnItemClickListener {
    private String articleId;
    private String mContent;
    private String commentId;
    private long lastClickTime;

    private ArrayList<Comment> commentList = new ArrayList<Comment>();
    private ListView mListView;
    private CommentAdapter mAdapter;

    private View emptyView;
    private int currentPage = 1;
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
        footView.findViewById(R.id.btn_load).setOnClickListener(this);
        mEditText = (EditText) findViewById(R.id.et_content);

        mAdapter = new CommentAdapter(this);
        mListView = (ListView) findViewById(R.id.list_comment);
        mListView.addFooterView(footView);
        footView.setVisibility(View.GONE);
        mListView.setEmptyView(emptyView);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);

        loadData();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Comment comment = commentList.get(position);
        mEditText.setHint("回复:" + comment.user.nickname);
        mEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);
        commentId = comment.id;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        CommonUtil.showToast("出错啦~~");
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
        CommonUtil.showToast(getString(R.string.login_fail));
    }

    @Override
    public void onClick(View v) {
        footView.findViewById(R.id.btn_load).setVisibility(View.GONE);
        footView.findViewById(R.id.loading_layout).setVisibility(View.VISIBLE);
        currentPage++;
        loadData();
    }

    private void loadData() {
        HttpUtils.getComments(String.format("http://www.u148.net/json/get_comment/%1$s/%2$d",
                articleId, currentPage), this, this);
    }

    public void onSendButtonClicked(View v) {
        long nowTime = System.currentTimeMillis();
        if (nowTime - lastClickTime > 2000L) {
            String content = mEditText.getText().toString();
            lastClickTime = nowTime;

            if (!TextUtils.isEmpty(content)) {
                if (CommonUtil.isLogin()) {
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
                CommonUtil.showToast("评论不能为空");
            }
        }
    }

    private void sendComment(final String content, String id) {
        if (content.equals(mContent)) return;

        final String url = "http://www.u148.net/json/comment";
        final User user = PrefsUtil.getUser();
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("评论提交中...");
        pd.show();

        Map<String, String> params = new HashMap<String, String>();
        params.put("id", articleId);
        params.put("token", user.token);
        params.put("content", content + "（来自有意思吧安卓客户端）");
        if (!TextUtils.isEmpty(id)) {
            params.put("review_id", id);
        }

        Log.i("test", "########sendComment");
        HttpUtils.post(url, params, new Response.Listener<String>() {
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
                                    CommonUtil.showToast("评论成功");
                                    loadData();
                                } else {
                                    CommonUtil.showToast("评论失败，请稍后再试");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            CommonUtil.showToast("评论失败，请稍后再试");
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pd.dismiss();
                        CommonUtil.showToast("评论失败，请稍后再试");
                    }
                }
        );
    }

    @Override
    public void onResponse(ArrayList<Comment> response) {
        if (null != response && response.size() > 0) {
            if (1 == currentPage) commentList.clear();

            commentList.addAll(response);
            mAdapter.notifyDataSetChanged();

            if (response.size() >= 30) {
                footView.findViewById(R.id.loading_layout).setVisibility(View.GONE);
                footView.findViewById(R.id.btn_load).setVisibility(View.VISIBLE);
                footView.setVisibility(View.VISIBLE);
            } else {
                footView.setVisibility(View.GONE);
            }
        } else {
            emptyView.findViewById(R.id.progress_bar).setVisibility(View.GONE);
            ((TextView) emptyView.findViewById(R.id.tv_empty_tip)).setText(getString(R.string.no_comment));
            footView.setVisibility(View.GONE);
        }
    }

    private class CommentAdapter extends BaseAdapter {
        LayoutInflater inflater;

        public CommentAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return commentList.size();
        }

        @Override
        public Comment getItem(int position) {
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
                holder.avatarImage = (ImageView) convertView.findViewById(R.id.avatar);
                holder.userText = (TextView) convertView.findViewById(R.id.user_name);
                holder.contentText = (TextView) convertView.findViewById(R.id.content);
                holder.replyText = (TextView) convertView.findViewById(R.id.reply);
                holder.replyLayout = (LinearLayout) convertView.findViewById(R.id.reply_layout);
                holder.splitLine = convertView.findViewById(R.id.split_v);

                if (Constants.MODE_NIGHT == theme) {
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

            Comment comment = getItem(position);
            long t = comment.time * 1000L;
            Date date = new Date(t);
            Format format = new SimpleDateFormat("yyyy-MM-dd");

            ImageLoader imageLoader = HttpUtils.getImageLoader();
            imageLoader.get(comment.user.icon, ImageLoader.getImageListener(holder.avatarImage,
                    R.drawable.pictrue_bg, R.drawable.pictrue_bg));

            String formattedString = comment.user.nickname + " " + (Constants.MODE_NIGHT == theme
                    ? "<font color='#666666'>" : "<font color='#999999'>") + format.format(date) + "</font>";
            holder.userText.setText(Html.fromHtml(formattedString));

            String content = comment.content;

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
        private ImageView avatarImage;
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

        if (Constants.MODE_NIGHT == theme) {
            mListView.setDivider(getResources().getDrawable(R.drawable.split_color_night));
            sendBtn.setBackgroundResource(R.drawable.btn_gray_night);
            sendBtn.setTextColor(getResources().getColor(R.color.text_color_summary));
            replyView.setBackgroundColor(0xFF1C1C1C);
            split.setBackgroundColor(0xFF303030);
        } else {
            mListView.setDivider(getResources().getDrawable(R.drawable.split_color));
            sendBtn.setBackgroundResource(R.drawable.btn_gray);
            sendBtn.setTextColor(getResources().getColor(R.color.text_color_regular));
            replyView.setBackgroundColor(0xFFF6F6F6);
            split.setBackgroundColor(0xFFFFFFFF);
        }

        mListView.setDividerHeight(1);
    }
}
