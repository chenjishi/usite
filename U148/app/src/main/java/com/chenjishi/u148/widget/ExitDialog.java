package com.chenjishi.u148.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import com.chenjishi.u148.R;

/**
 * Created by chenjishi on 14-1-22.
 */
public class ExitDialog extends Dialog implements View.OnClickListener {
    private OnLogoutListener listener;

    public ExitDialog(Context context, OnLogoutListener listener) {
        super(context, R.style.FullHeightDialog);

        this.listener = listener;
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_exit, null);
        setContentView(view);

        view.findViewById(R.id.btn_confirm).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_confirm) {
            listener.logout();
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

    public interface OnLogoutListener {
        void logout();
    }
}

