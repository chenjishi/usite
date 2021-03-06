package com.chenjishi.u148.widget;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import com.chenjishi.u148.Config;
import com.chenjishi.u148.R;
import com.chenjishi.u148.home.UserInfo;
import com.chenjishi.u148.utils.ErrorListener;
import com.chenjishi.u148.utils.Listener;
import com.chenjishi.u148.utils.NetworkRequest;
import com.chenjishi.u148.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.chenjishi.u148.utils.Constants.API_LOGIN;


/**
 * Created by chenjishi on 13-12-20.
 */
public class LoginDialog extends Dialog implements View.OnClickListener, Listener<String>, ErrorListener {
    private OnLoginListener listener;
    private Context context;

    public LoginDialog(Context context, OnLoginListener listener) {
        super(context, R.style.FullHeightDialog);
        this.context = context;
        this.listener = listener;

        View view = LayoutInflater.from(context).inflate(R.layout.login, null);
        setContentView(view);

        view.findViewById(R.id.btn_confirm).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (R.id.btn_confirm == v.getId()) {
            final String str1 = ((EditText) findViewById(R.id.user_name)).getText().toString().trim();
            final String str2 = ((EditText) findViewById(R.id.password)).getText().toString().trim();

            if (TextUtils.isEmpty(str1) || TextUtils.isEmpty(str2)) {
                Utils.showToast(context, context.getString(R.string.login_input_empty));
                return;
            }

            Map<String, String> params = new HashMap<String, String>();
            params.put("email", str1);
            params.put("password", str2);

            NetworkRequest.getInstance().post(API_LOGIN, params, this, this);
        } else {
            dismiss();
        }
    }

    @Override
    public void onErrorResponse() {
        listener.onLoginError();
        dismiss();
    }

    @Override
    public void onResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONObject jObj = new JSONObject(response);

                int code = jObj.optInt("code", -1);
                if (0 == code) {
                    JSONObject dataObj = jObj.getJSONObject("data");
                    UserInfo user = new UserInfo();

                    user.nickname = dataObj.optString("nickname", "");
                    user.sexStr = dataObj.optString("sex", "");
                    user.icon = dataObj.optString("icon", "");
                    user.token = dataObj.optString("token", "");

                    Config.setUser(context, user);
                    listener.onLoginSuccess();
                } else {
                    listener.onLoginError();
                }
            } catch (JSONException e) {
                listener.onLoginError();
            }
        } else {
            listener.onLoginError();
        }

        dismiss();
    }

    @Override
    public void show() {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.8f);
        getWindow().setAttributes(layoutParams);

        super.show();
    }

    public interface OnLoginListener {

        void onLoginSuccess();

        void onLoginError();
    }
}
