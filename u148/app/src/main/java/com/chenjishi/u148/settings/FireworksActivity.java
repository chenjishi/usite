package com.chenjishi.u148.settings;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.chenjishi.u148.BaseActivity;
import com.chenjishi.u148.Config;
import com.chenjishi.u148.R;
import com.chenjishi.u148.utils.ErrorListener;
import com.chenjishi.u148.utils.Listener;
import com.chenjishi.u148.utils.NetworkRequest;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jishichen on 2017/4/25.
 */
public class FireworksActivity extends Activity implements Listener<String>, ErrorListener {
    private MediaPlayer mPlayer;

    private FireworksView mFireworksView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fireworks);
        mFireworksView = (FireworksView) findViewById(R.id.fireworks_view);

        mPlayer = MediaPlayer.create(this, R.raw.fireworks);
        mPlayer.setLooping(true);
        mPlayer.start();

        NetworkRequest.getInstance().get("http://u148.oss-cn-beijing.aliyuncs.com/greetings", this, this);
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
    public void onResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONObject jObj = new JSONObject(response);
                String title = jObj.optString("title", "");
                String desc = jObj.optString("desc", "");

                if (!TextUtils.isEmpty(title)) {
                    Config.saveSurpriseTitle(this, title);
                    Config.saveSurpriseDesc(this, desc);

                    mFireworksView.setTitle(title, desc);
                }

            } catch (JSONException e) {
            }
        }
    }

    @Override
    public void onErrorResponse() {

    }
}
