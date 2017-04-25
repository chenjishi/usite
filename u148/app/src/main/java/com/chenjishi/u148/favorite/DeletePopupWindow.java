package com.chenjishi.u148.favorite;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import com.chenjishi.u148.R;

/**
 * Created by chenjishi on 14-3-3.
 */
public class DeletePopupWindow extends PopupWindow implements View.OnTouchListener, View.OnClickListener {
    private OnDeleteListener listener;

    public DeletePopupWindow(Context context) {
        super(context);
        setAnimationStyle(R.style.AnimBottom);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(0xB0000000));

        View view = LayoutInflater.from(context).inflate(R.layout.delete_popwindow, null);
        view.findViewById(R.id.btn_delete).setOnClickListener(this);
        view.setOnTouchListener(this);

        setContentView(view);
    }

    public void setOnDeleteListener(OnDeleteListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int h = getContentView().findViewById(R.id.btn_delete).getTop();
        int y = (int) event.getY();
        if (event.getAction() == MotionEvent.ACTION_UP && y < h) {
            dismiss();
        }

        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_delete && null != listener) {
            listener.onDelete();
        }

        dismiss();
    }

    public interface OnDeleteListener {

        void onDelete();
    }
}
