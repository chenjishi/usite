package com.chenjishi.u148.view;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import com.chenjishi.u148.R;
import com.chenjishi.u148.util.CommonUtil;

/**
 * Created by chenjishi on 14-1-10.
 */
public class AboutDialog extends Dialog implements View.OnClickListener {
    private AboutDialogListener listener;
    private int count;

    public AboutDialog(Context context, AboutDialogListener listener) {
        super(context, R.style.FullHeightDialog);

        this.listener = listener;
        setCanceledOnTouchOutside(true);
        View view = LayoutInflater.from(context).inflate(R.layout.about_view, null);
        setContentView(view);

        Button versionBtn = (Button) view.findViewById(R.id.btn_version);
        String versionName = CommonUtil.getVersionName(context);
        if (null != versionName) {
            versionBtn.setText(versionName);
        }

        versionBtn.setOnClickListener(this);
        findViewById(R.id.btn_close).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_version) {
            count++;
            if (count == 5) {
                CommonUtil.showToast("再点击两次有惊喜哦~");
                return;
            }

            if (count == 7) {
                listener.onVersionClicked();
                count = 0;
                dismiss();
            }
        } else {
            dismiss();
        }
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

    public interface AboutDialogListener {
        public void onVersionClicked();
    }
}
