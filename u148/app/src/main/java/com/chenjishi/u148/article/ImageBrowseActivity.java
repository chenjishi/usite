package com.chenjishi.u148.article;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.chenjishi.u148.BaseActivity;
import com.chenjishi.u148.R;
import com.chenjishi.u148.utils.*;
import com.chenjishi.u148.widget.GifMovieView;
import com.chenjishi.u148.widget.ShareDialog;
import com.chenjishi.u148.widget.TouchImageView;
import com.flurry.android.FlurryAgent;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jishichen on 2017/4/26.
 */
public class ImageBrowseActivity extends BaseActivity implements ViewPager.OnPageChangeListener,
        Listener<byte[]>, ErrorListener {
    private ViewPager mViewPager;
    private RelativeLayout mToolBar;

    private final List<String> mImageList = new ArrayList<>();

    private int mCurrentIndex;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
        setContentView(R.layout.activity_image_browse, true);

        Bundle bundle = getIntent().getExtras();
        if (null == bundle) return;

        List<String> list = bundle.getStringArrayList("images");
        mImageList.clear();
        mImageList.addAll(list);
        String currentUrl = bundle.getString("imgsrc");

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(currentUrl)) {
                mCurrentIndex = i;
                break;
            }
        }

        mToolBar = (RelativeLayout) findViewById(R.id.tool_bar);
        mViewPager = (ViewPager) findViewById(R.id.pager_photo);
        mViewPager.setAdapter(new PhotoPagerAdapter());
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setCurrentItem(mCurrentIndex);
    }

    @Override
    protected void onDestroy() {
        if (null != mViewPager) mViewPager.removeOnPageChangeListener(this);
        super.onDestroy();
    }

    public void onCloseButtonClicked(View v) {
        finish();
    }

    public void onDownloadButtonClicked(View v) {
        String imageUrl = mImageList.get(mCurrentIndex);
        if (TextUtils.isEmpty(imageUrl)) return;

        NetworkRequest.getInstance().getBytes(imageUrl, this, this);
    }

    public void onShareButtonClicked(View v) {
        ShareDialog dialog = new ShareDialog(this);
        String imageUrl = mImageList.get(mCurrentIndex);
        ArrayList<String> imageList = new ArrayList<>();
        imageList.add(imageUrl);
        dialog.setImageList(imageList);
        dialog.show();

        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.PARAM_URL, imageUrl);
        FlurryAgent.logEvent(Constants.EVENT_IMAGE_SHARE, params);
    }

    private void onImageTap() {
        final boolean visible = mToolBar.getVisibility() == View.VISIBLE;

        int h = mToolBar.getHeight();
        ObjectAnimator animator = ObjectAnimator.ofFloat(mToolBar, View.TRANSLATION_Y,
                visible ? 0 : h, visible ? h : 0);
        animator.setDuration(250);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                if (!visible) mToolBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (visible) mToolBar.setVisibility(View.GONE);
            }
        });
        animator.start();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mCurrentIndex = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onResponse(byte[] response) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(response, 0, response.length);

        String url = mImageList.get(mCurrentIndex);
        String suffix = ".jpg";
        int idx = url.lastIndexOf(".");
        if (-1 != idx) suffix = url.substring(idx);

        String name = System.currentTimeMillis() + suffix;
        ContentResolver cr = ImageBrowseActivity.this.getContentResolver();
        String picUrl = MediaStore.Images.Media.insertImage(cr, bitmap, name, "Image Saved From U148");

        if (!TextUtils.isEmpty(picUrl)) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            String imagePath = getFilePathByContentResolver(Uri.parse(picUrl));
            Uri uri = Uri.fromFile(new File(imagePath));
            intent.setData(uri);
            ImageBrowseActivity.this.sendBroadcast(intent);
        }

        showDownloadTips(getString(TextUtils.isEmpty(picUrl) ?
                R.string.image_save_fail : R.string.image_save_success));
    }

    @Override
    public void onErrorResponse() {
        showDownloadTips(getString(R.string.image_save_fail));
    }

    private void showDownloadTips(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showToast(ImageBrowseActivity.this, tip);
            }
        });
    }

    private String getFilePathByContentResolver(Uri uri) {
        if (null == uri) return null;

        Cursor c = getContentResolver().query(uri, null, null, null, null);
        String filePath = null;
        if (null == c) {
            throw new IllegalArgumentException(
                    "Query on " + uri + " returns null result.");
        }
        try {
            if ((c.getCount() != 1) || !c.moveToFirst()) {
            } else {
                filePath = c.getString(c.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
            }
        } finally {
            c.close();
        }
        return filePath;
    }

    private class PhotoPagerAdapter extends PagerAdapter {

        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onImageTap();
            }
        };

        @Override
        public int getCount() {
            return mImageList.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = LayoutInflater.from(ImageBrowseActivity.this)
                    .inflate(R.layout.photo_item, null);

            TextView textView = (TextView) view.findViewById(R.id.loading_text);
            TouchImageView imageView = (TouchImageView) view.findViewById(R.id.img_photo);
            GifMovieView gifView = (GifMovieView) view.findViewById(R.id.gif_view);

            String url = mImageList.get(position);
            if (!TextUtils.isEmpty(url)) {
                if (url.endsWith("gif") || url.endsWith("GIF") || url.endsWith("Gif")) {
                    imageView.setVisibility(View.GONE);
                    gifView.setImageUrl(url, getResources().getDisplayMetrics().widthPixels);
                    gifView.setVisibility(View.VISIBLE);
                    gifView.setOnClickListener(mOnClickListener);
                } else {
                    gifView.setVisibility(View.GONE);
                    Glide.with(ImageBrowseActivity.this).load(url).into(imageView);
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setOnClickListener(mOnClickListener);
                }
            } else {
                imageView.setVisibility(View.GONE);
                gifView.setVisibility(View.GONE);
                textView.setText(R.string.image_loading_fail);
                textView.setVisibility(View.VISIBLE);
            }

            container.addView(view);
            return view;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
