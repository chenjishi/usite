package com.chenjishi.u148.settings;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.chenjishi.u148.R;
import com.chenjishi.u148.utils.ErrorListener;
import com.chenjishi.u148.utils.Listener;
import com.chenjishi.u148.utils.NetworkRequest;
import com.chenjishi.u148.widget.GifMovieView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by jishichen on 2017/4/25.
 */
public class SurpriseActivity extends Activity implements View.OnClickListener,
        PhotoDialog.OnDialogDismissCallback, Listener<EasterData>,
        ErrorListener {

    private final static int MAX_FISH_COUNT = 10;
    private final static int MAX_BUBBLE_COUNT = 20;
    private final static long TIME_INTERVAL = 1000L;
    private final static int MSG_UPDATE = 101;
    private int mBubbleWidth;
    private int mBubbleHeight;

    private int mFishHeight;
    private int mFishWidth;

    private int mScreenWidth;
    private int mScreenHeight;

    private int mIndex;

    private ArrayList<PhotoItem> mPhotoList;

    private PhotoDialog mPhotoDialog;
    private MediaPlayer mPlayer;

    private SoundPool mSoundPool;
    private boolean mSoundLoaded;
    private int mSoundId;

    private float mDensity;
    private final Random mRand = new Random();

    private final LinkedList<GifMovieView> mFishList = new LinkedList<GifMovieView>();
    private final LinkedList<GifMovieView> mRecycleFishList = new LinkedList<GifMovieView>();
    private final LinkedList<ImageView> mBubbleList = new LinkedList<ImageView>();
    private final LinkedList<ImageView> mRecycleBubbleList = new LinkedList<ImageView>();

    private FrameLayout mContainer;
    private ImageButton mCloseButton;
    private final AccelerateInterpolator mInterpolator = new AccelerateInterpolator();
    private final Interpolator[] mInterpolators = new Interpolator[]{
            mInterpolator, new DecelerateInterpolator(), new LinearInterpolator()};

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_UPDATE) {
                addBubbleView();
                addFishView();
                sendEmptyMessageDelayed(MSG_UPDATE, TIME_INTERVAL);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surprise);
        mDensity = getResources().getDisplayMetrics().density;
        mFishWidth = (int) (83 * mDensity);
        mFishHeight = (int) (mFishWidth * 85.f / 166);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mContainer = (FrameLayout) findViewById(android.R.id.content);
        mCloseButton = (ImageButton) findViewById(R.id.button_close);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bubble);
        mBubbleWidth = bitmap.getWidth();
        mBubbleHeight = bitmap.getHeight();

        mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                mSoundLoaded = true;
            }
        });

        mSoundId = mSoundPool.load(this, R.raw.bubble, 1);

        mPlayer = MediaPlayer.create(this, R.raw.water);
        mPlayer.setLooping(true);
        mPlayer.start();

        NetworkRequest.getInstance().get("http://u148.oss-cn-beijing.aliyuncs.com/image/easter", EasterData.class, this, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mHandler.removeMessages(MSG_UPDATE);
        mHandler.sendEmptyMessage(MSG_UPDATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeMessages(MSG_UPDATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mPlayer) {
            mPlayer.release();
            mPlayer = null;
        }

        if (null != mSoundPool) {
            mSoundPool.release();
            mSoundPool = null;
        }
    }

    public void onExitButtonClicked(View v) {
        finish();
    }

    @Override
    public void onClick(View v) {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        float volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        if (mSoundLoaded) {
            mSoundPool.play(mSoundId, volume, volume, 1, 0, 1f);
        }

        if (null == mPhotoDialog) {
            mPhotoDialog = new PhotoDialog(this, this);
        }

        if (null != mPhotoList && mPhotoList.size() > 0) {
            mPhotoDialog.setPhotoItem(mPhotoList.get(mIndex), mIndex);
            mIndex++;
            if (mIndex >= mPhotoList.size()) {
                mIndex = 0;
            }
        } else {
            mPhotoDialog.setPhotoItem(null, 0);
        }

        mPhotoDialog.show();
        mCloseButton.setVisibility(View.GONE);
        mHandler.removeMessages(MSG_UPDATE);
    }

    @Override
    public void onDismiss() {
        mHandler.removeMessages(MSG_UPDATE);
        mHandler.sendEmptyMessage(MSG_UPDATE);
        mCloseButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResponse(EasterData response) {
        if (null == response || response.size() == 0) return;

        if (null == mPhotoList) {
            mPhotoList = new ArrayList<>();
            mPhotoList.addAll(response);
        }
    }

    @Override
    public void onErrorResponse() {

    }

    private void addBubbleView() {
        int startX = 24;
        int endX = (int) (Math.random() * mScreenWidth + 1);
        int startY = mScreenHeight;
        int endY = -mBubbleHeight;
        float speed = 1.f / ((int) (Math.random() * 100)) + 1.f;

        long duration = (long) (5000 * speed);

        float scale = 1.f / ((int) (Math.random() * 60)) + 1.f;

        int width = (int) (scale * mBubbleWidth);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width, width);
        lp.gravity = Gravity.BOTTOM;
        lp.leftMargin = 24;
        lp.bottomMargin = -(int) (1.f * width / 4);

        final ImageView imageView;
        if (mRecycleBubbleList.size() > 0) {
            imageView = mRecycleBubbleList.get(0);
            mRecycleBubbleList.remove(imageView);
        } else {
            imageView = new ImageView(this);
            imageView.setImageResource(R.drawable.bubble);
            if (mBubbleList.size() < MAX_BUBBLE_COUNT) {
                mBubbleList.add(imageView);
            }
        }
        imageView.setOnClickListener(this);
        mContainer.addView(imageView, lp);

        ObjectAnimator animX = ObjectAnimator.ofFloat(imageView, "x", startX, endX);
        ObjectAnimator animY = ObjectAnimator.ofFloat(imageView, "y", startY, endY);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(duration);
        animatorSet.setInterpolator(mInterpolator);
        animatorSet.playTogether(animX, animY);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mContainer.removeView(imageView);
                mRecycleBubbleList.add(imageView);
                mBubbleList.remove(imageView);
            }
        });
        animatorSet.start();
    }

    private void addFishView() {
        float scale = 1.f / ((int) (Math.random() * 60)) + 1.f;

        int width = (int) (scale * mFishWidth);
        int height = (int) (scale * mFishHeight);

        int x1 = mScreenWidth;
        int xMin = -width;
        int xMax = mScreenWidth / 2;
        int x2 = (int) (Math.random() * (xMax - xMin + 1)) + xMin;

        int y1 = mScreenHeight;
        int y2 = -height;

        float speed = 1.f / ((int) (Math.random() * 100)) + 1.f;

        long duration = (long) (3000 * speed);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width, height);
        lp.gravity = Gravity.BOTTOM;
        lp.leftMargin = 24;
        lp.bottomMargin = -(int) (1.f * width / 4);

        final GifMovieView gifView;
        if (mRecycleFishList.size() > 0) {
            gifView = mRecycleFishList.get(0);
            mRecycleFishList.remove(gifView);
        } else {
            gifView = new GifMovieView(this);
            if (mFishList.size() < MAX_FISH_COUNT) {
                mFishList.add(gifView);
            }
        }
        gifView.setImageResId(R.raw.nemo, width);
        mContainer.addView(gifView, lp);

        int index = mRand.nextInt(3);

        ObjectAnimator animX = ObjectAnimator.ofFloat(gifView, "x", x1, x2);
        ObjectAnimator animY = ObjectAnimator.ofFloat(gifView, "y", y1, y2);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(duration);
        animatorSet.setInterpolator(mInterpolators[index]);
        animatorSet.playTogether(animX, animY);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mContainer.removeView(gifView);
                mRecycleFishList.add(gifView);
                mFishList.remove(gifView);
            }
        });
        animatorSet.start();
    }
}
