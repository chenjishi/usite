package com.chenjishi.u148.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.chenjishi.u148.R;
import com.chenjishi.u148.util.Constants;
import com.chenjishi.u148.util.HttpUtils;
import com.chenjishi.u148.util.Utils;
import com.chenjishi.u148.view.GifMovieView;
import com.chenjishi.u148.view.ShareDialog;
import com.chenjishi.u148.view.TouchImageView;
import com.chenjishi.u148.volley.VolleyError;
import com.chenjishi.u148.volley.toolbox.ImageLoader;
import com.flurry.android.FlurryAgent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-6-23
 * Time: 上午11:30
 * To change this template use File | Settings | File Templates.
 */
public class ImageActivity extends BaseActivity implements GestureDetector.OnGestureListener,
        ViewPager.OnPageChangeListener {
    private ArrayList<String> mImageList = new ArrayList<String>();

    private int mCurrentIndex;
    private GestureDetector mGestureDetector;

    private ViewPager mViewPager;
    private RelativeLayout mToolBar;

    private ImageLoader imageLoader;

    private boolean showToolBar = false;

    @SuppressWarnings("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int sdk_int = Build.VERSION.SDK_INT;
        if (sdk_int < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();

            if (sdk_int >= 19) {
                decorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE);
            } else {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            }
        }

        setContentView(R.layout.photo_layout, true);

        Bundle bundle = getIntent().getExtras();
        if (null == bundle) return;

        mImageList = bundle.getStringArrayList("images");
        String currentUrl = bundle.getString("imgsrc");

        for (int i = 0; i < mImageList.size(); i++) {
            if (mImageList.get(i).equals(currentUrl)) {
                mCurrentIndex = i;
                break;
            }
        }

        mGestureDetector = new GestureDetector(this, this);
        imageLoader = HttpUtils.getImageLoader();

        mToolBar = (RelativeLayout) findViewById(R.id.tool_bar);
        mViewPager = (ViewPager) findViewById(R.id.pager_photo);
        mViewPager.setAdapter(new PhotoPagerAdapter(this));
        mViewPager.setOnPageChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewPager.setCurrentItem(mCurrentIndex);
    }

    public void onCloseButtonClicked(View v) {
        finish();
    }

    public void onDownloadButtonClicked(View v) {
        saveImage();
    }

    private ShareDialog shareDialog;

    public void onShareButtonClicked(View v) {
        if (null == shareDialog) {
            shareDialog = new ShareDialog(this);
        }

        String imageUrl = mImageList.get(mCurrentIndex);
        ArrayList<String> imageList = new ArrayList<String>();
        imageList.add(imageUrl);
        shareDialog.setImageList(imageList);
        shareDialog.show();

        HashMap<String, String> params = new HashMap<String, String>();
        params.put(Constants.PARAM_URL, imageUrl);
        FlurryAgent.logEvent(Constants.EVENT_IMAGE_SHARE, params);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        return mGestureDetector.onTouchEvent(ev);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        showToolBar = !showToolBar;
        mToolBar.setVisibility(showToolBar ? View.VISIBLE : View.GONE);
        float height = getResources().getDimension(R.dimen.action_bar_height);
        float startY = showToolBar ? 0 : height;
        float endY = showToolBar ? height : 0;
        Animation animation = new TranslateAnimation(0, 0, startY, endY);
        animation.setDuration(400);
        animation.setFillAfter(true);
        mToolBar.clearAnimation();
        mToolBar.startAnimation(animation);
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {
    }

    @Override
    public void onPageSelected(int i) {
        mCurrentIndex = i;
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }

    private void saveImage() {
        final String imageUrl = mImageList.get(mCurrentIndex);
        if (TextUtils.isEmpty(imageUrl)) return;

        imageLoader.get(imageUrl, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                String picUrl = null;
                Bitmap bitmap = response.getBitmap();

                if (null != bitmap) {
                    String name = System.currentTimeMillis() + ".jpg";
                    ContentResolver cr = ImageActivity.this.getContentResolver();
                    picUrl = MediaStore.Images.Media.insertImage(cr, bitmap, name, "Image Saved From U148");

                    if (!TextUtils.isEmpty(picUrl)) {
                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        String imagePath = getFilePathByContentResolver(Uri.parse(picUrl));
                        Uri uri = Uri.fromFile(new File(imagePath));
                        intent.setData(uri);
                        ImageActivity.this.sendBroadcast(intent);
                    }
                }

                Utils.showToast(getString(TextUtils.isEmpty(picUrl) ?
                        R.string.image_save_fail : R.string.image_save_success));
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Utils.showToast(getString(R.string.image_save_fail));
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
        private LayoutInflater inflater;

        public PhotoPagerAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mImageList.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = inflater.inflate(R.layout.photo_item, null);

            TextView textView = (TextView) view.findViewById(R.id.loading_text);
            TouchImageView imageView = (TouchImageView) view.findViewById(R.id.img_photo);
            GifMovieView gifView = (GifMovieView) view.findViewById(R.id.gif_view);

            String imageUrl = mImageList.get(position);
            if (!TextUtils.isEmpty(imageUrl)) {
                if (imageUrl.endsWith("gif") || imageUrl.endsWith("GIF") || imageUrl.endsWith("Gif")) {
                    int screenWidth = getResources().getDisplayMetrics().widthPixels;

                    imageView.setVisibility(View.GONE);

                    gifView.setImageUrl(imageUrl, screenWidth);
                    gifView.setVisibility(View.VISIBLE);
                } else {
                    gifView.setVisibility(View.GONE);
                    imageLoader.get(mImageList.get(position),
                            ImageLoader.getImageListener(imageView, R.drawable.gray, R.drawable.gray));
                    imageView.setVisibility(View.VISIBLE);
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
