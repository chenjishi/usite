package com.chenjishi.u148.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chenjishi.u148.R;
import com.chenjishi.u148.activity.VideoPlayActivity2;
import com.chenjishi.u148.model.Video;
import com.chenjishi.u148.util.HttpUtils;
import com.chenjishi.u148.volley.toolbox.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-11-29
 * Time: 下午4:20
 * To change this template use File | Settings | File Templates.
 */
public class VideoListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
    private LayoutInflater mInflater;
    private ArrayList<Video> mDataList = new ArrayList<Video>();
    private Context mContext;

    public VideoListAdapter(Context context, ListView listView) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        listView.setAdapter(this);
        listView.setOnItemClickListener(this);
    }

    @Override
    public int getCount() {
        return null == mDataList ? 0 : mDataList.size();
    }

    @Override
    public Video getItem(int position) {
        return null == mDataList ? null : mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (null == convertView) {
            convertView = mInflater.inflate(R.layout.video_list_cell, null);
        }

        view = convertView;

        Video video = getItem(position);
        ((TextView) view.findViewById(R.id.video_title)).setText(video.title);

        ImageView thumb = (ImageView) view.findViewById(R.id.video_thumb);
        HttpUtils.getImageLoader().get(video.thumbUrl, ImageLoader.getImageListener(thumb,
                R.drawable.pictrue_bg, R.drawable.pictrue_bg));

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Video video = getItem(position);
        Intent intent = new Intent(mContext, VideoPlayActivity2.class);
        intent.putExtra("local_path", video.localPath);
        intent.putExtra("id", video.id);
        intent.putExtra("thumb", video.thumbUrl);
        intent.putExtra("title", video.title);
        mContext.startActivity(intent);
    }

    public void dataChange(ArrayList<Video> dataList) {
        if (null != dataList && dataList.size() > 0) {
            if (mDataList.size() > 0) {
                mDataList.clear();
            }

            mDataList.addAll(dataList);
            Collections.reverse(mDataList);
        } else {
            mDataList.clear();
        }
        notifyDataSetChanged();
    }
}
