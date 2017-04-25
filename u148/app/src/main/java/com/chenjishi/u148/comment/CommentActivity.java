package com.chenjishi.u148.comment;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import com.chenjishi.u148.BaseActivity;
import com.chenjishi.u148.Config;
import com.chenjishi.u148.R;
import com.chenjishi.u148.home.UserInfo;
import com.chenjishi.u148.utils.*;
import com.chenjishi.u148.widget.LoginDialog;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.text.TextUtils.isEmpty;
import static com.chenjishi.u148.utils.Constants.*;
import static com.chenjishi.u148.utils.Utils.showToast;

/**
 * Created by jishichen on 2017/4/24.
 */
public class CommentActivity extends BaseActivity implements Listener<CommentDoc>, ErrorListener, OnPageEndListener,
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
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

    private void loadData() {
        mScrollListener.setIsLoading(true);

        String url = String.format(API_COMMENTS_GET, articleId, mPage);
        NetworkRequest.getInstance().get(url, CommentDoc.class, this, this);
    }

    @Override
    public void onResponse(CommentDoc response) {
        mListAdapter.hideFoot();
        mRefreshLayout.setRefreshing(false);
        mScrollListener.setIsLoading(false);

        if (null != response && null != response.data) {
            List<Comment> list = response.data.data;
            if (null != list && list.size() > 0) {
                if (mPage == 1) mListAdapter.clear();

                mListAdapter.addData(list);
                hideLoadingView();
            } else {
                if (1 == mPage) setError(getString(R.string.no_comment));
            }
        } else {
            if (1 == mPage) setError(getString(R.string.no_comment));
        }
    }

    @Override
    public void onErrorResponse() {
        mListAdapter.hideFoot();
        mRefreshLayout.setRefreshing(false);
        mScrollListener.setIsLoading(false);
        setError();
    }

    @Override
    public void onPageEnd() {
        mListAdapter.showFoot();

        mPage += 1;
        loadData();
    }

    @Override
    public void onRefresh() {
        mPage = 1;
        mRefreshLayout.setRefreshing(true);
        loadData();
    }

    @Override
    public void onLoginSuccess() {
        String content = mEditText.getText().toString();
        if (!isEmpty(commentId)) {
            sendComment(content, commentId);
            commentId = "";
        } else {
            sendComment(content, "");
        }
    }

    @Override
    public void onLoginError() {
        showToast(this, R.string.login_fail);
    }

    public void onSendButtonClicked(View v) {
        long nowTime = System.currentTimeMillis();
        if (nowTime - lastClickTime > 2000L) {
            String content = mEditText.getText().toString();
            lastClickTime = nowTime;

            if (!isEmpty(content)) {
                if (Config.isLogin(this)) {
                    if (!isEmpty(commentId)) {
                        sendComment(content, commentId);
                        commentId = "";
                    } else {
                        sendComment(content, "");
                    }
                } else {
                    new LoginDialog(this, this).show();
                }
            } else {
                showToast(this, R.string.empty_comment_tip);
            }
        }
    }

    private void sendComment(final String content, String id) {
        if (content.equals(mContent)) return;

        UserInfo user = Config.getUser(this);
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.submit_comment));
        pd.show();

        Map<String, String> params = new HashMap<>();
        params.put("id", articleId);
        params.put("token", user.token);
        params.put("client", "android");
        params.put("content", content);
        if (!isEmpty(id)) params.put("review_id", id);

        NetworkRequest.getInstance().post(API_COMMENT_POST, params, new Listener<String>() {
            @Override
            public void onResponse(String response) {
                pd.dismiss();
                handleResult(response, content);
            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse() {
                pd.dismiss();
                showToast(CommentActivity.this, R.string.comment_fail);
            }
        });
    }

    private void handleResult(String res, String content) {
        if (isEmpty(res)) {
            showToast(this, R.string.comment_fail);
            return;
        }

        try {
            JSONObject jObj = new JSONObject(res);
            int code = jObj.optInt("code", -1);

            if (0 == code) {
                mEditText.setText("");
                mEditText.setHint("");
                mEditText.clearFocus();
                mContent = content;
                showToast(this, R.string.comment_success);
                loadData();
            } else {
                showToast(this, R.string.comment_fail);
            }
        } catch (JSONException e) {
        }
    }

    @Override
    protected void applyTheme() {
        super.applyTheme();
        boolean isNight = Config.getThemeMode(this) == MODE_NIGHT;

        mEditText.setTextColor(isNight ? 0xFF999999 : 0xFF333333);
        mEditText.setHintTextColor(isNight ? 0xFF666666 : 0xFF9C9C9C);

        Button sendBtn = (Button) findViewById(R.id.btn_send);
        sendBtn.setBackgroundResource(isNight ?
                R.drawable.btn_gray_night : R.drawable.btn_gray);
        sendBtn.setTextColor(isNight ? 0xFF666666 : 0xFF333333);
        findViewById(R.id.comment_layout).setBackgroundColor(isNight ? 0xFF1C1C1C : 0xFFF6F6F6);
        findViewById(R.id.split_h_comment).setBackgroundColor(isNight ? 0xFF303030 : 0xFFFFFFFF);
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (null == v.getTag()) return;

            Comment cmt = (Comment) v.getTag();
            mEditText.setHint(getString(R.string.reply_to, cmt.usr.nickname));
            mEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);
            commentId = cmt.id;
        }
    };
}
