package com.chenjishi.u148.view;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import com.chenjishi.u148.R;

/**
 * Created by chenjishi on 13-12-20.
 */
public class LoginDialog extends Dialog implements View.OnClickListener {
    private OnLoginListener listener;

    public LoginDialog(Context context, OnLoginListener listener) {
        super(context, R.style.FullHeightDialog);

        this.listener = listener;

        setCanceledOnTouchOutside(true);
        View view = LayoutInflater.from(context).inflate(R.layout.login, null);
        setContentView(view);

        view.findViewById(R.id.btn_confirm).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_confirm) {
            String email = ((EditText) findViewById(R.id.user_name)).getText().toString().trim();
            String password = ((EditText) findViewById(R.id.password)).getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(getContext(), "请输入登陆信息", Toast.LENGTH_SHORT).show();
                return;
            }
            listener.onConfirm(email, password);
        }

        dismiss();
    }

    @Override
    public void show() {
        WindowManager windowManager = getWindow().getWindowManager();
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.width = (int) (metrics.widthPixels * 0.8f);
        getWindow().setAttributes(layoutParams);

        super.show();
    }

    public interface OnLoginListener {

        public void onConfirm(String userName, String password);
    }
}
