package com.chenjishi.u148.promotions;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import com.chenjishi.u148.R;
import com.chenjishi.u148.activity.BaseActivity;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.model.UserInfo;
import com.chenjishi.u148.util.HttpUtils;
import com.chenjishi.u148.util.Utils;
import com.chenjishi.u148.volley.Response;
import com.chenjishi.u148.volley.VolleyError;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenjishi on 15/9/9.
 */
public class PromotionsCommentActivity extends BaseActivity implements Response.Listener<ReplyDoc>, Response.ErrorListener {
    public static final String KEY_TOPIC_ID = "topic_id";

    private String mTopicId;
    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment);

        mTopicId = getIntent().getExtras().getString(KEY_TOPIC_ID);
        Log.i("test", "#topic id " + mTopicId);

        mEditText = (EditText) findViewById(R.id.et_content);

        requst();
    }

    private void requst() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("tid", mTopicId);
        params.put("offset", String.valueOf(0));
        params.put("limit", String.valueOf(20));

        String url = "http://app.goudaifu.com/funclub/v1/funclubreplylist";
        HttpUtils.post(url, params, ReplyDoc.class, this, this);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.i("test", "#error  " + error );

    }

    @Override
    public void onResponse(ReplyDoc response) {
        if (null != response && null != response.data) {
            List<Reply> replyList = response.data.replys;
            if (null != replyList && replyList.size() > 0) {
                Log.i("test", "#size " + replyList.size());
            }
        }

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
            params.put("name", "anonymity");
            params.put("avatar", "http://img.u148.net/uhead/selecthead/sort1/52.jpg");
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
}
