package com.chenjishi.u148.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chenjishi.u148.R;
import com.chenjishi.u148.base.DatabaseHelper;
import com.chenjishi.u148.entity.Video;
import com.chenjishi.u148.util.ConstantUtils;
import com.chenjishi.u148.util.HttpUtils;
import com.chenjishi.u148.volley.toolbox.ImageLoader;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-11-21
 * Time: 下午11:14
 * To change this template use File | Settings | File Templates.
 */
public class VideoListActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private ArrayList<Video> mVideoList = new ArrayList<Video>();

    private ListView mListView;
    private VideoAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListView = (ListView) findViewById(R.id.list_videos);
        mAdapter = new VideoAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);

        mVideoList = DatabaseHelper.getInstance(this).loadAll(DatabaseHelper.TB_NAME_VIDEOS);
        Log.i("test", "on create size" + mVideoList.size());
        if (null != mVideoList && mVideoList.size() > 0) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(mHandleDownloadReceiver,
                new IntentFilter(ConstantUtils.DOWNLOAD_STATUS_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mHandleDownloadReceiver);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_video_list;
    }

    @Override
    protected void backIconClicked() {
        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, VideoPlayActivity2.class);
        intent.putExtra("url", mVideoList.get(position).localPath);
        startActivity(intent);
    }

    private class VideoAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mInflater;

        public VideoAdapter(Context context) {
            mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mVideoList.size();
        }

        @Override
        public Object getItem(int position) {
            return mVideoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView;
            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.video_list_cell, null);
            }

            itemView = convertView;

            Video video = mVideoList.get(position);
            ((TextView) itemView.findViewById(R.id.video_title)).setText(video.title);

            final ImageView imageView = (ImageView) itemView.findViewById(R.id.video_thumb);
            ImageLoader imageLoader = HttpUtils.getImageLoader();
            imageLoader.get(video.thumbUrl, ImageLoader.getImageListener(imageView, R.drawable.icon, R.drawable.image_bg));

            return itemView;
        }
    }

    private int count;
    private final BroadcastReceiver mHandleDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int msgCode = intent.getIntExtra(ConstantUtils.KEY_MESSAGE_TYPE, ConstantUtils.MSG_NO_UPDATE);
            count = intent.getIntExtra(ConstantUtils.KEY_VIDEO_SIZE, 0);

            mHandler.sendEmptyMessage(msgCode);
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (ConstantUtils.MSG_DOWNLOAD_SUCCESS == msg.what) {
                if (null != mVideoList && mVideoList.size() > 0)
                    mVideoList.clear();

                mVideoList = DatabaseHelper.getInstance(VideoListActivity.this).loadAll(DatabaseHelper.TB_NAME_VIDEOS);
                if (null != mVideoList && mVideoList.size() > 0)
                    mAdapter.notifyDataSetChanged();
            }
        }
    };
}
