package com.chenjishi.u148.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import com.chenjishi.u148.R;
import io.vov.vitamio.widget.MediaController;

/**
 * Created by chenjishi on 14-2-15.
 */
public class VideoController extends MediaController {

    public VideoController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoController(Context context) {
        super(context);
    }

    @Override
    protected View makeControllerView() {
        return ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.media_controller_layout, this);
    }
}
