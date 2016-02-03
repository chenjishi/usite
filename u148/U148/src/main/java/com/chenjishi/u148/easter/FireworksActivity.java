package com.chenjishi.u148.easter;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.PrefsUtil;
import com.chenjishi.u148.util.HttpUtils;
import com.chenjishi.u148.view.FireworksView;
import com.chenjishi.u148.volley.Response;
import com.chenjishi.u148.volley.VolleyError;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chenjishi on 15/1/27.
 */
public class FireworksActivity extends Activity implements Response.Listener<String>, Response.ErrorListener {
    private MediaPlayer mPlayer;

    private FireworksView mFireworksView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fireworks);

        mFireworksView = (FireworksView) findViewById(R.id.fireworks_view);

        mPlayer = MediaPlayer.create(this, R.raw.fireworks);
        mPlayer.setLooping(true);
        mPlayer.start();

        HttpUtils.get("http://u148.oss-cn-beijing.aliyuncs.com/greetings", this, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != mPlayer) {
            mPlayer.release();
            mPlayer = null;
        }

        if (null != mFireworksView) {
            mFireworksView.stopPlay();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONObject jObj = new JSONObject(response);
                String title = jObj.optString("title", "");
                String desc = jObj.optString("desc", "");

                if (!TextUtils.isEmpty(title)) {
                    PrefsUtil.saveSurpriseTitle(title);
                    PrefsUtil.saveSurpriseDesc(desc);

                    mFireworksView.setTitle(title, desc);
                }

            } catch (JSONException e) {
            }
        }
    }
}
