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
 * Created by chenjishi on 13-12-21.
 */
public class CommentDialog extends Dialog implements View.OnClickListener {
    private OnCommentListner listener;

    public CommentDialog(Context context, OnCommentListner listener) {
        super(context, R.style.FullHeightDialog);

        this.listener = listener;

        setCanceledOnTouchOutside(true);
        View view = LayoutInflater.from(context).inflate(R.layout.comment_dialog, null);
        setContentView(view);

        view.findViewById(R.id.btn_confirm).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_review).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_confirm) {

            String text = ((EditText) findViewById(R.id.et_comment)).getText().toString().trim();

            if (TextUtils.isEmpty(text)) {
                Toast.makeText(getContext(), "请输入评论", Toast.LENGTH_SHORT).show();
                return;
            }

            listener.onConfirm(text);
        }

        if (v.getId() == R.id.btn_review) {
            listener.onCheckComment();
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

    public interface OnCommentListner {

        public void onConfirm(String comment);

        public void onCheckComment();
    }
}
