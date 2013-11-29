package com.chenjishi.u148.view;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import com.chenjishi.u148.R;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-11-25
 * Time: 下午11:11
 * To change this template use File | Settings | File Templates.
 */
public class ShareDialog extends Dialog implements View.OnClickListener {
    private OnShareListener listener;

    public ShareDialog(Context context, OnShareListener listener) {
        super(context, R.style.FullHeightDialog);
        this.listener = listener;

        setCanceledOnTouchOutside(true);
        View view = LayoutInflater.from(context).inflate(R.layout.view_share, null);
        setContentView(view);

        view.findViewById(R.id.share_friend).setOnClickListener(this);
        view.findViewById(R.id.share_session).setOnClickListener(this);
        view.findViewById(R.id.share_weibo).setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
        String tag = (String) v.getTag();
        if (null == tag) return;

        listener.onShare(Integer.parseInt(tag));
    }

    public interface OnShareListener {
        public void onShare(int type);
    }
}
