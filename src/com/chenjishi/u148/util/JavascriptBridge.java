package com.chenjishi.u148.util;

import android.content.Context;
import android.content.Intent;
import com.chenjishi.u148.activity.MusicPlayerActivity;
import com.chenjishi.u148.activity.PhotoViewActivity;
import com.chenjishi.u148.activity.VideoPlayerActivity;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 12-11-8
 * Time: 上午11:20
 * To change this template use File | Settings | File Templates.
 */
public class JavascriptBridge {
    private Context mContext;
    private ArrayList<String> mImageUrls = new ArrayList<String>();

    public JavascriptBridge(Context context) {
        mContext = context;
    }

    public void setImageUrls(ArrayList<String> imageUrls) {
        mImageUrls = imageUrls;
    }

    public void onImageClick(String src) {
        Intent intent = new Intent(mContext, PhotoViewActivity.class);
        intent.putExtra("imgsrc", src);
        intent.putStringArrayListExtra("images", mImageUrls);
        mContext.startActivity(intent);
    }

    public void onVideoClick(String src) {
        Intent intent = new Intent();
        if (src.contains("www.xiami.com")) {
            intent.setClass(mContext, MusicPlayerActivity.class);
        } else {
            intent.setClass(mContext, VideoPlayerActivity.class);
        }
        intent.putExtra("url", src);
        mContext.startActivity(intent);
    }
}
