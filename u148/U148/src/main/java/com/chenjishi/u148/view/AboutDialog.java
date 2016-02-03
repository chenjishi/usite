package com.chenjishi.u148.view;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import com.chenjishi.u148.R;
import com.chenjishi.u148.util.Utils;

/**
 * Created by chenjishi on 14-1-10.
 */
public class AboutDialog extends Dialog {

    public AboutDialog(Context context) {
        super(context, R.style.FullHeightDialog);

        setCanceledOnTouchOutside(true);
        View view = LayoutInflater.from(context).inflate(R.layout.about_view, null);
        setContentView(view);

        Button versionBtn = (Button) view.findViewById(R.id.btn_version);
        String versionName = Utils.getVersionName(context);
        if (null != versionName) {
            versionBtn.setText(versionName);
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
}
