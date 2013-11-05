package com.chenjishi.u148.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.chenjishi.u148.R;
import com.chenjishi.u148.volley.RequestQueue;
import com.chenjishi.u148.volley.Response;
import com.chenjishi.u148.volley.VolleyError;
import com.chenjishi.u148.volley.toolbox.StringRequest;
import com.chenjishi.u148.volley.toolbox.Volley;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-23
 * Time: 下午10:59
 * To change this template use File | Settings | File Templates.
 */
public class LoginActivity extends BaseActivity implements Response.Listener<String>, Response.ErrorListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.login;
    }

    @Override
    protected void backIconClicked() {
    }

    public void onButtonClicked(View view) {
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = "http://www.u148.net/usr/ajax_login.u?usr.uname=yak262@126.com&usr.password=331500311113&usr.exp=2592000&rand=0.21749974856052995";

        StringRequest request = new StringRequest(url, this, this);

        queue.add(request);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.i("test", "error " + error);
    }

    @Override
    public void onResponse(String response) {
        Log.i("test", "## response " + response);
    }
}
