package com.chenjishi.u148.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import com.chenjishi.meizi.R;
import com.chenjishi.meizi.base.FileCache;
import com.chenjishi.meizi.util.CommonUtils;
import com.chenjishi.meizi.util.StringUtil;
import com.chenjishi.meizi.view.TouchImageView;
import com.chenjishi.meizi.volley.RequestQueue;
import com.chenjishi.meizi.volley.VolleyError;
import com.chenjishi.meizi.volley.toolbox.BitmapLruCache;
import com.chenjishi.meizi.volley.toolbox.ImageLoader;
import com.chenjishi.meizi.volley.toolbox.Volley;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-6-23
 * Time: 上午11:30
 * To change this template use File | Settings | File Templates.
 */
public class PhotoViewActivity extends Activity implements GestureDetector.OnGestureListener,
        View.OnClickListener, ViewPager.OnPageChangeListener {
    private ArrayList<String> mImageList = new ArrayList<String>();

    private int mCurrentIndex;
    private GestureDetector mGestureDetector;

    private ViewPager mViewPager;
    private ImageLoader mImageLoader;
    private RelativeLayout mToolBar;

    private boolean showToolBar = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_layout);

        Bundle bundle = getIntent().getExtras();
        if (null == bundle) return;

        mImageList = bundle.getStringArrayList("imagelist");
        String currentUrl = bundle.getString("imgsrc");

        for (int i = 0; i < mImageList.size(); i++) {
            if (mImageList.get(i).equals(currentUrl)) {
                mCurrentIndex = i;
                break;
            }
        }

        mGestureDetector = new GestureDetector(this, this);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        mImageLoader = new ImageLoader(requestQueue, new BitmapLruCache(this));

        mToolBar = (RelativeLayout) findViewById(R.id.tool_bar);
        findViewById(R.id.btn_download).setOnClickListener(this);
        findViewById(R.id.btn_close).setOnClickListener(this);
        mViewPager = (ViewPager) findViewById(R.id.pager_photo);
        mViewPager.setAdapter(new PhotoPagerAdapter(this));
        mViewPager.setOnPageChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewPager.setCurrentItem(mCurrentIndex);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_download) {
            saveImage();
        } else if (v.getId() == R.id.btn_close) {
            finish();
        }
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

        mImageLoader.get(imageUrl, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                int tipResId;
                Bitmap bitmap = response.getBitmap();

                if (null != bitmap) {
                    String imagePath = FileCache.getSavedImageDirectory(PhotoViewActivity.this);
                    String name = imageUrl.hashCode() + ".jpg";

                    try {
                        FileOutputStream fos = new FileOutputStream(imagePath + name);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                        bos.close();
                        fos.close();
                    } catch (IOException e) {
                    }
                    tipResId = R.string.image_save_success;
                } else {
                    tipResId = R.string.image_save_fail;
                }

                CommonUtils.showTips(PhotoViewActivity.this, getString(tipResId));
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                CommonUtils.showTips(PhotoViewActivity.this, getString(R.string.image_save_fail));
            }
        });
    }

    class PhotoPagerAdapter extends PagerAdapter {
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
            View pageView = inflater.inflate(R.layout.photo_item, null);

            final TouchImageView imageView = (TouchImageView) pageView.findViewById(R.id.img_photo);
            mImageLoader.get(mImageList.get(position), new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    imageView.setImageBitmap(response.getBitmap());
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                }
            });

            container.addView(pageView);

            return pageView;
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
