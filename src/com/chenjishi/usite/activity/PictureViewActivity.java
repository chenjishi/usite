package com.chenjishi.usite.activity;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import com.chenjishi.usite.R;
import com.chenjishi.usite.image.IImageCallback;
import com.chenjishi.usite.image.ImagePool;
import com.chenjishi.usite.multitouch.ImageViewTouch;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-8
 * Time: 下午3:25
 * To change this template use File | Settings | File Templates.
 */
public class PictureViewActivity extends Activity {
    private ImageViewTouch imageTouch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture_view);

        imageTouch = (ImageViewTouch) findViewById(R.id.image_touch);

        String imgUrl = getIntent().getExtras().getString("imgsrc");

        if (null != imgUrl) {
            showImage(imgUrl);
        }
    }

    private void showImage(String url) {
        ImagePool pool = new ImagePool();

        pool.requestImage(url, new IImageCallback() {
            @Override
            public void onImageResponse(Drawable d) {
                imageTouch.setImageDrawable(d);
            }
        });
    }
}
