package com.chenjishi.usite.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;
import com.chenjishi.usite.R;
import com.chenjishi.usite.base.App;
import com.chenjishi.usite.base.BaseActivity;
import com.chenjishi.usite.image.IImageCallback;
import com.chenjishi.usite.image.ImagePool;
import com.chenjishi.usite.multitouch.ImageViewTouch;
import com.chenjishi.usite.util.JavascriptBridge;

import java.io.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-8
 * Time: 下午3:25
 * To change this template use File | Settings | File Templates.
 */
public class PictureViewActivity extends BaseActivity implements GestureDetector.OnGestureListener, View.OnClickListener {
    private ViewFlipper viewFlipper;
    private ImageViewTouch imageTouch1;
    private ImageViewTouch imageTouch2;

    private RelativeLayout imgToolBar;

    private GestureDetector detector;

    private List<String> imageList;

    private ImagePool pool;

    private int currentImgIdx = -1;
    private boolean isDisplay = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture_view);

        viewFlipper = (ViewFlipper) findViewById(R.id.image_fliper);
        imageTouch1 = (ImageViewTouch) findViewById(R.id.image_touch1);
        imageTouch2 = (ImageViewTouch) findViewById(R.id.image_touch2);
        findViewById(R.id.img_close).setOnClickListener(this);
        findViewById(R.id.img_download).setOnClickListener(this);

        imageTouch1.setDoubleTapToZoomEnabled(false);
        imageTouch2.setDoubleTapToZoomEnabled(false);

        imgToolBar = (RelativeLayout) findViewById(R.id.pic_tools);

        detector = new GestureDetector(this);

        imageList = JavascriptBridge.getImageList();

        String imgUrl = getIntent().getExtras().getString("imgsrc");

        for (int i = 0; i < imageList.size(); i++) {
            if (imgUrl.equalsIgnoreCase(imageList.get(i))) {
                currentImgIdx = i;
                break;
            }
        }

        pool = new ImagePool();
        pool.requestImage(imgUrl, new IImageCallback() {
            @Override
            public void onImageResponse(Drawable d) {
                imageTouch1.setImageDrawable(d);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_close:
                finish();
                break;
            case R.id.img_download:
                saveImage();
                break;
        }
    }

    private void saveImage() {
        new ImageSaveTask().execute(imageList.get(currentImgIdx));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        return detector.onTouchEvent(ev);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        isDisplay = !isDisplay;
        imgToolBar.setVisibility(isDisplay ? View.VISIBLE : View.GONE);

        return true;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        isDisplay = !isDisplay;

        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {

        if (motionEvent.getX() - motionEvent1.getX() > 50) {
            viewFlipper.setInAnimation(this, R.anim.page_push_left_in);
            viewFlipper.setOutAnimation(this, R.anim.page_push_left_out);

            if (currentImgIdx < imageList.size() - 1) {
                currentImgIdx++;
                setDisplayView();
            }
        }

        if (motionEvent1.getX() - motionEvent.getX() > 50) {
            viewFlipper.setInAnimation(this, R.anim.page_push_right_in);
            viewFlipper.setOutAnimation(this, R.anim.page_push_right_out);

            if (currentImgIdx > 0) {
                currentImgIdx--;
                setDisplayView();
            }
        }

        return true;
    }

    private void setDisplayView() {
        String url = imageList.get(currentImgIdx);

        if (viewFlipper.getDisplayedChild() == 0) {
            imageTouch2.setImageDrawable(null);
            initImages(1, url);
            viewFlipper.setDisplayedChild(1);
        } else if (viewFlipper.getDisplayedChild() == 1) {
            imageTouch1.setImageDrawable(null);
            initImages(0, url);
            viewFlipper.setDisplayedChild(0);
        }
    }

    private void initImages(final int idx, String imgUrl) {
        pool.requestImage(imgUrl, new IImageCallback() {
            @Override
            public void onImageResponse(Drawable d) {
                if (idx == 0) {
                    imageTouch1.setImageDrawable(d);
                }

                if (idx == 1) {
                    imageTouch2.setImageDrawable(d);
                }
            }
        });
    }

    class ImageSaveTask extends AsyncTask<String, Void, Boolean> {
        ProgressDialog dlg;
        @Override
        protected Boolean doInBackground(String... strings) {
            boolean result = false;

            Drawable d = pool.getCachedDrawable(strings[0]);
            Bitmap bitmap = drawable2Bitmap(d);

            if (bitmap != null) {
                String path = App.instance().getImageDirectory();
                String name = System.currentTimeMillis() + ".JPEG";

                try {
                    File file = new File(path);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    FileOutputStream fos = new FileOutputStream(path + name);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    bos.close();
                    fos.close();
                    result = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            dlg = new ProgressDialog(PictureViewActivity.this);
            dlg.setMessage("saving...");
            dlg.show();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            dlg.dismiss();
            Toast.makeText(PictureViewActivity.this, aBoolean ? "图片保存成功" : "图片保存失败", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap drawable2Bitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getMinimumHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}



