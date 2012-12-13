package com.chenjishi.usite.activity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;
import com.chenjishi.usite.R;
import com.chenjishi.usite.base.App;
import com.chenjishi.usite.base.BaseActivity;
import com.chenjishi.usite.image.IImageCallback;
import com.chenjishi.usite.image.ImagePool;
import com.chenjishi.usite.util.JavascriptBridge;
import com.chenjishi.usite.view.TouchImageView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-8
 * Time: 下午3:25
 * To change this template use File | Settings | File Templates.
 */
public class PictureViewActivity extends BaseActivity implements GestureDetector.OnGestureListener, View.OnClickListener, TouchImageView.ImageListener {
    private ViewFlipper viewFlipper;
    private TouchImageView currentImage;

    private RelativeLayout imgToolBar;

    private GestureDetector detector;

    private List<String> imageList = new ArrayList<String>();

    private ImagePool pool;

    private int currentImgIdx = -1;
    private boolean isDisplay = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture_view);

        viewFlipper = (ViewFlipper) findViewById(R.id.image_fliper);
        findViewById(R.id.img_close).setOnClickListener(this);
        findViewById(R.id.img_download).setOnClickListener(this);

        currentImage = getTouchImageView();


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
                currentImage.setImageDrawable(d);
            }
        });

        viewFlipper.addView(currentImage, 0);
    }

    @Override
    public boolean dragNext() {
        return false;
    }

    @Override
    public boolean dragPrev() {
        return false;
    }

    @Override
    public void singleClick() {
    }

    @Override
    public boolean doubleClick() {
        return false;
    }

    @Override
    public void loadingSuccess() {
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
                currentImage = getTouchImageView();

                pool.requestImage(imageList.get(currentImgIdx), new IImageCallback() {
                    @Override
                    public void onImageResponse(Drawable d) {
                        currentImage.setImageDrawable(d);
                    }
                });

                viewFlipper.addView(currentImage, 1);
                viewFlipper.showNext();
                viewFlipper.removeViewAt(0);

            }
        }

        if (motionEvent1.getX() - motionEvent.getX() > 50) {
            viewFlipper.setInAnimation(this, R.anim.page_push_right_in);
            viewFlipper.setOutAnimation(this, R.anim.page_push_right_out);

            if (currentImgIdx > 0) {
                currentImgIdx--;
                currentImage = getTouchImageView();
                pool.requestImage(imageList.get(currentImgIdx), new IImageCallback() {
                    @Override
                    public void onImageResponse(Drawable d) {
                        currentImage.setImageDrawable(d);
                    }
                });

                viewFlipper.addView(currentImage, -1);
                viewFlipper.showPrevious();
                viewFlipper.removeViewAt(0);
            }
        }

        return true;
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

    private TouchImageView getTouchImageView() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        TouchImageView imageView = new TouchImageView(this, metrics.widthPixels, metrics.heightPixels, this);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        imageView.setLayoutParams(lp);
        return imageView;
    }
}



