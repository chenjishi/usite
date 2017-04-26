package com.chenjishi.u148.settings;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.chenjishi.u148.R;

/**
 * Created by chenjishi on 15/1/26.
 */
public class PhotoDialog extends Dialog implements View.OnClickListener {
    private ImageView mImageView;
    private TextView mTextView;

    private OnDialogDismissCallback mCallback;

    public PhotoDialog(Context context, OnDialogDismissCallback callback) {
        super(context, R.style.FullHeightDialog);
        setCanceledOnTouchOutside(false);
        setCancelable(false);

        mCallback = callback;

        View view = LayoutInflater.from(context).inflate(R.layout.photo_dialog, null);
        setContentView(view);

        mImageView = (ImageView) view.findViewById(R.id.photo_view);
        mImageView.setOnClickListener(this);

        mTextView = (TextView) view.findViewById(R.id.photo_text);
        view.findViewById(R.id.btn_exit).setOnClickListener(this);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float density = metrics.density;
        int imageWidth = (int) (metrics.widthPixels * .5f - 2 * 12 * density);
        int imageHeight = (int) (400.f * imageWidth / 600);

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mImageView.getLayoutParams();
        lp.width = imageWidth;
        lp.height = imageHeight;
        mImageView.setLayoutParams(lp);
    }

    private int mIndex;

    public void setPhotoItem(PhotoItem item, int index) {
        if (null != item) {
            Glide.with(getContext()).load(item.image).into(mImageView);
            mTextView.setText(item.title);
        } else {
            mImageView.setImageResource(R.drawable.dinosaur);
            mTextView.setText("Written By Jishi Chen, 2015/07/20");
        }
        mIndex = index;
    }

    @Override
    public void show() {
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.width = (int) (metrics.widthPixels * 0.5f);
        layoutParams.height = (int) (metrics.heightPixels * 0.8f);
        getWindow().setAttributes(layoutParams);

        super.show();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.photo_view) {
            if (mIndex == 3) {
                Context context = getContext();
                context.startActivity(new Intent(context, FireworksActivity.class));
            }
        } else {
            dismiss();
            if (null != mCallback) {
                mCallback.onDismiss();
            }
        }
    }

    public interface OnDialogDismissCallback {

        public void onDismiss();
    }
}
