package com.chenjishi.u148.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.chenjishi.u148.R;
import com.chenjishi.u148.adapter.VideoListAdapter;
import com.chenjishi.u148.base.DatabaseHelper;
import com.chenjishi.u148.base.FileCache;
import com.chenjishi.u148.entity.Video;
import com.chenjishi.u148.util.ConstantUtils;
import com.chenjishi.u148.util.FileUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-11-21
 * Time: 下午11:14
 * To change this template use File | Settings | File Templates.
 */
public class VideoListActivity extends BaseActivity {
    private static final int MSG_CLEAR_ALL = 222;

    private ArrayList<Video> mVideoList = new ArrayList<Video>();
    private VideoListAdapter mAdapter;

    private DatabaseHelper mDatabase;

    private View mEmptyView;

    private String mDiskOccupiedSize;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitleText(R.string.video);

        ImageView clearBtn = ((ImageView) findViewById(R.id.icon_menu2));
        clearBtn.setImageResource(R.drawable.ic_clear);
        clearBtn.setVisibility(View.VISIBLE);

        mEmptyView = getLayoutInflater().inflate(R.layout.empty_view, null);
        ListView listView = (ListView) findViewById(R.id.list_videos);
        ((ViewGroup) listView.getParent()).addView(mEmptyView);
        listView.setEmptyView(mEmptyView);
        mAdapter = new VideoListAdapter(this, listView);

        mDatabase = DatabaseHelper.getInstance(this);

        new Thread(){
            @Override
            public void run() {
                mVideoList = mDatabase.loadAll(DatabaseHelper.TB_NAME_VIDEOS);
                mDiskOccupiedSize = FileUtils.getVideoCacheSize();
                mHandler.sendEmptyMessage(ConstantUtils.MSG_DOWNLOAD_SUCCESS);
            }
        }.start();
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

    private void clearAll() {
        String videoCachePath = FileCache.getVideoDirectory(this);
        File[] flists = new File(videoCachePath).listFiles();

        if (null == flists || flists.length == 0) return;

        for (File f : flists) f.delete();

        for (Video video : mVideoList) {
            mDatabase.deleteVideo(video.id, DatabaseHelper.TB_NAME_VIDEOS);
            mDatabase.deleteVideo(video.id, DatabaseHelper.TB_NAME_LINKS);
        }
        mDiskOccupiedSize = FileUtils.getVideoCacheSize();
    }

    @Override
    protected void backIconClicked() {
        finish();
    }

    public void onButtonClicked(View view) {
        if (null == mProgress) {
            mProgress = new ProgressDialog(this);
            mProgress.setCancelable(false);
            mProgress.setMessage("正在清理");
        }

        mProgress.show();
        new Thread(){
            @Override
            public void run() {
                clearAll();
                mHandler.sendEmptyMessage(MSG_CLEAR_ALL);
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    private final BroadcastReceiver mHandleDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int msgCode = intent.getIntExtra(ConstantUtils.KEY_MESSAGE_TYPE, ConstantUtils.MSG_NO_UPDATE);
            mDiskOccupiedSize = FileUtils.getVideoCacheSize();

            mHandler.sendEmptyMessage(msgCode);
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (ConstantUtils.MSG_DOWNLOAD_SUCCESS == msg.what) {
                if (null != mVideoList && mVideoList.size() > 0)
                    mVideoList.clear();

                mVideoList = mDatabase.loadAll(DatabaseHelper.TB_NAME_VIDEOS);
                if (null != mVideoList && mVideoList.size() > 0) {
                    mAdapter.dataChange(mVideoList);
                }
            } else if (MSG_CLEAR_ALL == msg.what){
                if (null != mVideoList && mVideoList.size() > 0) {
                    mVideoList.clear();
                }

                mAdapter.dataChange(mVideoList);
                mEmptyView.findViewById(R.id.progress_bar).setVisibility(View.GONE);
                ((TextView) mEmptyView.findViewById(R.id.tv_empty_tip)).setText("清理完畢，有新視頻更新會自動下載");
                mProgress.dismiss();
            }
            setTitleText2(String.format(getString(R.string.occupied_size), mDiskOccupiedSize));
        }
    };
}
