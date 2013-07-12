package com.chenjishi.u148.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import com.chenjishi.u148.R;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-18
 * Time: 上午9:46
 * To change this template use File | Settings | File Templates.
 */
public class ExitDialog extends CustomDialog implements View.OnClickListener {
    private IAppExitCallback callback;

    public ExitDialog(Context context, int style) {
        super(context, style);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_exit, null);
        view.findViewById(R.id.text_confirm).setOnClickListener(this);
        view.findViewById(R.id.text_cancel).setOnClickListener(this);

        setContentView(view);
    }

    public void setCallback(IAppExitCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.text_confirm:
                callback.onAppExitCallback();
                break;
            case R.id.text_cancel:
                break;
        }
        dismiss();
    }

    public interface IAppExitCallback {
        public void onAppExitCallback();
    }
}
